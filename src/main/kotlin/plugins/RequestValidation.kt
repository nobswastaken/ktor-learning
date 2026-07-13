package com.example.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult

fun Application.configureRequestValidation() {
    install(RequestValidation){


        validate<Product>{ body ->
            if (body.name.isNullOrBlank()) ValidationResult.Invalid("Invalid product name")
            else if(body.price == null || body.price <= 0) ValidationResult.Invalid("Invalid price name")
            else if(body.category.isNullOrBlank()) ValidationResult.Invalid("Invalid category name")
            else ValidationResult.Valid
        }
    }
}