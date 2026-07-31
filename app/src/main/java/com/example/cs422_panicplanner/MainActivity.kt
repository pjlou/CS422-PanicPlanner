package com.example.cs422_panicplanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs422_panicplanner.database.DatabaseProvider
import com.example.cs422_panicplanner.ui.theme.CS422PanicPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = DatabaseProvider.getDatabase(this)
        val factory = EventViewModelFactory(database.eventDao())

        setContent {
            CS422PanicPlannerTheme {
                val viewModel: EventViewModel = viewModel(factory = factory)
                val events by viewModel.events.collectAsState()

                // Reload events whenever we return to this screen
                LaunchedEffect(Unit) {
                    viewModel.loadEvents()
                }

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            startActivity(Intent(this, EventActivity::class.java))
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Event")
                        }
                    }
                ) { padding ->
                    Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                        Text(
                            "Panic Planner", 
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        
                        if (events.isEmpty()) {
                            Text("No events scheduled.", modifier = Modifier.padding(16.dp))
                        } else {
                            LazyColumn {
                                items(events) { event ->
                                    EventItem(
                                        event = event,
                                        onDelete = { viewModel.deleteEvent(event) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium)
                Text(event.description, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
