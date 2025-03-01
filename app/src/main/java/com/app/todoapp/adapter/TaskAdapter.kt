package com.app.todoapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.todoapp.databinding.ItemTaskBinding
import com.app.todoapp.entity.TaskEntity

class TaskAdapter(
    private var fullTaskList: MutableList<TaskEntity>,
    private val onDelete: (TaskEntity) -> Unit,
    private val onStatusChange: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var filteredTaskList: MutableList<TaskEntity> = fullTaskList.toMutableList()

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskEntity) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description
            binding.cbTaskComplete.isChecked = task.isCompleted

            // Log data for debugging
            Log.d("TaskAdapter", "Task: ${task.title}, Reminder Time: ${task.reminderTime ?: "No Reminder"}")

            // Set Category Visibility
            binding.tvTaskCategory.apply {
                text = "📂 ${task.category}"
                visibility = if (!task.category.isNullOrEmpty()) View.VISIBLE else View.GONE
            }

            // Set Reminder Time Visibility
            binding.tvTaskTime.apply {
                if (!task.reminderTime.isNullOrEmpty()) {
                    text = "⏰ ${task.reminderTime}"
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }

            // Prevent infinite loop by temporarily removing listener
            binding.cbTaskComplete.setOnCheckedChangeListener(null)
            binding.cbTaskComplete.isChecked = task.isCompleted

            // Handle status change
            binding.cbTaskComplete.setOnCheckedChangeListener { _, isChecked ->
                if (task.isCompleted != isChecked) {
                    val updatedTask = task.copy(isCompleted = isChecked)
                    onStatusChange(updatedTask)
                }
            }

            // Handle delete
            binding.btnDeleteTask.setOnClickListener { onDelete(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int = filteredTaskList.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(filteredTaskList[position])
    }

    fun updateTask(newList: List<TaskEntity>) {
        fullTaskList.clear()
        fullTaskList.addAll(newList)
        filter("") // Refresh with no filter
        notifyDataSetChanged()  // Ensure UI updates
    }

    // Filter tasks based on query and status
    fun filter(query: String, filterStatus: Boolean? = null) {
        filteredTaskList = fullTaskList.filter { task ->
            val matchesQuery = task.title.contains(query, ignoreCase = true)
            val matchesStatus = filterStatus?.let { task.isCompleted == it } ?: true
            matchesQuery && matchesStatus
        }.toMutableList()
        notifyDataSetChanged()
    }

    // Clear any active filter and reset to full list
    fun resetFilter() {
        filteredTaskList = fullTaskList.toMutableList()
        notifyDataSetChanged()
    }
}
