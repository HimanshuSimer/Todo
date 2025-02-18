package com.app.todoapp.entity

import androidx.room.*

interface TaskDao {

    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks Order by id DESC")
    suspend fun getAllTasks(): List<TaskEntity>
}