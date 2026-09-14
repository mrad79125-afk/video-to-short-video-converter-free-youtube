package com.example.data.model

enum class FramingMode(val displayName: String, val description: String) {
    SPLIT_SCREEN(
        displayName = "Split Screen",
        description = "Top video + Bottom dynamic satisfying background"
    ),
    BLURRED_BACKDROP(
        displayName = "Blurred Backdrop",
        description = "Full 9:16 canvas with blurred ambient zoom"
    ),
    CENTER_CROP(
        displayName = "Smart Crop",
        description = "Direct 9:16 vertical center punch-in"
    ),
    HEADLINE_BANNER(
        displayName = "Headline Frame",
        description = "Video centered with bold viral banner bar"
    )
}

enum class TargetAspectRatio(val displayName: String, val ratioText: String, val ratioFloat: Float) {
    RATIO_9_16("Shorts / Reels (9:16)", "9:16", 9f / 16f),
    RATIO_1_1("Square Post (1:1)", "1:1", 1f),
    RATIO_16_9("Original (16:9)", "16:9", 16f / 9f)
}

data class ConvertedShortProject(
    val id: Long = 0,
    val title: String,
    val sourceVideoTitle: String,
    val durationSec: Int,
    val viralityScore: Int,
    val framingMode: FramingMode,
    val captionStyle: CaptionStyle,
    val captionPosition: CaptionPosition,
    val soundbite: String,
    val hashtags: String,
    val createdAt: Long = System.currentTimeMillis()
)
