package com.example.tugasakhir

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasakhir.ui.theme.secondaryContainerLight

@Composable
fun LineGraph(
    graphData: List<Float>,
    xAxisLabels: List<String>,
    lineColor: Color = secondaryContainerLight,
    pointColor: Color = Color.Red,
    height: Dp = 200.dp,
    width: Dp = 300.dp
) {
    val yAxisHeight = height
    val xAxisWidth = width
    val maxData = 1 // Karena hanya ada 0 dan 1

    Box(
        modifier = Modifier
            .padding(16.dp)
            .width(xAxisWidth)
            .height(yAxisHeight + 40.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.Bottom
        ) {
            Canvas(modifier = Modifier.size(xAxisWidth, yAxisHeight)) {
                val stepX = size.width / (graphData.size - 1).coerceAtLeast(1)
                val stepY = size.height / maxData.toFloat()
                val path = Path()

                graphData.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - (value * stepY)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    drawCircle(
                        color = pointColor,
                        radius = 5f,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 15f) // Menebalkan garis
                )

                // Menambahkan label sumbu Y
                drawContext.canvas.nativeCanvas.apply {
                    drawText("Anxiety", -10f, 20f, android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 40f
                        isFakeBoldText = true
                    })
                    drawText("No Anxiety", -10f, size.height - 20f, android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 40f
                        isFakeBoldText = true
                    })
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xAxisLabels.forEach {
                Text(text = it, fontSize = 12.sp, color = Color.Black)
            }
        }
    }
}
