package com.app.todoapp.ui

import android.os.Bundle
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