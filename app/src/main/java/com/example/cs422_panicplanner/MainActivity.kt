package com.example.cs422_panicplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs422_panicplanner.database.DatabaseProvider
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var headerMonth: TextView
    private lateinit var rvCalendar: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var viewModel: EventViewModel
    private var currentMonth: YearMonth = YearMonth.now()
    
    // Track events grouped by day for the current month
    private var eventsByDay: Map<Int, List<Event>> = emptyMap()
    private var allEvents: List<Event> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply dark mode setting before super.onCreate
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_main)

        val database = DatabaseProvider.getDatabase(this)
        val factory = EventViewModelFactory(database.eventDao())
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        headerMonth = findViewById(R.id.header_month)
        rvCalendar = findViewById(R.id.rv_calendar)
        val btnAddEvent = findViewById<Button>(R.id.btn_add_event)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings)
        val btnPrevMonth = findViewById<ImageButton>(R.id.btn_prev_month)
        val btnNextMonth = findViewById<ImageButton>(R.id.btn_next_month)

        rvCalendar.layoutManager = LinearLayoutManager(this)
        adapter = CalendarAdapter(
            currentMonth.lengthOfMonth(),
            eventsByDay,
            onEventClick = { event ->
                val intent = Intent(this, EventActivity::class.java)
                intent.putExtra("EVENT_ID", event.id)
                startActivity(intent)
            }
        )
        rvCalendar.adapter = adapter

        observeEvents()
        updateHeader()

        headerMonth.setOnClickListener {
            showMonthYearPickerDialog()
        }

        btnPrevMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateCalendar()
        }

        btnAddEvent.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            startSettingsActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadEvents()
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            viewModel.events.collect { events ->
                allEvents = events
                updateCalendar()
            }
        }
    }

    private fun updateHeader() {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        headerMonth.text = currentMonth.format(formatter)
    }

    private fun updateCalendar() {
        updateHeader()
        
        eventsByDay = allEvents
            .filter { it.startTime.year == currentMonth.year && it.startTime.monthValue == currentMonth.monthValue }
            .groupBy { it.startTime.dayOfMonth }
        
        adapter.updateData(currentMonth.lengthOfMonth(), eventsByDay)
    }

    private fun showMonthYearPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.month_picker)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.year_picker)

        val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.displayedValues = months
        monthPicker.value = currentMonth.monthValue

        yearPicker.minValue = 2000
        yearPicker.maxValue = 2100
        yearPicker.value = currentMonth.year

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Month and Year")
        builder.setView(dialogView)
        builder.setPositiveButton("OK") { _, _ ->
            currentMonth = YearMonth.of(yearPicker.value, monthPicker.value)
            updateCalendar()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}