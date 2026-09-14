package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "converted_shorts")
data class ShortProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceVideoTitle: String,
    val durationSec: Int,
    val viralityScore: Int,
    val framingMode: String,
    val captionStyle: String,
    val captionPosition: String,
    val soundbite: String,
    val hashtags: String,
    val createdAt: Long = System.currentTimeMillis()
)
