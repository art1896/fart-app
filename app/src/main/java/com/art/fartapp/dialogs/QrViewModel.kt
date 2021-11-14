package com.art.fartapp.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.art.fartapp.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrViewModel @Inject constructor(private val preferencesManager: PreferencesManager) :
    ViewModel() {


    fun updateToken(token: String) = viewModelScope.launch {
        preferencesManager.updateToken(token)
    }
}