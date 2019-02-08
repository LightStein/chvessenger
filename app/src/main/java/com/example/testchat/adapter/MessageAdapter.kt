package com.example.testchat.adapter

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.testchat.R
import com.example.testchat.model.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.all_messages_display_layout.view.*

class MessageAdapter(private val mContext: Context, private val mMessages: ArrayList<Messages>) :
        RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val v = LayoutInflater.from(mContext).inflate(R.layout.all_messages_display_layout, parent, false)
        return MessageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mMessages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage: Messages = mMessages[position]

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val receiverUid = currentMessage.receiverId
        val fromId = currentMessage.from
        val message = currentMessage.message

        FirebaseDatabase.getInstance().reference.child("Users")
                .child(senderUid!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild("profileimageUrl")) {
                            val senderImage = dataSnapshot.child("profileimageUrl").value!!.toString()
                            Picasso.get()
                                    .load(senderImage)
                                    .resize(40, 40)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .into(holder.imageView_sender)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

        FirebaseDatabase.getInstance().reference.child("Users")
                .child(receiverUid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild("profileimageUrl")) {
                            val receiverImage = dataSnapshot.child("profileimageUrl").value!!.toString()
                            Picasso.get()
                                    .load(receiverImage)
                                    .resize(40, 40)
                                    .onlyScaleDown()
                                    .centerCrop()
                                    .into(holder.imageView_receiver)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

        if (fromId.equals(senderUid)) {
            holder.cv_receiver.visibility = View.GONE
            holder.cv_sender.visibility = View.VISIBLE
            holder.textView_message_sender.visibility = View.VISIBLE
            holder.textView_message_receiver.visibility = View.GONE

            holder.textView_message_sender.text = message
        } else {
            holder.cv_receiver.visibility = View.VISIBLE
            holder.cv_sender.visibility = View.GONE
            holder.textView_message_sender.visibility = View.GONE
            holder.textView_message_receiver.visibility = View.VISIBLE

            holder.textView_message_receiver.text = message
        }

    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cv_receiver: CardView = itemView.cv_all_messages_receiver
        val cv_sender: CardView = itemView.cv_all_messages_sender
        val imageView_receiver: ImageView = itemView.imageView_all_messages_receiver_profile
        val imageView_sender: ImageView = itemView.imageView_all_messages_sender_profile
        val textView_message_receiver: TextView = itemView.textView_all_messages_receiver
        val textView_message_sender: TextView = itemView.textView_all_messages_sender
    }
}