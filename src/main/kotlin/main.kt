package com.example

import com.example.plugins.configureAuth
import com.example.plugins.configureAutoHeadResponse
import com.example.plugins.configureDigestAuth
import com.example.plugins.configurePartialContent
import com.example.plugins.configureRequestValidation
import com.example.plugins.configureResources
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import com.example.plugins.configurerateLimit
import io.ktor.server.engine.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configurerateLimit()
//    configureAuth()
    configureDigestAuth()
    configureRouting()
    configureSerialization()
    configureResources()
    configureStatusPages()
    configureRequestValidation()
    configureAutoHeadResponse()
    configurePartialContent()


}