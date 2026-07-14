package com.example.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.plugins.statuspages.statusFile
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import kotlin.collections.mapOf

fun Application.configureStatusPages() {
    install(StatusPages) {

        status(HttpStatusCode.TooManyRequests){call, status ->
            val retryAfter = call.response.headers["Retry-After"]
            call.respondText(text = "429: Too many requests! Wait for $retryAfter seconds")
        }


        exception<Throwable>{call,cause ->
            call.respondText("500: ${cause.message}", status = HttpStatusCode.InternalServerError )
        }

        exception <RequestValidationException>{ call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("errors" to cause.reasons))
        }

        status(HttpStatusCode.Unauthorized) {call,cause ->
            call.respondText ("401:You're not authorized to access this resource", status = HttpStatusCode.Unauthorized )
        }

        status(HttpStatusCode.BadRequest) {call,cause ->
            call.respondText ("400: Please check request body", status = HttpStatusCode.BadRequest )
        }

        status(HttpStatusCode.NotFound) {call,cause ->
            call.respondText ("404: Page not found", status = HttpStatusCode.NotFound )
        }

        statusFile(HttpStatusCode.BadRequest,HttpStatusCode.Unauthorized,HttpStatusCode.NotFound, filePattern = "errors/error#.html")
    }
}