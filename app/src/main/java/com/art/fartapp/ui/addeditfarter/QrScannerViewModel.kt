package com.art.fartapp.ui.addeditfarter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.art.fartapp.db.Farter
import com.art.fartapp.db.FarterDao
import com.art.fartapp.ui.farters.FartersViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor(private val farterDao: FarterDao) : ViewModel() {

    private val fartersEventChannel = Channel<FartersEvent>()
    val fartersEvent = fartersEventChannel.receiveAsFlow()

    fun insertFarter(farter: Farter) = viewModelScope.launch {
        farterDao.insertFarter(farter)
    }

    fun onHandleResult(farter: Farter) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToFartersFragment(farter))
    }

    sealed class FartersEvent {
        data class NavigateToFartersFragment(val farter: Farter) : FartersEvent()
    }
}