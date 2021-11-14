package com.art.fartapp.model

data class FcmData(
    val data: Data,
    val to: String,
    val priority: String = "high"
)