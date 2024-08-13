package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.CardData

class CarousalAdapter(private val cardList: List<CardData>) :
    RecyclerView.Adapter<CarousalAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousal_item, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val event = cardList[position]
        holder.itemView.findViewById<TextView>(R.id.eventTitle).text = event.eventTitle
        holder.itemView.findViewById<TextView>(R.id.eventOrganizer).text = event.eventOrganizer
        holder.itemView.findViewById<TextView>(R.id.eventTime).text = event.eventTiming
        holder.itemView.findViewById<TextView>(R.id.eventCategory).text = event.eventCategory
        holder.itemView.findViewById<TextView>(R.id.eventDescription).text = event.eventDescription
        holder.itemView.findViewById<TextView>(R.id.eventLocation).text = event.eventLocation
    }

    override fun getItemCount() = cardList.size
}
