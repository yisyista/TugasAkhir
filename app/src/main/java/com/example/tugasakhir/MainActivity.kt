package com.example.tugasakhir

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.tugasakhir.ui.theme.TugasAkhirTheme
import com.example.tugasakhir.ui.theme.Purple500
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    private val hrvViewModel: HrvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TugasAkhirTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(hrvViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "MainActivity resumed")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(hrvViewModel: HrvViewModel) {
    val hrvValue by hrvViewModel.hrvValue.observeAsState("HRV: Waiting...")
    val tingkatAnxietyList by hrvViewModel.tingkatAnxietyList.observeAsState(emptyList())

    val context = LocalContext.current

    LaunchedEffect(key1 = tingkatAnxietyList) {
        Log.d("MainScreen", "Tingkat Anxiety List updated.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HRV App") },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, BluetoothConfigActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Bluetooth Configuration")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "HRV: $hrvValue", style = MaterialTheme.typography.titleLarge)

            Button(
                onClick = {
                    val intent = Intent(context, PredictionActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Open Prediction")
            }

            Button(
                onClick = {
                    val intent = Intent(context, BreathingExercise::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Breathing Exercise")
            }

            // Button untuk membuka halaman Anxiety Log
            Button(
                onClick = {
                    val intent = Intent(context, AnxietyLogActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("View Anxiety Log")
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val dataList = tingkatAnxietyList.map { it.tingkatAnxiety }
                Log.d("GraphData", "Raw Data: $dataList")

                if (dataList.isEmpty()) {
                    Log.e("GraphData", "Data List is empty. Cannot create graph.")
                    return@Column
                }

                val maxValue = dataList.maxOrNull()?.toFloat() ?: 1f
                Log.d("GraphData", "Max Value: $maxValue")

                val normalizedData = dataList.map {
                    if (maxValue != 0f) it.toFloat() / maxValue else 0f
                }

                val finalData = normalizedData.map { it.takeIf { value -> value.isFinite() } ?: 0f }
                val datesList = List(dataList.size) { it + 1 }
                Log.d("GraphData", "Normalized Data: $finalData")

                // Ensure you have BarGraph component available
                BarGraph(
                    graphBarData = finalData,
                    xAxisScaleData = datesList,
                    barData_ = dataList,
                    height = 300.dp,
                    roundType = BarType.TOP_CURVED,
                    barWidth = 10.dp,
                    barColor = Purple500,
                    barArrangement = Arrangement.SpaceEvenly
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val context = LocalContext.current
    val mockHrvViewModel = HrvViewModel(context.applicationContext as Application)
    mockHrvViewModel.updateHrvValue(0f)

    TugasAkhirTheme {
        MainScreen(mockHrvViewModel)
    }
}
