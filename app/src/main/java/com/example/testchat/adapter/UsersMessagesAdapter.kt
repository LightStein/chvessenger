package com.example.testchat.adapter
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.testchat.MessageActivity
import com.example.testchat.R
import com.example.testchat.model.FindFriends
import com.example.testchat.model.UsersMessages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.all_users_main_messages_layout.view.*
import se.simbio.encryption.Encryption

class UsersMessagesAdapter(private val mContext: Context, private val mUsersMessages: ArrayList<UsersMessages>) :
    RecyclerView.Adapter<UsersMessagesAdapter.UserMessageViewHolder>() {

    val encryption = Encryption.getDefault("(20skumsi02)", "reynolds1968", ByteArray(16))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserMessageViewHolder {
        val v = LayoutInflater.from(mContext).inflate(R.layout.all_users_main_messages_layout, parent, false)
        return UserMessageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mUsersMessages.size
    }

    override fun onBindViewHolder(holder: UserMessageViewHolder, position: Int) {
        val currentMessage: UsersMessages = mUsersMessages[position]

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val from = currentMessage.fromid
        val to = currentMessage.toid
        val type = currentMessage.type
        val message = currentMessage.message
        val status = currentMessage.status

        var currentProfile: FindFriends? = null
        var receiver: String? = null

        if (senderUid.equals(from)) {
            receiver = to
        } else {
            receiver = from
        }

        FirebaseDatabase.getInstance().reference.child("Users")
            .child(receiver)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild("profileimageUrl")) {

                        currentProfile = dataSnapshot.getValue<FindFriends>(FindFriends::class.java)
                        currentProfile?.mKey = receiver!!
                        val sendername = dataSnapshot.child("username").value.toString()
                        val senderImage = dataSnapshot.child("profileimageUrl").value.toString()

                        holder.textView_username.text = sendername
                        Picasso.get()
                            .load(senderImage)
                            .resize(40, 40)
                            .onlyScaleDown()
                            .centerCrop()
                            .into(holder.imageView_profile)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        holder.textView_message.text = encryption.decryptOrNull(message)

        if (status.equals("unseen")) {
            holder.textView_message.typeface = Typeface.DEFAULT_BOLD
        } else {
            holder.textView_message.typeface = Typeface.DEFAULT
        }

        holder.itemView.setOnClickListener {
            val latest_message_set_ref = "Latest-Messages/$senderUid/$receiver"

            FirebaseDatabase.getInstance().reference
                .child(latest_message_set_ref)
                .child("status")
                .setValue("seen")
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val newMessageIntent = Intent(mContext, MessageActivity::class.java)
                        newMessageIntent.putExtra("key", currentProfile)
                        mContext.startActivity(newMessageIntent)
                    }
                }
        }
    }

    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView_profile: ImageView = itemView.all_users_main_profile_image
        val textView_username: TextView = itemView.all_users_main_profile_name
        val textView_message: TextView = itemView.all_users_main_message
    }
}
