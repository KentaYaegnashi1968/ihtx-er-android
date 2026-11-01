package com.example.videoeffects

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private val gson = Gson()
    private val editorConfig = EditorConfig()
    private val allEffects = mutableListOf<EffectConfig>()
    private lateinit var effectAdapter: EffectAdapter

    private var selectedVideoUri: Uri? = null
    private var selectedAudioUri: Uri? = null

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
            findViewById<TextView>(R.id.selectedVideoText).text = "Video source: ${uri.toString()}"
        }
    }

    private val pickAudioLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedAudioUri = uri
            findViewById<TextView>(R.id.selectedAudioText).text = "Audio source: ${uri.toString()}"
        }
    }

    private val createJsonLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@registerForActivityResult
        val text = gson.toJson(editorConfig)
        contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(text.toByteArray())
        }
        findViewById<TextView>(R.id.commandPreview).text = "Saved effect config JSON to ${uri.path}"
    }

    private val openJsonLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text.isNullOrBlank()) {
                findViewById<TextView>(R.id.commandPreview).text = "Import failed: empty JSON file"
                return@registerForActivityResult
            }

            val parsed = gson.fromJson(text, EditorConfig::class.java)
            editorConfig.videoEffects.clear()
            editorConfig.audioEffects.clear()
            if (parsed.videoEffects != null) editorConfig.videoEffects.addAll(parsed.videoEffects)
            if (parsed.audioEffects != null) editorConfig.audioEffects.addAll(parsed.audioEffects)
            syncEffects()
            findViewById<TextView>(R.id.commandPreview).text = "Imported JSON config from ${uri.toString()}"
        } catch (e: Exception) {
            findViewById<TextView>(R.id.commandPreview).text = "Import failed: ${e.message}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.effectList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        effectAdapter = EffectAdapter(allEffects) { effect -> removeEffect(effect) }
        recyclerView.adapter = effectAdapter

        findViewById<Button>(R.id.addVideoEffectButton).setOnClickListener {
            showEffectPicker(isVideo = true)
        }

        findViewById<Button>(R.id.addAudioEffectButton).setOnClickListener {
            showEffectPicker(isVideo = false)
        }

        findViewById<Button>(R.id.saveJsonButton).setOnClickListener {
            createJsonLauncher.launch("effects_${System.currentTimeMillis()}.json")
        }

        findViewById<Button>(R.id.importJsonButton).setOnClickListener {
            openJsonLauncher.launch(arrayOf("application/json"))
        }

        findViewById<Button>(R.id.replaceAudioButton).setOnClickListener {
            pickAudioLauncher.launch(arrayOf("audio/*"))
        }

        findViewById<Button>(R.id.exportButton).setOnClickListener {
            val outputName = "output.${editorConfig.outputFormat}"
            val ffmpegCommand = EffectEngine.createFfmpegCommand(
                config = editorConfig,
                videoInput = selectedVideoUri?.toString() ?: null,
                audioInput = selectedAudioUri?.toString() ?: null,
                outputPath = outputName
            )
            findViewById<TextView>(R.id.commandPreview).text = ffmpegCommand
        }

        findViewById<Button>(R.id.addVideoEffectButton).performClick()
    }

    private fun showEffectPicker(isVideo: Boolean) {
        val names = if (isVideo) EffectEngine.videoEffectNames else EffectEngine.audioEffectNames
        AlertDialog.Builder(this)
            .setTitle(if (isVideo) "Add Video effect" else "Add Audio effect")
            .setItems(names.toTypedArray()) { _, index ->
                val name = names[index]
                val effect = EffectConfig(
                    name = name,
                    type = name,
                    isVideo = isVideo,
                    repetitions = 1,
                    startMs = 0,
                    durationMs = 1000,
                    parameters = mutableMapOf(
                        "strength" to "1.0",
                        "pitch" to "2.0"
                    )
                )

                if (isVideo) {
                    editorConfig.videoEffects.add(effect)
                } else {
                    editorConfig.audioEffects.add(effect)
                }

                syncEffects()
            }
            .show()
    }

    private fun removeEffect(effect: EffectConfig) {
        if (effect.isVideo) {
            editorConfig.videoEffects.removeIf { it.id == effect.id }
        } else {
            editorConfig.audioEffects.removeIf { it.id == effect.id }
        }
        syncEffects()
    }

    private fun syncEffects() {
        allEffects.clear()
        allEffects.addAll(editorConfig.videoEffects)
        allEffects.addAll(editorConfig.audioEffects)
        effectAdapter.notifyDataSetChanged()
    }
}
