package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData

class MyEventCardAdapter(
    private val promotionCards: List<EventData>,
    private val listener: OnMyEventClickListener
) : RecyclerView.Adapter<MyEventCardAdapter.MyEventCardViewHolder>() {
    interface OnMyEventClickListener {
        fun OnMyEventCardClickListener(cardData: EventData)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyEventCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_event_view_card, parent, false)
        return MyEventCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyEventCardViewHolder, position: Int) {
        holder.bind(promotionCards[position])
    }

    override fun getItemCount(): Int {
        return promotionCards.size
    }

    inner class MyEventCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var currentCardData: EventData

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(cardData: EventData) {
            currentCardData = cardData
            itemView.findViewById<TextView>(R.id.eventTitleTv).text = cardData.eventTitle
            itemView.findViewById<TextView>(R.id.eventOrganizer).text = cardData.eventOrganizer
            itemView.findViewById<TextView>(R.id.eventTime).text = cardData.eventDate
            itemView.findViewById<TextView>(R.id.eventStatus).text = cardData.eventStatus
        }

        override fun onClick(v: View?) {
            listener.OnMyEventCardClickListener(currentCardData)
        }
    }
}
