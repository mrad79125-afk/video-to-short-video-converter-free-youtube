package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaptionLine
import com.example.data.model.CaptionPosition
import com.example.data.model.CaptionStyle
import com.example.data.model.FramingMode
import com.example.data.model.TargetAspectRatio
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.ShortsPurple
import com.example.ui.theme.YouTubeRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaptionStylePicker(
    currentFraming: FramingMode,
    currentAspectRatio: TargetAspectRatio,
    currentStyle: CaptionStyle,
    currentPosition: CaptionPosition,
    fontSizeSp: Float,
    showEmojis: Boolean,
    captions: List<CaptionLine>,
    onSelectFraming: (FramingMode) -> Unit,
    onSelectAspectRatio: (TargetAspectRatio) -> Unit,
    onSelectStyle: (CaptionStyle) -> Unit,
    onSelectPosition: (CaptionPosition) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onToggleEmojis: (Boolean) -> Unit,
    onUpdateCaption: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingCaptionLine by remember { mutableStateOf<CaptionLine?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section: Aspect Ratio Switcher
        SectionHeader(icon = Icons.Default.AspectRatio, title = "Output Ratio")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TargetAspectRatio.values().forEach { ratio ->
                val isSelected = ratio == currentAspectRatio
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) YouTubeRed else Color(0xFF1E1C2B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) YouTubeRed else Color(0xFF2E2C3F)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectAspectRatio(ratio) }
                        .testTag("ratio_${ratio.name}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = ratio.ratioText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (ratio == TargetAspectRatio.RATIO_9_16) "Shorts" else if (ratio == TargetAspectRatio.RATIO_1_1) "Square" else "Landscape",
                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Framing Mode (Split Screen / Blurred Backdrop / Center Crop / Headline)
        SectionHeader(icon = Icons.Default.CropRotate, title = "Shorts Framing Mode")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FramingMode.values().forEach { mode ->
                val isSelected = mode == currentFraming
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF2E1838) else Color(0xFF191724),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) ShortsPurple else Color(0xFF28253A)
                    ),
                    modifier = Modifier
                        .clickable { onSelectFraming(mode) }
                        .testTag("framing_${mode.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ShortsPurple else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mode.displayName,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Caption Animations & Visual Styles
        SectionHeader(icon = Icons.Default.FormatPaint, title = "AI Caption Style")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptionStyle.values().forEach { style ->
                val isSelected = style == currentStyle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF231C38) else Color(0xFF171523),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) style.highlightColor else Color(0xFF2C283F)
                    ),
                    modifier = Modifier
                        .clickable { onSelectStyle(style) }
                        .testTag("style_${style.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(style.highlightColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = style.displayName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Position & Font Size
        SectionHeader(icon = Icons.Default.TextFields, title = "Caption Position & Size")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaptionPosition.values().forEach { pos ->
                val isSelected = pos == currentPosition
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFF251A40) else Color(0xFF171523),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) ShortsPurple else Color(0xFF2A273D)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectPosition(pos) }
                        .testTag("pos_${pos.name}")
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pos.displayName.substringBefore(" "),
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Font Size: ${fontSizeSp.toInt()} sp",
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp
            )
            Slider(
                value = fontSizeSp,
                onValueChange = onFontSizeChange,
                valueRange = 14f..32f,
                modifier = Modifier
                    .width(180.dp)
                    .testTag("font_size_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = ShortsPurple,
                    activeTrackColor = ShortsPurple,
                    inactiveTrackColor = Color(0xFF2A283C)
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Caption Lines List & Quick Edit
        if (captions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Generated Captions (${captions.size} lines)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to edit text",
                    color = NeonYellow,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                captions.take(5).forEachIndexed { i, line ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF161421),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262338)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingCaptionLine = line }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%02d:%02d", line.startMs / 1000 / 60, (line.startMs / 1000) % 60),
                                color = ShortsPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = line.text,
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit caption",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Caption Dialog
    editingCaptionLine?.let { line ->
        var editedText by remember { mutableStateOf(line.text) }
        AlertDialog(
            onDismissRequest = { editingCaptionLine = null },
            title = { Text("Edit Caption Line", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text("Caption Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateCaption(line.id, editedText)
                        editingCaptionLine = null
                    }
                ) {
                    Text("Save", color = YouTubeRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCaptionLine = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1C2B)
        )
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = YouTubeRed,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
