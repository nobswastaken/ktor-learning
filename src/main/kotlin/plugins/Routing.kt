package com.example.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.Serializable
import java.io.File

fun Application.configureRouting() {


    routing {
        route("message") {
            install(RequestValidation) {
                validate<String> { body ->
                    if (body.isBlank()) ValidationResult.Invalid("Body is empty")
                    else if (!body.startsWith("Hello")) ValidationResult.Invalid("Invalid message, queen")
                    else ValidationResult.Valid
                }
            }
            post{
                val message = call.receive<String>()
                call.respondText(message)
            }
        }

        route("message2") {
            install(RequestValidation) {
                validate<String> { body ->
                    if (body.isBlank()) ValidationResult.Invalid("Body is empty")
                    else if (!body.startsWith("Ice cream")) ValidationResult.Invalid("Invalid message again, queen")
                    else ValidationResult.Valid
                }
            }
            post{
                val message = call.receive<String>()
                call.respondText(message)
            }
        }


            post("product") {
                val product = call.receive<Product>()
                call.respond(product)
            }

        }
    }

    @Serializable
    data class Product(
            val name: String?,
            val price: Int?,
            val category: String?
        )