package com.example.roomplan.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomplan.ui.theme.BlueprintDark
import com.example.roomplan.ui.theme.PrimaryBlueLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1150,
                easing = FastOutSlowInEasing
            )
        )
        delay(150)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueprintDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RoomPlan",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "ARCHITECTURAL FLOOR-PLAN GENERATOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Progressive architectural room drawing
            val strokeColor = PrimaryBlueLight
            val mutedGridColor = Color(0xFF334155)

            Canvas(modifier = Modifier.size(240.dp, 192.dp)) {
                val p = animationProgress.value

                // Outer border path
                val outerPath = Path().apply {
                    addRect(androidx.compose.ui.geometry.Rect(8f, 8f, size.width - 8f, size.height - 8f))
                }
                // Inner room boundary path
                val innerPath = Path().apply {
                    addRect(androidx.compose.ui.geometry.Rect(48f, 40f, size.width - 48f, size.height - 40f))
                }

                // Architectural corner crosshairs
                val crossLength = 16f
                // Top-left
                drawLine(mutedGridColor, Offset(0f, 8f), Offset(crossLength, 8f), 2f)
                drawLine(mutedGridColor, Offset(8f, 0f), Offset(8f, crossLength), 2f)
                // Bottom-right
                drawLine(mutedGridColor, Offset(size.width - crossLength, size.height - 8f), Offset(size.width, size.height - 8f), 2f)
                drawLine(mutedGridColor, Offset(size.width - 8f, size.height - crossLength), Offset(size.width - 8f, size.height), 2f)

                // Progressive drawing using PathMeasure
                val outerMeasure = PathMeasure().apply { setPath(outerPath, false) }
                val outerDrawn = Path()
                outerMeasure.getSegment(0f, outerMeasure.length * p, outerDrawn, true)
                drawPath(
                    path = outerDrawn,
                    color = strokeColor.copy(alpha = 0.5f),
                    style = Stroke(width = 3f)
                )

                val innerMeasure = PathMeasure().apply { setPath(innerPath, false) }
                val innerDrawn = Path()
                innerMeasure.getSegment(0f, innerMeasure.length * p, innerDrawn, true)
                drawPath(
                    path = innerDrawn,
                    color = strokeColor,
                    style = Stroke(width = 5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "500 × 400 cm",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Calculate dimensions. Generate proportional layouts.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
