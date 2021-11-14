package com.art.fartapp.data.network

import com.art.fartapp.BuildConfig
import com.art.fartapp.data.response.FcmResponse
import com.art.fartapp.model.FcmData
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Api {


    companion object {
        const val BASE_URL = "https://fcm.googleapis.com/"
        const val FIREBASE_KEY = BuildConfig.FIREBASE_SERVER_KEY
    }

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=$FIREBASE_KEY"
    )
    @POST("fcm/send")
    suspend fun sendNotification(@Body body: FcmData): FcmResponse

}