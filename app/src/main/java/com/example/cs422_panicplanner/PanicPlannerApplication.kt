package com.example.cs422_panicplanner

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class PanicPlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}