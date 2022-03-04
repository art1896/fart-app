package com.art.fartapp.ui.addeditfarter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.art.fartapp.db.Farter
import com.art.fartapp.db.FarterDao
import com.art.fartapp.ui.EDIT_FARTER_RESULT_OK
import com.art.fartapp.ui.farters.FartersViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFarterViewModel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    private val fartersEventChannel = Channel<FartersEvent>()
    val fartersEvent = fartersEventChannel.receiveAsFlow()

    fun onScannClick(name: String) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToEditFarterScreen(name))
    }

    fun onPairWithNfcClick(name: String) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToNfcScreen(name))
    }


    sealed class FartersEvent {
        data class NavigateToEditFarterScreen(val name: String) : FartersEvent()
        data class NavigateToNfcScreen(val name: String): FartersEvent()
    }
}
