package com.app.todoapp.model

data class TaskData(
    val title: String,
    val description: String,
    var isCompleted: Boolean
)
