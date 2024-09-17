package com.example.eventmanagement.ui.fragments.usermanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eventmanagement.R
import com.example.eventmanagement.models.User
import com.google.android.material.imageview.ShapeableImageView

class UserDataAdapter(
    private var users: List<User.UserData>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<UserDataAdapter.EventViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(userData: User.UserData)
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var currentUser: User.UserData

        init {
            itemView.setOnClickListener {
                listener.onItemClick(currentUser)
            }
        }

        fun bind(user: User.UserData) {
            currentUser = user
            itemView.findViewById<TextView>(R.id.userName).text = user.userName
            itemView.findViewById<TextView>(R.id.userEmail).text = user.userEmail
            itemView.findViewById<TextView>(R.id.userPhone).text = user.userPhone

            val userImageView = itemView.findViewById<ShapeableImageView>(R.id.userProfileImg)

            Glide.with(itemView.context)
                .load(user.userImg)
                .placeholder(R.drawable.ic_avatar_male)
                .error(R.drawable.ic_placeholder)
                .into(userImageView)

        }
    }

    fun updatedUsersList(newUsersList: List<User.UserData>) {
        users = newUsersList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_card, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size
}
