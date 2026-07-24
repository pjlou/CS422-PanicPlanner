package com.example.cs422_panicplanner.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Event::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
}