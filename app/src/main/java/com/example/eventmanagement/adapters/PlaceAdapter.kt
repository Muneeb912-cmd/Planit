package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.example.eventmanagement.R

class PlaceAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(placeId: String, placeName: String)
    }

    private var places: List<AutocompletePrediction> = listOf()

    fun submitList(newPlaces: List<AutocompletePrediction>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.placeName.text = place.getPrimaryText(null).toString()

        holder.itemView.setOnClickListener {
            listener.onItemClick(place.placeId, place.getPrimaryText(null).toString())
        }
    }

    override fun getItemCount() = places.size

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.place_name)
    }
}
