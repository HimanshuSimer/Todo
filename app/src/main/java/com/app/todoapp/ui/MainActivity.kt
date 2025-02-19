package com.app.todoapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todoapp.R
import com.app.todoapp.adapter.TaskAdapter
import com.app.todoapp.databinding.ActivityMainBinding
import com.app.todoapp.entity.TaskDatabase
import com.app.todoapp.entity.TaskEntity
import com.app.todoapp.model.TaskData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var db: TaskDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = TaskDatabase.getDatabase(this)

        taskAdapter = TaskAdapter(mutableListOf()) { task ->
            deleteTask(task)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter

        loadTasks()

        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val etTaskTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val etTaskDescription = dialogView.findViewById<EditText>(R.id.etTaskDescription)

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTaskTitle.text.toString()
                val description = etTaskDescription.text.toString()

                if (title.isNotEmpty() && description.isNotEmpty()) {
                    val task =
                        TaskEntity(title = title, description = description, isCompleted = false)
                    insertTask(task)
                } else {
                    Toast.makeText(
                        this,
                        "Please enter both title and description",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    private fun deleteTask(task: TaskEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().deleteTask(task)
            loadTasks()
        }
    }
}