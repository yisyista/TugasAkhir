package com.example.tugasakhir

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
import android.app.Application
import androidx.compose.ui.tooling.preview.Preview
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import com.example.tugasakhir.ui.theme.TugasAkhirTheme

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
        // Memanggil loadTingkatAnxiety untuk memuat ulang data setiap kali MainActivity tampil
        hrvViewModel.loadTingkatAnxiety()
        Log.d("MainActivity", "loadTingkatAnxiety() called in onResume")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(hrvViewModel: HrvViewModel) {
    val hrvValue by hrvViewModel.hrvValue.observeAsState("HRV: Waiting...")
    val tingkatAnxietyList by hrvViewModel.tingkatAnxietyList.observeAsState(emptyList()) // Observasi tingkatAnxietyList
    val context = LocalContext.current

    // Memicu pembaruan data setiap kali tingkatAnxietyList berubah
    LaunchedEffect(key1 = tingkatAnxietyList) {
        // Pastikan data diperbarui setiap kali tingkatAnxietyList berubah
        hrvViewModel.loadTingkatAnxiety()
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
            // Display HRV value
            Text(text = hrvValue, style = MaterialTheme.typography.titleLarge)

            // Button to open PredictionActivity
            Button(
                onClick = {
                    val intent = Intent(context, PredictionActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Open Prediction")
            }

            // Bar chart
            Column(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mapping tingkatAnxietyList data into a graph-friendly format
                val dataList = tingkatAnxietyList.map { it.tingkatAnxiety } // Mengambil nilai tingkatAnxiety
                Log.d("GraphData", "Data List: $dataList")  // Log data yang digunakan untuk grafik

                val floatValue = dataList.map { it.toFloat() / dataList.maxOrNull()?.toFloat()!! ?: 1f } // Normalisasi
                val datesList = List(dataList.size) { it + 1 } // Membuat tanggal (misal, 1, 2, 3...)
                Log.d("GraphData", "Normalized Data: $floatValue") // Log normalized data

                BarGraph(
                    graphBarData = floatValue,
                    xAxisScaleData = datesList,
                    barData_ = dataList,
                    height = 300.dp,
                    roundType = BarType.TOP_CURVED,
                    barWidth = 20.dp,
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
    val mockHrvViewModel = HrvViewModel(context.applicationContext as Application) // Gunakan Application dari LocalContext
    mockHrvViewModel.updateHrvValue("HRV: 50") // Set default value for preview

    TugasAkhirTheme {
        MainScreen(mockHrvViewModel) // Pass the mock ViewModel
    }
}

