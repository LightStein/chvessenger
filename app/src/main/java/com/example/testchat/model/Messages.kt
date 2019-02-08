package com.example.testchat.model

import com.google.firebase.database.Exclude

data class Messages(
        var message: String = "",
        val type: String = "",
        val from: String = "",
        val date: String = "",
        val time: String = "",
        @Exclude var receiverId: String = ""
)
