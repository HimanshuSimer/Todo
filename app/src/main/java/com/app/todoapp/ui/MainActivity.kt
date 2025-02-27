package com.app.todoapp.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.app.todoapp.R
import com.app.todoapp.adapter.TaskAdapter
import com.app.todoapp.databinding.ActivityMainBinding
import com.app.todoapp.entity.TaskDatabase
import com.app.todoapp.entity.TaskEntity
import com.app.todoapp.utils.TaskReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var db: TaskDatabase

    // Holds the reminder delay in milliseconds
    private var reminderDelayMillis: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        db = TaskDatabase.getDatabase(this)

        taskAdapter = TaskAdapter(mutableListOf(),
            onDelete = { task -> deleteTask(task) },
            onStatusChange = { task -> updatedTaskStatus(task) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter


//      RecyclerView Animation
        val fadeIn = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fade_in)
        binding.recyclerView.layoutAnimation = fadeIn

//      Recycler Item Animation
        val animation = DefaultItemAnimator()
        animation.addDuration = 1000
        binding.recyclerView.itemAnimator = animation


        loadTasks()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                taskAdapter.filter(s.toString())
            }
        })

        binding.fabAddTask.setOnClickListener {
            animationFab()
            showAddTaskDialog()
        }
    }

    private fun animationFab() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        binding.fabAddTask.startAnimation(anim)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val etTaskTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val etTaskDescription = dialogView.findViewById<EditText>(R.id.etTaskDescription)
        val btnSelectTime = dialogView.findViewById<Button>(R.id.btnSelectTime)
        val tvSelectedTime = dialogView.findViewById<TextView>(R.id.tvSelectedTime)

        reminderDelayMillis = -1

        btnSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val targetCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (targetCalendar.timeInMillis <= System.currentTimeMillis()) {
                    targetCalendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                reminderDelayMillis = targetCalendar.timeInMillis - System.currentTimeMillis()
                tvSelectedTime.text = "Reminder set for: ${hourOfDay}:${minute.toString().padStart(2, '0')}"
            }, currentHour, currentMinute, true).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTaskTitle.text.toString()
                val description = etTaskDescription.text.toString()
                if (title.isNotEmpty() && description.isNotEmpty()) {
                    val task = TaskEntity(title = title, description = description, isCompleted = false)
                    insertTask(task)
                    if (reminderDelayMillis > 0) {
                        scheduleReminder(task, reminderDelayMillis)
                    } else {
                        Toast.makeText(this, "Task added without reminder.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please enter both title and description", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleReminder(task: TaskEntity, delayMillis: Long) {
        val data = Data.Builder()
            .putString("task_title", task.title)
            .putString("task_description", task.description)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = db.taskDao().getAllTasks()
            runOnUiThread {
                taskAdapter.updateTask(tasks)
            }
        }
    }

    private fun insertTask(task: TaskEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().insertTask(task)
            loadTasks()
        }
    }

    private fun updatedTaskStatus(task: TaskEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().updateTask(task)
            loadTasks()
        }
    }

    private fun deleteTask(task: TaskEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().deleteTask(task)
            loadTasks()
        }
    }
}