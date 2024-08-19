package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData

class FeaturedEventAdapter(
    private val promotionCards: List<EventData>,
    private val listener: OnFeaturedEventClickListener
) : RecyclerView.Adapter<FeaturedEventAdapter.PromotionCardViewHolder>() {
    interface OnFeaturedEventClickListener {
        fun onFeaturedEventCardClick(cardData: EventData)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_card, parent, false)
        return PromotionCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromotionCardViewHolder, position: Int) {
        holder.bind(promotionCards[position])
    }

    override fun getItemCount(): Int {
        return promotionCards.size
    }

    inner class PromotionCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var currentCardData: EventData

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(cardData: EventData) {
            currentCardData = cardData
            itemView.findViewById<TextView>(R.id.eventTitleTv).text = cardData.eventTitle
            itemView.findViewById<TextView>(R.id.eventOrganizer).text = cardData.eventOrganizer
            itemView.findViewById<TextView>(R.id.eventDate).text = cardData.eventDate
        }

        override fun onClick(v: View?) {
            listener.onFeaturedEventCardClick(currentCardData) // Notify the listener when an item is clicked
        }
    }
}
