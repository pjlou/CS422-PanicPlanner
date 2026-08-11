package com.example.cs422_panicplanner.database

import androidx.room.*
import com.example.cs422_panicplanner.TodoItem

@Dao
interface TodoDao {
    @Insert
    suspend fun insert(todo: TodoItem): Long

    @Update
    suspend fun update(todo: TodoItem)

    @Delete
    suspend fun delete(todo: TodoItem)

    @Query("SELECT * FROM todo_items ORDER BY id DESC")
    suspend fun getAllTodoItems(): List<TodoItem>

    @Query("SELECT * FROM todo_items WHERE id = :id")
    suspend fun getTodoItem(id: Int): TodoItem?
}