package com.example.tugasakhir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AnxietyLogViewModelFactory(private val dao: DataAccessObject) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnxietyLogViewModel::class.java)) {
            return AnxietyLogViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
