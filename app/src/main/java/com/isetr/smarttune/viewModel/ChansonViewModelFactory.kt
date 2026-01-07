package com.isetr.smarttune.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.isetr.smarttune.data.ChansonRepository

class ChansonViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChansonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChansonViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

