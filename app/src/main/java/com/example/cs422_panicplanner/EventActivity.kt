package com.example.cs422_panicplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button as XmlButton
import android.widget.ImageButton
import android.widget.TextView as XmlTextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs422_panicplanner.database.DatabaseProvider
import com.example.cs422_panicplanner.ui.theme.CS422PanicPlannerTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EventActivity : AppCompatActivity() {

    private var currentEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DatabaseProvider.getDatabase(this)
        val factory = EventViewModelFactory(database.eventDao())

        val eventId = intent.getIntExtra("EVENT_ID", -1)

        if (eventId != -1) {
            setContentView(R.layout.event_detail)
            setupDetailScreen(eventId, factory)
        } else {
            // Creation mode
            setContent {
                CS422PanicPlannerTheme {
                    val viewModel: EventViewModel = viewModel(factory = factory)
                    EventCreationScreen(
                        onEventCreated = { event ->
                            viewModel.addEvent(event) { newId ->
                                val intent = Intent(this, EventActivity::class.java).apply {
                                    putExtra("EVENT_ID", newId.toInt())
                                }
                                startActivity(intent)
                                finish()
                            }
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun setupDetailScreen(eventId: Int, factory: EventViewModelFactory) {
        val viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            val event = viewModel.getEventById(eventId)
            if (event != null) {
                currentEvent = event
                updateUiWithEvent(event)
            }
        }

        findViewById<XmlButton>(R.id.edit_button).setOnClickListener {
            currentEvent?.let { event ->
                setContent {
                    CS422PanicPlannerTheme {
                        EventCreationScreen(
                            initialEvent = event,
                            onEventCreated = { updatedEvent ->
                                viewModel.updateEvent(updatedEvent)
                                currentEvent = updatedEvent
                                setContentView(R.layout.event_detail)
                                setupDetailScreen(updatedEvent.id, factory)
                            },
                            onCancel = {
                                setContentView(R.layout.event_detail)
                                setupDetailScreen(event.id, factory)
                            }
                        )
                    }
                }
            }
        }

        findViewById<XmlButton>(R.id.delete_button).setOnClickListener {
            currentEvent?.let { event ->
                viewModel.deleteEvent(event)
                finish()
            }
        }
    }

    private fun updateUiWithEvent(event: Event) {
        findViewById<XmlTextView>(R.id.tv_event_title).text = event.title
        findViewById<XmlTextView>(R.id.tv_event_description).text = event.description
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        findViewById<XmlTextView>(R.id.tv_event_datetime).text = event.startTime.format(formatter)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreationScreen(
    initialEvent: Event? = null,
    onEventCreated: (Event) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    var date by remember { mutableStateOf(initialEvent?.startTime?.format(dateFormatter) ?: "") }
    var time by remember { mutableStateOf(initialEvent?.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEvent?.startTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialEvent?.startTime?.hour ?: 12,
        initialMinute = initialEvent?.startTime?.minute ?: 0
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        date = selectedDate.format(dateFormatter)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Text(
                if (initialEvent == null) "Add New Event" else "Edit Event",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { },
                    label = { Text("Date (Month and Day)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { },
                    label = { Text("Time (Hour and Minute)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showTimePicker = true }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val selectedDate = datePickerState.selectedDateMillis?.let {
                                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                            } ?: LocalDate.now()

                            val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            val startDateTime = LocalDateTime.of(selectedDate, selectedTime)

                            onEventCreated(
                                initialEvent?.copy(
                                    title = title,
                                    description = description,
                                    startTime = startDateTime,
                                    endTime = startDateTime.plusHours(1)
                                ) ?: Event(
                                    title = title,
                                    description = description,
                                    startTime = startDateTime,
                                    endTime = startDateTime.plusHours(1)
                                )
                            )
                        }
                    }
                ) {
                    Text(if (initialEvent == null) "Save Event" else "Update Event")
                }
            }
        }
    }
}
