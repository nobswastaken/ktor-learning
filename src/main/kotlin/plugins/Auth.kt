package com.example.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserHashedTableAuth
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic
import io.ktor.util.getDigestFunction

fun Application.configureAuth() {

    val hashedUserTable = createHashedUserTable()

    install(Authentication) {
        basic("basic-auth"){
            validate{ credentials ->
//                val username = credentials.name
//                val password = credentials.password
//
//                if (username == "admin" && password == "password"){
//                    UserIdPrincipal(username)
//                }
//                else{
//                    null
//                }
                hashedUserTable.authenticate(credentials)

            }
        }
    }
}

fun createHashedUserTable () : UserHashedTableAuth {

    val digestFunction = getDigestFunction("SHA-256"){"ktor${it.length}"}

    return UserHashedTableAuth(
        digester = digestFunction,
        table = mapOf(
            "admin" to digestFunction("password"),
            "user" to digestFunction("123")
        )
    )
}