package com.art.fartapp.dialogs.bottomsheet

import androidx.lifecycle.*
import com.art.fartapp.data.PreferencesManager
import com.art.fartapp.data.repository.FirebaseRepository
import com.art.fartapp.db.Farter
import com.art.fartapp.db.Sound
import com.art.fartapp.db.SoundDao
import com.art.fartapp.di.ApplicationScope
import com.art.fartapp.model.Data
import com.art.fartapp.model.FcmData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BottomSheetViewModel @Inject constructor(
    private val soundDao: SoundDao,
    private val state: SavedStateHandle,
    private val repository: FirebaseRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
    preferencesManager: PreferencesManager
) : ViewModel() {

    val sounds = soundDao.getAllSounds().asLiveData()

    val selectedSound: MutableLiveData<Sound> = this.state.getLiveData("selectedSound", null)

    private val preferencesFlow = preferencesManager.preferencesFlow

    private val bottomSheetFartersEventChannel = Channel<BottomSheetFartersEvent>()
    val bottomSheetFartersEvent = bottomSheetFartersEventChannel.receiveAsFlow()

    fun onSendFartClick(farter: Farter, canSendBack: Boolean, rawRes: String, senderName: String?) =
        applicationScope.launch {
            repository.sendFart(
                FcmData(
                    Data(
                        title = "Fart",
                        body = "You received a FART",
                        token = preferencesFlow.first().token,
                        canSendBack = canSendBack,
                        rawRes = rawRes,
                        senderName = senderName
                    ), farter.token
                )
            )
            delay(1000)
            bottomSheetFartersEventChannel.send(
                BottomSheetFartersEvent.NavigateToHomeScreen
            )
        }

    fun onEditFarterClick(farter: Farter) = viewModelScope.launch {
        bottomSheetFartersEventChannel.send(
            BottomSheetFartersEvent.NavigateToEditFarterScreen(
                farter
            )
        )
    }


    sealed class BottomSheetFartersEvent {
        data class NavigateToEditFarterScreen(val farter: Farter) : BottomSheetFartersEvent()
        object NavigateToHomeScreen : BottomSheetFartersEvent()
    }


}