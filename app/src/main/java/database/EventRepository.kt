package com.example.cs422_panicplanner.database

class EventRepository(
    private val eventDao: EventDao
) {

    suspend fun insert(event: Event) =
        eventDao.insert(event)

    suspend fun update(event: Event) =
        eventDao.update(event)

    suspend fun delete(event: Event) =
        eventDao.delete(event)

    suspend fun getAllEvents() =
        eventDao.getAllEvents()

    suspend fun getEvent(id: Int) =
        eventDao.getEvent(id)
}