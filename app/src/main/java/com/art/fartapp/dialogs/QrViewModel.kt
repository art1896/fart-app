package com.art.fartapp.dialogs

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.art.fartapp.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrViewModel @Inject constructor(private val preferencesManager: PreferencesManager) :
    ViewModel() {

    private val qrEventsChannel = Channel<QrEvents>()
    val qrEvents = qrEventsChannel.receiveAsFlow()

    fun updateToken(token: String) = viewModelScope.launch {
        preferencesManager.updateToken(token)
    }

    fun onShareQrClick(qr: Bitmap?) = viewModelScope.launch {
        qrEventsChannel.send(QrEvents.ShareQr(qr))
    }

    fun onNfcClick() = viewModelScope.launch {
        qrEventsChannel.send(QrEvents.OpenNfcDialog)
    }


    sealed class QrEvents {
        data class ShareQr(val qr: Bitmap?): QrEvents()
        object OpenNfcDialog: QrEvents()
    }

}