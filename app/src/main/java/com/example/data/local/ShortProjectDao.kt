package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortProjectDao {
    @Query("SELECT * FROM converted_shorts ORDER BY createdAt DESC")
    fun getAllShorts(): Flow<List<ShortProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShort(project: ShortProjectEntity): Long

    @Query("DELETE FROM converted_shorts WHERE id = :id")
    suspend fun deleteShort(id: Long)

    @Query("SELECT COUNT(*) FROM converted_shorts")
    suspend fun getCount(): Int
}
