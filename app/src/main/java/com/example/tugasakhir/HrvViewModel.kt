package com.example.tugasakhir

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HrvViewModel(application: Application) : AndroidViewModel(application) {

    // Instance dari DAO untuk akses ke database
    private val dao = AppDatabase.getDatabase(application).dataAccessObject()

    // LiveData untuk menyimpan nilai HRV
    val _hrvValue = MutableLiveData<Float>(0f)  // Store as Float
    val hrvValue: LiveData<Float> get() = _hrvValue

    // LiveData untuk menyimpan list tingkatAnxiety
    private val _tingkatAnxietyList = MutableLiveData<List<TingkatAnxietyEntity>>()
    val tingkatAnxietyList: LiveData<List<TingkatAnxietyEntity>> get() = _tingkatAnxietyList

    // Fungsi untuk update nilai HRV
    fun updateHrvValue(value: Float) {
        _hrvValue.value = value
    }

    // Fungsi untuk mengambil data tingkatAnxiety dari database
    fun loadTingkatAnxiety() {
        viewModelScope.launch {
            try {
                val data = dao.getAllTingkatAnxiety()
                _tingkatAnxietyList.postValue(data)
            } catch (e: Exception) {
                // Log error atau tangani kesalahan
                Log.i("HrvViewModel", "error load tingkatAnxiety")
            }
        }
    }

}
