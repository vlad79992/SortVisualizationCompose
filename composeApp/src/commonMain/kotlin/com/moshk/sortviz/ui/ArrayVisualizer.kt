package com.moshk.sortviz.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.moshk.sortviz.ui.theme.SortAppTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun ArrayVisualizer(
    array: Array<Int>,
    selected: Pair<Int, Int> = -1 to -1,
    barsColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedColor: Color = MaterialTheme.colorScheme.error,
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
                selected,
                textMeasurer,
                barsColor,
                selectedColor,
                textSize
            )
        }
    }
}

fun DrawScope.drawBarChart(
    array: Array<Int>,
    selected: Pair<Int, Int>,
    textMeasurer: TextMeasurer,
    barsColor: Color,
    selectedColor: Color,
    textSize: TextUnit
) {
    val width = size.width
    val height = size.height
    val maxValue = array.max()

    val barWidth = width / (array.size + 2)
    val barHeightMultiplier = height / (maxValue + 2)

    for (i in array.indices) {
        drawBar(array, selected, textMeasurer, barsColor, selectedColor,
            textSize, barWidth, barHeightMultiplier, maxValue, i)
    }


    if (selected.first != selected.second &&
        selected.first in 0..array.size &&
        selected.second in 0..array.size) {
        drawArrow(array, selected, selectedColor,
                barWidth, barHeightMultiplier, maxValue)
        }
}

fun DrawScope.drawBar(
    array: Array<Int>,
    selected: Pair<Int, Int>,
    textMeasurer: TextMeasurer,
    barsColor: Color,
    selectedColor: Color,
    textSize: TextUnit,
    barWidth: Float,
    barHeightMultiplier: Float,
    maxValue: Int,
    current: Int
) {
    // ТЕКСТ (РАЗМЕР СТОЛБЦА)
    val text = array[current].toString()
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
            barWidth * (current + 1.5f) - textSize.width * 0.6f,
            barHeightMultiplier * (1.1f + maxValue)
        ),
        style = TextStyle(
            fontSize = 18.sp,
            color = barsColor,
        )
    )
    // СТОЛБЕЦ
    drawRect(
        color = if (selected.first == current || selected.second == current) {
            selectedColor
        } else {
            barsColor
        },
        topLeft = Offset(
            barWidth * (current + 1),
            barHeightMultiplier * (1 - array[current] + maxValue)
        ),
        size = Size(
            barWidth * 0.9f,
            barHeightMultiplier * array[current])
    )
}

fun DrawScope.drawArrow(
    array: Array<Int>,
    selected: Pair<Int, Int>,
    arrowColor: Color,
    barWidth: Float,
    barHeightMultiplier: Float,
    maxValue: Int,
) {
    val start = getBarTop(array, selected.first, barWidth, barHeightMultiplier, maxValue)
    val control = Offset(
        x = ((selected.first + selected.second) / 2f + 1.5f) * barWidth,
        y = 0f
    )
    val end = getBarTop(array, selected.second, barWidth, barHeightMultiplier, maxValue)

    drawPath(
        path = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(control.x, control.y, end.x, end.y)
        },
        color = arrowColor,
        style = Stroke(width = 8f, cap = StrokeCap.Square)
    )

//    drawPath(
//        path = Path().apply {
//            moveTo(start.x, start.y)
//            lineTo(control.x, control.y)
//            lineTo(end.x, end.y)
//        },
//        color = arrowColor,
//        style = Stroke(
//            width = 4f,
//            cap = StrokeCap.Square,
//            pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 25f))
//        )
//    )

    drawTangentArrow(
        start = start, control, 50f, 30f, arrowColor
    )
    drawTangentArrow(
        start = start, control, 50f, -30f, arrowColor
    )

    drawTangentArrow(
        start = end, control, 50f, 30f, arrowColor
    )
    drawTangentArrow(
        start = end, control, 50f, -30f, arrowColor
    )
}

private fun DrawScope.drawTangentArrow(
    start: Offset,
    control: Offset,
    length: Float,
    angleDeg: Float,
    color: Color,
) {
    val dx = control.x - start.x
    val dy = control.y - start.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist == 0f) return

    val baseAngle = atan2(dy, dx)
    val finalAngle = baseAngle + angleDeg.toDouble() * (PI / 180.0)

    val endX = start.x + length * cos(finalAngle).toFloat()
    val endY = start.y + length * sin(finalAngle).toFloat()

    drawPath(
        path = Path().apply {
            moveTo(start.x, start.y)
            lineTo(endX, endY)
        },
        color = color,
        style = Stroke(width = 8f, cap = StrokeCap.Round)
    )
}

private fun getBarTop(
    array: Array<Int>,
    index: Int,
    barWidth: Float,
    barHeightMultiplier: Float,
    maxValue: Int,
): Offset = Offset(
    x = (index + 1.5f) * barWidth,
    y = barHeightMultiplier * (1 - array[index] + maxValue)
)

@Composable
@Preview
fun PreviewSortVisualizerLight() {
    val random = Random(42)
    val array = Array(10) { i -> i + 1}
    array.shuffle(random)
    Surface(modifier = Modifier.fillMaxSize()) {
        ArrayVisualizer(
            array,
            selected = random.nextInt(0, 10) to random.nextInt(0, 10)
        )
    }
}

@Composable
@Preview
fun PreviewSortVisualizerDark() {
    SortAppTheme(darkTheme = true) {
        PreviewSortVisualizerLight()
    }
}