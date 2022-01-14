package com.art.fartapp.model

data class Data(
    val title: String,
    val body: String,
    val token: String?,
    val senderName: String?,
    val rawRes: String,
    val canSendBack: Boolean
)