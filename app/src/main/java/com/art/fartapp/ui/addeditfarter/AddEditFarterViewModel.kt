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
class AddEditFarterViewModel @Inject constructor(
    private val farterDao: FarterDao,
    private val state: SavedStateHandle
) : ViewModel() {

    val farter = state.get<Farter>("farter")
    private val fartersEventChannel = Channel<FartersEvent>()
    val fartersEvent = fartersEventChannel.receiveAsFlow()

    var farterName = state.get<String>("farterName") ?: farter?.name ?: ""
        set(value) {
            field = value
            state.set("farterName", value)
        }

    fun onScannClick(name: String) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToEditFarterScreen(name))
    }

    fun onSaveClick() {
        if (farterName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }
        if (farter != null) {
            val updateFarter = farter.copy(name = farterName)
            updateFarter(updateFarter)
        }
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.ShowInvalidInputMessage(text))
    }

    private fun updateFarter(farter: Farter) = viewModelScope.launch {
        farterDao.update(farter)
        fartersEventChannel.send(FartersEvent.NavigateBackWithResult(EDIT_FARTER_RESULT_OK))
    }

    sealed class FartersEvent {
        data class ShowInvalidInputMessage(val msg: String): FartersEvent()
        data class NavigateToEditFarterScreen(val name: String) : FartersEvent()
        data class NavigateBackWithResult(val result: Int) : FartersEvent()
    }
}
