package com.example.cs422_panicplanner

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class EventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_detail)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}