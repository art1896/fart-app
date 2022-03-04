package com.art.fartapp.data.repository

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentRepository @Inject constructor(){
    private val _intent = MutableLiveData<Intent?>()

    val get: LiveData<Intent?> = Transformations.map(_intent) { it }

    fun set(intent: Intent?) { _intent.value = intent }
}