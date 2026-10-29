package com.ghostvoice.app

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.content.Context
import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread
import kotlin.math.*

class MainActivity : ComponentActivity() {
    private var running = false
    private var recorder: AudioRecord? = null
    private var player: AudioTrack? = null
    private var intensity = 0.72f
    private lateinit var status: TextView
    private lateinit var startButton: Button
    private val sampleRate = 48000
    private val requestCode = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(10, 10, 13)
        window.navigationBarColor = Color.rgb(10, 10, 13)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(Color.rgb(10, 10, 13))
        }
        val title = TextView(this).apply {
            text = "GHOSTVOICE"
            textSize = 29f
            setTextColor(Color.WHITE)
            letterSpacing = 0.18f
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "REAL-TIME VOICE FX"
            textSize = 11f
            letterSpacing = 0.24f
            setTextColor(Color.rgb(190, 55, 65))
            gravity = Gravity.CENTER
        }
        val orb = TextView(this).apply {
            text = "◉"
            textSize = 94f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(220, 45, 58))
            setPadding(0, 44, 0, 24)
        }
        status = TextView(this).apply {
            text = "MICROPHONE INACTIF"
            textSize = 12f
            letterSpacing = 0.12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        startButton = Button(this).apply {
            text = "ACTIVER LE MICRO"
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(165, 28, 42)))
            setOnClickListener { toggle() }
        }
        val label = TextView(this).apply {
            text = "INTENSITÉ DE L’EFFET"
            textSize = 12f
            letterSpacing = 0.08f
            setTextColor(Color.LTGRAY)
            setPadding(0, 42, 0, 8)
        }
        val slider = SeekBar(this).apply {
            max = 100
            progress = 72
            progressTintList = android.content.res.ColorStateList.valueOf(Color.rgb(210, 42, 55))
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) { intensity = p / 100f }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        val note = TextView(this).apply {
            text = "Voix cinéma • grave • sombre\nUtilise de préférence un casque pour éviter le larsen."
            textSize = 12f
            setTextColor(Color.rgb(145, 145, 153))
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 0)
        }
        root.addView(title, match())
        root.addView(subtitle, match())
        root.addView(orb, LinearLayout.LayoutParams(-1, 190))
        root.addView(status, match())
        root.addView(startButton, LinearLayout.LayoutParams(-1, 58).apply { topMargin = 24 })
        root.addView(label, match())
        root.addView(slider, match())
        root.addView(note, match())
        setContentView(root)
    }

    private fun match() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun toggle() {
        if (running) stopAudio() else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), requestCode)
            } else startAudio()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == this.requestCode && results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) startAudio()
        else status.text = "AUTORISATION MICRO REFUSÉE"
    }

    private fun startAudio() {
        if (running) return
        val minIn = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val minOut = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minIn <= 0 || minOut <= 0) {
            status.text = "AUDIO NON DISPONIBLE"
            return
        }
        try {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, max(minIn * 2, 4096)
            )
            player = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(sampleRate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(max(minOut * 2, 4096))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            recorder?.startRecording()
            player?.play()
            running = true
            status.text = "ÉCOUTE EN DIRECT"
            startButton.text = "DÉSACTIVER LE MICRO"
            thread(name = "GhostVoiceAudio") {
                val input = ShortArray(1024)
                var lowState = 0.0
                while (running) {
                    val count = recorder?.read(input, 0, input.size) ?: -1
                    if (count > 0) {
                        val output = ShortArray(count)
                        val amount = intensity.toDouble()
                        for (i in 0 until count) {
                            val x = input[i].toDouble() / 32768.0
                            // Dark tonal shaping: low-pass body + gentle saturation.
                            lowState += 0.19 * (x - lowState)
                            val body = lowState * (1.0 + 1.15 * amount)
                            val grit = tanh(body * (1.0 + amount * 1.7))
                            val mixed = (body * (1.0 - amount * 0.35) + grit * amount * 0.35)
                            output[i] = (mixed.coerceIn(-1.0, 1.0) * 32767.0).roundToInt().toShort()
                        }
                        player?.write(output, 0, count)
                    }
                }
            }
        } catch (e: Exception) {
            stopAudio()
            status.text = "ERREUR AUDIO : ${e.message ?: "inconnue"}"
        }
    }

    private fun stopAudio() {
        running = false
        try { recorder?.stop() } catch (_: Exception) {}
        try { player?.pause(); player?.flush(); player?.stop() } catch (_: Exception) {}
        recorder?.release()
        player?.release()
        recorder = null
        player = null
        status.text = "MICROPHONE INACTIF"
        startButton.text = "ACTIVER LE MICRO"
    }

    override fun onDestroy() {
        stopAudio()
        super.onDestroy()
    }
}
