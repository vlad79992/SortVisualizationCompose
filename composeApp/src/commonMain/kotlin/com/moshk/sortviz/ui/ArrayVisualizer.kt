package com.moshk.sortviz.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArrayVisualizer(
    array: Array<Int>,
    barsColor: Color = MaterialTheme.colorScheme.inverseSurface,
    comparing: Pair<Int, Int>? = null,
    comparingColor: Color = MaterialTheme.colorScheme.error,
    selected: Set<Int> = emptySet(),
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    swapping: Set<Pair<Int, Int>> = emptySet(),
    onSwapFinished: (Pair<Int, Int>) -> Unit = {},
    textSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = MaterialTheme.motionScheme.fastSpatialSpec(),
) {
    val textMeasurer = rememberTextMeasurer()

    val swapAnim = remember { Animatable(0f) }

    LaunchedEffect(swapping) {
        if (swapping.isNotEmpty()) {
            swapAnim.snapTo(0f)
            swapAnim.animateTo(1f, animationSpec)
            swapping.forEach { onSwapFinished(it) }
        }
    }
    val swapProgress = swapAnim.value

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
                textMeasurer,
                swapping,
                swapProgress
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
    textMeasurer: TextMeasurer,
    swapping: Set<Pair<Int, Int>>,
    swapProgress: Float
) {
    val width = size.width
    val height = size.height
    val maxValue = array.maxOrNull() ?: 0

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
            current = i,
            swapping,
            swapProgress
        )
    }


    if (swapping.isEmpty() && comparing != null &&
        comparing.first in 0 until array.size &&
        comparing.second in 0 until array.size) {
        drawArrow(
            array,
            selected = comparing,
            arrowColor = comparingColor,
            barWidth,
            barHeightMultiplier,
            maxValue,
            textMeasurer,
            textSize,
            textColor
        )
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
    current: Int,
    swapping: Set<Pair<Int, Int>>,
    swapProgress: Float,
) {
    var xOffset = barWidth * (current + 1)
    var drawValue = array[current]

    val swapPair = swapping.firstOrNull { it.first == current || it.second == current }

    if (swapPair != null) {
        val left = minOf(swapPair.first, swapPair.second)
        val right = maxOf(swapPair.first, swapPair.second)

        if (current == left) {
            xOffset = barWidth * (current + 1 + swapProgress)
            drawValue = array[right]
        } else if (current == right) {
            xOffset = barWidth * (current + 1 - swapProgress)
            drawValue = array[left]
        }
    }

    // ТЕКСТ (ЗНАЧЕНИЕ СТОЛБЦА)
    val text = drawValue.toString()
    val measuredText = textMeasurer.measure(
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
            xOffset + barWidth * 0.5f - measuredText.width * 0.5f,
            size.height - (measuredText.height.toFloat() * 1.5f)
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
            xOffset,
            barHeightMultiplier * (maxValue - drawValue + 2) -
                    (measuredText.height.toFloat() * 1.5f)
        ),
        size = Size(
            barWidth * 0.9f,
            barHeightMultiplier * drawValue.toFloat()
        )
    )
}

fun DrawScope.drawArrow(
    array: Array<Int>,
    selected: Pair<Int, Int>,
    arrowColor: Color,
    barWidth: Float,
    barHeightMultiplier: Float,
    maxValue: Int,
    textMeasurer: TextMeasurer,
    textSize: TextUnit,
    textColor: Color,
) {
    val start = getBarTop(array, index = selected.first, barWidth, barHeightMultiplier, maxValue, textMeasurer, textSize, textColor)

    val end = getBarTop(array, index = selected.second, barWidth, barHeightMultiplier, maxValue, textMeasurer, textSize, textColor)

    val quadraticControl = Offset(
        x = ((selected.first + selected.second) / 2f + 1.5f) * barWidth,
        y = -sqrt(start.y * end.y)
    )

    val controlY = calculateControlYForTangent(start.y, end.y)

    val control1 = Offset(start.x, controlY)
    val control2 = Offset(end.x, controlY)

    drawPath(
        path = Path().apply {
            moveTo(start.x, start.y)
            //quadraticTo(quadraticControl.x, quadraticControl.y, end.x, end.y)
            cubicTo(control1.x, control1.y, control2.x, control2.y, end.x, end.y)
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
//            join = StrokeJoin.Round
//            //pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 25f))
//        )
//    )

//    drawPath(
//        path = Path().apply {
//            moveTo(start.x, start.y)
//            lineTo(start.x, control.y)
//            lineTo(end.x, control.y)
//            lineTo(end.x, end.y)
//        },
//        color = arrowColor,
//        style = Stroke(
//            width = 4f,
//            cap = StrokeCap.Square,
//            join = StrokeJoin.Round
//            //pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 25f))
//        )
//    )

    drawTangentArrow(
        //start = start, control, 50f, 30f, arrowColor
        start = start, control = control1, length = 50f, angleDeg = 30f, color = arrowColor
    )
    drawTangentArrow(
        //start = start, control, 50f, -30f, arrowColor
        start = start, control = control1, length = 50f, angleDeg = -30f, color = arrowColor
    )

    drawTangentArrow(
        //start = end, control, 50f, 30f, arrowColor
        start = end, control2, 50f, 30f, arrowColor
    )
    drawTangentArrow(
        //start = end, control, 50f, -30f, arrowColor
        start = end, control2, 50f, -30f, arrowColor
    )
}

private fun calculateControlYForTangent(y0: Float, y3: Float): Float {
    if (abs(y0 - y3) < 1f) {
        return -y0 / 3f
    }

    val k = y3 / y0
    var left = 0.001f
    var right = 0.999f

    for (i in 0 until 20) {
        val mid = (left + right) / 2f
        val num = (1 - mid) * (1 - mid) * (1 - mid) * (1 + mid)
        val den = mid * mid * mid * (2 - mid)
        val fMid = num / den
        if (fMid > k) {
            left = mid
        } else {
            right = mid
        }
    }

    val t = (left + right) / 2f

    return ((1 - t) * (1 - t) * y0 - t * t * y3) / (1 - 2 * t)
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
    textMeasurer: TextMeasurer,
    textSize: TextUnit,
    textColor: Color
): Offset = Offset(
    x = (index + 1.5f) * barWidth,
    y = barHeightMultiplier * (maxValue - array[index] + 2) -
            (textMeasurer.measure(
                array[index].toString(),
                style = TextStyle(
                    fontSize = textSize,
                    color = textColor,
                )
            ).size.height.toFloat() * 1.5f)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            selected = selected,
            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
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