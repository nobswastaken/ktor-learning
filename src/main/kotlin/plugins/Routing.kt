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
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.Serializable
import java.io.File

fun Application.configureRouting(config: JWTConfig,httpClient: HttpClient ) {

    val userDB = mutableMapOf<String, UserInfo>()

    routing {



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