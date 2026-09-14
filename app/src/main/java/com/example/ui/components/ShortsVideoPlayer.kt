package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaptionLine
import com.example.data.model.CaptionPosition
import com.example.data.model.CaptionStyle
import com.example.data.model.FramingMode
import com.example.data.model.TargetAspectRatio
import com.example.data.model.ViralClip
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.ShortsPurple
import com.example.ui.theme.YouTubeRed

@Composable
fun ShortsVideoPlayer(
    clip: ViralClip,
    sourceVideoTitle: String,
    framingMode: FramingMode,
    targetAspectRatio: TargetAspectRatio,
    captionStyle: CaptionStyle,
    captionPosition: CaptionPosition,
    captionFontSizeSp: Float,
    showEmojis: Boolean,
    playbackPositionMs: Long,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMs = (clip.durationSec * 1000L).coerceAtLeast(1000L)
    val activeLine = clip.captions.find { playbackPositionMs in it.startMs..it.endMs }

    // Floating pulsating background transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )

    val kineticSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "kineticSweep"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF100F17))
            .border(1.dp, Color(0xFF2A283C), RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Aspect Ratio Viewport Frame
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .aspectRatio(targetAspectRatio.ratioFloat)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .clickable { onTogglePlayPause() }
                .testTag("player_viewport"),
            contentAlignment = Alignment.Center
        ) {
            // Visual Framing Renderers
            when (framingMode) {
                FramingMode.SPLIT_SCREEN -> {
                    SplitScreenFraming(
                        sourceTitle = sourceVideoTitle,
                        clipTitle = clip.title,
                        isPlaying = isPlaying,
                        sweepProgress = kineticSweep
                    )
                }
                FramingMode.BLURRED_BACKDROP -> {
                    BlurredBackdropFraming(
                        sourceTitle = sourceVideoTitle,
                        pulse = ambientPulse,
                        isPlaying = isPlaying
                    )
                }
                FramingMode.CENTER_CROP -> {
                    CenterCropFraming(
                        sourceTitle = sourceVideoTitle,
                        isPlaying = isPlaying
                    )
                }
                FramingMode.HEADLINE_BANNER -> {
                    HeadlineBannerFraming(
                        clip = clip,
                        isPlaying = isPlaying
                    )
                }
            }

            // Captions Overlay
            CaptionsOverlay(
                activeLine = activeLine,
                playbackPositionMs = playbackPositionMs,
                captionStyle = captionStyle,
                captionPosition = captionPosition,
                fontSizeSp = captionFontSizeSp,
                showEmojis = showEmojis,
                modifier = Modifier.fillMaxSize()
            )

            // Top Status Overlay (YouTube Shorts badge & Virality score)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.6f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) YouTubeRed else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SHORTS 9:16",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.7f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔥 ${clip.viralityScore}/100",
                            color = NeonYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Center Play/Pause Overlay Indicator when paused
            if (!isPlaying) {
                Surface(
                    shape = CircleShape,
                    color = YouTubeRed.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(16.dp, CircleShape),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrub Bar & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1C2B))
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = YouTubeRed,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Slider(
                value = (playbackPositionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f),
                onValueChange = { frac ->
                    onSeekTo((frac * totalMs).toLong())
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("playback_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = YouTubeRed,
                    activeTrackColor = YouTubeRed,
                    inactiveTrackColor = Color(0xFF2A283C)
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Formatted Time (e.g. 00:12 / 00:45)
            val curSec = playbackPositionMs / 1000
            val totSec = totalMs / 1000
            Text(
                text = String.format("%02d:%02d / %02d:%02d", curSec / 60, curSec % 60, totSec / 60, totSec % 60),
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SplitScreenFraming(
    sourceTitle: String,
    clipTitle: String,
    isPlaying: Boolean,
    sweepProgress: Float
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top 52%: Video Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.52f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A122E), Color(0xFF0F0B18))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Simulated camera interview frame
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2E1B4E),
                    border = androidx.compose.foundation.BorderStroke(2.dp, ShortsPurple),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = sourceTitle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }

        // Bottom 48%: Dynamic Satisfying Viral Motion Backdrop (Subway Surfer / Kinetic Grid)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.48f)
                .drawBehind {
                    // Draw dynamic kinetic grid lines
                    val step = 28.dp.toPx()
                    val offset = sweepProgress * step
                    for (x in -step.toInt()..size.width.toInt() step step.toInt()) {
                        drawLine(
                            color = Color(0x338B5CF6),
                            start = Offset(x + offset, 0f),
                            end = Offset(x + offset, size.height),
                            strokeWidth = 1.5f
                        )
                    }
                    for (y in 0..size.height.toInt() step step.toInt()) {
                        drawLine(
                            color = Color(0x228B5CF6),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(size.width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0C091A), Color(0xFF06040C))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KINETIC RETENTION BACKDROP",
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlurredBackdropFraming(
    sourceTitle: String,
    pulse: Float,
    isPlaying: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF35124D), Color(0xFF0C0A14)),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Centered 16:9 Video Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1633))
                .border(1.5.dp, Color(0xFF4C2882), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = NeonYellow,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sourceTitle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun CenterCropFraming(
    sourceTitle: String,
    isPlaying: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E1035), Color(0xFF0F081D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF3B1E6D),
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = sourceTitle,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HeadlineBannerFraming(
    clip: ViralClip,
    isPlaying: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0910))
    ) {
        // Top Viral Hook Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(YouTubeRed)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = clip.title.uppercase(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }

        // Center Video Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF151221)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\"${clip.soundbite}\"",
                color = Color(0xFFE2E8F0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp)
            )
        }

        // Bottom Hook Prompt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1633))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "WAIT FOR THE END 👇",
                color = NeonYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun CaptionsOverlay(
    activeLine: CaptionLine?,
    playbackPositionMs: Long,
    captionStyle: CaptionStyle,
    captionPosition: CaptionPosition,
    fontSizeSp: Float,
    showEmojis: Boolean,
    modifier: Modifier = Modifier
) {
    if (activeLine == null) return

    val alignment = when (captionPosition) {
        CaptionPosition.TOP -> Alignment.TopCenter
        CaptionPosition.CENTER -> Alignment.Center
        CaptionPosition.BOTTOM -> Alignment.BottomCenter
    }

    val verticalPadding = when (captionPosition) {
        CaptionPosition.TOP -> 64.dp
        CaptionPosition.CENTER -> 0.dp
        CaptionPosition.BOTTOM -> 50.dp
    }

    Box(
        modifier = modifier.padding(vertical = verticalPadding, horizontal = 16.dp),
        contentAlignment = alignment
    ) {
        // Render word-by-word highlighted caption block
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (captionStyle.hasBackgroundBox) Color.Black.copy(alpha = 0.85f) else Color.Transparent,
            border = if (captionStyle.hasBackgroundBox) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333344)) else null,
            shadowElevation = if (captionStyle.hasBackgroundBox) 4.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeLine.words.isNotEmpty()) {
                    activeLine.words.forEachIndexed { idx, wordObj ->
                        val isCurrentWord = playbackPositionMs in wordObj.startMs..wordObj.endMs
                        val isTargetKeyword = wordObj.isHighlighted || isCurrentWord

                        val textColor = when {
                            isCurrentWord -> captionStyle.highlightColor
                            isTargetKeyword -> captionStyle.highlightColor
                            else -> captionStyle.primaryColor
                        }

                        val scale = if (isCurrentWord) 1.08f else 1.0f

                        val displayWord = if (captionStyle.isUppercase) {
                            wordObj.word.uppercase()
                        } else {
                            wordObj.word
                        }

                        Text(
                            text = "$displayWord ",
                            color = textColor,
                            fontSize = (fontSizeSp * scale).sp,
                            fontWeight = if (isTargetKeyword) FontWeight.Black else FontWeight.Bold,
                            modifier = Modifier.scale(scale)
                        )
                    }
                } else {
                    Text(
                        text = if (captionStyle.isUppercase) activeLine.text.uppercase() else activeLine.text,
                        color = captionStyle.highlightColor,
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
