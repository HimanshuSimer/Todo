package com.app.todoapp.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class TaskReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val taskTitle = inputData.getString("task_title") ?: "Task Reminder"
        val taskDescription = inputData.getString("task_description") ?: "You have a task reminder"

        // Create channel and show notification.
        NotificationHelper.createNotificationChannel(applicationContext)
        NotificationHelper.showNotification(applicationContext, taskTitle, taskDescription)

        return Result.success()
    }
}