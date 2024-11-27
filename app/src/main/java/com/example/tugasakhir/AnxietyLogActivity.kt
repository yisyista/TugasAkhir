package com.example.tugasakhir

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
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
                    AnxietyLogScreen(hrvViewModel)
                }
            }
        }
    }
}

@Composable
fun AnxietyLogScreen(hrvViewModel: HrvViewModel) {
    val tingkatAnxietyList by hrvViewModel.tingkatAnxietyList.observeAsState(emptyList())
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(tingkatAnxietyList) { anxiety ->
            val formattedTimestamp = formatTimestamp(anxiety.timestamp) // Format timestamp
            Text(text = "Tingkat Anxiety: ${anxiety.tingkatAnxiety}\nTimestamp: $formattedTimestamp \n")
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'pada pukul' HH:mm:ss", Locale.getDefault())
    val date = Date(timestamp)
    return dateFormat.format(date)
}

@Preview(showBackground = true)
@Composable
fun PreviewAnxietyLogScreen() {
    val mockHrvViewModel = HrvViewModel(Application()) // Gunakan Application dari LocalContext
    TugasAkhirTheme {
        AnxietyLogScreen(mockHrvViewModel)
    }
}
