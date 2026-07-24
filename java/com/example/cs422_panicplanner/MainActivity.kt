package com.example.cs422_panicplanner

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var headerMonth: TextView
    private lateinit var rvCalendar: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private var currentMonth: YearMonth = YearMonth.now()
    
    // Track which days have events for the current month
    private val daysWithEvents = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_main)

        headerMonth = findViewById(R.id.header_month)
        rvCalendar = findViewById(R.id.rv_calendar)
        val btnAddEvent = findViewById<Button>(R.id.btn_add_event)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings)
        val btnPrevMonth = findViewById<ImageButton>(R.id.btn_prev_month)
        val btnNextMonth = findViewById<ImageButton>(R.id.btn_next_month)

        rvCalendar.layoutManager = LinearLayoutManager(this)
        adapter = CalendarAdapter(currentMonth.lengthOfMonth(), daysWithEvents) { day ->
            if (daysWithEvents.contains(day)) {
                val intent = Intent(this, EventActivity::class.java)
                intent.putExtra("SELECTED_DAY", day)
                intent.putExtra("SELECTED_MONTH", currentMonth.monthValue)
                intent.putExtra("SELECTED_YEAR", currentMonth.year)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No event on this day", Toast.LENGTH_SHORT).show()
            }
        }
        rvCalendar.adapter = adapter

        updateHeader()

        headerMonth.setOnClickListener {
            showMonthYearPickerDialog()
        }

        btnPrevMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            daysWithEvents.clear()
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            daysWithEvents.clear()
            updateCalendar()
        }

        btnAddEvent.setOnClickListener {
            // Logic: Add a blank event to the first day of the month for demonstration
            daysWithEvents.add(1)
            updateCalendar()
            Toast.makeText(this, "Blank event added to day 1", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            startSettingsActivity()
        }
    }

    private fun updateHeader() {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        headerMonth.text = currentMonth.format(formatter)
    }

    private fun updateCalendar() {
        updateHeader()
        adapter.updateData(currentMonth.lengthOfMonth(), daysWithEvents)
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
            // For demo, clear events when changing months
            daysWithEvents.clear()
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