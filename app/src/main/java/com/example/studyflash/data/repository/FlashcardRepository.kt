package com.example.studyflash.data.repository


import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.remote.CreateFlashcardRequest
import com.example.studyflash.data.remote.KtorApi
import kotlinx.coroutines.flow.Flow


class FlashcardRepository(
    private val api: KtorApi,
    private val dao: FlashcardDao
) {
    fun observeLocal(): Flow<List<FlashcardEntity>> = dao.observeAll()


    suspend fun createLocal(type: String, front: String?, back: String?) {
        dao.upsert(FlashcardEntity(type = type, frontText = front, backText = back))
    }


    suspend fun syncFromServer() {
        val remote = api.getAll()
        remote.forEach {
            dao.upsert(
                FlashcardEntity(
                    id = it.id ?: 0,
                    type = it.type,
                    frontText = it.frontText,
                    backText = it.backText
                )
            )
        }
    }


    suspend fun pushToServer(entity: FlashcardEntity) {
        val created = api.create(
            CreateFlashcardRequest(entity.type, entity.frontText, entity.backText)
        )
// Atualiza localmente com o id retornado
        dao.upsert(entity.copy(id = created.id ?: entity.id))
    }


    suspend fun deleteLocal(entity: FlashcardEntity) = dao.delete(entity)
}