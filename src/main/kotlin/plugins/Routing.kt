package com.example.plugins

import io.ktor.client.HttpClient
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.*
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.http.content.file
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.sse.sse
import io.ktor.server.websocket.webSocket
import io.ktor.sse.ServerSentEvent
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.websocket.CloseReason
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.time.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Frame
import java.io.File
import java.util.concurrent.ConcurrentHashMap

fun Application.configureRouting(config: JWTConfig,httpClient: HttpClient ) {

    val userDB = mutableMapOf<String, UserInfo>()

    val onlineUsers = ConcurrentHashMap<String, WebSocketSession>()

    routing {

        webSocket ("chat"){
            val username = call.request.queryParameters["username"] ?: run {
                this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Username is required for establishing connection"))
                return@webSocket
            }

            onlineUsers[username] = this
            send("You are connected!")
            try{
                incoming.consumeEach{ frame ->
                    if (frame is io.ktor.websocket.Frame.Text) {
                        val message = Json.decodeFromString<Message>(frame.readText())
                        if(message.to.isNullOrBlank()){
                            onlineUsers.values.forEach {
                                it.send("$username : ${message.text}")
                            }
                        }else{
                            val session = onlineUsers[message.to]

                            session?.send("$username : ${message.text}")
                        }
                    }
                }
            }finally{
                    onlineUsers.remove(username)
                this.close()
            }


        }

        sse("events"){
            repeat(6){
                send(ServerSentEvent("Event: ${it + 1}"))
                delay(1000L)
            }
        }


        authenticate("google-oauth"){
            get("login"){
                //
                }

            get("callback"){
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                if (principal == null){
                    call.respondText("OAUTH failed", status = HttpStatusCode.Unauthorized)
                    return@get
                }
                val accessToken = principal.accessToken
                val userInfo = fetchGoogleUserInfo(httpClient = httpClient, accessToken = accessToken)

                if (userInfo != null){
                    userDB[userInfo.userId] = userInfo
                    val token = generateToken(config, username = userInfo.userId)
                    call.respond(mapOf("token" to token))
                }
            }
        }



        authenticate ("jwt-auth") {
            get(""){
                val principal = call. principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                val userInfo = userDB[username] ?: mapOf("error" to true)

                call.respond(userInfo)
            }
        }

//        staticResources("static", "static") {
//            extensions("html")
//        }
//
//        staticFiles("uploads", File("uploads")) {
//            exclude { file -> file.path.contains("video") }
//
//            contentType { file ->
//                when (file.name){
//                    "testfile.txt" -> ContentType.Text.Html
//                    else -> null
//                }
//            }
//
//            cacheControl { file ->
//                when(file.name){
//                    "testfile.txt" -> listOf(Immutable,CacheControl.MaxAge(10000))
//                    else -> emptyList()
//                }
////                listOf(CacheControl.MaxAge(10000))
//            }
//
//        }





//        get(""){
//            call.respondText(
//                  text = "hello world",
//                contentType = ContentType.Text.Plain,
//                status = HttpStatusCode.OK
//            )
//        }
//
//        get("products"){
//            val response = ProductResponse(
//                message = "Successfully fetched products",
//                data = List(10){Product(name = "Apple", 10, category = "Fruits") }
//            )
//
//            call.respond(response)
//        }
//
//        get("stream"){
//            val fileName = call.request.queryParameters["fileName"]?: ""
//            val file = File("uploads/$fileName")
//            if (file.exists()) return@get call.respond(HttpStatusCode.NotFound)
//
//            call.respondFile(file)
//        }


//
//        post("hello"){
//            val requestsLeft = call.response.headers["X-RateLimit-Remaining"]?.toInt() ?: 0
//            call.respondText("Ice cream supremacy!, but you have $requestsLeft requests left")
//        }
//
//        post("helloagain"){
//            val requestsLeft = call.response.headers["X-RateLimit-Remaining"]?.toInt() ?: 0
//            call.respondText("Ice cream supremacy!, but you have $requestsLeft requests left")
//        }
//
//        rateLimit (RateLimitName("public")){
//            post("public"){
//                val requestsLeft = call.response.headers["X-RateLimit-Remaining"]?.toInt() ?: 0
//                call.respondText("Ice cream supremacy!, but you have $requestsLeft requests left")
//            }
//        }
//
//        rateLimit(RateLimitName("protected")){
//        post("protected"){
//            val requestsLeft = call.response.headers["X-RateLimit-Remaining"]?.toInt() ?: 0
//            call.respondText("Ice cream supremacy!, but you have $requestsLeft requests left")
//        }
//            }
//
//
//        route("message") {
//            install(RequestValidation) {
//                validate<String> { body ->
//                    if (body.isBlank()) ValidationResult.Invalid("Body is empty")
//                    else if (!body.startsWith("Hello")) ValidationResult.Invalid("Invalid message, queen")
//                    else ValidationResult.Valid
//                }
//            }
//            post{
//                val message = call.receive<String>()
//                call.respondText(message)
//            }
//        }
//
//        route("message2") {
//            install(RequestValidation) {
//                validate<String> { body ->
//                    if (body.isBlank()) ValidationResult.Invalid("Body is empty")
//                    else if (!body.startsWith("Ice cream")) ValidationResult.Invalid("Invalid message again, queen")
//                    else ValidationResult.Valid
//                }
//            }
//            post{
//                val message = call.receive<String>()
//                call.respondText(message)
//            }
//        }
//
//
//            post("product") {
//                val product = call.receive<Product>()
//                call.respond(product)
//            }

        }
    }

    object Immutable: CacheControl(null){
        override fun toString(): String{
            return "Immutable"
        }
    }


    @Serializable
    data class Message(
        val text: String,
        val to:String? = null,
    )


    @Serializable
    data class Authrequest
        (
        val username: String,
        val password: String
    )

    @Serializable
    data class ProductResponse
        (
            val message: String,
            val data: List<Product>
         )

    @Serializable
    data class Product(
            val name: String?,
            val price: Int?,
            val category: String?
        )