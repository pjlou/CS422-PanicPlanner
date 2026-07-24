package com.example.cs422_panicplanner.database

import androidx.room.*

@Dao
interface EventDao {

    @Insert
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM events ORDER BY date")
    suspend fun getAllEvents(): List<Event>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEvent(id: Int): Event?
}