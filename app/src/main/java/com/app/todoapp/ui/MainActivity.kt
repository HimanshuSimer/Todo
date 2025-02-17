package com.app.todoapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.todoapp.R
import com.app.todoapp.adapter.TaskAdapter
import com.app.todoapp.databinding.ActivityMainBinding
import com.app.todoapp.model.TaskData

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var taskList = mutableListOf<TaskData>()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskList = mutableListOf(
            TaskData("Buy Groceries", "Milk, Eggs, Bread", false),
            TaskData("Finish Project", "Complete the Android project", true),
            TaskData("Workout", "Go to the gym", false)
        )

        taskAdapter = TaskAdapter(taskList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter
    }
}