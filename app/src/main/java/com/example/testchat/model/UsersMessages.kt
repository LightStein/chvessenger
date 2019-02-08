package com.example.testchat.model

data class UsersMessages(
        val message: String = "",
        val type: String = "",
        val fromid: String = "",
        val toid: String = "",
        val status: String = ""
)