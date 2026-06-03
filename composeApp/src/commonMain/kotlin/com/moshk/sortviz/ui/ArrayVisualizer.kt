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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun ArrayVisualizer(
    array: Array<Int>,
    barsColor: Color = MaterialTheme.colorScheme.inverseSurface,
    comparing: Pair<Int, Int>? = null,
    comparingColor: Color = MaterialTheme.colorScheme.error,
    selected: Set<Int> = emptySet(),
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    textSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
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
                barsColor,
                comparing,
                comparingColor,
                selected,
                selectedColor,
                textSize,
                textColor,
                textMeasurer
            )
        }
    }
}

fun DrawScope.drawBarChart(
    array: Array<Int>,
    barsColor: Color,
    comparing: Pair<Int, Int>?,
    comparingColor: Color,
    selected: Set<Int>,
    selectedColor: Color,
    textSize: TextUnit,
    textColor: Color,
    textMeasurer: TextMeasurer
) {
    val width = size.width
    val height = size.height
    val maxValue = array.max()

    val barWidth = width / (array.size + 2)
    val barHeightMultiplier = height / (maxValue + 2)

    for (i in array.indices) {
        drawBar(
            array,
            barsColor,
            comparing,
            comparingColor,
            selected,
            selectedColor,
            textSize,
            textColor,
            textMeasurer,
            barWidth,
            barHeightMultiplier,
            maxValue,
            current = i
        )
    }


    if (comparing != null &&
        comparing.first  in 0..array.size &&
        comparing.second in 0..array.size) {
        drawArrow(array, comparing, comparingColor,
                barWidth, barHeightMultiplier, maxValue)
        }
}

fun DrawScope.drawBar(
    array: Array<Int>,
    barsColor: Color,
    comparing: Pair<Int, Int>?,
    comparingColor: Color,
    selected: Set<Int>,
    selectedColor: Color,
    textSize: TextUnit,
    textColor: Color,
    textMeasurer: TextMeasurer,
    barWidth: Float,
    barHeightMultiplier: Float,
    maxValue: Int,
    current: Int
) {
    // ТЕКСТ (ЗНАЧЕНИЕ СТОЛБЦА)
    val text = array[current].toString()
    val textSize = textMeasurer.measure(
        text,
        style = TextStyle(
            fontSize = textSize,
            color = textColor,
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
            color = textColor,
        )
    )
    // СТОЛБЕЦ
    drawRect(
        color = if (comparing?.first == current || comparing?.second == current) {
            comparingColor
        } else if (current in selected){
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

    drawPath(
        path = Path().apply {
            moveTo(start.x, start.y)
            lineTo(control.x, control.y)
            lineTo(end.x, end.y)
        },
        color = arrowColor,
        style = Stroke(
            width = 4f,
            cap = StrokeCap.Square,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 25f))
        )
    )

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
    Surface(modifier = Modifier.fillMaxSize()) {
        val random = Random(42)
        val arrayLength = 10
        val array = Array(arrayLength) { i -> i + 1}
        array.shuffle(random)
        val comparing = random.nextInt(0, 10) to random.nextInt(0, 10)
        val selected = Array(2) { random.nextInt(0, 10) }.toSet()
        ArrayVisualizer(
            array = array,
            comparing = comparing,
            selected = selected
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