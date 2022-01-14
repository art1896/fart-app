package com.art.fartapp.dialogs

import androidx.lifecycle.ViewModel
import com.art.fartapp.data.PreferencesManager
import com.art.fartapp.data.repository.FirebaseRepository
import com.art.fartapp.di.ApplicationScope
import com.art.fartapp.model.Data
import com.art.fartapp.model.FcmData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendBackViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
    preferencesManager: PreferencesManager
) :
    ViewModel() {

    private val preferencesFlow = preferencesManager.preferencesFlow

    fun sendBack(farterToken: String, rawRes: String) = applicationScope.launch {
        repository.sendFart(
            FcmData(
                Data(
                    "Fart",
                    "You received a FART",
                    preferencesFlow.first().token,
                    null,
                    rawRes,
                    false
                ), farterToken
            )
        )
    }
}