package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData

class HomeEventCardAdapter(
    private var events: List<EventData>,
    private var favoriteEvents: List<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<HomeEventCardAdapter.EventViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(cardData: EventData)
        fun onFavIconClick(cardData: EventData)
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var currentEvent: EventData

        // Reference to the favorite icon and other views
        private val favIcon: View = itemView.findViewById(R.id.favIcon)

        init {
            favIcon.setOnClickListener {
                listener.onFavIconClick(currentEvent)
            }
            itemView.setOnClickListener {
                listener.onItemClick(currentEvent)
            }
        }

        fun bind(event: EventData, isFavorite: Boolean) {
            currentEvent = event
            itemView.findViewById<TextView>(R.id.eventTitleTv).text = event.eventTitle
            itemView.findViewById<TextView>(R.id.eventTimeTv).text = event.eventTiming

            if (isFavorite) {
                favIcon.setBackgroundResource(R.drawable.ic_fav_filled)
            } else {
                favIcon.setBackgroundResource(R.drawable.ic_fav)
            }
        }
    }

    fun updatedFilteredEvents(filteredEvents:List<EventData>){
        events=filteredEvents
        notifyDataSetChanged()
    }


    fun updatedFavEvents(favEvents:List<String>){
        favoriteEvents=favEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_view_card, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val isFavorite = favoriteEvents.any { it == event.eventId }
        holder.bind(event, isFavorite)
    }

    override fun getItemCount(): Int = events.size
}
