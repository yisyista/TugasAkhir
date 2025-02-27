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
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import java.text.SimpleDateFormat
import java.util.*
import androidx.work.WorkManager
import com.example.tugasakhir.ui.theme.errorLight
import com.example.tugasakhir.ui.theme.onTertiaryDark
import com.example.tugasakhir.ui.theme.secondaryContainerLight
import com.example.tugasakhir.ui.theme.secondaryDark
import com.example.tugasakhir.ui.theme.surfaceContainerLight
import com.example.tugasakhir.ui.theme.tertiaryContainerDark
import java.util.concurrent.TimeUnit
import androidx.work.WorkInfo




class MainActivity : ComponentActivity() {
    private val hrvViewModel: HrvViewModel by viewModels()
    private val REQUEST_CODE_NOTIFICATIONS = 1001


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /// Meminta izin untuk notifikasi jika SDK >= TIRAMISU (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
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

        val workManager = WorkManager.getInstance(this)

// Cek apakah worker sudah ada sebelum menjadwalkannya
        workManager.getWorkInfosByTagLiveData("AnxietyNotificationWorker")
            .observe(this) { workInfos ->
                val isWorkerActive = workInfos?.any {
                    it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                } ?: false

                if (!isWorkerActive) {
                    // Jika worker belum ada, jadwalkan worker baru
                    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(12, TimeUnit.HOURS)
                        .addTag("AnxietyNotificationWorker")
                        .build()
                    Log.d(
                        "MainActivity",
                        "Scheduling NotificationWorker with a period of 12 hours."
                    )
                    workManager.enqueue(workRequest)
                } else {
                    Log.d("MainActivity", "NotificationWorker is already active. Skipping enqueue.")
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
            val latestAnxiety = averageAnxietyPerHour.last().avgTingkatAnxiety
            Log.d("MainScreen", "Latest Anxiety Level: $averageAnxietyPerHour")
            Log.d("MainScreen", "Last Anxiety Level: $latestAnxiety")

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
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bluetooth),
                            contentDescription = "Bluetooth Configuration"
                        )

                        //Icon(Icons.Filled.Settings, contentDescription = "Bluetooth Configuration")
                    }
                }
            )
        },
                bottomBar = { BottomNavigationBar(currentScreen = "Main") }
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
                val latestAnxiety = averageAnxietyPerHour.last().avgTingkatAnxiety
                val formattedAnxiety = String.format("%.2f", latestAnxiety) // Memformat angka ke dua desimal

                // Mendapatkan timestamp saat ini
                val timestamp = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) // Format waktu yang diinginkan
                val formattedTimestamp = dateFormat.format(Date(timestamp))

                Column {
                    //Text(text = "Anxiety Level: $formattedAnxiety", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text(text = "Anxiety Level:", fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text(text = "(In the last 1 hour)", fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    showProgress(latestAnxiety)
                    Text(text = "Last updated: $formattedTimestamp", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))

                    if (latestAnxiety > 0.5 ){
                        Text(text = "\nYour anxiety level is high. Let's manage your anxiety by doing some relaxation", fontSize = 16.sp, textAlign = TextAlign.Center)

                        Button(
                            onClick = {
                                val intent = Intent(context, BreathingExercise::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = errorLight)
                        ) {
                            Text("Try Breathing Exercise")
                        }
                    }


                }
            } else {
                Text(text = "Anxiety Level: Waiting...", style = MaterialTheme.typography.titleLarge)
            }

            //bisa tambah button di sini



            Column(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ambil 10 data terakhir dari tingkatAnxietyList
                val dataList = tingkatAnxietyList
                    .takeLast(5)  // Ambil 10 data terakhir
                    .map { it.tingkatAnxiety.toFloat() }  // Ambil tingkatAnxiety

                val timestampList = tingkatAnxietyList
                    .takeLast(5)  // Ambil timestamp dari 10 data terakhir
                    .map { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.timestamp)) } // Format timestamp

                Log.d("GraphData", "Formatted Timestamps: $timestampList")

                if (dataList.isEmpty()) {
                    Log.e("GraphData", "Data List is empty. Cannot create graph.")
                    return@Column
                }

                val maxValue = dataList.maxOrNull() ?: 1f
                val normalizedData = dataList.map { if (maxValue != 0f) it / maxValue else 0f }

// BarGraph dengan sumbu X menggunakan timestamp
                BarGraph2(
                    graphBarData = normalizedData,
                    xAxisScaleData = timestampList, // Gunakan timestamp sebagai label sumbu X
                    barData_ = dataList,
                    height = 250.dp,
                    roundType = BarType.TOP_CURVED,
                    barWidth = 20.dp,
                    barColor = secondaryContainerLight,
                    barArrangement = Arrangement.SpaceEvenly
                )

            }

        }
    }
}

@Preview
@Composable
fun showProgress(score : Float =.2f){


    val gradient = Brush.linearGradient(listOf(
        onTertiaryDark,
        onTertiaryDark,
        onTertiaryDark,
        errorLight))


    val progressFactor by remember(score) {
        mutableStateOf(score*1)
    }

    Row(modifier = Modifier
        .padding(8.dp)
        .width(350.dp).height(60.dp).border(
            width = 4.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    onTertiaryDark,
                    onTertiaryDark
                )
            ) ,
            shape = RoundedCornerShape(50.dp)
        )
        .clip(
            RoundedCornerShape(
                topStartPercent = 50,
                topEndPercent = 50,
                bottomEndPercent = 50,
                bottomStartPercent = 50
            )
        )
        .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Button(
            contentPadding = PaddingValues(1.dp),
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(progressFactor)
                .background(brush = gradient),

            enabled = false,
            elevation = null,
            colors = buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )) {

            val score_percent = score*100

            Text(text = String.format("%.0f", score_percent) + "%" ,
                fontSize = 30.sp,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(23.dp))
                    .fillMaxHeight(0.87f)
                    .fillMaxWidth()
                    .padding(7.dp),
                color=secondaryDark,
                textAlign = TextAlign.Center)
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

