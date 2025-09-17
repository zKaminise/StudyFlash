package com.example.server.repository

import com.example.server.model.CreateFlashcardRequest
import com.example.server.model.FlashcardDto
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FlashcardRepository {
    private val seq = AtomicLong(1L)
    private val db = ConcurrentHashMap<Long, FlashcardDto>()

    fun all(): List<FlashcardDto> =
        db.values.sortedBy { it.id ?: Long.MAX_VALUE }

    fun create(req: CreateFlashcardRequest): FlashcardDto {
        val id = seq.getAndIncrement()
        val dto = FlashcardDto(
            id = id,
            type = req.type,
            frontText = req.frontText,
            backText = req.backText,
            wrong1 = req.wrong1,
            wrong2 = req.wrong2,
            wrong3 = req.wrong3,
            payloadJson = req.payloadJson
        )
        db[id] = dto
        return dto
    }

    fun upsert(id: Long, req: CreateFlashcardRequest): FlashcardDto {
        val dto = FlashcardDto(
            id = id,
            type = req.type,
            frontText = req.frontText,
            backText = req.backText,
            wrong1 = req.wrong1,
            wrong2 = req.wrong2,
            wrong3 = req.wrong3,
            payloadJson = req.payloadJson
        )
        db[id] = dto
        seq.accumulateAndGet(id + 1) { cur, next -> maxOf(cur, next) }
        return dto
    }

    fun delete(id: Long) {
        db.remove(id)
    }
}
