package com.art.fartapp.ui.addeditfarter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.art.fartapp.db.Farter
import com.art.fartapp.db.FarterDao
import com.art.fartapp.ui.EDIT_FARTER_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val farterDao: FarterDao,
    private val state: SavedStateHandle
) : ViewModel() {

    val farter = state.get<Farter>("farter")
    private val editEventChannel = Channel<EditEvent>()
    val editEvent = editEventChannel.receiveAsFlow()
    var farterName = state.get<String>("farterName") ?: farter?.name ?: ""
        set(value) {
            field = value
            state.set("farterName", value)
        }

    fun onSaveClick() {
        if (farterName.isBlank()) {
            showInvalidInputMessage()
            return
        }
        if (farter != null) {
            val updateFarter = farter.copy(name = farterName)
            updateFarter(updateFarter)
        }
    }

    private fun showInvalidInputMessage() = viewModelScope.launch {
        editEventChannel.send(EditEvent.ShowInvalidInputMessage)
    }

    private fun updateFarter(farter: Farter) = viewModelScope.launch {
        farterDao.update(farter)
        editEventChannel.send(EditEvent.NavigateToFartersFragment(EDIT_FARTER_RESULT_OK))
    }

    sealed class EditEvent {
        object ShowInvalidInputMessage: EditEvent()
        data class NavigateToFartersFragment(val result: Int) : EditEvent()
    }
}
