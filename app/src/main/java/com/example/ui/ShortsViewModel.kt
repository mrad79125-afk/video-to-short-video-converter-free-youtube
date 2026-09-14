package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiClipService
import com.example.data.local.AppDatabase
import com.example.data.model.CaptionLine
import com.example.data.model.CaptionPosition
import com.example.data.model.CaptionStyle
import com.example.data.model.CaptionWord
import com.example.data.model.ConvertedShortProject
import com.example.data.model.FramingMode
import com.example.data.model.TargetAspectRatio
import com.example.data.model.VideoItem
import com.example.data.model.ViralClip
import com.example.data.repository.ShortsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class StudioTab(val label: String) {
    IMPORT("Import & AI"),
    CLIPS("Viral Clips"),
    STUDIO("Shorts Studio"),
    LIBRARY("Saved Shorts")
}

data class ShortsUiState(
    val currentTab: StudioTab = StudioTab.IMPORT,
    val selectedVideo: VideoItem,
    val sampleVideos: List<VideoItem> = emptyList(),
    val extractedClips: List<ViralClip> = emptyList(),
    val selectedClip: ViralClip? = null,
    val isAnalyzing: Boolean = false,
    val analysisProgressText: String = "",
    val framingMode: FramingMode = FramingMode.SPLIT_SCREEN,
    val targetAspectRatio: TargetAspectRatio = TargetAspectRatio.RATIO_9_16,
    val captionStyle: CaptionStyle = CaptionStyle.HORMOZI_YELLOW,
    val captionPosition: CaptionPosition = CaptionPosition.BOTTOM,
    val captionFontSizeSp: Float = 20f,
    val showEmojisInCaptions: Boolean = true,
    val isPlaying: Boolean = false,
    val playbackPositionMs: Long = 0L,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportSuccessMessage: String? = null,
    val inputUrl: String = "",
    val customTitleInput: String = "",
    val customTranscriptInput: String = ""
)

class ShortsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ShortsRepository(db.shortProjectDao())
    private val geminiService = GeminiClipService()

    val savedShorts: StateFlow<List<ConvertedShortProject>> = repository.savedShorts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(
        ShortsUiState(
            selectedVideo = repository.sampleVideos.first(),
            sampleVideos = repository.sampleVideos
        )
    )
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Automatically pre-extract initial clips for the default showcase video
        viewModelScope.launch {
            extractClipsForVideo(repository.sampleVideos.first())
        }
    }

    fun switchTab(tab: StudioTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun updateInputUrl(url: String) {
        _uiState.update { it.copy(inputUrl = url) }
    }

    fun updateCustomTitle(title: String) {
        _uiState.update { it.copy(customTitleInput = title) }
    }

    fun updateCustomTranscript(transcript: String) {
        _uiState.update { it.copy(customTranscriptInput = transcript) }
    }

    fun selectVideo(video: VideoItem) {
        _uiState.update {
            it.copy(
                selectedVideo = video,
                inputUrl = video.youtubeUrl
            )
        }
        extractClipsForVideo(video)
    }

    fun importCustomVideo() {
        val state = _uiState.value
        val url = state.inputUrl.ifBlank { "https://www.youtube.com/watch?v=custom_shorts" }
        val video = repository.createCustomVideo(
            url = url,
            title = state.customTitleInput.takeIf { it.isNotBlank() },
            transcript = state.customTranscriptInput.takeIf { it.isNotBlank() }
        )
        _uiState.update { it.copy(selectedVideo = video) }
        extractClipsForVideo(video)
    }

    fun extractClipsForVideo(video: VideoItem) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    analysisProgressText = "Analyzing audio cadence & video transcript..."
                )
            }
            delay(400)
            _uiState.update {
                it.copy(analysisProgressText = "Gemini AI detecting high-retention viral hooks & emotional spikes...")
            }

            val clips = geminiService.extractViralClips(video)

            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    extractedClips = clips,
                    selectedClip = clips.firstOrNull(),
                    currentTab = StudioTab.CLIPS,
                    playbackPositionMs = 0L,
                    isPlaying = false
                )
            }
        }
    }

    fun selectClip(clip: ViralClip) {
        stopPlayback()
        _uiState.update {
            it.copy(
                selectedClip = clip,
                currentTab = StudioTab.STUDIO,
                playbackPositionMs = 0L,
                isPlaying = true
            )
        }
        startPlayback()
    }

    fun setFramingMode(mode: FramingMode) {
        _uiState.update { it.copy(framingMode = mode) }
    }

    fun setAspectRatio(ratio: TargetAspectRatio) {
        _uiState.update { it.copy(targetAspectRatio = ratio) }
    }

    fun setCaptionStyle(style: CaptionStyle) {
        _uiState.update { it.copy(captionStyle = style) }
    }

    fun setCaptionPosition(position: CaptionPosition) {
        _uiState.update { it.copy(captionPosition = position) }
    }

    fun setCaptionFontSize(sizeSp: Float) {
        _uiState.update { it.copy(captionFontSizeSp = sizeSp) }
    }

    fun toggleEmojis(enabled: Boolean) {
        _uiState.update { it.copy(showEmojisInCaptions = enabled) }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    fun seekTo(positionMs: Long) {
        val clip = _uiState.value.selectedClip ?: return
        val maxMs = clip.durationSec * 1000L
        val clamped = positionMs.coerceIn(0L, maxMs)
        _uiState.update { it.copy(playbackPositionMs = clamped) }
    }

    private fun startPlayback() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            while (isActive) {
                delay(50)
                val clip = _uiState.value.selectedClip ?: break
                val maxMs = (clip.durationSec * 1000L).coerceAtLeast(1000L)
                val current = _uiState.value.playbackPositionMs
                if (current >= maxMs) {
                    _uiState.update { it.copy(playbackPositionMs = 0L) } // loop short
                } else {
                    _uiState.update { it.copy(playbackPositionMs = current + 50L) }
                }
            }
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun updateCaptionText(lineId: String, newText: String) {
        val clip = _uiState.value.selectedClip ?: return
        val updatedCaptions = clip.captions.map { line ->
            if (line.id == lineId) {
                val words = newText.split(" ").filter { it.isNotBlank() }
                val wordDur = if (words.isNotEmpty()) (line.endMs - line.startMs) / words.size else 0L
                val newWords = words.mapIndexed { i, w ->
                    CaptionWord(
                        word = w,
                        startMs = line.startMs + (i * wordDur),
                        endMs = line.startMs + ((i + 1) * wordDur),
                        isHighlighted = i % 3 == 0
                    )
                }
                line.copy(text = newText, words = newWords)
            } else line
        }
        val updatedClip = clip.copy(captions = updatedCaptions)
        _uiState.update { it.copy(selectedClip = updatedClip) }
    }

    fun exportCurrentShort() {
        val clip = _uiState.value.selectedClip ?: return
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgress = 0f) }
            for (p in 1..10) {
                delay(120)
                _uiState.update { it.copy(exportProgress = p / 10f) }
            }

            val project = ConvertedShortProject(
                title = clip.title,
                sourceVideoTitle = currentState.selectedVideo.title,
                durationSec = clip.durationSec,
                viralityScore = clip.viralityScore,
                framingMode = currentState.framingMode,
                captionStyle = currentState.captionStyle,
                captionPosition = currentState.captionPosition,
                soundbite = clip.soundbite,
                hashtags = clip.suggestedHashtags.joinToString(" ")
            )
            repository.saveShort(project)

            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportSuccessMessage = "Short successfully rendered & saved to Library! (9:16 MP4)"
                )
            }
        }
    }

    fun dismissExportMessage() {
        _uiState.update { it.copy(exportSuccessMessage = null) }
    }

    fun deleteSavedShort(id: Long) {
        viewModelScope.launch {
            repository.deleteShort(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
