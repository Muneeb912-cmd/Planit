package com.example.eventmanagement.ui.fragments.adminhome

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData

class AdminHomeAdapter(
    private var events: List<EventData>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AdminHomeAdapter.EventViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(cardData: EventData)
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var currentEvent: EventData


        init {
            itemView.setOnClickListener {
                listener.onItemClick(currentEvent)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(event: EventData) {
            currentEvent = event
            itemView.findViewById<TextView>(R.id.eventTitleTv).text = event.eventTitle
            itemView.findViewById<TextView>(R.id.eventCategoryTv).text = "Category: ${event.eventCategory}"
            itemView.findViewById<TextView>(R.id.eventTimeTv).text = event.eventTiming
            itemView.findViewById<TextView>(R.id.eventDateTv).text = event.eventDate
            itemView.findViewById<TextView>(R.id.peopleAttendingTv).text = " : ${event.numberOfPeopleAttending.toString()}"
        }
    }

    fun updatedFilteredEvents(filteredEvents:List<EventData>){
        events=filteredEvents
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_home_event_card, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = events.size
}
