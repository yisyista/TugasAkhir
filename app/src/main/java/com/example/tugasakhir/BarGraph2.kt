package com.example.tugasakhir

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import android.graphics.Paint
import kotlin.math.round
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Top
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import android.graphics.Typeface
import com.example.tugasakhir.ui.theme.onSurfaceVariantLight

@Composable
fun <T> BarGraph2(
    graphBarData: List<Float>,
    xAxisScaleData: List<T>,
    barData_: List<Float>,
    height: Dp,
    roundType: BarType,
    barWidth: Dp,
    barColor: Color,
    barArrangement: Arrangement.Horizontal
) {

    val barData by remember {
        mutableStateOf(barData_.toList() + 0f)  // Pastikan barData_ adalah List<Float> dan menambahkan 0f
    }

    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp.dp

    val xAxisScaleHeight = 40.dp
    val yAxisScaleSpacing by remember {
        mutableStateOf(100f)
    }

    val yAxisTextWidth by remember {
        mutableStateOf(100.dp)
    }

    val barShap = when (roundType) {
        BarType.CIRCULAR_TYPE -> CircleShape
        BarType.TOP_CURVED -> RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
    }

    val density = LocalDensity.current
    val textPaint = remember(density) {
        Paint().apply {
            color = Color.Black.hashCode()
            var textAlign = Paint.Align.CENTER
            var textSize = density.run { 16.sp.toPx() }
        }
    }

    val yCoordinates = mutableListOf<Float>()
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val lineHeightXAxis = 10.dp
    val horizontalLineHeight = 5.dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .padding(top = xAxisScaleHeight, end = 3.dp)
                .height(height)
                .fillMaxWidth(),
            horizontalAlignment = CenterHorizontally
        ) {

            Canvas(modifier = Modifier.padding(bottom = 10.dp).fillMaxSize()) {

                // Tentukan posisi untuk teks sumbu Y lebih ke kiri
                val textPaddingLeft = 40f // Sesuaikan dengan kebutuhan untuk menggeser teks ke kiri

                // Hitung posisi tengah antara "No Anxiety" dan "Anxiety"
                val middleY = size.height - yAxisScaleSpacing - (size.height / 2f)

                // Menggambar teks "No Anxiety" dan "Anxiety" dalam dua baris
                drawContext.canvas.nativeCanvas.apply {
                    textPaint.apply {
                        typeface = Typeface.DEFAULT_BOLD // Bold text
                        textSize = 14.sp.toPx()
                        color = Color.Black.toArgb()
                    }

                    // Gambar "No" di baris pertama
                    val noWidth = textPaint.measureText("No") // Ukuran lebar teks "No"
                    drawText(
                        "No",
                        textPaddingLeft - noWidth / 2, // Rata tengah
                        size.height - yAxisScaleSpacing - 40f, // Posisi Y untuk No
                        textPaint
                    )

                    // Gambar "Anxiety" di baris kedua
                    val anxietyWidth = textPaint.measureText("Anxiety") // Ukuran lebar teks "Anxiety"
                    drawText(
                        "Anxiety",
                        textPaddingLeft - anxietyWidth / 2, // Rata tengah
                        size.height - yAxisScaleSpacing, // Geser sedikit ke bawah untuk baris kedua
                        textPaint
                    )

                    // Gambar "Anxiety" pada bagian atas untuk sumbu Y
                    val anxietyTopWidth = textPaint.measureText("Anxiety") // Ukuran lebar teks "Anxiety"
                    drawText(
                        "Anxiety",
                        textPaddingLeft - anxietyTopWidth / 2, // Rata tengah
                        size.height - yAxisScaleSpacing - size.height, // Di atas canvas
                        textPaint
                    )
                }

                // Menggambar garis putus-putus di tengah antara "No Anxiety" dan "Anxiety"
                drawLine(
                    start = Offset(x = yAxisScaleSpacing + 40f, y = middleY), // Tengah antara 0 dan 1
                    end = Offset(x = size.width, y = middleY),
                    color = Color.Gray,
                    strokeWidth = 5f,
                    pathEffect = pathEffect
                )
            }





        }

        Box(
            modifier = Modifier
                .padding(start = 50.dp)
                .width(width - yAxisTextWidth)
                .height(height + xAxisScaleHeight),
            contentAlignment = BottomCenter
        ) {

            Row(
                modifier = Modifier
                    .width(width - yAxisTextWidth)
                    .horizontalScroll(rememberScrollState()), // Apply horizontal scroll here
                verticalAlignment = Alignment.Top,
                horizontalArrangement = barArrangement
            ) {

                graphBarData.forEachIndexed { index, value ->

                    var animationTriggered by remember {
                        mutableStateOf(false)
                    }
                    val graphBarHeight by animateFloatAsState(
                        targetValue = if (animationTriggered) value else 0f,
                        animationSpec = tween(
                            durationMillis = 1000,
                            delayMillis = 0
                        )
                    )
                    LaunchedEffect(key1 = true) {
                        animationTriggered = true
                    }

                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Top,
                        horizontalAlignment = CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .padding(bottom = 5.dp)
                                .clip(barShap)
                                .width(barWidth)
                                .height(height - 10.dp)
                                .background(Color.Transparent),
                            contentAlignment = BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(barShap)
                                    .fillMaxWidth()
                                    .fillMaxHeight(graphBarHeight)
                                    .background(barColor)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .height(xAxisScaleHeight),
                            verticalArrangement = Top,
                            horizontalAlignment = CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            bottomStart = 2.dp,
                                            bottomEnd = 2.dp
                                        )
                                    )
                                    .width(horizontalLineHeight)
                                    .height(lineHeightXAxis)
                                    .background(onSurfaceVariantLight)
                            )

                            Text(
                                modifier = Modifier.padding(bottom = 3.dp),
                                text = xAxisScaleData[index].toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )

                        }

                    }

                }

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                horizontalAlignment = CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .padding(bottom = xAxisScaleHeight + 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .fillMaxWidth()
                        .height(horizontalLineHeight)
                        .background(onSurfaceVariantLight)
                )

            }

        }

    }

}



