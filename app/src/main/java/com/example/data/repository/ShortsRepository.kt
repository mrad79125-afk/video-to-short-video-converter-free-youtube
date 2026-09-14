package com.example.data.repository

import com.example.data.local.ShortProjectDao
import com.example.data.local.ShortProjectEntity
import com.example.data.model.CaptionLine
import com.example.data.model.CaptionPosition
import com.example.data.model.CaptionStyle
import com.example.data.model.CaptionWord
import com.example.data.model.ConvertedShortProject
import com.example.data.model.FramingMode
import com.example.data.model.VideoItem
import com.example.data.model.ViralClip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShortsRepository(private val shortProjectDao: ShortProjectDao) {

    val savedShorts: Flow<List<ConvertedShortProject>> = shortProjectDao.getAllShorts().map { list ->
        list.map { entity ->
            ConvertedShortProject(
                id = entity.id,
                title = entity.title,
                sourceVideoTitle = entity.sourceVideoTitle,
                durationSec = entity.durationSec,
                viralityScore = entity.viralityScore,
                framingMode = try { FramingMode.valueOf(entity.framingMode) } catch (e: Exception) { FramingMode.SPLIT_SCREEN },
                captionStyle = try { CaptionStyle.valueOf(entity.captionStyle) } catch (e: Exception) { CaptionStyle.HORMOZI_YELLOW },
                captionPosition = try { CaptionPosition.valueOf(entity.captionPosition) } catch (e: Exception) { CaptionPosition.BOTTOM },
                soundbite = entity.soundbite,
                hashtags = entity.hashtags,
                createdAt = entity.createdAt
            )
        }
    }

    suspend fun saveShort(project: ConvertedShortProject): Long {
        val entity = ShortProjectEntity(
            id = project.id,
            title = project.title,
            sourceVideoTitle = project.sourceVideoTitle,
            durationSec = project.durationSec,
            viralityScore = project.viralityScore,
            framingMode = project.framingMode.name,
            captionStyle = project.captionStyle.name,
            captionPosition = project.captionPosition.name,
            soundbite = project.soundbite,
            hashtags = project.hashtags,
            createdAt = project.createdAt
        )
        return shortProjectDao.insertShort(entity)
    }

    suspend fun deleteShort(id: Long) {
        shortProjectDao.deleteShort(id)
    }

    // Curated high-engagement YouTube long-form videos ready for Shorts conversion
    val sampleVideos: List<VideoItem> = listOf(
        VideoItem(
            id = "yt_mrbeast_01",
            title = "How I Engineered Viral YouTube Videos to Reach 1 Billion Views",
            channelTitle = "MrBeast Clips & Talks",
            durationSeconds = 620,
            youtubeUrl = "https://www.youtube.com/watch?v=0e3GPea1Tyg",
            category = "Creator Strategy",
            description = "Jimmy Donaldson reveals the secret formula behind video retention, first 5-second hooks, and why pacing determines everything on modern algorithms.",
            transcript = """
[00:15] The biggest mistake creators make is waiting 30 seconds to explain what the video is about.
[00:28] If you don't grab someone in the first three seconds, they swipe away and the algorithm buries you forever.
[01:12] We spend 40 hours just testing the opening shot. The visual must match the title immediately with zero fluff.
[01:34] You have to create what I call an impossible curiosity loop. The viewer must feel physical pain if they don't see the ending.
[02:15] Most people think virality is luck or magic. It's actually pure mathematics of retention rate and click-through percentage.
[02:50] When we trimmed 15 seconds of dead air from our challenge video, the average view duration jumped from 60% to 84%.
[03:40] Every single sentence in your script must do one of two things: build tension or deliver an unexpected payoff.
[04:20] If you want a billion views, stop making videos for your ego and start obsessing over the viewer's second-by-second dopamine.
            """.trimIndent()
        ),
        VideoItem(
            id = "yt_huberman_02",
            title = "The 3 Non-Negotiable Morning Habits for Peak Focus & Dopamine",
            channelTitle = "Huberman Lab Podcast",
            durationSeconds = 890,
            youtubeUrl = "https://www.youtube.com/watch?v=gXDMoiEkyu8",
            category = "Health & Science",
            description = "Neuroscientist Dr. Andrew Huberman explains how early morning sunlight, cold exposure, and delayed caffeine reset circadian rhythm and elevate baseline dopamine.",
            transcript = """
[00:10] If you drink caffeine within 90 minutes of waking up, you are almost guaranteed to crash hard at 2:00 PM.
[00:35] Here is the biological reason: adenosine builds up while you sleep, and coffee only masks the receptor without clearing it.
[01:10] The single highest leverage habit you can do every morning is viewing sunlight outside within 30 minutes of waking.
[01:45] Photon stimulation on retinal ganglion cells triggers a timed cortisol pulse that locks in alertness for 14 straight hours.
[02:30] Delaying your first cup of coffee until 10:00 AM changes your cognitive energy trajectory completely.
[03:15] Add 2 minutes of deliberate cold water exposure, and your baseline dopamine elevates by 250% for hours without a crash.
[04:05] People spend thousands of dollars on nootropic supplements when daylight and cold water are completely free.
            """.trimIndent()
        ),
        VideoItem(
            id = "yt_stevejobs_03",
            title = "Steve Jobs: How Connecting The Dots Changes Your Life",
            channelTitle = "Stanford Commencement",
            durationSeconds = 905,
            youtubeUrl = "https://www.youtube.com/watch?v=UF8uR6Z6KLc",
            category = "Motivation & Life",
            description = "Steve Jobs delivers his iconic address on love, loss, calligraphy, dropping out of college, and trusting that the dots will somehow connect in your future.",
            transcript = """
[00:20] You cannot connect the dots looking forward; you can only connect them looking backwards.
[00:45] So you have to trust that the dots will somehow connect in your future. You have to trust in something: your gut, destiny, life, karma, whatever.
[01:25] Because believing that the dots will connect down the road will give you the confidence to follow your heart even when it leads you off the well-worn path.
[02:10] Getting fired from Apple was the best thing that could have ever happened to me. The heaviness of being successful was replaced by the lightness of being a beginner again.
[02:55] Your time is limited, so don't waste it living someone else's life. Don't be trapped by dogma, which is living with the results of other people's thinking.
[03:45] Don't let the noise of others' opinions drown out your own inner voice. And most importantly, have the courage to follow your heart and intuition.
            """.trimIndent()
        ),
        VideoItem(
            id = "yt_lex_altman_04",
            title = "Sam Altman on The Next Leap in Artificial Superintelligence",
            channelTitle = "Lex Fridman Podcast",
            durationSeconds = 1140,
            youtubeUrl = "https://www.youtube.com/watch?v=jvqFAi7vkBc",
            category = "AI & Tech",
            description = "OpenAI CEO Sam Altman discusses autonomous AI agents, energy limits, and what human society will look like in 5 years.",
            transcript = """
[00:15] In the next 36 months, the cost of intelligence will drop asymptotically towards zero.
[00:42] When intelligence becomes as abundant as electricity, the definition of work and value changes overnight.
[01:18] An individual creator with AI agents will soon be able to build a billion-dollar company with zero full-time employees.
[01:58] The bottleneck won't be software or code anymore; the real bottleneck will be electricity, compute clusters, and genuine human taste.
[02:45] The people who win in the next decade are not the ones who fear AI, but the ones who learn to orchestrate swarms of intelligence.
[03:30] We are moving from tools that assist thought to partners that generate novel hypotheses across physics and biology.
            """.trimIndent()
        )
    )

    fun createCustomVideo(url: String, title: String? = null, transcript: String? = null): VideoItem {
        val videoId = extractYouTubeId(url).ifEmpty { "custom_${System.currentTimeMillis()}" }
        val finalTitle = if (!title.isNullOrBlank()) title else "YouTube Video [${videoId.take(11)}]"
        val finalTranscript = if (!transcript.isNullOrBlank()) {
            transcript
        } else {
            """
[00:05] Welcome back! Today we are breaking down the biggest secret to exponential growth.
[00:22] Most people focus on the wrong 80%, but when you flip the switch, everything changes in days.
[00:45] The critical moment happened when we stopped guessing and started testing with relentless speed.
[01:10] Within three weeks, the entire audience exploded and engagement skyrocketed by 400%.
[01:38] If there is one takeaway you remember from today, let it be this exact rule.
            """.trimIndent()
        }

        return VideoItem(
            id = videoId,
            title = finalTitle,
            channelTitle = "Imported Creator",
            durationSeconds = 480,
            youtubeUrl = url,
            category = "Custom Video",
            description = "Imported YouTube video for AI viral shorts extraction.",
            transcript = finalTranscript
        )
    }

    private fun extractYouTubeId(url: String): String {
        return try {
            val patterns = listOf(
                "(?:v=|vi=|youtu\\.be/|embed/|shorts/)([a-zA-Z0-9_-]{11})".toRegex()
            )
            for (p in patterns) {
                val match = p.find(url)
                if (match != null && match.groupValues.size > 1) {
                    return match.groupValues[1]
                }
            }
            ""
        } catch (e: Exception) {
            ""
        }
    }
}
