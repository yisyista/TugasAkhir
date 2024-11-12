package com.example.tugasakhir
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer

class OnnxModelPredictor(private val ortEnvironment: OrtEnvironment) {
    private lateinit var ortSession: OrtSession

    fun initializeSession(modelResourceId: Int, context: Context) {
        val modelBytes = context.resources.openRawResource(modelResourceId).readBytes()
        ortSession = ortEnvironment.createSession(modelBytes)
    }

    // Modify runPrediction to accept both Int and Float types as required by the model
    fun runPrediction(
        nn20: Long,
        scrFrequency: Double,
        scrAmplitudeMax: Double,
        scrNumber: Long,
        scrAmplitudeStd: Double
    ): Long {
        // Define input tensors with correct types for each feature
        val inputTensors = mapOf(
            "NN20" to OnnxTensor.createTensor(ortEnvironment, LongBuffer.wrap(longArrayOf(nn20.toLong())), longArrayOf(1, 1)),
            "SCR_Frequency" to OnnxTensor.createTensor(ortEnvironment, DoubleBuffer.wrap(doubleArrayOf(scrFrequency.toDouble())), longArrayOf(1, 1)),
            "SCR_Amplitude_Max" to OnnxTensor.createTensor(ortEnvironment, DoubleBuffer.wrap(doubleArrayOf(scrAmplitudeMax.toDouble())), longArrayOf(1, 1)),
            "SCR_Number" to OnnxTensor.createTensor(ortEnvironment, LongBuffer.wrap(longArrayOf(scrNumber.toLong())), longArrayOf(1, 1)),
            "SCR_Amplitude_STD" to OnnxTensor.createTensor(ortEnvironment, DoubleBuffer.wrap(doubleArrayOf(scrAmplitudeStd.toDouble())), longArrayOf(1, 1))
        )

        // Run the model with the prepared input tensors
        val results = ortSession.run(inputTensors)
        Log.i("OnnxModelPredictor", "results masuk")
        val output = results[0].value as LongArray
        Log.i("OnnxModelPredictor", "output masuk")
        return output[0]
        Log.i("OnnxModelPredictor", "return output berhasil")
    }

}
