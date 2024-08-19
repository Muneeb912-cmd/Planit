package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData

class PopularEventCardAdapter(
    private val eventCards: List<EventData>,
    private val listener: EventCardClickListener
) : RecyclerView.Adapter<PopularEventCardAdapter.EventCardViewHolder>() {

    interface EventCardClickListener {
        fun onEventCardClick(cardData: EventData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousal_item, parent, false)
        return EventCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventCardViewHolder, position: Int) {
        holder.bind(eventCards[position])
    }

    override fun getItemCount(): Int {
        return eventCards.size
    }

    inner class EventCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        fun bind(cardData: EventData) {
            itemView.findViewById<TextView>(R.id.eventTitle).text = cardData.eventTitle
            itemView.findViewById<TextView>(R.id.eventOrganizer).text = cardData.eventOrganizer
            itemView.findViewById<TextView>(R.id.eventTime).text = cardData.eventTiming
            itemView.findViewById<TextView>(R.id.eventCategory).text = cardData.eventCategory
            itemView.findViewById<TextView>(R.id.eventDescription).text = cardData.eventDescription
            itemView.findViewById<TextView>(R.id.eventLocation).text = cardData.eventLocation
            itemView.findViewById<TextView>(R.id.eventDate).text = cardData.eventDate
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onEventCardClick(eventCards[position])
            }
        }
    }
}
