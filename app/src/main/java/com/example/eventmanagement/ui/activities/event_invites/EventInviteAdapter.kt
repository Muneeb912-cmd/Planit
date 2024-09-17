package com.example.eventmanagement.ui.activities.event_invites

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventInvites
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EventInviteAdapter(
    private var invites: List<EventInvites>,
    private val listener: EventCardClickListener
) : RecyclerView.Adapter<EventInviteAdapter.EventCardViewHolder>() {
    interface EventCardClickListener {
        fun onEventCardClick(invite: EventInvites)
        fun onAcceptIconClick(invite: EventInvites)
        fun onRejectIconClick(invite: EventInvites)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_invite, parent, false)
        return EventCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventCardViewHolder, position: Int) {
        val eventInvites = invites[position]
        holder.bind(eventInvites)
    }

    override fun getItemCount(): Int {
        return invites.size
    }

    private fun parseDateTimeAndFormatTime(dateTime: String): String {
        val inputFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a z", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        return date?.let { timeFormat.format(it) } ?: ""
    }

    inner class EventCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val acceptIcon: View = itemView.findViewById(R.id.acceptBtn)
        private val rejectIcon: View = itemView.findViewById(R.id.rejectBtn)

        @SuppressLint("SetTextI18n")
        fun bind(invite: EventInvites) {
            itemView.findViewById<TextView>(R.id.eventTitle).text = invite.eventTitle
            itemView.findViewById<TextView>(R.id.eventOrganizer).text = invite.eventOrganizer
            itemView.findViewById<TextView>(R.id.eventTiming).text = invite.eventTiming
            itemView.findViewById<TextView>(R.id.eventDate).text = invite.eventDate
            itemView.findViewById<TextView>(R.id.inviteStatus).text = invite.inviteStatus
            itemView.findViewById<TextView>(R.id.inviteSender).text =
                "Sent By: ${invite.senderName}"
            itemView.findViewById<TextView>(R.id.inviteTime).text = "Sent On: ${
                parseDateTimeAndFormatTime(invite.inviteTime.toString())
            }"

            when (invite.inviteStatus) {
                "accepted" -> {
                    acceptIcon.visibility = View.GONE
                    rejectIcon.visibility = View.GONE
                }

                "expired" -> {
                    acceptIcon.visibility = View.GONE
                    rejectIcon.visibility = View.GONE
                }

                else -> {
                    acceptIcon.visibility = View.VISIBLE
                    rejectIcon.visibility = View.VISIBLE
                }
            }

            itemView.setOnClickListener {
                listener.onEventCardClick(invite)
            }

            acceptIcon.setOnClickListener {
                listener.onAcceptIconClick(invite)
            }

            rejectIcon.setOnClickListener {
                listener.onRejectIconClick(invite)
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onEventCardClick(invites[position])
            }
        }
    }

    private fun formatTimestampToTime(timestamp: Timestamp): String {
        val date = Date(timestamp.seconds * 1000)
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun updateData(filteredEvents: List<EventInvites>) {
        invites = filteredEvents
        notifyDataSetChanged()
    }
}