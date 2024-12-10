package com.example.tugasakhir

import android.app.Application
import androidx.compose.material3.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.tugasakhir.ui.theme.TugasAkhirTheme
import java.text.SimpleDateFormat
import java.util.*

class AnxietyLogActivity : ComponentActivity() {
    private val hrvViewModel: HrvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TugasAkhirTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Menambahkan Scaffold dengan BottomNavigationBar
                    Scaffold(
                        bottomBar = { BottomNavigationBar(currentScreen = "Log") }  // Menambahkan BottomNavigationBar
                    ) { paddingValues ->
                        // Menyusun konten AnxietyLogScreen dengan padding
                        AnxietyLogScreen(hrvViewModel, paddingValues)
                    }
                }
            }
        }
    }
}

@Composable
fun AnxietyLogScreen(hrvViewModel: HrvViewModel, paddingValues: PaddingValues) {
    // Mengamati LiveData
    val tingkatAnxietyList by hrvViewModel.tingkatAnxietyList.observeAsState(emptyList())

    // Menggunakan remember untuk menyimpan data terbalik tanpa merusak observasi
    val reversedList = remember(tingkatAnxietyList) { tingkatAnxietyList.reversed() }

    // Menampilkan data menggunakan LazyColumn
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .padding(bottom = paddingValues.calculateBottomPadding())) {
        items(reversedList) { anxiety ->
            val formattedTimestamp = formatTimestamp(anxiety.timestamp)
            Text(text = "Anxiety Level: ${anxiety.tingkatAnxiety}\nTimestamp: $formattedTimestamp \n")
        }
    }
}


fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val date = Date(timestamp)
    return dateFormat.format(date)
}

@Preview(showBackground = true)
@Composable
fun PreviewAnxietyLogScreen() {
    val mockHrvViewModel = HrvViewModel(Application()) // Gunakan Application dari LocalContext
    TugasAkhirTheme {
        // Tambahkan padding untuk memastikan tampilan sesuai
        AnxietyLogScreen(mockHrvViewModel, PaddingValues())
    }
}
