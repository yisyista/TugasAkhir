package com.example.tugasakhir

import ai.onnxruntime.OrtEnvironment
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class DataProcessingWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        val ortEnvironment = OrtEnvironment.getEnvironment()
        val dataAccessObject = AppDatabase.getDatabase(context).dataAccessObject()
        val predictor = OnnxModelPredictor(ortEnvironment)

        return try {
            // Ambil data dari input Worker
            val nn20 = inputData.getInt("NN20", 0)
            val scrFrequency = inputData.getFloat("SCR_Frequency", 0.0f)
            val scrRisetimeMax = inputData.getFloat("SCR_Risetime_Max", 0.0f)
            val scrRisetimeMin = inputData.getFloat("SCR_Risetime_Min", 0.0f)
            val scrRisetimeStd = inputData.getFloat("SCR_Risetime_STD", 0.0f)

            // Simpan data sensor ke database
            val dataSensor = DataSensorEntity(
                nn20 = nn20,
                scrFreq = scrFrequency,
                scrRisetimeMax = scrRisetimeMax,
                scrRisetimeMin = scrRisetimeMin,
                scrRisetimeStd = scrRisetimeStd,
                timestamp = System.currentTimeMillis()
            )

            runBlocking(Dispatchers.IO) {
                dataAccessObject.insertDataSensor(dataSensor)
                Log.d("Worker", "Data successfully saved to database")
            }

            // Jalankan prediksi menggunakan ONNX model
            predictor.initializeSession(R.raw.svm_anxiety, context)

            val predictionResult: Float = runBlocking(Dispatchers.IO) {
                predictor.runPrediction(
                    nn20 = nn20.toLong(),
                    scrFrequency = scrFrequency.toDouble(),
                    scrRisetimeMax = scrRisetimeMax.toDouble(),
                    scrRisetimeMin = scrRisetimeMin.toDouble(),
                    scrRisetimeStd = scrRisetimeStd.toDouble(),
                    context = context
                )
            }.toFloat()

            // Simpan hasil prediksi ke database
            val tingkatAnxietyEntity = TingkatAnxietyEntity(
                id = 0,
                tingkatAnxiety = predictionResult.toLong()
            )

            runBlocking(Dispatchers.IO) {
                dataAccessObject.insertTingkatAnxiety(tingkatAnxietyEntity)
                Log.d("Worker", "Prediction result saved: ${predictionResult.toInt()}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("Worker", "Error in processing data: ${e.message}")
            Result.failure()
        }
    }
}
