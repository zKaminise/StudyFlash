package com.example.server


import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import com.example.server.routes.registerFlashcardRoutes


fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}


fun Application.module() {
    install(CORS) { anyHost() }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; prettyPrint = true })
    }
    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }
    }
    registerFlashcardRoutes()
}