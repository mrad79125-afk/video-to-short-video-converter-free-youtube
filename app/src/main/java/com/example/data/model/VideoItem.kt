package com.example.data.model

data class VideoItem(
    val id: String,
    val title: String,
    val channelTitle: String,
    val durationSeconds: Int,
    val youtubeUrl: String,
    val description: String,
    val category: String,
    val transcript: String
) {
    fun formattedDuration(): String {
        val m = durationSeconds / 60
        val s = durationSeconds % 60
        return String.format("%d:%02d", m, s)
    }
}
