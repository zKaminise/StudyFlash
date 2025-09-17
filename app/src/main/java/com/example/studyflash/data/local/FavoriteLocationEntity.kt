package com.example.studyflash.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_locations")
data class FavoriteLocationEntity(
    @PrimaryKey val id: String,          // UUID
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float = 150f,      // ⬅️ usado nas telas/edição
    val isCurrent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
