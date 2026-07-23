package com.example.cs422_panicplanner.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {

    fun insert(event: Event) {
        viewModelScope.launch {
            repository.insert(event)
        }
    }

    fun update(event: Event) {
        viewModelScope.launch {
            repository.update(event)
        }
    }

    fun delete(event: Event) {
        viewModelScope.launch {
            repository.delete(event)
        }
    }
}