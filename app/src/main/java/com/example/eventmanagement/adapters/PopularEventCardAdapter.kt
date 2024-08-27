package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData
import kotlin.io.path.fileVisitor

class PopularEventCardAdapter(
    private var eventCards: List<EventData>,
    private var favoriteEvents: List<EventData>,
    private val listener: EventCardClickListener
) : RecyclerView.Adapter<PopularEventCardAdapter.EventCardViewHolder>() {

    interface EventCardClickListener {
        fun onEventCardClick(cardData: EventData)
        fun onFavIconClick(cardData: EventData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousal_item, parent, false)
        return EventCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventCardViewHolder, position: Int) {
        val event = eventCards[position]
        val isFavorite = favoriteEvents.any { it.eventId == event.eventId }
        holder.bind(event, isFavorite)
    }

    override fun getItemCount(): Int {
        return eventCards.size
    }

    fun submitList(filteredPopularEvents: List<EventData>) {
        eventCards = filteredPopularEvents
        notifyDataSetChanged()
    }

    fun submitFavEventsList(favEvents: List<EventData>) {
        favoriteEvents= favEvents
        notifyDataSetChanged()
    }

    inner class EventCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private lateinit var currentEvent: EventData
        fun bind(cardData: EventData, isFavorite: Boolean) {
            currentEvent = cardData
            itemView.findViewById<TextView>(R.id.eventTitle).text = cardData.eventTitle
            itemView.findViewById<TextView>(R.id.eventOrganizer).text = cardData.eventOrganizer
            itemView.findViewById<TextView>(R.id.eventTime).text = cardData.eventTiming
            itemView.findViewById<TextView>(R.id.eventCategory).text = cardData.eventCategory
            itemView.findViewById<TextView>(R.id.eventDescription).text = cardData.eventDescription
            itemView.findViewById<TextView>(R.id.eventLocation).text = cardData.eventLocation
            itemView.findViewById<TextView>(R.id.eventDate).text = cardData.eventDate

            if (isFavorite) {
                favIcon.setBackgroundResource(R.drawable.ic_fav_filled)
            } else {
                favIcon.setBackgroundResource(R.drawable.ic_fav)
            }
        }

        // Reference to the favorite icon and other views
        private val favIcon: View = itemView.findViewById(R.id.favIcon)

        init {
            itemView.setOnClickListener {
                listener.onEventCardClick(currentEvent)
            }

            favIcon.setOnClickListener {
                listener.onFavIconClick(currentEvent)
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onEventCardClick(eventCards[position])
            }
        }
    }
}
