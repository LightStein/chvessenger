package com.example.testchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.example.testchat.adapter.UsersMessagesAdapter
import com.example.testchat.model.UsersMessages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var currentUser: FirebaseUser? = null
    var mUMArrayList: ArrayList<UsersMessages> = ArrayList()
    var mUMAdapter: UsersMessagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentUser = FirebaseAuth.getInstance().currentUser

        checkUserLoginStatus()

        recyclerView_main_messages.setHasFixedSize(true)
        val layoutmanager = LinearLayoutManager(this)
        recyclerView_main_messages.layoutManager = layoutmanager

        mUMAdapter = UsersMessagesAdapter(this, mUMArrayList)
        recyclerView_main_messages.adapter = mUMAdapter

    }

    override fun onResume() {
        super.onResume()
        fetchUsersAndMessages()
    }

    private fun checkUserLoginStatus() {
        if (currentUser?.uid == null) {
            SendUserToLoginActivity()
        } else {
            FetchUserInfo()
        }
    }

    fun showPopupSetting(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.nav_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_find_friends -> {
                    SendUserToFindFriendsActivity()
                }
                R.id.menu_log_out -> {
                    FirebaseAuth.getInstance().signOut()
                    SendUserToLoginActivity()
                }
            }
            true
        }
    }

    private fun SendUserToFindFriendsActivity() {
        val findFriendIntent = Intent(this@MainActivity, FindFriendsActivity::class.java)
        startActivity(findFriendIntent)
    }

    private fun SendUserToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
        finish()
    }

    private fun fetchUsersAndMessages() {
        val senderUid = currentUser?.uid
        if (senderUid != null) {
            val latestRef = FirebaseDatabase.getInstance().reference
                .child("Latest-Messages")

            latestRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChild(senderUid)) {
                        recyclerView_main_messages.visibility = View.VISIBLE
                        ll_main_no_messages.visibility = View.GONE
                    } else {
                        recyclerView_main_messages.visibility = View.GONE
                        ll_main_no_messages.visibility = View.VISIBLE
                    }
                }

            })

            latestRef.child(senderUid)
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                        mUMArrayList.clear()
                        val latestmessage = p0.getValue(UsersMessages::class.java) ?: return
                        mUMArrayList.add(latestmessage)
                        mUMAdapter!!.notifyDataSetChanged()
                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                        mUMArrayList.clear()
                        val latestmessage = p0.getValue(UsersMessages::class.java) ?: return
                        mUMArrayList.add(latestmessage)
                        mUMAdapter!!.notifyDataSetChanged()
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@MainActivity, "onCancelled: no message", Toast.LENGTH_SHORT).show()
                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                    }
                })
            /*.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    mUMArrayList.clear()
                    for (singleSnapShot: DataSnapshot in p0.children) {
                        val latestmessage = singleSnapShot.getValue(UsersMessages::class.java)
                        mUMArrayList.add(latestmessage!!)
                    }

                    mUMAdapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(p0: DatabaseError?) {
                }
            })*/
        }
    }

    private fun FetchUserInfo() {
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
//                override fun onCancelled(p0: DatabaseError?) {
//                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        Picasso.get().load(p0.child("profileimageUrl").value.toString())
                            .resize(40,40)
                            .onlyScaleDown()
                            .centerCrop()
                            .into(imageView_main_userprofile)
                    }
                }

            })
    }
}
