package com.example.videoeffects

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val timeZoneIds = listOf(
        "UTC",
        "America/New_York",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Paris",
        "Asia/Tokyo",
        "Asia/Dubai",
        "Australia/Sydney"
    )

    private val mainHandler = Handler(Looper.getMainLooper())

    private val updateClockRunnable = object : Runnable {
        override fun run() {
            val systemZone = ZoneId.systemDefault()
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
            val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)

            findViewById<TextView>(R.id.currentTimeText).text =
                Instant.now().atZone(systemZone).format(timeFormatter)
            findViewById<TextView>(R.id.currentZoneText).text =
                "${systemZone.id} • ${Instant.now().atZone(systemZone).format(dateFormatter)}"

            val timezoneContainer = findViewById<LinearLayout>(R.id.timezoneContainer)
            for (i in 0 until timezoneContainer.childCount) {
                val row = timezoneContainer.getChildAt(i) as LinearLayout
                val zoneLabel = row.getChildAt(0) as TextView
                val zoneTime = row.getChildAt(1) as TextView
                val zoneDate = row.getChildAt(2) as TextView

                val zoneId = timeZoneIds[i]
                val zone = ZoneId.of(zoneId)
                val time = Instant.now().atZone(zone)
                zoneLabel.text = zoneId
                zoneTime.text = time.format(timeFormatter)
                zoneDate.text = time.format(dateFormatter)
            }

            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buildTimeZoneCards()
        mainHandler.post(updateClockRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacks(updateClockRunnable)
    }

    private fun buildTimeZoneCards() {
        val container = findViewById<LinearLayout>(R.id.timezoneContainer)
        container.removeAllViews()

        for (zoneId in timeZoneIds) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.parseColor("#111827"))
                setPadding(20, 18, 20, 18)
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 12
                }
            }

            val zoneName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                textSize = 16f
                setTextColor(Color.parseColor("#E2E8F0"))
                gravity = Gravity.START
            }

            val zoneTime = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                textSize = 18f
                setTextColor(Color.parseColor("#7DD3FC"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val zoneDate = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 18
                }
                textSize = 13f
                setTextColor(Color.parseColor("#CBD5E1"))
            }

            row.addView(zoneName)
            row.addView(zoneTime)
            row.addView(zoneDate)
            container.addView(row)
        }
    }
}
