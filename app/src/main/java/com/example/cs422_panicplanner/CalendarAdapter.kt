package com.example.cs422_panicplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class CalendarAdapter(
    private var daysCount: Int,
    private var eventsByDay: Map<Int, List<Event>> = emptyMap(),
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val eventsContainer: LinearLayout = view.findViewById(R.id.events_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_row, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val dayNumber = position + 1
        holder.tvDate.text = dayNumber.toString()
        
        holder.eventsContainer.removeAllViews()
        val events = eventsByDay[dayNumber]
        
        if (!events.isNullOrEmpty()) {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            events.sortedBy { it.startTime }.forEach { event ->
                val eventView = TextView(holder.itemView.context).apply {
                    text = "${event.startTime.format(timeFormatter)}   ${event.title}"
                    textSize = 14f
                    setPadding(0, 4, 0, 4)
                    setOnClickListener { onEventClick(event) }
                }
                holder.eventsContainer.addView(eventView)
            }
            holder.itemView.alpha = 1.0f
        } else {
            val noEventsView = TextView(holder.itemView.context).apply {
                text = "No events"
                textSize = 14f
                alpha = 0.5f
            }
            holder.eventsContainer.addView(noEventsView)
            holder.itemView.alpha = 0.5f
        }
    }

    override fun getItemCount(): Int = daysCount

    fun updateData(newDaysCount: Int, newEventsByDay: Map<Int, List<Event>>) {
        daysCount = newDaysCount
        eventsByDay = newEventsByDay
        notifyDataSetChanged()
    }
}
