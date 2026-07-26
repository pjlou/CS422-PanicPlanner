package com.example.cs422_panicplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs422_panicplanner.database.EventDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel(private val eventDao: EventDao) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _events.value = eventDao.getAllEvents()
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            eventDao.insert(event)
            loadEvents()
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventDao.delete(event)
            loadEvents()
        }
    }
}

class EventViewModelFactory(private val eventDao: EventDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(eventDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
