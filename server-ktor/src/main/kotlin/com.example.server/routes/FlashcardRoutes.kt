package com.example.server.routes


import com.example.server.model.CreateFlashcardRequest
import com.example.server.repository.FlashcardRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*


private val repo = FlashcardRepository()


fun Application.registerFlashcardRoutes() {
    routing {
        route("/api/flashcards") {
            get { call.respond(repo.all()) }
            post {
                val body = call.receive<CreateFlashcardRequest>()
                val created = repo.create(body.type, body.frontText, body.backText)
                call.respond(created)
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(mapOf("error" to "invalid id"))
                } else {
                    repo.delete(id)
                    call.respond(mapOf("status" to "deleted"))
                }
            }
        }
    }
}