package com.example.tugasakhir

import ai.onnxruntime.OrtEnvironment
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ClassificationActivity : ComponentActivity() {
    private lateinit var predictor: OnnxModelPredictor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ortEnvironment = OrtEnvironment.getEnvironment()
        predictor = OnnxModelPredictor(ortEnvironment)
        predictor.initializeSession(R.raw.svm_anxiety, this)

        // Ambil data terbaru dari database
        val dataAccessObject = AppDatabase.getDatabase(this).dataAccessObject()

        GlobalScope.launch(Dispatchers.Main) {
            val latestData = dataAccessObject.getLatestDataSensor().firstOrNull()

            if (latestData != null) {
                // Menjalankan prediksi dengan data yang diambil dari database
                val result = runPrediction(latestData)

                // Menampilkan hasil di UI
                setContent {
                    ClassificationScreen(result = result)
                }
            } else {
                // Menampilkan pesan jika tidak ada data
                setContent {
                    ClassificationScreen(result = "No data available")
                }
            }
        }
    }

    private fun runPrediction(data: DataSensorEntity): String {
        return try {
            val output = predictor.runPrediction(
                nn20 = data.nn20.toLong(),
                scrFrequency = data.scrFreq.toDouble(),
                scrAmplitudeMax = data.scrAmplitudeMax.toDouble(),
                scrNumber = data.scrNumber.toLong(),
                scrAmplitudeStd = data.scrAmplitudeStd.toDouble(),
                context = this
            )
            "Classification result: $output"
        } catch (e: Exception) {
            Log.e("ClassificationActivity", "Error: ${e.message}")
            "Error during prediction"
        }
    }
}

@Composable
fun ClassificationScreen(result: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = result,
            fontSize = 24.sp
        )

    }
}
