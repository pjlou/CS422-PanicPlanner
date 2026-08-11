package com.example.cs422_panicplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
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
import androidx.compose.material3.Button as ComposeButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs422_panicplanner.database.DatabaseProvider
import com.example.cs422_panicplanner.ui.theme.CS422PanicPlannerTheme
import kotlinx.coroutines.launch

class TodoListActivity : AppCompatActivity() {

    private lateinit var viewModel: TodoViewModel
    private lateinit var adapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DatabaseProvider.getDatabase(this)
        val factory = TodoViewModelFactory(database.todoDao())
        viewModel = ViewModelProvider(this, factory)[TodoViewModel::class.java]

        setupListScreen()
        observeTodoItems()
    }

    private fun setupListScreen() {
        setContentView(R.layout.todo_list)

        val btnAdd = findViewById<Button>(R.id.btn_add_todo)
        val rvTodo = findViewById<RecyclerView>(R.id.rv_todo)
        val btnNavCalendar = findViewById<ImageButton>(R.id.btn_nav_calendar)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings)

        rvTodo.layoutManager = LinearLayoutManager(this)
        adapter = TodoAdapter(
            onToggle = { item -> viewModel.updateTodo(item) },
            onDelete = { item -> viewModel.deleteTodo(item) }
        )
        rvTodo.adapter = adapter
        
        // Refresh list content if returning from creation
        viewModel.loadTodoItems()

        btnAdd.setOnClickListener {
            showAddTodoCompose()
        }

        btnNavCalendar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showAddTodoCompose() {
        setContent {
            CS422PanicPlannerTheme {
                TodoCreationScreen(
                    onSave = { title ->
                        viewModel.addTodo(title)
                        setupListScreen()
                        observeTodoItems() // Re-attach observer after setContent
                    },
                    onCancel = {
                        setupListScreen()
                        observeTodoItems() // Re-attach observer after setContent
                    }
                )
            }
        }
    }

    private fun observeTodoItems() {
        lifecycleScope.launch {
            viewModel.todoItems.collect { items ->
                // Check if adapter is initialized before submitting
                if (::adapter.isInitialized) {
                    adapter.submitList(items)
                }
            }
        }
    }
}

@Composable
fun TodoCreationScreen(
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }

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
                    text = "Add New Task",
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
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ComposeButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        ComposeButton(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSave(title)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Task")
                        }
                    }
                }
            }
        }
    }
}
