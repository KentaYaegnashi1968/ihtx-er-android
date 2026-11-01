package com.example.videoeffects

object EffectEngine {
    val videoEffectNames = listOf(
        "Hue Shift",
        "Hue CC Shift",
        "Invert",
        "HFlip",
        "VFlip",
        "Pinch & Punch",
        "Wave Warp",
        "Swirl",
        "RGB to BGR",
        "Glow",
        "Displacement Map"
    )

    val audioEffectNames = listOf(
        "Pitch Shift",
        "Multi-Pitch",
        "Delay",
        "Echo",
        "Chorus",
        "Bitcrusher",
        "Vibrato",
        "Vocoder",
        "Upload Vocoder"
    )

    fun buildVideoFilterGraph(effects: List<EffectConfig>): String {
        if (effects.isEmpty()) return ""

        return effects.joinToString(";") { effect ->
            when (effect.name.lowercase()) {
                "hue shift" -> "hue=h=${effect.parameters["angle"] ?: "45"}"
                "hue cc shift" -> "hue=s=${effect.parameters["saturation"] ?: "1.0"}:h=${effect.parameters["angle"] ?: "30"}"
                "invert" -> "negate=1"
                "hflip" -> "hflip"
                "vflip" -> "vflip"
                "pinch & punch" -> "pinch=p=${effect.parameters["strength"] ?: "0.5"}"
                "wave warp" -> "wave=amp=${effect.parameters["amp"] ?: "10"}:freq=${effect.parameters["freq"] ?: "10"}"
                "swirl" -> "swirl=${effect.parameters["radius"] ?: "8"}"
                "rgb to bgr" -> "swaprb"
                "glow" -> "glow=${effect.parameters["strength"] ?: "0.5"}"
                "displacement map" -> "displace=x=${effect.parameters["x"] ?: "10"}:y=${effect.parameters["y"] ?: "10"}"
                else -> "null"
            }
        }.replace("null", "")
            .split(";")
            .filter { it.isNotBlank() }
            .joinToString(";")
    }

    fun buildAudioFilterGraph(effects: List<EffectConfig>): String {
        if (effects.isEmpty()) return ""

        return effects.joinToString(",") { effect ->
            when (effect.name.lowercase()) {
                "pitch shift" -> "rubberband=pitch=${effect.parameters["pitch"] ?: "2"}"
                "multi-pitch" -> "rubberband=pitch=${effect.parameters["pitches"] ?: "1.0,1.5,2.0"}"
                "delay" -> "adelay=${effect.parameters["delayMs"] ?: "500"}|${effect.parameters["delayMs"] ?: "500"}"
                "echo" -> "aecho=0.8:0.9:1000:0.3"
                "chorus" -> "chorus=0.5:0.9:55:0.4:0.25:2"
                "bitcrusher" -> "bitplanenoise=1"
                "vibrato" -> "vibrato=f=${effect.parameters["freq"] ?: "5"}"
                "vocoder" -> "afftfilt=real='hypot(re,im)':imag='hypot(re,im)'"
                "upload vocoder" -> "afftdn=nr=100:nf=-30"
                else -> "anull"
            }
        }.replace("anull", "")
            .split(",")
            .filter { it.isNotBlank() }
            .joinToString(",")
    }

    fun createFfmpegCommand(
        config: EditorConfig,
        videoInput: String? = null,
        audioInput: String? = null,
        outputPath: String = "output.${config.outputFormat}"
    ): String {
        val videoSource = videoInput?.ifBlank { "input_video.${config.inputFormat}" } ?: "input_video.${config.inputFormat}"
        val audioSource = audioInput?.ifBlank { null }

        val videoFilters = buildVideoFilterGraph(config.videoEffects)
        val audioFilters = buildAudioFilterGraph(config.audioEffects)

        val command = buildString {
            append("ffmpeg -y ")
            append("-i \"$videoSource\" ")
            if (audioSource != null) {
                append("-i \"$audioSource\" ")
            }

            if (videoFilters.isNotEmpty() || audioFilters.isNotEmpty()) {
                append("-filter_complex \"")
                if (videoFilters.isNotEmpty()) {
                    append(videoFilters)
                }
                if (audioFilters.isNotEmpty()) {
                    if (videoFilters.isNotEmpty()) {
                        append(";")
                    }
                    append(audioFilters)
                }
                append("\" ")
            }

            append("-c:v libx264 -c:a aac -movflags +faststart ")
            append("\"$outputPath\"")
        }

        return command
    }
}
