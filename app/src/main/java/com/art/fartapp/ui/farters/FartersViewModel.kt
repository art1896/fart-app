package com.art.fartapp.ui.farters

import androidx.lifecycle.*
import com.art.fartapp.data.PreferencesManager
import com.art.fartapp.data.SortOrder
import com.art.fartapp.data.repository.FirebaseRepository
import com.art.fartapp.db.Farter
import com.art.fartapp.db.FarterDao
import com.art.fartapp.ui.EDIT_FARTER_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FartersViewModel @Inject constructor(
    private val farterDao: FarterDao,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = this.state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val fartersEventChannel = Channel<FartersEvent>()
    val fartersEvent = fartersEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        farterDao.getFarters(query, filterPreferences.sortOrder)
    }

    val farters = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }


    fun onFarterSwiped(farter: Farter) = viewModelScope.launch {
        farterDao.deleteFarter(farter)
        fartersEventChannel.send(FartersEvent.ShowUndoDeleteFarterMessage(farter))
    }

    fun onUndoDeletedClick(farter: Farter) = viewModelScope.launch {
        farterDao.insertFarter(farter)
    }

    fun onAddNewFarterClick() = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToAddFarterScreen)
    }

    fun onEditFarterClick(farter: Farter) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToEditFarterScreen(farter))
    }

    fun onAddEditResult(result: Int) {
        when(result) {
            EDIT_FARTER_RESULT_OK -> showFarterSavedConfirmationMessage("Farter updated")
        }
    }

    fun onQrClick() = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToQrScreen(preferencesFlow.first().token))
    }

    fun showFarterSavedConfirmationMessage(msg: String) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.ShowFarterSavedConfirmationMessage(msg))
    }

    fun onDeleteAllFartersClick() = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigatetoDeleteAllFartersScreen)
    }

    fun onSendFartClick(farter: Farter) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToSendFartScreen(farter))
    }

    sealed class FartersEvent {
        object NavigateToAddFarterScreen : FartersEvent()
        data class NavigateToEditFarterScreen(val farter: Farter) : FartersEvent()
        data class ShowUndoDeleteFarterMessage(val farter: Farter) : FartersEvent()
        data class ShowFarterSavedConfirmationMessage(val msg: String) : FartersEvent()
        object NavigatetoDeleteAllFartersScreen: FartersEvent()
        data class NavigateToSendFartScreen(val farter: Farter): FartersEvent()
        data class NavigateToQrScreen(val token: String?): FartersEvent()
    }


}

