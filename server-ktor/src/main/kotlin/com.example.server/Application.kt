package com.example.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Serializable
data class FlashcardDto(
    val id: Long? = null,
    val type: String,
    val frontText: String? = null,
    val backText: String? = null
)

@Serializable
data class CreateFlashcardRequest(
    val type: String,
    val frontText: String? = null,
    val backText: String? = null
)

private object InMemoryDb {
    private val seq = AtomicLong(1)
    private val map = ConcurrentHashMap<Long, FlashcardDto>()

    fun all(): List<FlashcardDto> = map.values.sortedBy { it.id }
    fun create(req: CreateFlashcardRequest): FlashcardDto {
        val id = seq.getAndIncrement()
        val dto = FlashcardDto(id, req.type, req.frontText, req.backText)
        map[id] = dto
        return dto
    }
    fun upsert(id: Long, dto: FlashcardDto): FlashcardDto {
        val stored = dto.copy(id = id)
        map[id] = stored
        // mantém o sequence acima do maior id
        seq.accumulateAndGet(id + 1) { cur, next -> maxOf(cur, next) }
        return stored
    }
    fun delete(id: Long) { map.remove(id) }
}

fun Application.module() {
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
    install(ContentNegotiation) { json() }

    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }

        route("/api/flashcards") {
            get {
                call.respond(InMemoryDb.all())
            }
            post {
                val req = call.receive<CreateFlashcardRequest>()
                call.respond(HttpStatusCode.Created, InMemoryDb.create(req))
            }
            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid id")
                val dto = call.receive<FlashcardDto>()
                call.respond(HttpStatusCode.OK, InMemoryDb.upsert(id, dto))
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")
                InMemoryDb.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

/**
 * ✅ Main com assinatura Java-friendly:
 * Garante que o Gradle/Java encontrem `public static void main(String[] args)`.
 */
fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}
