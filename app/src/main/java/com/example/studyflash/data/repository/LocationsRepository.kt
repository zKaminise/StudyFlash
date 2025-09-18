package com.example.studyflash.data.repository

import com.example.studyflash.data.local.FavoriteLocationDao
import com.example.studyflash.data.local.FavoriteLocationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationsRepository @Inject constructor(
    private val dao: FavoriteLocationDao
) {
    fun observe(): Flow<List<FavoriteLocationEntity>> = dao.observeAll()

    // 🔽 NOVO: pega uma foto da lista atual (sem precisar criar novo DAO)
    suspend fun snapshot(): List<FavoriteLocationEntity> = observe().first()

    suspend fun getById(id: String): FavoriteLocationEntity? = dao.getById(id)

    suspend fun add(name: String, latitude: Double? = null, longitude: Double? = null): Result<Unit> {
        val max = dao.count()
        if (max >= 7) return Result.failure(IllegalStateException("Limite de 7 locais atingido"))
        val id = UUID.randomUUID().toString()
        dao.insert(
            FavoriteLocationEntity(
                id = id,
                name = name.trim().ifBlank { "Local ${max + 1}" },
                latitude = latitude,
                longitude = longitude,
                // radiusMeters default = 150f (na entity)
                isCurrent = max == 0 // primeiro local vira atual automaticamente
            )
        )
        return Result.success(Unit)
    }

    suspend fun delete(id: String) { dao.delete(id) }

    suspend fun setCurrent(id: String) { dao.setCurrentExclusive(id) }

    suspend fun getCurrentLocationId(): String? = dao.getCurrentId()

    suspend fun updateCoordinates(id: String, lat: Double, lon: Double) {
        dao.updateCoordinates(id, lat, lon)
    }

    suspend fun updateRadius(id: String, radius: Float) {
        dao.updateRadius(id, radius)
    }
}
