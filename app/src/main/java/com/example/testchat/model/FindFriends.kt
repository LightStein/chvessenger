package com.example.testchat.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class FindFriends(
        val username: String = "",
        val profileimageUrl: String = "",   //must be same as in firebase database else picasso crashes
        val fullname: String = "",
        val dob: String = "",
        val countryname: String = "",
        val gender: String = "",
        val status: String = "",
        val relationshipstatus: String = "",
        @Exclude var mKey: String = ""
) : Parcelable