package com.example.server.repository


import com.example.server.model.FlashcardDto
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong


class FlashcardRepository {
    private val seq = AtomicLong(1)
    private val db = ConcurrentHashMap<Long, FlashcardDto>()


    fun all(): List<FlashcardDto> = db.values.sortedByDescending { it.id }


    fun create(type: String, front: String?, back: String?): FlashcardDto {
        val id = seq.getAndIncrement()
        val dto = FlashcardDto(id = id, type = type, frontText = front, backText = back)
        db[id] = dto
        return dto
    }


    fun delete(id: Long) { db.remove(id) }
}