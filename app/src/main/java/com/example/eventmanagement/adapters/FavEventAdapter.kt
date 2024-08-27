package com.example.eventmanagement.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData

class FavEventAdapter(
    private val favoriteEvents: List<EventData>,
    private val listener: EventCardClickListener
) : RecyclerView.Adapter<FavEventAdapter.EventCardViewHolder>() {

    interface EventCardClickListener {
        fun onEventCardClick(cardData: EventData)
        fun onFavIconClick(cardData: EventData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fav_event_item, parent, false)
        return EventCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventCardViewHolder, position: Int) {
        val event = favoriteEvents[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return favoriteEvents.size
    }

    inner class EventCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var currentEvent: EventData

        private val favIcon: View = itemView.findViewById(R.id.favIcon)

        fun bind(cardData: EventData) {
            currentEvent = cardData
            itemView.findViewById<TextView>(R.id.eventTitle).text = cardData.eventTitle
            itemView.findViewById<TextView>(R.id.eventOrganizer).text = cardData.eventOrganizer
            itemView.findViewById<TextView>(R.id.eventTime).text = cardData.eventTiming
            itemView.findViewById<TextView>(R.id.eventLocation).text = cardData.eventLocation
            itemView.findViewById<TextView>(R.id.eventDate).text = cardData.eventDate

            itemView.setOnClickListener {
                listener.onEventCardClick(currentEvent)
            }

            favIcon.setOnClickListener {
                listener.onFavIconClick(currentEvent)
            }
        }
    }
}
