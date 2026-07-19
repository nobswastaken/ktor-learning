package com.example

import com.example.plugins.JWTConfig
import com.example.plugins.configureAuth
import com.example.plugins.configureAutoHeadResponse
import com.example.plugins.configureBearerAuthentication
import com.example.plugins.configureDigestAuth
import com.example.plugins.configureJWTAuth
import com.example.plugins.configurePartialContent
import com.example.plugins.configureRequestValidation
import com.example.plugins.configureResources
import com.example.plugins.configureRouting
import com.example.plugins.configureSSE
import com.example.plugins.configureSerialization
import com.example.plugins.configureSessionAuth
import com.example.plugins.configureSessions
import com.example.plugins.configureStatusPages
import com.example.plugins.configureWebsockets
import com.example.plugins.configurerateLimit
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.*
import io.ktor.server.application.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    val httpClient = HttpClient(CIO){
        install(ContentNegotiation) {
            json(Json{
                ignoreUnknownKeys = true
            })
        }
    }
    val jwt = environment.config.config("ktor.jwt")
    val realm = jwt.property("realm").getString()
    val secret = jwt.property("secret").getString()
    val issuer = jwt.property("issuer").getString()
    val audience = jwt.property("audience").getString()
    val tokenExpiry = jwt.property("expiry").getString().toLong()

    val config = JWTConfig(
        realm = realm,
        issuer = issuer,
        audience = audience,
        tokenExpiry = tokenExpiry,
        secret = secret
    )


    configurerateLimit()
//  configureAuth()
//  configureDigestAuth()
//    configureBearerAuthentication()
    configureSessions()
//    configureSessionAuth()
    configureJWTAuth(config, httpClient)
    configureSSE()
    configureWebsockets()
    configureRouting(config, httpClient)
    configureSerialization()
    configureResources()
    configureStatusPages()
    configureRequestValidation()
    configureAutoHeadResponse()
    configurePartialContent()


}