package com.example.eventmanagement.ui.activities.manage_invites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.models.User

class ManageInviteAdapter(
    private var users: List<User.UserData>,
    private var currentEventInvitedUsers: List<User.UserData>,
    private var invites: List<Invites>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ManageInviteAdapter.EventViewHolder>() {

    interface OnItemClickListener {
        fun onSendInviteButtonClick(userData: User.UserData, key: String)
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var currentUser: User.UserData
        private val sendInviteBtn = itemView.findViewById<TextView>(R.id.sendInviteBtn)
        private lateinit var key: String

        init {
            sendInviteBtn.setOnClickListener {
                listener.onSendInviteButtonClick(currentUser, key)
            }
        }

        fun bind(user: User.UserData) {
            currentUser = user
            itemView.findViewById<TextView>(R.id.userName).text = user.userName
            itemView.findViewById<TextView>(R.id.userEmail).text = user.userEmail
            itemView.findViewById<TextView>(R.id.userPhone).text = user.userPhone

            // Assuming invites is a list of Invite objects, filter for the current user's invite
            val currentUserInvite = invites.find { invite -> invite.receiverId == user.userId }
            val inviteStatus = currentUserInvite?.inviteStatus

            when (inviteStatus) {
                "pending" -> {
                    key = "un-send"
                    sendInviteBtn.text = "Un-send Invite"
                    sendInviteBtn.isEnabled = true
                    sendInviteBtn.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_delete,
                        0,
                        0,
                        0
                    )
                    sendInviteBtn.textDirection = View.TEXT_DIRECTION_LTR
                }

                "accepted" -> {
                    sendInviteBtn.text = "Invite Accepted"
                    sendInviteBtn.isEnabled = false
                    sendInviteBtn.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                    ) // No icon for accepted
                }

                "rejected" -> {
                    key = "re-send"
                    sendInviteBtn.text = "Re-send Invite"
                    sendInviteBtn.isEnabled = true
                    sendInviteBtn.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_send,
                        0,
                        0,
                        0
                    )
                    sendInviteBtn.textDirection = View.TEXT_DIRECTION_LTR
                }

                else -> {
                    // If there's no invite or it's a new invite, default behavior
                    key = "send"
                    sendInviteBtn.text = "Send Invite"
                    sendInviteBtn.isEnabled = true
                    sendInviteBtn.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_send,
                        0,
                        0,
                        0
                    )
                    sendInviteBtn.textDirection = View.TEXT_DIRECTION_LTR
                }
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

    fun updatedInvites(newInvited: List<Invites>) {
        invites = newInvited
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
