package com.example.studyflash.data.sync

import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.remote.KtorApi
import com.example.studyflash.data.remote.toCreateRequest
import com.example.studyflash.data.remote.toEntity
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val api: KtorApi,
    private val dao: FlashcardDao
) {
    // Baixa tudo do servidor e substitui local
    suspend fun pullAll(): Int {
        val remote = api.getAll()
        val entities = remote.map { it.toEntity() }
        dao.clearAll()
        entities.forEach { dao.upsert(it) }
        return entities.size
    }

    // Envia tudo que está local pro servidor (cria ou atualiza)
    suspend fun pushAll(): Int {
        val snapshot: List<FlashcardEntity> = dao.listAll()
        snapshot.forEach { card ->
            val id = if (card.id == 0L) null else card.id
            val body = card.toCreateRequest()
            if (id == null) {
                // criar
                api.create(body)
            } else {
                // atualizar / upsert
                api.upsert(id, body)
            }
        }
        return snapshot.size
    }
}
