package com.app.todoapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.todoapp.databinding.ItemTaskBinding
import com.app.todoapp.entity.TaskDatabase
import com.app.todoapp.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAdapter(private val taskList: MutableList<TaskEntity>,
    private val onDelete: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        holder.binding.tvTaskTitle.text = task.title
        holder.binding.tvTaskDescription.text = task.description
        holder.binding.cbTaskComplete.isChecked = task.isCompleted


        holder.binding.cbTaskComplete.setOnCheckedChangeListener {_, isChecked ->
            CoroutineScope(Dispatchers.IO).launch {
                task.isCompleted = isChecked
                TaskDatabase.getDatabase(holder.itemView.context).taskDao().updateTask(task)
            }
        }


        holder.binding.root.setOnClickListener {
            onDelete(task)
            true
        }
    }

    fun updateTask(newList: List<TaskEntity>) {
        taskList.clear()
        taskList.addAll(newList)
        notifyDataSetChanged()
    }
}