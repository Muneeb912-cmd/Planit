package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData

class FeaturedEventAdapter(
    private var promotionCards: List<EventData>,
    private var favoriteEvents: List<EventData>,
    private val listener: OnFeaturedEventClickListener
) : RecyclerView.Adapter<FeaturedEventAdapter.PromotionCardViewHolder>() {
    interface OnFeaturedEventClickListener {
        fun onFeaturedEventCardClick(cardData: EventData)
        fun onFavIconClick(cardData: EventData)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_card, parent, false)
        return PromotionCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromotionCardViewHolder, position: Int) {
        val event = promotionCards[position]
        val isFavorite = favoriteEvents.any { it.eventId == event.eventId }
        holder.bind(event, isFavorite)
    }

    override fun getItemCount(): Int {
        return promotionCards.size
    }

    fun submitList(filteredEvents: List<EventData>) {
        promotionCards = filteredEvents
        notifyDataSetChanged()
    }

    fun submitFavEventsList(favEvents: List<EventData>) {
        favoriteEvents=favEvents
        notifyDataSetChanged()
    }

    inner class PromotionCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var currentCardData: EventData
        private val favIcon: View = itemView.findViewById(R.id.favIcon)
        init {
            itemView.setOnClickListener {
                listener.onFeaturedEventCardClick(currentCardData)
            }

            favIcon.setOnClickListener {
                listener.onFavIconClick(currentCardData)
            }
        }

        fun bind(cardData: EventData, isFavorite: Boolean) {
            currentCardData = cardData
            itemView.findViewById<TextView>(R.id.eventTitleTv).text = cardData.eventTitle
            itemView.findViewById<TextView>(R.id.eventOrganizer).text = cardData.eventOrganizer
            itemView.findViewById<TextView>(R.id.eventDate).text = cardData.eventDate

            if (isFavorite) {
                favIcon.setBackgroundResource(R.drawable.ic_fav_filled)
            } else {
                favIcon.setBackgroundResource(R.drawable.ic_fav)
            }
        }

        override fun onClick(v: View?) {
            listener.onFeaturedEventCardClick(currentCardData)
        }
    }
}
