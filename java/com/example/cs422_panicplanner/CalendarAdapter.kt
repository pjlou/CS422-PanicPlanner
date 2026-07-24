package com.example.cs422_panicplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private var daysCount: Int,
    private var daysWithEvents: Set<Int> = emptySet(),
    private val onDayClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvEventSummary: TextView = view.findViewById(R.id.tv_event_summary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_row, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val dayNumber = position + 1
        holder.tvDate.text = dayNumber.toString()
        
        if (daysWithEvents.contains(dayNumber)) {
            holder.tvEventSummary.text = "Event Scheduled"
            holder.itemView.alpha = 1.0f
        } else {
            holder.tvEventSummary.text = "No events"
            holder.itemView.alpha = 0.5f // Dim days without events
        }
        
        holder.itemView.setOnClickListener {
            onDayClick(dayNumber)
        }
    }

    override fun getItemCount(): Int = daysCount

    fun updateData(newDaysCount: Int, newDaysWithEvents: Set<Int>) {
        daysCount = newDaysCount
        daysWithEvents = newDaysWithEvents
        notifyDataSetChanged()
    }
}
