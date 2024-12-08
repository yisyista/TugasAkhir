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
import kotlinx.coroutines.withContext


class HrvViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dataAccessObject()

    // LiveData untuk menyimpan nilai HRV
    val _hrvValue = MutableLiveData<Float>(0f)
    val hrvValue: LiveData<Float> get() = _hrvValue

    // LiveData untuk menyimpan list tingkatAnxiety
    private val _tingkatAnxietyList = MutableLiveData<List<TingkatAnxietyEntity>>()
    val tingkatAnxietyList: LiveData<List<TingkatAnxietyEntity>> get() = _tingkatAnxietyList

    // LiveData untuk menyimpan rata-rata anxiety per jam
    private val _averageAnxietyPerHour = MutableLiveData<List<AverageAnxietyData>>()
    val averageAnxietyPerHour: LiveData<List<AverageAnxietyData>> get() = _averageAnxietyPerHour

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
                        Log.d("HrvViewModel", "Tingkat Anxiety Data: $anxietyList")
                        _tingkatAnxietyList.postValue(anxietyList)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HrvViewModel", "Error observing anxiety levels: ${e.message}")
            }
        }
    }

    fun getAverageAnxietyByHour() {
        // Pastikan ini dilakukan di viewModelScope untuk menghindari crash pada UI thread
        viewModelScope.launch(Dispatchers.IO) { // Pindahkan ke thread IO untuk operasi database
            try {
                // Mengambil data rata-rata anxiety per jam dari database
                val currentTimestamp = System.currentTimeMillis()
                val startTimestamp = currentTimestamp - 24 * 60 * 60 * 1000 // 24 jam yang lalu
                val endTimestamp = currentTimestamp

                Log.d("HrvViewModel", "Start Timestamp: $startTimestamp, End Timestamp: $endTimestamp")

                // Pastikan Anda memanggil DAO pada background thread
                val data = dao.getAverageAnxietyByHour(startTimestamp, endTimestamp)
                Log.d("HrvViewModel", "Average Anxiety Per Hour: $data")

                // Update LiveData di main thread setelah mendapatkan data
                withContext(Dispatchers.Main) {
                    _averageAnxietyPerHour.postValue(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HrvViewModel", "Error getting average anxiety by hour: ${e.message}")
            }
        }
    }




    // Optional: Fungsi untuk memulai pengambilan data sekali saja saat ViewModel diinisialisasi
    init {
        observeAnxietyLevels() // Mulai mengamati data saat ViewModel pertama kali diinisialisasi
        getAverageAnxietyByHour() // Mulai mengambil rata-rata anxiety per jam
    }
}
