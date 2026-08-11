package com.example.cs422_panicplanner.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cs422_panicplanner.Event
import com.example.cs422_panicplanner.TodoItem

@Database(
    entities = [Event::class, TodoItem::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun todoDao(): TodoDao
}