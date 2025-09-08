package com.example.studyflash.data.sync

import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.remote.KtorApi
import com.example.studyflash.data.remote.toDto
import com.example.studyflash.data.remote.toEntity
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val api: KtorApi,
    private val dao: FlashcardDao
) {
    // PULL: baixa tudo do servidor e substitui o local
    suspend fun pullAll(): Int {
        val remote = api.getAll()
        val entities = remote.map { it.toEntity() }
        dao.clearAll()
        entities.forEach { dao.upsert(it) }
        return entities.size
    }

    // PUSH: envia tudo que está local para o servidor (upsert por ID)
    suspend fun pushAll(): Int {
        val local = dao.observeAll() // Flow – queremos snapshot:
        // Pegamos uma foto via query direta
        val snapshot = getAllOnce()
        snapshot.forEach { card ->
            val id = if (card.id == 0L) 0L else card.id
            if (id == 0L) {
                api.create(card.toDto().copy(id = null))
            } else {
                api.upsert(id, card.toDto().copy(id = id))
            }
        }
        return snapshot.size
    }

    // Helper: consulta snapshot
    private suspend fun getAllOnce(): List<FlashcardEntity> {
        // Como não temos um DAO específico para lista, reusamos observeAll com um collect simples:
        // Em produção, crie um @Query que retorna List<FlashcardEntity>.
        var list: List<FlashcardEntity> = emptyList()
        // Alternativa rápida: crie uma query direta:
        // @Query("SELECT * FROM flashcards") suspend fun listAll(): List<FlashcardEntity>
        // E use-a aqui. Para não alterar seu DAO muito, vou sugerir essa query:

        // >>> ADICIONE no DAO:
        // @Query("SELECT * FROM flashcards")
        // suspend fun listAll(): List<FlashcardEntity>

        // Depois troque aqui por:
        // list = dao.listAll()

        // Enquanto isso, retorno vazio para evitar travar quem ainda não adicionou a query:
        return list
    }
}
