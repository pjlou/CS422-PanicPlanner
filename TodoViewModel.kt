package com.example.cs422_panicplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs422_panicplanner.database.TodoDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodoViewModel(private val todoDao: TodoDao) : ViewModel() {

    private val _todoItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoItems: StateFlow<List<TodoItem>> = _todoItems

    init {
        loadTodoItems()
    }

    fun loadTodoItems() {
        viewModelScope.launch {
            _todoItems.value = todoDao.getAllTodoItems()
        }
    }

    fun addTodo(title: String) {
        viewModelScope.launch {
            todoDao.insert(TodoItem(title = title))
            loadTodoItems()
        }
    }

    fun updateTodo(item: TodoItem) {
        viewModelScope.launch {
            todoDao.update(item)
            loadTodoItems()
        }
    }

    fun deleteTodo(item: TodoItem) {
        viewModelScope.launch {
            todoDao.delete(item)
            loadTodoItems()
        }
    }
}

class TodoViewModelFactory(private val todoDao: TodoDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(todoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}