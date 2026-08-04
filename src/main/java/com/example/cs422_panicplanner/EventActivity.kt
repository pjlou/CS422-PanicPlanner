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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.time.LocalDateTime
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

@Composable
fun EventCreationScreen(
    initialEvent: Event? = null,
    onEventCreated: (Event) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header (Styled like XML headers)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = if (initialEvent == null) "Add New Event" else "Edit Event",
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Main Form Card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onEventCreated(
                                        initialEvent?.copy(
                                            title = title,
                                            description = description
                                        ) ?: Event(
                                            title = title,
                                            description = description,
                                            startTime = LocalDateTime.now(),
                                            endTime = LocalDateTime.now().plusHours(1)
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (initialEvent == null) "Save Event" else "Update Event")
                        }
                    }
                }
            }
        }
    }
}
