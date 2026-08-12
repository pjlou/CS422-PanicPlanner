package com.example.cs422_panicplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button as XmlButton
import android.widget.ImageButton
import android.widget.TextView as XmlTextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        val database =
            DatabaseProvider.getDatabase(this)

        val factory =
            EventViewModelFactory(
                database.eventDao()
            )

        /*
         * If EVENT_ID exists, we are viewing an existing event.
         * Otherwise, we are creating a new event.
         * Added via AI assistance to connect event creation screen to calendar
         */
        val eventId =
            intent.getIntExtra(
                "EVENT_ID",
                -1
            )

        if (eventId != -1) {

            setContentView(
                R.layout.event_detail
            )

            setupDetailScreen(
                eventId,
                factory
            )

        } else {

            // Creation mode.
            setContent {

                CS422PanicPlannerTheme {

                    val viewModel:
                            EventViewModel =
                        viewModel(
                            factory = factory
                        )

                    EventCreationScreen(

                        onEventCreated = { event ->

                            /*
                             * First save the event into Room.
                             *
                             * Room generates the new event ID,
                             * which is then used for the unique
                             * WorkManager notification.
                             */
                            viewModel.addEvent(
                                event
                            ) { newId ->

                                val savedEvent =
                                    event.copy(
                                        id = newId.toInt()
                                    )

                                scheduleNotification(
                                    savedEvent
                                )

                                /*
                                 * Open the detail page for the
                                 * newly-created event.
                                 */
                                val intent =
                                    Intent(
                                        this,
                                        EventActivity::class.java
                                    ).apply {

                                        putExtra(
                                            "EVENT_ID",
                                            savedEvent.id
                                        )
                                    }

                                startActivity(intent)

                                finish()
                            }
                        },

                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    /*
     * Converts the LocalDateTime used by the team's Room
     * Event object into milliseconds for WorkManager.
     *
     * Also reads the user's preferred reminder time from
     * SharedPreferences.
     */
    private fun scheduleNotification(
        event: Event
    ) {
        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val reminderMinutes = sharedPreferences.getLong("notification_time", 10L)
        
        val isAutoTimeZone = sharedPreferences.getBoolean("auto_timezone", true)
        val zoneId = if (isAutoTimeZone) {
            ZoneId.systemDefault()
        } else {
            val timeZoneId = sharedPreferences.getString("time_zone", "GMT+00:00") ?: "GMT+00:00"
            ZoneId.of(timeZoneId)
        }

        val eventStartTimeMillis =
            event.startTime
                .atZone(
                    zoneId
                )
                .toInstant()
                .toEpochMilli()

        EventNotificationScheduler
            .scheduleEventNotification(
                context = this,
                eventId = event.id,
                eventTitle = event.title,
                eventDescription =
                    event.description,
                eventStartTimeMillis =
                    eventStartTimeMillis,
                reminderMinutesBefore = reminderMinutes
            )
    }

    private fun setupDetailScreen(
        eventId: Int,
        factory: EventViewModelFactory
    ) {

        val viewModel =
            ViewModelProvider(
                this,
                factory
            )[EventViewModel::class.java]

        findViewById<ImageButton>(
            R.id.btn_back
        ).setOnClickListener {

            finish()
        }

        /*
         * Retrieve the event from Room.
         * Needed for updating screen with edits
         * addition added with assistance from AI
         */
        lifecycleScope.launch {

            val event =
                viewModel.getEventById(
                    eventId
                )

            if (event != null) {

                currentEvent = event

                updateUiWithEvent(event)
            }
        }

        /*
         * EDIT EVENT
         */
        findViewById<XmlButton>(
            R.id.edit_button
        ).setOnClickListener {

            currentEvent?.let { event ->

                setContent {

                    CS422PanicPlannerTheme {

                        EventCreationScreen(

                            initialEvent = event,

                            onEventCreated =
                                { updatedEvent ->

                                    /*
                                     * Update Room.
                                     */
                                    viewModel.updateEvent(
                                        updatedEvent
                                    )

                                    /*
                                     * Schedule again using the same
                                     * event ID.
                                     *
                                     * ExistingWorkPolicy.REPLACE
                                     * removes the old reminder.
                                     */
                                    scheduleNotification(
                                        updatedEvent
                                    )

                                    currentEvent =
                                        updatedEvent

                                    /*
                                     * Return to event details.
                                     */
                                    setContentView(
                                        R.layout.event_detail
                                    )

                                    setupDetailScreen(
                                        updatedEvent.id,
                                        factory
                                    )
                                },

                            onCancel = {

                                setContentView(
                                    R.layout.event_detail
                                )

                                setupDetailScreen(
                                    event.id,
                                    factory
                                )
                            }
                        )
                    }
                }
            }
        }

        /*
         * DELETE EVENT
         */
        findViewById<XmlButton>(
            R.id.delete_button
        ).setOnClickListener {

            currentEvent?.let { event ->

                /*
                 * Cancel the WorkManager reminder before
                 * removing the event from Room.
                 */
                EventNotificationScheduler
                    .cancelEventNotification(
                        this,
                        event.id
                    )

                viewModel.deleteEvent(event)

                finish()
            }
        }
    }

    private fun updateUiWithEvent(
        event: Event
    ) {

        findViewById<XmlTextView>(
            R.id.tv_event_title
        ).text = event.title

        findViewById<XmlTextView>(
            R.id.tv_event_description
        ).text = event.description

        val formatter =
            DateTimeFormatter.ofPattern(
                "MMM dd, yyyy HH:mm"
            )

        findViewById<XmlTextView>(
            R.id.tv_event_datetime
        ).text =
            event.startTime.format(
                formatter
            )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreationScreen(
    initialEvent: Event? = null,
    onEventCreated: (Event) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    
    val isAutoTimeZone = remember { sharedPreferences.getBoolean("auto_timezone", true) }
    val zoneId = remember {
        if (isAutoTimeZone) {
            ZoneId.systemDefault()
        } else {
            val timeZoneId = sharedPreferences.getString("time_zone", "GMT+00:00") ?: "GMT+00:00"
            ZoneId.of(timeZoneId)
        }
    }

    var title by remember {
        mutableStateOf(
            initialEvent?.title ?: ""
        )
    }

    var description by remember {
        mutableStateOf(
            initialEvent?.description ?: ""
        )
    }

    val dateFormatter =
        DateTimeFormatter.ofPattern(
            "MMM dd, yyyy"
        )

    var date by remember {
        mutableStateOf(
            initialEvent
                ?.startTime
                ?.format(dateFormatter)
                ?: ""
        )
    }

    var time by remember {
        mutableStateOf(
            initialEvent
                ?.startTime
                ?.format(
                    DateTimeFormatter.ofPattern(
                        "HH:mm"
                    )
                )
                ?: ""
        )
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }

    val datePickerState =
        rememberDatePickerState(

            initialSelectedDateMillis =
                initialEvent
                    ?.startTime
                    ?.atZone(
                        zoneId
                    )
                    ?.toInstant()
                    ?.toEpochMilli()
        )

    val timePickerState =
        rememberTimePickerState(

            initialHour =
                initialEvent
                    ?.startTime
                    ?.hour
                    ?: 12,

            initialMinute =
                initialEvent
                    ?.startTime
                    ?.minute
                    ?: 0
        )

    /*
     * DATE PICKER
     */
    if (showDatePicker) {

        DatePickerDialog(

            onDismissRequest = {
                showDatePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let {

                                val selectedDate =
                                    Instant
                                        .ofEpochMilli(it)
                                        .atZone(
                                            ZoneOffset.UTC
                                        )
                                        .toLocalDate()

                                date =
                                    selectedDate.format(
                                        dateFormatter
                                    )
                            }

                        showDatePicker = false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }

    /*
     * TIME PICKER
     */
    if (showTimePicker) {

        AlertDialog(

            onDismissRequest = {
                showTimePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        time =
                            String.format(
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )

                        showTimePicker = false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            },

            text = {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    TimePicker(
                        state = timePickerState
                    )
                }
            }
        )
    }

    /*
     * MAIN EVENT CREATION / EDIT SCREEN
     */
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
                .padding(16.dp),

        contentAlignment =
            Alignment.Center
    ) {

        Column(

            modifier =
                Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            /*
             * Header
             */
            Surface(

                color =
                    MaterialTheme
                        .colorScheme
                        .surface,

                shape =
                    RoundedCornerShape(
                        8.dp
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        MaterialTheme
                            .colorScheme
                            .onSurface
                            .copy(alpha = 0.5f)
                    ),

                modifier =
                    Modifier.padding(
                        bottom = 24.dp
                    )
            ) {

                Text(

                    text =
                        if (
                            initialEvent == null
                        ) {
                            "Add New Event"
                        } else {
                            "Edit Event"
                        },

                    modifier =
                        Modifier.padding(
                            horizontal = 32.dp,
                            vertical = 16.dp
                        ),

                    fontSize = 28.sp,

                    fontWeight =
                        FontWeight.Bold,

                    textAlign =
                        TextAlign.Center,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }

            /*
             * Main form.
             */
            Surface(

                color =
                    MaterialTheme
                        .colorScheme
                        .surface,

                shape =
                    RoundedCornerShape(
                        8.dp
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        MaterialTheme
                            .colorScheme
                            .onSurface
                            .copy(alpha = 0.5f)
                    ),

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(

                    modifier =
                        Modifier.padding(
                            24.dp
                        ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    /*
                     * Event title.
                     */
                    OutlinedTextField(

                        value = title,

                        onValueChange = {
                            title = it
                        },

                        label = {
                            Text(
                                "Event Title"
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        singleLine = true
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )

                    /*
                     * Event description.
                     */
                    OutlinedTextField(

                        value =
                            description,

                        onValueChange = {
                            description = it
                        },

                        label = {
                            Text(
                                "Description"
                            )
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    120.dp
                                )
                    )

                    /*
                     * Date field.
                     */
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(

                            value = date,

                            onValueChange = { },

                            label = {
                                Text(
                                    "Date (Month and Day)"
                                )
                            },

                            readOnly = true,

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Box(

                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .clickable {

                                        showDatePicker =
                                            true
                                    }
                        )
                    }

                    /*
                     * Time field.
                     */
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(

                            value = time,

                            onValueChange = { },

                            label = {
                                Text(
                                    "Time (Hour and Minute)"
                                )
                            },

                            readOnly = true,

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Box(

                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .clickable {

                                        showTimePicker =
                                            true
                                    }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                24.dp
                            )
                    )

                    /*
                     * Cancel / Save buttons.
                     */
                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                16.dp
                            )
                    ) {

                        Button(

                            onClick =
                                onCancel,

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        ) {

                            Text("Cancel")
                        }

                        Button(

                            onClick = {

                                if (
                                    title.isNotBlank()
                                ) {

                                    val selectedDate =
                                        datePickerState
                                            .selectedDateMillis
                                            ?.let {

                                                Instant
                                                    .ofEpochMilli(
                                                        it
                                                    )
                                                    .atZone(
                                                        ZoneOffset.UTC
                                                    )
                                                    .toLocalDate()
                                            }
                                            ?: LocalDate.now()

                                    val selectedTime =
                                        LocalTime.of(
                                            timePickerState.hour,
                                            timePickerState.minute
                                        )

                                    val startDateTime =
                                        LocalDateTime.of(
                                            selectedDate,
                                            selectedTime
                                        )

                                    /*
                                     * Editing preserves the existing
                                     * Room ID using copy().
                                     *
                                     * Creating makes a new Event whose
                                     * ID will later be generated by Room.
                                     */
                                    onEventCreated(

                                        initialEvent?.copy(

                                            title =
                                                title,

                                            description =
                                                description,

                                            startTime =
                                                startDateTime,

                                            endTime =
                                                startDateTime
                                                    .plusHours(
                                                        1
                                                    )

                                        ) ?: Event(

                                            title =
                                                title,

                                            description =
                                                description,

                                            startTime =
                                                startDateTime,

                                            endTime =
                                                startDateTime
                                                    .plusHours(
                                                        1
                                                    )
                                        )
                                    )
                                }
                            },

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        ) {

                            Text(

                                if (
                                    initialEvent == null
                                ) {
                                    "Save Event"
                                } else {
                                    "Update Event"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}