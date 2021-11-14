package com.art.fartapp.data.repository

import com.art.fartapp.data.network.Api
import com.art.fartapp.model.FcmData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(private val api: Api) {

    suspend fun sendFart(body: FcmData) = api.sendNotification(body)
}