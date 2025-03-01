package com.app.todoapp.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todoapp.R
import com.app.todoapp.adapter.TaskAdapter
import com.app.todoapp.databinding.ActivityMainBinding
import com.app.todoapp.entity.TaskDatabase
import com.app.todoapp.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var db: TaskDatabase
    private var allTasks: List<TaskEntity> = emptyList() // Store all tasks for filtering

    private var selectedTime: String? = null // Ensure a time is always selected

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(3000)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the Toolbar as the ActionBar
        setSupportActionBar(binding.homeToolbar)

        // Customize window colors
        window.statusBarColor = getColor(R.color.your_background_color)
        window.navigationBarColor = Color.BLACK

        // Request notification permission (for Android 13+)
        requestNotificationPermission()

        // Initialize the database and task adapter
        db = TaskDatabase.getDatabase(this)

        taskAdapter = TaskAdapter(mutableListOf(),
            onDelete = { task -> deleteTask(task) },
            onStatusChange = { task -> updateTaskStatus(task) }
        )

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter

        // Load tasks from database
        loadTasks()

        // Add task button click listener
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    // Request notification permission (Android 13+)
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    // Show Add Task dialog
    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val etTaskTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val etTaskDescription = dialogView.findViewById<EditText>(R.id.etTaskDescription)
        val etTaskCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.etTaskCategory)
        val btnSelectTime = dialogView.findViewById<Button>(R.id.btnSelectTime)
        val tvSelectedTime = dialogView.findViewById<TextView>(R.id.tvSelectedTime)

        val categories = listOf("Work", "Personal", "Shopping", "Health", "Others")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etTaskCategory.setAdapter(categoryAdapter)

        etTaskCategory.setOnClickListener {
            etTaskCategory.showDropDown()
        }

        // Time picker logic
        btnSelectTime.setOnClickListener {
            val timePicker = TimePickerDialog(this, { _, hour, minute ->
                val formattedTime = String.format("%02d:%02d", hour, minute)
                selectedTime = formattedTime
                tvSelectedTime.text = "Selected Time: $formattedTime"
            }, 12, 0, true)
            timePicker.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTaskTitle.text.toString().trim()
                val description = etTaskDescription.text.toString().trim()
                val category = etTaskCategory.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty() && category.isNotEmpty() && selectedTime != null) {
                    val task = TaskEntity(title = title, description = description, category = category, reminderTime = selectedTime!!, isCompleted = false)
                    insertTask(task)
                } else {
                    Toast.makeText(this, "Please fill all fields and select a time", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Load tasks from the database
    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = db.taskDao().getAllTasks()
            runOnUiThread {
                allTasks = tasks
                taskAdapter.updateTask(tasks)
            }
        }
    }

    // Insert a new task into the database
    private fun insertTask(task: TaskEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().insertTask(task)
            loadTasks()
        }
    }

    // Delete a task from the database
    private fun deleteTask(task: TaskEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().deleteTask(task)
            loadTasks()
        }
    }

    // Update task completion status
    private fun updateTaskStatus(task: TaskEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().updateTask(task)
            loadTasks()
        }
    }

    // Inflate menu to add the filter button in the toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Handle menu item click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showCategoryFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Show category filter dialog
    private fun showCategoryFilterDialog() {
        val categories = listOf("All", "Work", "Personal", "Shopping", "Health", "Others")

        AlertDialog.Builder(this)
            .setTitle("Filter by Category")
            .setItems(categories.toTypedArray()) { _, which ->
                val selectedCategory = categories[which]
                filterTasksByCategory(selectedCategory)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Filter tasks based on selected category
    private fun filterTasksByCategory(category: String) {
        val filteredTasks = if (category == "All") {
            allTasks
        } else {
            allTasks.filter { it.category == category }
        }
        taskAdapter.updateTask(filteredTasks)
    }
}
