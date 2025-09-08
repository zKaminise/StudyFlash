package com.example.studyflash.data.local

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface AttemptHistoryDao {
    @Insert
    suspend fun insert(entity: AttemptHistoryEntity): Long
}
