package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.CardData

class HomeEventCardAdapter(
    private val events: List<CardData>,
    private val listener: OnItemClickListener // Add the listener
) : RecyclerView.Adapter<HomeEventCardAdapter.EventViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(cardData: CardData)
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var currentEvent: CardData

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(event: CardData) {
            currentEvent = event
            itemView.findViewById<TextView>(R.id.eventTitleTv).text = event.eventTitle
            itemView.findViewById<TextView>(R.id.eventTimeTv).text = event.eventTiming
        }

        override fun onClick(v: View?) {
            listener.onItemClick(currentEvent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_view_card, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = events.size
}
