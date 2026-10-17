package com.example.proyecto_desarrollo.ui.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_desarrollo.data.model.Task
import com.example.proyecto_desarrollo.databinding.ItemTaskBinding

class TaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onToggleDone: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback) {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvDescription.text = task.description
            binding.tvDescription.isVisible = task.description.isNotBlank()
            binding.cbDone.isChecked = task.done

            // Tachado y atenuado cuando la tarea está hecha
            val strike = Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvTitle.paintFlags =
                if (task.done) binding.tvTitle.paintFlags or strike
                else binding.tvTitle.paintFlags and strike.inv()
            binding.root.alpha = if (task.done) 0.6f else 1f

            binding.root.setOnClickListener { onItemClick(task) }
            binding.cbDone.setOnClickListener {
                onToggleDone(task.copy(done = binding.cbDone.isChecked))
            }
            binding.btnDelete.setOnClickListener { onDeleteClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}