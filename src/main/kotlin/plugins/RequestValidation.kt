package com.example.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult

fun Application.configureRequestValidation() {
    install(RequestValidation){
        validate<String> {body ->
            if (body.isBlank()) ValidationResult.Invalid("Body is empty")
            else ValidationResult.Valid
        }
    }
}