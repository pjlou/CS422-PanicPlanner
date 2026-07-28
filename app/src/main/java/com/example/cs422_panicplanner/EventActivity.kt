package com.example.cs422_panicplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs422_panicplanner.database.DatabaseProvider
import com.example.cs422_panicplanner.ui.theme.CS422PanicPlannerTheme
import java.time.LocalDateTime
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class EventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = DatabaseProvider.getDatabase(this)
        val factory = EventViewModelFactory(database.eventDao())

        setContent {
            CS422PanicPlannerTheme {
                val viewModel: EventViewModel = viewModel(factory = factory)
                EventCreationScreen(
                    onEventCreated = { 
                        viewModel.addEvent(it)
                        finish() // Go back after saving
                    },
                    onCancel = { finish() }
                )
            }
        }
    }
}

@Composable
fun EventCreationScreen(onEventCreated: (Event) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = { Text("Add New Event", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium) }
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
            
            // For MVP, we'll default to "Now" for start/end
            // Adrian or Marissa might add DatePickers later
            
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
                            onEventCreated(
                                Event(
                                    title = title,
                                    description = description,
                                    startTime = LocalDateTime.now(),
                                    endTime = LocalDateTime.now().plusHours(1)
                                )
                            )
                        }
                    }
                ) {
                    Text("Save Event")
                }
            }
        setContentView(R.layout.event_detail)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}
