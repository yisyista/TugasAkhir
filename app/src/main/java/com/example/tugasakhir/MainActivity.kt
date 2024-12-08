package com.example.tugasakhir

import android.app.Application
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val hrvViewModel: HrvViewModel by viewModels()
    private val REQUEST_CODE_NOTIFICATIONS = 1001


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /// Meminta izin untuk notifikasi jika SDK >= TIRAMISU (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATIONS
                )
            }
        }


        setContent {
            TugasAkhirTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(hrvViewModel, mainActivity = this)
                }
            }
        }
        Log.d("MainActivity", "MainActivity created and content set")

        // Menjadwalkan Worker untuk pengecekan periodik setiap satu jam
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.MINUTES)
            .addTag("AnxietyNotificationWorker") // Tambahkan tag unik
            .build()
        Log.d("MainActivity", "Scheduling NotificationWorker with a period of 1 minute.")

        // Batalkan worker sebelumnya (jika ada) untuk menghindari duplikasi
        WorkManager.getInstance(this).cancelAllWorkByTag("AnxietyNotificationWorker")
        WorkManager.getInstance(this).enqueue(workRequest)

        WorkManager.getInstance(this).getWorkInfosByTagLiveData("AnxietyNotificationWorker")
            .observe(this) { workInfos ->
                workInfos?.forEach { workInfo ->
                    Log.d("MainActivity", "WorkInfo state: ${workInfo.state}")
                }
            }


    }

    // Fungsi untuk menyimpan nilai latestAnxiety ke SharedPreferences
    fun saveLatestAnxietyToPreferences(latestAnxiety: Float) {
        val sharedPreferences = getSharedPreferences("anxiety_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("latestAnxiety", latestAnxiety)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "MainActivity resumed")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(hrvViewModel: HrvViewModel, mainActivity: MainActivity) {
    val hrvValue by hrvViewModel.hrvValue.observeAsState("Anxiety Level: Waiting...")
    val averageAnxietyPerHour by hrvViewModel.averageAnxietyPerHour.observeAsState(emptyList())
    val tingkatAnxietyList by hrvViewModel.tingkatAnxietyList.observeAsState(emptyList())

    val context = LocalContext.current

    LaunchedEffect(key1 = averageAnxietyPerHour, key2 = tingkatAnxietyList) {
        if (averageAnxietyPerHour.isNotEmpty()) {
            val latestAnxiety = averageAnxietyPerHour.first().avgTingkatAnxiety
            Log.d("MainScreen", "Latest Anxiety Level: $latestAnxiety")

            // Simpan latestAnxiety ke SharedPreferences
            mainActivity.saveLatestAnxietyToPreferences(latestAnxiety)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calm") },
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
            // Update text to display average anxiety per hour
            if (averageAnxietyPerHour.isNotEmpty()) {
                val latestAnxiety = averageAnxietyPerHour.first().avgTingkatAnxiety
                val formattedAnxiety = String.format("%.2f", latestAnxiety) // Memformat angka ke dua desimal

                // Mendapatkan timestamp saat ini
                val timestamp = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) // Format waktu yang diinginkan
                val formattedTimestamp = dateFormat.format(Date(timestamp))

                Column {
                    Text(text = "Anxiety Level: $formattedAnxiety", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text(text = "Last updated: $formattedTimestamp", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            } else {
                Text(text = "Anxiety Level: Waiting...", style = MaterialTheme.typography.titleLarge)
            }

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

            // Button untuk membuka halaman Anxiety Graph
            Button(
                onClick = {
                    val intent = Intent(context, AnxietyGraphActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("View Anxiety Graph")
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ambil 10 data terakhir dari tingkatAnxietyList
                val dataList = tingkatAnxietyList
                    .takeLast(10)  // Mengambil 10 data terakhir
                    .map { it.tingkatAnxiety.toFloat() }  // Mengonversi tingkatAnxiety menjadi float
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
                val datesList = List(dataList.size) { it + 1 } // Membuat label untuk sumbu x
                Log.d("GraphData", "Normalized Data: $finalData")

                // Ensure you have BarGraph component available
                BarGraph(
                    graphBarData = finalData,
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
    val mockHrvViewModel = HrvViewModel(context.applicationContext as Application)
    mockHrvViewModel.updateHrvValue(0f)

    // Dummy mainActivity untuk preview
    val dummyMainActivity = MainActivity()

    TugasAkhirTheme {
        MainScreen(mockHrvViewModel, mainActivity = dummyMainActivity)
    }
}

