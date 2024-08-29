package com.example.eventmanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.models.User

class ManageInviteAdapter(
    private var users: List<User.UserData>,
    private var currentEventInvitedUsers:List<User.UserData>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ManageInviteAdapter.EventViewHolder>() {

    interface OnItemClickListener {
        fun onSendInviteButtonClick(userData: User.UserData,key:String)
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var currentUser: User.UserData
        private val sendInviteBtn = itemView.findViewById<TextView>(R.id.sendInviteBtn)
        private lateinit var key:String
        init {
            sendInviteBtn.setOnClickListener {
                listener.onSendInviteButtonClick(currentUser,key)
            }
        }

        fun bind(user: User.UserData) {
            currentUser = user
            itemView.findViewById<TextView>(R.id.userName).text = user.userName
            itemView.findViewById<TextView>(R.id.userEmail).text = user.userEmail
            itemView.findViewById<TextView>(R.id.userPhone).text = user.userPhone

            val isInvited = currentEventInvitedUsers.any { it.userId == user.userId }
            key=if (isInvited){
                "un-send"
            }else{
                "send"
            }
            sendInviteBtn.text = if (isInvited) "Un-send Invite" else "Send Invite"
            if (isInvited) {
                sendInviteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0)
                sendInviteBtn.textDirection=View.TEXT_DIRECTION_LTR
            }else{
                sendInviteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_send, 0, 0, 0)
                sendInviteBtn.textDirection=View.TEXT_DIRECTION_LTR

            }
        }
    }


    fun updatedUsersList(newUsersList: List<User.UserData>) {
        users = newUsersList
        notifyDataSetChanged()
    }

    fun updatedCurrentEventInvites(newCurrentEventInvitedUsers: List<User.UserData>) {
        currentEventInvitedUsers = newCurrentEventInvitedUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.invite_user_card, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size
}
