package com.example.tugasakhir

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn


class HrvViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dataAccessObject()

    // LiveData untuk menyimpan nilai HRV
    val _hrvValue = MutableLiveData<Float>(0f)
    val hrvValue: LiveData<Float> get() = _hrvValue

    // LiveData untuk menyimpan list tingkatAnxiety
    private val _tingkatAnxietyList = MutableLiveData<List<TingkatAnxietyEntity>>()
    val tingkatAnxietyList: LiveData<List<TingkatAnxietyEntity>> get() = _tingkatAnxietyList

    // Fungsi untuk update nilai HRV
    fun updateHrvValue(value: Float) {
        _hrvValue.value = value
    }

    // Fungsi untuk mengamati tingkatAnxiety menggunakan Flow
    fun observeAnxietyLevels() {
        viewModelScope.launch {
            try {
                // Mengambil Flow dari DAO
                dao.getAllTingkatAnxiety() // Flow dari database
                    .flowOn(Dispatchers.IO) // Menjalankan Flow di thread IO
                    .collect { anxietyList ->
                        // Mengirim data ke LiveData
                        _tingkatAnxietyList.postValue(anxietyList)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Optional: Fungsi untuk memulai pengambilan data sekali saja saat ViewModel diinisialisasi
    init {
        observeAnxietyLevels() // Mulai mengamati data saat ViewModel pertama kali diinisialisasi
    }
}
