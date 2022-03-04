package com.art.fartapp.ui.addeditfarter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.art.fartapp.data.repository.IntentRepository
import com.art.fartapp.db.Farter
import com.art.fartapp.db.FarterDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (intentRepository: IntentRepository, private val farterDao: FarterDao) : ViewModel() {
    val intent = intentRepository


    fun insertFarter(farter: Farter) = viewModelScope.launch {
        farterDao.insertFarter(farter)
    }
}