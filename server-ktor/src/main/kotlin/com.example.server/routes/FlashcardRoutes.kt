package com.example.server.routes

import com.example.server.model.CreateFlashcardRequest
import com.example.server.repository.FlashcardRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val repo = FlashcardRepository()

fun Application.registerFlashcardRoutes() {
    routing {
        route("/api/flashcards") {
            get {
                call.respond(repo.all())
            }
            post {
                val body = runCatching { call.receive<CreateFlashcardRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid body"))
                    return@post
                }
                if (body.type.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing field: type"))
                    return@post
                }
                call.respond(HttpStatusCode.OK, repo.create(body))
            }
            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                    return@put
                }
                val body = runCatching { call.receive<CreateFlashcardRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid body"))
                    return@put
                }
                call.respond(HttpStatusCode.OK, repo.upsert(id, body))
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
                } else {
                    repo.delete(id)
                    call.respond(HttpStatusCode.OK, mapOf("status" to "deleted"))
                }
            }
        }
    }
}
