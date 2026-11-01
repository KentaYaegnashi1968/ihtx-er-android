package com.example.videoeffects

import com.google.gson.annotations.SerializedName

data class EditorConfig(
    @SerializedName("videoEffects")
    var videoEffects: MutableList<EffectConfig> = mutableListOf(),
    @SerializedName("audioEffects")
    var audioEffects: MutableList<EffectConfig> = mutableListOf(),
    @SerializedName("inputFormat")
    var inputFormat: String = "mp4",
    @SerializedName("outputFormat")
    var outputFormat: String = "mp4",
    @SerializedName("videoSourceGrid")
    var videoSourceGrid: String = "1x1",
    @SerializedName("exportVideo")
    var exportVideo: Boolean = true,
    @SerializedName("clipCount")
    var clipCount: Int = 1
)

data class EffectConfig(
    @SerializedName("id")
    var id: String = java.util.UUID.randomUUID().toString(),
    @SerializedName("name")
    var name: String = "",
    @SerializedName("type")
    var type: String = "",
    @SerializedName("isVideo")
    var isVideo: Boolean = true,
    @SerializedName("repetitions")
    var repetitions: Int = 1,
    @SerializedName("startMs")
    var startMs: Int = 0,
    @SerializedName("durationMs")
    var durationMs: Int = 1000,
    @SerializedName("parameters")
    var parameters: MutableMap<String, String> = mutableMapOf()
)
