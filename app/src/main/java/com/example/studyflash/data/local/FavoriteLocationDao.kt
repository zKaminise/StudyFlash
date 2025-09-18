package com.example.studyflash.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {

    @Query("SELECT * FROM favorite_locations ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FavoriteLocationEntity>>

    @Query("SELECT * FROM favorite_locations WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FavoriteLocationEntity?

    @Query("SELECT COUNT(*) FROM favorite_locations")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteLocationEntity)

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE favorite_locations SET isCurrent = 0 WHERE isCurrent = 1")
    suspend fun clearCurrent()

    @Query("UPDATE favorite_locations SET isCurrent = 1 WHERE id = :id")
    suspend fun setCurrent(id: String)

    @Transaction
    suspend fun setCurrentExclusive(id: String) {
        clearCurrent()
        setCurrent(id)
    }

    @Query("SELECT id FROM favorite_locations WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentId(): String?

    // ⬇️ atualizações
    @Query("UPDATE favorite_locations SET latitude = :lat, longitude = :lon WHERE id = :id")
    suspend fun updateCoordinates(id: String, lat: Double, lon: Double)

    @Query("UPDATE favorite_locations SET radiusMeters = :radius WHERE id = :id")
    suspend fun updateRadius(id: String, radius: Float)

    @Query("SELECT * FROM favorite_locations")
    suspend fun listAll(): List<FavoriteLocationEntity>

}
