package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respondText
import java.util.Date

fun Application.configureJWTAuth(config:JWTConfig, httpClient: HttpClient) {
    install(Authentication) {
        jwt("jwt-auth"){
            realm = config.realm
            val jwtVerifier = JWT
            .require(Algorithm.HMAC256(config.secret))
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .build()

            verifier(jwtVerifier)
            validate { jwtCredential ->
                val username = jwtCredential.payload.getClaim("username").asString()
                if (username.isNullOrBlank()) {
                    JWTPrincipal(jwtCredential.payload)
                }else{
                    null
                }
            }

            challenge { _, _ ->
                call.respondText("Token invalid or expired",
                    status = HttpStatusCode.Unauthorized)
            }
        }
        configureGoogleOAuth(httpClient)
    }
}

fun generateToken(config: JWTConfig, username:String):String{
    return JWT.create()
        .withAudience(config.audience)
        .withIssuer(config.issuer)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + config.tokenExpiry))
        .sign(Algorithm.HMAC256(config.secret))
}



data class JWTConfig(
    val realm: String,
    val secret: String,
    val issuer: String,
    val audience: String,
    val tokenExpiry: Long
)