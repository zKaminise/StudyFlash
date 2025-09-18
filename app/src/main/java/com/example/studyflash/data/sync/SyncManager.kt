package com.example.studyflash.data.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.studyflash.data.local.FlashcardDao
import com.example.studyflash.data.local.FlashcardEntity
import com.example.studyflash.data.remote.KtorApi
import com.example.studyflash.data.remote.toCreateRequest
import com.example.studyflash.data.remote.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.max

class SyncManager @Inject constructor(
    private val api: KtorApi,
    private val dao: FlashcardDao,
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    private var lastSyncAt: Long
        get() = prefs.getLong("lastSyncAt", 0L)
        set(value) = prefs.edit { putLong("lastSyncAt", value) }

    /**
     * PULL com merge por updatedAt:
     * - se local não existe -> insere
     * - se remoto.updatedAt > local.updatedAt -> sobrescreve
     * - senão mantém local
     */
    suspend fun pullAll(): Result<Int> = runCatching {
        val remote = api.getAll().map { it.toEntity() }
        val locals = dao.listAll().associateBy { it.id }
        var applied = 0

        for (r in remote) {
            val l = locals[r.id]
            if (l == null) {
                dao.upsert(r)
                applied++
            } else if (r.updatedAt > l.updatedAt) {
                dao.upsert(r.copy(id = l.id))
                applied++
            }
        }
        lastSyncAt = System.currentTimeMillis()
        applied
    }

    /** PUSH (antigo): envia todos os cartões locais */
    suspend fun pushAll(): Result<Int> = runCatching {
        val snapshot: List<FlashcardEntity> = dao.listAll()
        snapshot.forEach { card ->
            val id = if (card.id == 0L) null else card.id
            val body = card.toCreateRequest()
            if (id == null) api.create(body) else api.upsert(id, body)
        }
        lastSyncAt = System.currentTimeMillis()
        snapshot.size
    }

    /** PUSH seletivo: envia apenas updatedAt > lastSyncAt */
    suspend fun pushSinceLastSync(): Result<Int> = runCatching {
        val since = lastSyncAt
        val toSend = dao.listUpdatedSince(since)
        if (toSend.isEmpty()) return@runCatching 0

        var maxUpdated = since
        toSend.forEach { card ->
            val id = if (card.id == 0L) null else card.id
            val body = card.toCreateRequest()
            if (id == null) api.create(body) else api.upsert(id, body)
            maxUpdated = max(maxUpdated, card.updatedAt)
        }
        lastSyncAt = maxUpdated
        toSend.size
    }
}
