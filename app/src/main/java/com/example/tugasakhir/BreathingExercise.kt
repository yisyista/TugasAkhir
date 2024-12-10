package com.example.tugasakhir

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import com.plcoding.composetimer.ui.theme.ComposeTimerTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.example.tugasakhir.ui.theme.Purple500
import com.example.tugasakhir.ui.theme.Purple80
import android.media.MediaPlayer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import com.example.tugasakhir.ui.theme.TugasAkhirTheme


class BreathingExercise : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null  // Tambahkan MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Gunakan MaterialTheme untuk memastikan tema gelap diterapkan
            TugasAkhirTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF101010) // Tetap menggunakan warna gelap
                ) {
                    Scaffold(
                        bottomBar = { BottomNavigationBar(currentScreen = "Breathing") }
                    ) { paddingValues ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues) // Padding agar tidak tertutup BottomNavigationBar
                        ) {
                            Timer(
                                totalTime = 100L * 1000L,
                                handleColor = Purple500,
                                inactiveBarColor = Color.DarkGray,
                                activeBarColor = Purple80,
                                modifier = Modifier.size(200.dp),
                                onStartStopClick = { start ->
                                    if (start) {
                                        mediaPlayer?.start()  // Mulai musik
                                    } else {
                                        mediaPlayer?.pause()  // Pause musik
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Inisialisasi MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.relax)  // Ganti 'relax' dengan nama file MP3 Anda
        mediaPlayer?.isLooping = true  // Musik akan diputar berulang-ulang jika diinginkan
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()  // Hapus mediaPlayer ketika aktivitas dihancurkan
    }
}



@Composable
fun Timer(
    totalTime: Long,
    handleColor: Color,
    inactiveBarColor: Color,
    activeBarColor: Color,
    modifier: Modifier = Modifier,
    initialValue: Float = 1f,
    strokeWidth: Dp = 5.dp,
    onStartStopClick: (Boolean) -> Unit // Parameter untuk menerima aksi klik
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var value by remember { mutableStateOf(initialValue) }
    var currentTime by remember { mutableStateOf(totalTime) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Tambahkan state untuk fase pernapasan
    var phase by remember { mutableStateOf("Inhale") }
    var phaseCountdown by remember { mutableStateOf(4) }

    // Update logika pernapasan
    LaunchedEffect(key1 = currentTime, key2 = isTimerRunning) {
        if (currentTime > 0 && isTimerRunning) {
            delay(1000L) // Tunggu 1 detik
            currentTime -= 1000L
            phaseCountdown-- // Kurangi hitungan fase

            if (phaseCountdown <= 0) {
                when (phase) {
                    "Inhale" -> {
                        phase = "Hold"
                        phaseCountdown = 7
                    }
                    "Hold" -> {
                        phase = "Exhale"
                        phaseCountdown = 8
                    }
                    "Exhale" -> {
                        phase = "Inhale"
                        phaseCountdown = 4
                    }
                }
            }

            value = currentTime / totalTime.toFloat()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .onSizeChanged {
                size = it
            }
    ) {
        Canvas(modifier = modifier) {
            drawArc(
                color = inactiveBarColor,
                startAngle = -215f,
                sweepAngle = 250f,
                useCenter = false,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = activeBarColor,
                startAngle = -215f,
                sweepAngle = 250f * value,
                useCenter = false,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            val center = Offset(size.width / 2f, size.height / 2f)
            val beta = (250f * value + 145f) * (PI / 180f).toFloat()
            val r = size.width / 2f
            val a = cos(beta) * r
            val b = sin(beta) * r
            drawPoints(
                listOf(Offset(center.x + a, center.y + b)),
                pointMode = PointMode.Points,
                color = handleColor,
                strokeWidth = (strokeWidth * 3f).toPx(),
                cap = StrokeCap.Round
            )
        }

        // Gunakan Column agar phase dan phaseCountdown tidak bertumpuk
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animasi untuk teks `phase`
            AnimatedContent(targetState = phase) { currentPhase ->
                Text(
                    text = currentPhase,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // Teks `phaseCountdown` tanpa animasi
            Text(
                text = phaseCountdown.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.LightGray
            )
        }

        // Tombol untuk mengontrol timer
        Button(
            onClick = {
                if (currentTime <= 0L) {
                    currentTime = totalTime
                    phase = "Inhale" // Reset fase ke awal
                    phaseCountdown = 4
                    isTimerRunning = true
                } else {
                    isTimerRunning = !isTimerRunning
                }

                // Memanggil fungsi untuk mengontrol musik
                onStartStopClick(isTimerRunning)
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(
                containerColor =
                if (!isTimerRunning || currentTime <= 0L) {
                    Color.Green
                } else {
                    Color.Red
                }
            )
        ) {
            Text(
                text = if (isTimerRunning && currentTime > 0L) "Stop"
                else if (!isTimerRunning && currentTime > 0L) "Start"
                else "Restart"
            )
        }
    }
}


