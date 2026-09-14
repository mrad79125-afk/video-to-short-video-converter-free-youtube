package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CaptionStylePicker
import com.example.ui.components.SavedShortsSheet
import com.example.ui.components.ShortsVideoPlayer
import com.example.ui.components.ViralClipsList
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.ShortsPurple
import com.example.ui.theme.YouTubeRed

@Composable
fun ShortsAppScreen(viewModel: ShortsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val savedShorts by viewModel.savedShorts.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.exportSuccessMessage) {
        state.exportSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissExportMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0B10)),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StudioTopBar(
                savedCount = savedShorts.size,
                onOpenLibrary = { viewModel.switchTab(StudioTab.LIBRARY) }
            )
        },
        bottomBar = {
            StudioBottomNavigation(
                currentTab = state.currentTab,
                onSelectTab = { viewModel.switchTab(it) },
                clipsCount = state.extractedClips.size
            )
        },
        containerColor = Color(0xFF0C0B10)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            when (state.currentTab) {
                StudioTab.IMPORT -> {
                    item {
                        ImportSection(
                            state = state,
                            onUrlChange = { viewModel.updateInputUrl(it) },
                            onTitleChange = { viewModel.updateCustomTitle(it) },
                            onTranscriptChange = { viewModel.updateCustomTranscript(it) },
                            onImportCustom = { viewModel.importCustomVideo() },
                            onSelectPreset = { viewModel.selectVideo(it) }
                        )
                    }
                }

                StudioTab.CLIPS -> {
                    if (state.isAnalyzing) {
                        item {
                            AnalyzingStateCard(statusText = state.analysisProgressText)
                        }
                    } else if (state.extractedClips.isEmpty()) {
                        item {
                            EmptyClipsCard(onGoToImport = { viewModel.switchTab(StudioTab.IMPORT) })
                        }
                    } else {
                        item {
                            ViralClipsList(
                                video = state.selectedVideo,
                                clips = state.extractedClips,
                                selectedClip = state.selectedClip,
                                onSelectClip = { clip ->
                                    viewModel.selectClip(clip)
                                }
                            )
                        }
                    }
                }

                StudioTab.STUDIO -> {
                    val clip = state.selectedClip
                    if (clip == null) {
                        item {
                            EmptyStudioCard(onSelectClipTab = { viewModel.switchTab(StudioTab.CLIPS) })
                        }
                    } else {
                        item {
                            // Player & Live Converted Preview
                            ShortsVideoPlayer(
                                clip = clip,
                                sourceVideoTitle = state.selectedVideo.title,
                                framingMode = state.framingMode,
                                targetAspectRatio = state.targetAspectRatio,
                                captionStyle = state.captionStyle,
                                captionPosition = state.captionPosition,
                                captionFontSizeSp = state.captionFontSizeSp,
                                showEmojis = state.showEmojisInCaptions,
                                playbackPositionMs = state.playbackPositionMs,
                                isPlaying = state.isPlaying,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onSeekTo = { viewModel.seekTo(it) }
                            )
                        }

                        item {
                            // Export Button Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF161324)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Button(
                                        onClick = { viewModel.exportCurrentShort() },
                                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("export_short_button"),
                                        enabled = !state.isExporting
                                    ) {
                                        if (state.isExporting) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Rendering 9:16 Short (${(state.exportProgress * 100).toInt()}%)...",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Render & Save 9:16 Short",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    if (state.isExporting) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        LinearProgressIndicator(
                                            progress = { state.exportProgress },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = YouTubeRed,
                                            trackColor = Color(0xFF2E2C3F)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            // Styling & Customization Picker
                            CaptionStylePicker(
                                currentFraming = state.framingMode,
                                currentAspectRatio = state.targetAspectRatio,
                                currentStyle = state.captionStyle,
                                currentPosition = state.captionPosition,
                                fontSizeSp = state.captionFontSizeSp,
                                showEmojis = state.showEmojisInCaptions,
                                captions = clip.captions,
                                onSelectFraming = { viewModel.setFramingMode(it) },
                                onSelectAspectRatio = { viewModel.setAspectRatio(it) },
                                onSelectStyle = { viewModel.setCaptionStyle(it) },
                                onSelectPosition = { viewModel.setCaptionPosition(it) },
                                onFontSizeChange = { viewModel.setCaptionFontSize(it) },
                                onToggleEmojis = { viewModel.toggleEmojis(it) },
                                onUpdateCaption = { id, text -> viewModel.updateCaptionText(id, text) }
                            )
                        }
                    }
                }

                StudioTab.LIBRARY -> {
                    item {
                        SavedShortsSheet(
                            shorts = savedShorts,
                            onDeleteShort = { viewModel.deleteSavedShort(it) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun StudioTopBar(savedCount: Int, onOpenLibrary: () -> Unit) {
    Surface(
        color = Color(0xFF0F0E17),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF211E2E)),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = YouTubeRed,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SHORTS AI",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2B1938),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ShortsPurple)
                        ) {
                            Text(
                                text = "FREE AI",
                                color = ShortsPurple,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Video to Viral Shorts Converter & Auto Captions",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E1A2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF373153)),
                modifier = Modifier.clickable { onOpenLibrary() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = NeonYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$savedCount Saved",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioBottomNavigation(
    currentTab: StudioTab,
    onSelectTab: (StudioTab) -> Unit,
    clipsCount: Int
) {
    Surface(
        color = Color(0xFF100F18),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252236)),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                    color = YouTubeRed,
                    height = 3.dp
                )
            }
        ) {
            StudioTab.values().forEach { tab ->
                val isSelected = tab == currentTab
                Tab(
                    selected = isSelected,
                    onClick = { onSelectTab(tab) },
                    text = {
                        val label = when (tab) {
                            StudioTab.CLIPS -> if (clipsCount > 0) "Clips ($clipsCount)" else "Clips"
                            else -> tab.label
                        }
                        Text(
                            text = label,
                            color = if (isSelected) YouTubeRed else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name}")
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImportSection(
    state: ShortsUiState,
    onUrlChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onTranscriptChange: (String) -> Unit,
    onImportCustom: () -> Unit,
    onSelectPreset: (com.example.data.model.VideoItem) -> Unit
) {
    var showAdvancedInputs by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141220)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29253C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1-Click Viral Shorts Generator",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Convert any long-form YouTube video or podcast into high-retention 9:16 vertical Shorts with animated captions and AI hook detection.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // YouTube URL Input Field
                OutlinedTextField(
                    value = state.inputUrl,
                    onValueChange = onUrlChange,
                    placeholder = { Text("Paste YouTube Video URL (or choose preset below)...", color = Color(0xFF64748B), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = YouTubeRed
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("youtube_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = Color(0xFF333049),
                        focusedContainerColor = Color(0xFF0F0E17),
                        unfocusedContainerColor = Color(0xFF0F0E17),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Collapsible Advanced Transcript / Custom Video input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedInputs = !showAdvancedInputs }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showAdvancedInputs) "Hide Custom Transcript / Video Title" else "+ Add Custom Video Title or Transcript",
                        color = ShortsPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (showAdvancedInputs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = ShortsPurple
                    )
                }

                AnimatedVisibility(visible = showAdvancedInputs) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.customTitleInput,
                            onValueChange = onTitleChange,
                            placeholder = { Text("Custom Video Title (optional)", color = Color(0xFF64748B), fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShortsPurple,
                                unfocusedBorderColor = Color(0xFF333049),
                                focusedContainerColor = Color(0xFF0F0E17),
                                unfocusedContainerColor = Color(0xFF0F0E17),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.customTranscriptInput,
                            onValueChange = onTranscriptChange,
                            placeholder = { Text("Paste video transcript or speech notes...", color = Color(0xFF64748B), fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShortsPurple,
                                unfocusedBorderColor = Color(0xFF333049),
                                focusedContainerColor = Color(0xFF0F0E17),
                                unfocusedContainerColor = Color(0xFF0F0E17),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onImportCustom,
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("extract_viral_clips_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Extract Viral Clips with AI",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preset YouTube Videos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trending YouTube Creator Presets",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Instant 1-Tap Extract",
                color = NeonYellow,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        state.sampleVideos.forEach { sample ->
            val isCurrent = sample.id == state.selectedVideo.id
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFF1C172B) else Color(0xFF13111C)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrent) ShortsPurple else Color(0xFF242136)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onSelectPreset(sample) }
                    .testTag("sample_preset_${sample.id}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF271A3B),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.YoutubeSearchedFor,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sample.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${sample.channelTitle} • ${sample.formattedDuration()}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) YouTubeRed else Color(0xFF201D2E)
                    ) {
                        Text(
                            text = if (isCurrent) "ACTIVE" else "SELECT",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyzingStateCard(statusText: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151221)),
        border = androidx.compose.foundation.BorderStroke(1.dp, ShortsPurple.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = YouTubeRed,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Gemini AI Analyzing Video",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusText,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyClipsCard(onGoToImport: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14121F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242136)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MovieCreation,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No Viral Clips Extracted Yet",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Paste a YouTube video URL or select one of the presets to run AI viral moment detection.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onGoToImport,
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Go to Import & AI", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyStudioCard(onSelectClipTab: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14121F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF242136)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = ShortsPurple,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Select a Clip to Edit",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose an AI viral clip to open the 9:16 vertical shorts converter, caption animations, and export tools.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onSelectClipTab,
                colors = ButtonDefaults.buttonColors(containerColor = ShortsPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Browse Viral Clips", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
