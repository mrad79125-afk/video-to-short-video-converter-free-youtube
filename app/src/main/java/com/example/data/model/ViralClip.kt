package com.example.data.model

data class ViralClip(
    val id: String,
    val videoId: String,
    val title: String,
    val hookCategory: String, // e.g. "Shocking Truth", "Curiosity Gap", "Key Insight", "Actionable Step"
    val hookExplanation: String,
    val startTimeSec: Int,
    val endTimeSec: Int,
    val viralityScore: Int, // 1 to 100
    val soundbite: String,
    val suggestedHashtags: List<String>,
    val captions: List<CaptionLine> = emptyList()
) {
    val durationSec: Int
        get() = (endTimeSec - startTimeSec).coerceAtLeast(1)

    fun formattedTimeRange(): String {
        val startM = startTimeSec / 60
        val startS = startTimeSec % 60
        val endM = endTimeSec / 60
        val endS = endTimeSec % 60
        return String.format("%02d:%02d - %02d:%02d (%ds)", startM, startS, endM, endS, durationSec)
    }
}
