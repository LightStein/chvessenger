package com.example.testchat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.example.testchat.adapter.FindFriendsAdapter
import com.example.testchat.model.FindFriends
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_find_friends.*

class FindFriendsActivity : AppCompatActivity() {

    var mFriendArrayList: ArrayList<FindFriends> = ArrayList()
    var mAdapter: FindFriendsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
//        supportActionBar?.title = "Find Friends"

        recyclerView_find_friends_list.setHasFixedSize(true)
        recyclerView_find_friends_list.layoutManager = LinearLayoutManager(this)

        mAdapter = FindFriendsAdapter(this, mFriendArrayList)
        recyclerView_find_friends_list.adapter = mAdapter

        fetchFriends()

    }

    private fun fetchFriends() {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("Users")
        val currentuserid = FirebaseAuth.getInstance().currentUser?.uid

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mFriendArrayList.clear()

                for (findSnapshot: DataSnapshot in dataSnapshot.children) {
                    if (!((findSnapshot.key).equals(currentuserid))) {
                        val findFriends = findSnapshot.getValue<FindFriends>(FindFriends::class.java)
                        findFriends!!.mKey = findSnapshot.key!!
                        mFriendArrayList.add(findFriends)
                    }
                }
                mAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@FindFriendsActivity, "Find Friends Error: \n" + databaseError, Toast.LENGTH_SHORT).show()
            }
        })
    }

}
