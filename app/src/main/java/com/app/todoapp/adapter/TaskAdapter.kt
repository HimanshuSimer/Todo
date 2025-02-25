package com.app.todoapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.todoapp.databinding.ItemTaskBinding
import com.app.todoapp.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAdapter(
    private var fullTaskList: MutableList<TaskEntity>,
    private val onDelete: (TaskEntity) -> Unit,
    private val onStatusChange: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var filteredTaskList: MutableList<TaskEntity> = fullTaskList.toMutableList()

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredTaskList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = filteredTaskList[position]
        holder.binding.tvTaskTitle.text = task.title
        holder.binding.tvTaskDescription.text = task.description
        holder.binding.cbTaskComplete.isChecked = task.isCompleted

        holder.binding.cbTaskComplete.setOnCheckedChangeListener { _, isChecked ->
            if (task.isCompleted != isChecked) {
                task.isCompleted = isChecked
                onStatusChange(task)
            }
        }

        holder.binding.root.setOnClickListener {
            onDelete(task)
        }
    }


    fun updateTask(newList: List<TaskEntity>) {
        fullTaskList.clear()
        fullTaskList.addAll(newList)

        filter("")
    }

    // Filter based on the fullTaskList.
    fun filter(query: String, filterStatus: Boolean? = null) {
        filteredTaskList = fullTaskList.filter { task ->
            val matchesQuery = task.title.contains(query, ignoreCase = true)
            val matchesStatus = filterStatus?.let { task.isCompleted == it } ?: true
            matchesQuery && matchesStatus
        }.toMutableList()
        notifyDataSetChanged()
    }
}