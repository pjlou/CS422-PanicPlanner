package com.example.cs422_panicplanner.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val category: String
)