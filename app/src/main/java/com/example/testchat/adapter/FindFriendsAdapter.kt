package com.example.testchat.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.testchat.MessageActivity
import com.example.testchat.R
import com.example.testchat.model.FindFriends
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.all_users_display_layout.view.*

class FindFriendsAdapter(private val mContext: Context, private val mFriends: List<FindFriends>) :
    RecyclerView.Adapter<FindFriendsAdapter.FindViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindViewHolder {
        val v = LayoutInflater.from(mContext).inflate(R.layout.all_users_display_layout, parent, false)
        return FindViewHolder(v)
    }

    override fun onBindViewHolder(holder: FindViewHolder, position: Int) {
        val currentProfile: FindFriends = mFriends[position]

        val username: String
        val status: String
        val profileImageUrl: String

        username = currentProfile.username
        status = currentProfile.status
        profileImageUrl = currentProfile.profileimageUrl

        holder.username.text = username
        holder.profileStatus.text = status
        Picasso.get().load(profileImageUrl)
            .resize(90, 90)
            .onlyScaleDown()
            .centerCrop()
            .placeholder(R.mipmap.ic_launcher)
            .into(holder.profileImage)

        holder.itemView.setOnClickListener {
            Toast.makeText(mContext, "არჩეულია " + username, Toast.LENGTH_SHORT).show()
            val newMessageIntent = Intent(mContext, MessageActivity::class.java)
            newMessageIntent.putExtra("key", currentProfile)
            mContext.startActivity(newMessageIntent)
        }

    }

    override fun getItemCount(): Int {
        return mFriends.size
    }

    class FindViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.all_users_profile_name
        val profileStatus: TextView = itemView.all_users_status
        val profileImage: ImageView = itemView.all_users_profile_image
    }
}