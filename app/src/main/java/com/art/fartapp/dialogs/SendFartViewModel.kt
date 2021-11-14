package com.art.fartapp.dialogs

import android.util.Log
import androidx.lifecycle.ViewModel
import com.art.fartapp.data.repository.FirebaseRepository
import com.art.fartapp.db.Farter
import com.art.fartapp.db.FarterDao
import com.art.fartapp.di.ApplicationScope
import com.art.fartapp.model.Data
import com.art.fartapp.model.FcmData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendFartViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
): ViewModel() {

    fun onConfirmClick(farter: Farter) = applicationScope.launch {
        repository.sendFart(FcmData(Data("Fart", "You received a FART"), farter.token))
    }
}