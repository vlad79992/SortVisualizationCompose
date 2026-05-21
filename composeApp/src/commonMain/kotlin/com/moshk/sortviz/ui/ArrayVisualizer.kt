package com.moshk.sortviz.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.moshk.sortviz.ui.theme.SortAppTheme

@Composable
fun ArrayVisualizer(
    array: Array<Int>,
    barsColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    textSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBarChart(
                array,
                textMeasurer,
                barsColor,
                textSize
            )
        }
    }
}

fun DrawScope.drawBarChart(
    array: Array<Int>,
    textMeasurer: TextMeasurer,
    barsColor: Color,
    textSize: TextUnit
) {
    val width = size.width
    val height = size.height
    val maxValue = array.max()

    val barWidth = width / (array.size + 2)
    val barHeightMultiplier = height / (maxValue + 2)

    for (i in array.indices) {
        val text = array[i].toString()
        val textSize = textMeasurer.measure(
            text,
            style = TextStyle(
                fontSize = textSize,
                color = barsColor,
                )
        ).size
        drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = Offset(
                barWidth * (i + 1.5f) - textSize.width * 0.6f,
                barHeightMultiplier * (1.1f + maxValue)
            ),
            style = TextStyle(
                fontSize = 18.sp,
                color = barsColor,
            )
        )
        drawRect(
            color = barsColor,
            topLeft = Offset(
                barWidth * (i + 1),
                barHeightMultiplier * (1 - array[i] + maxValue)
            ),
            size = Size(
                barWidth * 0.9f,
                barHeightMultiplier * array[i])
        )
    }
}

@Composable
@Preview
fun PreviewSortVisualizerLight() {
    val array = Array(10) { i -> i + 1}
    array.shuffle()
    Surface(modifier = Modifier.fillMaxSize()) {
        ArrayVisualizer(array)
    }
}

@Composable
@Preview
fun PreviewSortVisualizerDark() {
    SortAppTheme(darkTheme = true) {
        PreviewSortVisualizerLight()
    }
}