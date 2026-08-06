package com.example.cs422_panicplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val onToggle: (TodoItem) -> Unit,
    private val onDelete: (TodoItem) -> Unit
) : ListAdapter<TodoItem, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbTodo: CheckBox = view.findViewById(R.id.cb_todo)
        val tvTitle: TextView = view.findViewById(R.id.tv_todo_title)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_todo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvTitle.text = item.title
        
        holder.cbTodo.setOnCheckedChangeListener(null)
        holder.cbTodo.isChecked = item.isCompleted
        
        holder.cbTodo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != item.isCompleted) {
                onToggle(item.copy(isCompleted = isChecked))
            }
        }

        holder.btnDelete.setOnClickListener {
            onDelete(item)
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem): Boolean {
            return oldItem == newItem
        }
    }
}