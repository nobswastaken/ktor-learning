package com.example.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer


val userdb: Map<String,String> = mapOf(
    "token1" to "Nobara",
    "token2" to "Gregory",
    "token3" to "Sean",
    "token4" to "Tobi",
)

fun Application.configureBearerAuthentication(){
    install(Authentication){
        bearer("bearer-auth"){
            realm = "Access protected routes"
            authenticate { tokenCredential ->
                val user = userdb[tokenCredential.token]
                if(!user.isNullOrBlank()){
                    UserIdPrincipal(user)
                }else{
                    null
                }
            }
        }
    }
}