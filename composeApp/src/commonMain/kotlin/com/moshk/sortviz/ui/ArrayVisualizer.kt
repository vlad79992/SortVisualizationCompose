package com.moshk.sortviz.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import com.moshk.sortviz.ui.theme.SortAppTheme

@Composable
fun ArrayVisualizer() {
    val infiniteTransition = rememberInfiniteTransition()
    val radius by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(

                color = Color(0, 150, 255),
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                size = Size(radius * 2, radius * 2),
                alpha = 1f,
                style = Fill
            )
        }
    }
}

@Composable
@Preview
fun PreviewSortVisualizerLight() {
    Surface(modifier = Modifier.fillMaxSize()) {
        ArrayVisualizer()
    }
}

@Composable
@Preview
fun PreviewSortVisualizerDark() {
    SortAppTheme(darkTheme = true) {
        PreviewSortVisualizerLight()
    }
}