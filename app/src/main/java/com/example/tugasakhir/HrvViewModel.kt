// HrvViewModel.kt
package com.example.tugasakhir

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class HrvViewModel : ViewModel() {
    // Use LiveData to observe changes
    private val _hrvValue = MutableLiveData<String>("HRV: Waiting...")
    val hrvValue: LiveData<String> get() = _hrvValue

    // Function to update the HRV value
    fun updateHrvValue(value: String) {
        _hrvValue.value = value
    }
}
