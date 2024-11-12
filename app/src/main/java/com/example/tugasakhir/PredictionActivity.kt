package com.example.tugasakhir
import ai.onnxruntime.OrtEnvironment
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PredictionActivity : ComponentActivity() {
    private lateinit var predictor: OnnxModelPredictor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ortEnvironment = OrtEnvironment.getEnvironment()
        predictor = OnnxModelPredictor(ortEnvironment)
        predictor.initializeSession(R.raw.svm_anxiety, this)

        setContent {
            PredictionScreen { nn20, scrFrequency, scrAmplitudeMax, scrNumber, scrAmplitudeSTD ->
                try {
                    val output = predictor.runPrediction(nn20, scrFrequency, scrAmplitudeMax, scrNumber, scrAmplitudeSTD)
                    "Output is $output"
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    null
                }
            }
        }
    }
}

@Composable
fun PredictionScreen(onPredict: (Long, Double, Double, Long, Double) -> String?) {
    var nn20 by remember { mutableStateOf("") }
    var scrFrequency by remember { mutableStateOf("") }
    var scrAmplitudeMax by remember { mutableStateOf("") }
    var scrNumber by remember { mutableStateOf("") }
    var scrAmplitudeSTD by remember { mutableStateOf("") }
    var output by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = nn20,
            onValueChange = { nn20 = it },
            label = { Text("NN20") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = scrFrequency,
            onValueChange = { scrFrequency = it },
            label = { Text("SCR Frequency") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = scrAmplitudeMax,
            onValueChange = { scrAmplitudeMax = it },
            label = { Text("SCR Amplitude Max") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = scrNumber,
            onValueChange = { scrNumber = it },
            label = { Text("SCR Number") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = scrAmplitudeSTD,
            onValueChange = { scrAmplitudeSTD = it },
            label = { Text("SCR Amplitude STD") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                val nn20Int = nn20.toLongOrNull()
                Log.i("PredictionActivity", "nn20 masuk")
                val scrFrequencyFloat = scrFrequency.toDoubleOrNull()
                Log.i("PredictionActivity", "scrFreq masuk")
                val scrAmplitudeMaxFloat = scrAmplitudeMax.toDoubleOrNull()
                Log.i("PredictionActivity", "scrAmpMax masuk")
                val scrNumberInt = scrNumber.toLongOrNull()
                Log.i("PredictionActivity", "scrNum masuk")
                val scrAmplitudeSTDFloat = scrAmplitudeSTD.toDoubleOrNull()
                Log.i("PredictionActivity", "scrAmpSTD masuk")

                if (nn20Int != null && scrFrequencyFloat != null && scrAmplitudeMaxFloat != null && scrNumberInt != null && scrAmplitudeSTDFloat != null) {
                    output = onPredict(nn20Int, scrFrequencyFloat, scrAmplitudeMaxFloat, scrNumberInt, scrAmplitudeSTDFloat)
                } else {
                    output = "Please check the inputs"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Predict")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = output ?: "",
            fontSize = 18.sp
        )
    }
}

