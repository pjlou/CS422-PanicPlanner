package com.example.cs422_panicplanner

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val switchDarkMode = findViewById<Switch>(R.id.switch_dark_mode)

        switchDarkMode.isChecked = sharedPreferences.getBoolean("dark_mode", false)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Reminder Time Setting created with AI assistance
        val spinnerReminderTime = findViewById<Spinner>(R.id.spinner_reminder_time)
        val minuteValues = listOf(5L, 10L, 15L, 30L, 60L)
        val currentReminderMinutes = sharedPreferences.getLong("notification_time", 10L)
        val initialPosition = minuteValues.indexOf(currentReminderMinutes).coerceAtLeast(0)
        spinnerReminderTime.setSelection(initialPosition)

        spinnerReminderTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMinutes = minuteValues[position]
                sharedPreferences.edit().putLong("notification_time", selectedMinutes).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Time Zone Setting created with AI assistance
        val switchAutoTimeZone = findViewById<Switch>(R.id.switch_auto_timezone)
        val layoutCustomTimeZone = findViewById<View>(R.id.layout_custom_timezone)
        val spinnerTimeZone = findViewById<Spinner>(R.id.spinner_timezone)

        val isAutoTimeZone = sharedPreferences.getBoolean("auto_timezone", true)
        switchAutoTimeZone.isChecked = isAutoTimeZone
        layoutCustomTimeZone.visibility = if (isAutoTimeZone) View.GONE else View.VISIBLE

        switchAutoTimeZone.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("auto_timezone", isChecked).apply()
            layoutCustomTimeZone.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        val timeZones = mutableListOf<String>()
        for (i in -12..14) {
            val sign = if (i >= 0) "+" else "-"
            val hour = Math.abs(i)
            timeZones.add(String.format(Locale.US, "GMT%s%02d:00", sign, hour))
        }

        val tzAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeZones)
        tzAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeZone.adapter = tzAdapter

        val currentTimeZone = sharedPreferences.getString("time_zone", "GMT+00:00")
        val tzPosition = timeZones.indexOf(currentTimeZone).coerceAtLeast(timeZones.indexOf("GMT+00:00"))
        spinnerTimeZone.setSelection(tzPosition)

        spinnerTimeZone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTz = timeZones[position]
                sharedPreferences.edit().putString("time_zone", selectedTz).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}