package com.art.fartapp.ui.farters

import androidx.lifecycle.*
import com.art.fartapp.data.PreferencesManager
import com.art.fartapp.data.SortOrder
import com.art.fartapp.db.Farter
import com.art.fartapp.db.FarterDao
import com.art.fartapp.ui.EDIT_FARTER_RESULT_OK
import com.art.fartapp.util.ConnectivityManager
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
    private val state: SavedStateHandle,
    private val connectivityManager: ConnectivityManager
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

    private val acceptedFartsFlow = farterDao.getAcceptedFarts()

    val farters = taskFlow.asLiveData()

    val acceptedFarts = acceptedFartsFlow.asLiveData()

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


    fun onAddEditResult(result: Int) {
        when (result) {
            EDIT_FARTER_RESULT_OK -> showFarterSavedConfirmationMessage("Farter updated")
        }
    }

    fun onQrClick() = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToQrScreen(preferencesFlow.first().token))
    }

    fun onUserGuideClick() = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigateToUserGuideScreen)
    }

    private fun showFarterSavedConfirmationMessage(msg: String) = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.ShowFarterSavedConfirmationMessage(msg))
    }

    fun onDeleteAllFartersClick() = viewModelScope.launch {
        fartersEventChannel.send(FartersEvent.NavigatetoDeleteAllFartersScreen)
    }

    fun onSendFartClick(farter: Farter) = viewModelScope.launch {
        if (connectivityManager.isNetworkAvailable.value) {
            fartersEventChannel.send(FartersEvent.OpenBottomSheet(farter))
        } else {
            fartersEventChannel.send(FartersEvent.ShowNoInternetConnectionMessage)
        }
    }

    fun updateIsGuideShowed(showed: Boolean) = viewModelScope.launch {
        preferencesManager.updateGuideShowed(showed)
    }


    sealed class FartersEvent {
        object NavigateToAddFarterScreen : FartersEvent()
        data class ShowUndoDeleteFarterMessage(val farter: Farter) : FartersEvent()
        data class ShowFarterSavedConfirmationMessage(val msg: String) : FartersEvent()
        object NavigatetoDeleteAllFartersScreen : FartersEvent()
        data class NavigateToSendFartScreen(val farter: Farter) : FartersEvent()
        data class NavigateToQrScreen(val token: String?) : FartersEvent()
        object ShowNoInternetConnectionMessage : FartersEvent()
        object NavigateToUserGuideScreen : FartersViewModel.FartersEvent()
        data class OpenBottomSheet(val farter: Farter) : FartersViewModel.FartersEvent()
    }


}

