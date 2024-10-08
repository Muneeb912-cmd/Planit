package com.example.eventmanagement.ui.fragments.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData

class AllEventCardAdapter(
    private var eventCards: List<EventData>,
    private var favoriteEvents: List<String>,
    private val listener: EventCardClickListener
) : RecyclerView.Adapter<AllEventCardAdapter.EventCardViewHolder>() {

    interface EventCardClickListener {
        fun onEventCardClick(cardData: EventData)
        fun onFavIconClick(cardData: EventData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.all_event_card_layout, parent, false)
        return EventCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventCardViewHolder, position: Int) {
        val event = eventCards[position]
        val isFavorite = favoriteEvents.any { it == event.eventId }
        holder.bind(event, isFavorite)
    }

    override fun getItemCount(): Int {
        return eventCards.size
    }

    fun submitList(filteredPopularEvents: List<EventData>) {
        eventCards = filteredPopularEvents
        notifyDataSetChanged()
    }

    fun submitFavEventsList(favEvents: List<String>) {
        favoriteEvents = favEvents
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
            itemView.findViewById<TextView>(R.id.eventLocation).text = cardData.eventLocation
            itemView.findViewById<TextView>(R.id.eventDate).text = cardData.eventDate
            itemView.findViewById<TextView>(R.id.eventStatus).text =
                if (cardData.eventStatus == "Missed") "Event Closed" else cardData.eventStatus


            if (cardData.isEventPopular == true) {
                itemView.findViewById<ImageView>(R.id.eventTag)
                    .setImageResource(R.drawable.ic_popular)
            } else if (cardData.isEventFeatured == true) {
                itemView.findViewById<ImageView>(R.id.eventTag)
                    .setImageResource(R.drawable.ic_featured)
            }

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
