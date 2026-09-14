package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FireOrange
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow

data class CaptionWord(
    val word: String,
    val startMs: Long,
    val endMs: Long,
    val isHighlighted: Boolean = false
)

data class CaptionLine(
    val id: String,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val words: List<CaptionWord> = emptyList()
)

enum class CaptionStyle(
    val displayName: String,
    val primaryColor: Color,
    val highlightColor: Color,
    val isUppercase: Boolean,
    val hasBackgroundBox: Boolean,
    val glowEffect: Boolean
) {
    HORMOZI_YELLOW(
        displayName = "Viral Pop (Hormozi)",
        primaryColor = Color.White,
        highlightColor = NeonYellow,
        isUppercase = true,
        hasBackgroundBox = true,
        glowEffect = false
    ),
    CYBER_NEON(
        displayName = "Cyber Neon",
        primaryColor = Color.White,
        highlightColor = NeonGreen,
        isUppercase = true,
        hasBackgroundBox = false,
        glowEffect = true
    ),
    FIRE_PUNCH(
        displayName = "Fire Punch",
        primaryColor = Color.White,
        highlightColor = FireOrange,
        isUppercase = true,
        hasBackgroundBox = true,
        glowEffect = true
    ),
    MINIMAL_CLEAN(
        displayName = "Clean Minimal",
        primaryColor = Color.White,
        highlightColor = Color(0xFF67E8F9),
        isUppercase = false,
        hasBackgroundBox = false,
        glowEffect = false
    )
}

enum class CaptionPosition(val displayName: String) {
    BOTTOM("Bottom (Standard)"),
    CENTER("Center (Viral)"),
    TOP("Top (Headline)")
}
