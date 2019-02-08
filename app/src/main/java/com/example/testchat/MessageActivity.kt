package com.example.testchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.example.testchat.adapter.MessageAdapter
import com.example.testchat.model.FindFriends
import com.example.testchat.model.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.*
import java.text.SimpleDateFormat
import java.util.*
import se.simbio.encryption.Encryption

class MessageActivity : AppCompatActivity() {
    companion object {
        val TAG = "MESSAGEACTIVITY"
    }

    val encryption = Encryption.getDefault("(20skumsi02)", "reynolds1968", ByteArray(16))

    var mMessagesArrayList: ArrayList<Messages> = ArrayList()
    var mMessageAdapter: MessageAdapter? = null

    private var senderUid: String? = null
    private var receiverUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val receiver: FindFriends = intent.getParcelableExtra("key")

        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        receiverUid = receiver.mKey
        //fetchReceiverInfo()
        setReceiverInfo(receiver)

        mMessageAdapter = MessageAdapter(this@MessageActivity, mMessagesArrayList)
        recyclerView_message_list.setHasFixedSize(true)
        val linearLayout = LinearLayoutManager(this)
        linearLayout.stackFromEnd = true
        linearLayout.reverseLayout = false
        recyclerView_message_list.layoutManager = linearLayout
        recyclerView_message_list.adapter = mMessageAdapter

        fetchMessages()

        imageButton_message_send.setOnClickListener {
            val messageText = editText_message_message.text.toString()
            if (messageText.trim().isEmpty()) {
                Snackbar.make(it, "ჯერ ტექსტი დაწერე", Snackbar.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "SendMessage():::  Sending message...")
                SendMessage(encryption.encryptOrNull(messageText))
            }
        }

        imageView_message_back.setOnClickListener {
            SendUserToMainActivity()
        }
    }

    fun showPopupSetting(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.menu_user, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_view_user_profile -> {
                }
            }
            true
        }
    }

    private fun setReceiverInfo(receiver: FindFriends) {
        textView_message_receivername.text = receiver.username
        Picasso.get().load(receiver.profileimageUrl)
            .resize(40, 40)
            .onlyScaleDown()
            .centerCrop()
            .placeholder(R.drawable.ic_person_24dp)
            .into(imageView_message_receiverprofile)
    }

    /* private fun fetchReceiverInfo() {
         FirebaseDatabase.getInstance().reference
                 .child("Users")
                 .child(receiverUid)
                 .addListenerForSingleValueEvent(object : ValueEventListener {
                     override fun onDataChange(p0: DataSnapshot) {
                         if (p0.exists()) {
                             supportActionBar?.title = p0.child("username").value.toString()
                         }
                     }

                     override fun onCancelled(p0: DatabaseError) {
                         supportActionBar?.title = "No User"
                     }
                 })
     }*/

    private fun fetchMessages() {
        FirebaseDatabase.getInstance().reference
            .child("Messages")
            .child(senderUid!!)
            .child(receiverUid!!)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    if (dataSnapshot.exists()) {
                        //  mChatsArrayList.clear();    //don't use since this is onChildAdded() eventListener
                        val messages = dataSnapshot.getValue(Messages::class.java)
                        messages!!.receiverId = receiverUid!!
                        var tempMessages: Messages = messages
                        tempMessages.message = encryption.decryptOrNull(tempMessages.message)
                        mMessagesArrayList.add(tempMessages)

                        mMessageAdapter!!.notifyDataSetChanged()
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
    }


    private fun SendMessage(messageText: String) {
        val message_send_ref = "Messages/$senderUid/$receiverUid"
        val latest_message_send_ref = "Latest-Messages/$senderUid/$receiverUid"
        val message_receive_ref = "Messages/$receiverUid/$senderUid"
        val latestmessage_receive_ref = "Latest-Messages/$receiverUid/$senderUid"

        //CREATE A UNIQUE KEY FOR THE MESSAGES
        val user_message_key = FirebaseDatabase.getInstance().reference
            .child("Messages")
            .child(senderUid!!)
            .child(receiverUid!!)
            .push()
        val message_push_id = user_message_key.key

        val calForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MMMM-yyyy")
        val saveCurrentDate = currentDate.format(calForDate.time)

        val calForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm aa")
        val saveCurrentTime = currentTime.format(calForTime.time)

        //this is for adding value
        val messageTextBody = HashMap<String, Any>()
        messageTextBody.put("message", messageText)
        messageTextBody.put("time", saveCurrentTime)
        messageTextBody.put("date", saveCurrentDate)
        messageTextBody.put("type", "text")
        messageTextBody.put("from", senderUid!!)

        val latestsendmessageTextBody = HashMap<String, Any>()
        latestsendmessageTextBody.put("message", messageText)
        latestsendmessageTextBody.put("type", "text")
        latestsendmessageTextBody.put("fromid", senderUid!!)
        latestsendmessageTextBody.put("status", "seen")
        latestsendmessageTextBody.put("toid", receiverUid!!)

        val latestreceivemessageTextBody = HashMap<String, Any>()
        latestreceivemessageTextBody.put("message", messageText)
        latestreceivemessageTextBody.put("type", "text")
        latestreceivemessageTextBody.put("fromid", senderUid!!)
        latestreceivemessageTextBody.put("status", "unseen")
        latestreceivemessageTextBody.put("toid", receiverUid!!)

        //this is for nodes in firebase
        val messageBodyDetails = HashMap<String, Any>()
        messageBodyDetails.put(message_send_ref + "/" + message_push_id, messageTextBody)
        messageBodyDetails.put(message_receive_ref + "/" + message_push_id, messageTextBody)
        messageBodyDetails.put(latest_message_send_ref, latestsendmessageTextBody)
        messageBodyDetails.put(latestmessage_receive_ref, latestreceivemessageTextBody)

        FirebaseDatabase.getInstance().reference
            .updateChildren(messageBodyDetails)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    editText_message_message.text.clear()
//                    Toast.makeText(this@MessageActivity, "Message Sent", Toast.LENGTH_SHORT).show()
                    recyclerView_message_list.scrollToPosition(mMessageAdapter!!.itemCount - 1)
                } else {
                    val message = it.exception!!.message
                    Toast.makeText(this@MessageActivity, "Error sending: /n" + message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@MessageActivity, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(mainIntent)
        finish()
    }
}
