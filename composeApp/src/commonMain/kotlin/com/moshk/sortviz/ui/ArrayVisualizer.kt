package com.moshk.sortviz.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.ui.geometry.Rect
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

data class ArrayState(
    val array: Array<Int>,
    val id: String = "",
    val comparing: Pair<Int, Int>? = null,
    val comparingColor: Color = Color.Unspecified,
    val selected: Set<Int> = emptySet(),
    val selectedColor: Color = Color.Unspecified,
    val swapping: Set<Pair<Int, Int>> = emptySet(),
    val onSwapFinished: (Pair<Int, Int>) -> Unit = {},
    val barsColor: Color = Color.Unspecified,
)

data class ArrayArrow(
    val fromArrayId: String,
    val fromIndex: Int,
    val toArrayId: String,
    val toIndex: Int,
    val color: Color = Color.Unspecified,
)

// Оригинальная сигнатура для обратной совместимости
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
    ArrayVisualizer(
        arrays = listOf(
            ArrayState(
                array = array,
                id = "0",
                comparing = comparing,
                comparingColor = comparingColor,
                selected = selected,
                selectedColor = selectedColor,
                swapping = swapping,
                onSwapFinished = onSwapFinished,
                barsColor = barsColor
            )
        ),
        arrows = emptyList(),
        defaultBarsColor = barsColor,
        defaultComparingColor = comparingColor,
        defaultSelectedColor = selectedColor,
        textSize = textSize,
        textColor = textColor,
        modifier = modifier,
        animationSpec = animationSpec,
    )
}

// Новая сигнатура для нескольких массивов
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArrayVisualizer(
    arrays: List<ArrayState>,
    arrows: List<ArrayArrow> = emptyList(),
    defaultBarsColor: Color = MaterialTheme.colorScheme.inverseSurface,
    defaultComparingColor: Color = MaterialTheme.colorScheme.error,
    defaultSelectedColor: Color = MaterialTheme.colorScheme.primary,
    textSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = MaterialTheme.motionScheme.fastSpatialSpec(),
) {
    val textMeasurer = rememberTextMeasurer()

    val swapAnims = remember(arrays.map { it.id }) {
        arrays.associate { it.id to Animatable(0f) }
    }

    arrays.forEach { arrayState ->
        val anim = swapAnims[arrayState.id] ?: return@forEach
        LaunchedEffect(arrayState.swapping) {
            if (arrayState.swapping.isNotEmpty()) {
                anim.snapTo(0f)
                anim.animateTo(1f, animationSpec)
                arrayState.swapping.forEach { arrayState.onSwapFinished(it) }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMultipleArrays(
                arrays = arrays,
                arrows = arrows,
                defaultBarsColor = defaultBarsColor,
                defaultComparingColor = defaultComparingColor,
                defaultSelectedColor = defaultSelectedColor,
                textSize = textSize,
                textColor = textColor,
                textMeasurer = textMeasurer,
                swapAnims = swapAnims
            )
        }
    }
}

fun DrawScope.drawMultipleArrays(
    arrays: List<ArrayState>,
    arrows: List<ArrayArrow>,
    defaultBarsColor: Color,
    defaultComparingColor: Color,
    defaultSelectedColor: Color,
    textSize: TextUnit,
    textColor: Color,
    textMeasurer: TextMeasurer,
    swapAnims: Map<String, Animatable<Float, AnimationVector1D>>
) {
    if (arrays.isEmpty()) return

    val areas = calculateAreas(arrays.size, size)
    val areasMap = arrays.mapIndexed { i, s -> s.id to areas[i] }.toMap()

    val isPortrait = size.height > size.width
    val mainArrayId = arrays.firstOrNull()?.id ?: ""

    arrays.forEachIndexed { index, arrayState ->
        val area = areas[index]
        val swapProgress = swapAnims[arrayState.id]?.value ?: 0f

        drawBarChart(
            array = arrayState.array,
            barsColor = arrayState.barsColor.takeIf { it != Color.Unspecified } ?: defaultBarsColor,
            comparing = arrayState.comparing,
            comparingColor = arrayState.comparingColor.takeIf { it != Color.Unspecified } ?: defaultComparingColor,
            selected = arrayState.selected,
            selectedColor = arrayState.selectedColor.takeIf { it != Color.Unspecified } ?: defaultSelectedColor,
            textSize = textSize,
            textColor = textColor,
            textMeasurer = textMeasurer,
            swapping = arrayState.swapping,
            swapProgress = swapProgress,
            area = area
        )
    }

    drawInterArrayArrows(
        arrays = arrays,
        arrows = arrows,
        areas = areasMap,
        defaultColor = defaultComparingColor,
        textMeasurer = textMeasurer,
        textSize = textSize,
        textColor = textColor,
        isPortrait = isPortrait,
        mainArrayId = mainArrayId
    )
}

private fun calculateAreas(count: Int, canvasSize: Size): List<Rect> {
    if (count == 0) return emptyList()
    if (count == 1) return listOf(Rect(0f, 0f, canvasSize.width, canvasSize.height))

    val isPortrait = canvasSize.height > canvasSize.width
    val mainArea: Rect
    val additionalAreas: List<Rect>

    if (isPortrait) {
        val splitRatio = 0.5f
        val mainHeight = canvasSize.height * splitRatio
        mainArea = Rect(0f, 0f, canvasSize.width, mainHeight)

        val addCount = count - 1
        val addHeight = canvasSize.height - mainHeight
        val addWidth = canvasSize.width / addCount
        additionalAreas = (0 until addCount).map { i ->
            Rect(i * addWidth, mainHeight, (i + 1) * addWidth, canvasSize.height)
        }
    } else {
        val splitRatio = 0.5f
        val mainWidth = canvasSize.width * splitRatio
        mainArea = Rect(0f, 0f, mainWidth, canvasSize.height)

        val addCount = count - 1
        val addWidth = canvasSize.width - mainWidth
        val addHeight = canvasSize.height / addCount
        additionalAreas = (0 until addCount).map { i ->
            Rect(mainWidth, i * addHeight, canvasSize.width, (i + 1) * addHeight)
        }
    }

    return listOf(mainArea) + additionalAreas
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
    swapProgress: Float,
    area: Rect
) {
    val width = area.width
    val height = area.height
    val maxValue = array.maxOrNull() ?: 0

    val barWidth = width / (array.size + 2)
    val barHeightMultiplier = height / (maxValue + 2)

    for (i in array.indices) {
        drawBar(
            array = array, barsColor = barsColor, comparing = comparing, comparingColor = comparingColor,
            selected = selected, selectedColor = selectedColor, textSize = textSize, textColor = textColor,
            textMeasurer = textMeasurer, barWidth = barWidth, barHeightMultiplier = barHeightMultiplier,
            maxValue = maxValue, current = i, swapping = swapping, swapProgress = swapProgress, area = area
        )
    }

    if (swapping.isEmpty() && comparing != null &&
        comparing.first in 0 until array.size &&
        comparing.second in 0 until array.size) {
        drawArrow(
            array = array, selected = comparing, arrowColor = comparingColor, barWidth = barWidth,
            barHeightMultiplier = barHeightMultiplier, maxValue = maxValue, textMeasurer = textMeasurer,
            textSize = textSize, textColor = textColor, area = area
        )
    }
}

fun DrawScope.drawBar(
    array: Array<Int>, barsColor: Color, comparing: Pair<Int, Int>?, comparingColor: Color,
    selected: Set<Int>, selectedColor: Color, textSize: TextUnit, textColor: Color,
    textMeasurer: TextMeasurer, barWidth: Float, barHeightMultiplier: Float, maxValue: Int,
    current: Int, swapping: Set<Pair<Int, Int>>, swapProgress: Float, area: Rect
) {
    var xOffset = area.left + barWidth * (current + 1)
    var drawValue = array[current]

    val swapPair = swapping.firstOrNull { it.first == current || it.second == current }

    if (swapPair != null) {
        val left = minOf(swapPair.first, swapPair.second)
        val right = maxOf(swapPair.first, swapPair.second)

        if (current == left) {
            xOffset = area.left + barWidth * (current + 1 + swapProgress)
            drawValue = array[right]
        } else if (current == right) {
            xOffset = area.left + barWidth * (current + 1 - swapProgress)
            drawValue = array[left]
        }
    }

    val text = drawValue.toString()
    val measuredText = textMeasurer.measure(
        text, style = TextStyle(fontSize = textSize, color = textColor)
    ).size

    drawText(
        textMeasurer = textMeasurer, text = text,
        topLeft = Offset(
            xOffset + barWidth * 0.5f - measuredText.width * 0.5f,
            area.bottom - (measuredText.height.toFloat() * 1.5f)
        ),
        style = TextStyle(fontSize = 18.sp, color = textColor)
    )

    drawRect(
        color = if (comparing?.first == current || comparing?.second == current) comparingColor
        else if (current in selected) selectedColor
        else barsColor,
        topLeft = Offset(
            xOffset,
            area.top + barHeightMultiplier * (maxValue - drawValue + 2) - (measuredText.height.toFloat() * 1.5f)
        ),
        size = Size(barWidth * 0.9f, barHeightMultiplier * drawValue.toFloat())
    )
}

fun DrawScope.drawArrow(
    array: Array<Int>, selected: Pair<Int, Int>, arrowColor: Color, barWidth: Float,
    barHeightMultiplier: Float, maxValue: Int, textMeasurer: TextMeasurer,
    textSize: TextUnit, textColor: Color, area: Rect
) {
    val start = getBarTop(array, selected.first, barWidth, barHeightMultiplier, maxValue, area, textMeasurer, textSize, textColor)
    val end = getBarTop(array, selected.second, barWidth, barHeightMultiplier, maxValue, area, textMeasurer, textSize, textColor)

    val localStartY = start.y - area.top
    val localEndY = end.y - area.top
    val localControlY = calculateControlYForTangent(localStartY, localEndY)
    val controlY = area.top + localControlY

    val control1 = Offset(start.x, controlY)
    val control2 = Offset(end.x, controlY)

    drawPath(
        path = Path().apply {
            moveTo(start.x, start.y)
            cubicTo(control1.x, control1.y, control2.x, control2.y, end.x, end.y)
        },
        color = arrowColor,
        style = Stroke(width = 8f, cap = StrokeCap.Square)
    )

    drawTangentArrow(start, control1, 50f, 30f, arrowColor)
    drawTangentArrow(start, control1, 50f, -30f, arrowColor)
    drawTangentArrow(end, control2, 50f, 30f, arrowColor)
    drawTangentArrow(end, control2, 50f, -30f, arrowColor)
}

private fun calculateControlYForTangent(y0: Float, y3: Float): Float {
    if (abs(y0 - y3) < 1f) return -y0 / 3f

    val k = y3 / y0
    var left = 0.001f
    var right = 0.999f

    for (i in 0 until 20) {
        val mid = (left + right) / 2f
        val num = (1 - mid) * (1 - mid) * (1 - mid) * (1 + mid)
        val den = mid * mid * mid * (2 - mid)
        val fMid = num / den
        if (fMid > k) left = mid else right = mid
    }

    val t = (left + right) / 2f
    return ((1 - t) * (1 - t) * y0 - t * t * y3) / (1 - 2 * t)
}

private fun DrawScope.drawTangentArrow(
    start: Offset, control: Offset, length: Float, angleDeg: Float, color: Color
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
        path = Path().apply { moveTo(start.x, start.y); lineTo(endX, endY) },
        color = color, style = Stroke(width = 8f, cap = StrokeCap.Round)
    )
}

private fun getBarTop(
    array: Array<Int>, index: Int, barWidth: Float, barHeightMultiplier: Float,
    maxValue: Int, area: Rect, textMeasurer: TextMeasurer, textSize: TextUnit, textColor: Color
): Offset {
    val textHeight = textMeasurer.measure(
        array[index].toString(), style = TextStyle(fontSize = textSize, color = textColor)
    ).size.height.toFloat()

    return Offset(
        x = area.left + (index + 1.5f) * barWidth,
        y = area.top + barHeightMultiplier * (maxValue - array[index] + 2) - (textHeight * 1.5f)
    )
}

private fun getGlobalBarTop(
    array: Array<Int>, index: Int, area: Rect, textMeasurer: TextMeasurer, textSize: TextUnit, textColor: Color
): Offset {
    val maxValue = array.maxOrNull() ?: 0
    val barWidth = area.width / (array.size + 2)
    val barHeightMultiplier = area.height / (maxValue + 2)
    return getBarTop(array, index, barWidth, barHeightMultiplier, maxValue, area, textMeasurer, textSize, textColor)
}

private fun getGlobalBarBottom(
    array: Array<Int>, index: Int, area: Rect, textMeasurer: TextMeasurer, textSize: TextUnit, textColor: Color
): Offset {
    val textHeight = textMeasurer.measure(
        array[index].toString(), style = TextStyle(fontSize = textSize, color = textColor)
    ).size.height.toFloat()

    val barWidth = area.width / (array.size + 2)
    return Offset(
        x = area.left + (index + 1.5f) * barWidth,
        y = area.bottom - (textHeight * 1.5f)
    )
}

fun DrawScope.drawInterArrayArrows(
    arrays: List<ArrayState>, arrows: List<ArrayArrow>, areas: Map<String, Rect>,
    defaultColor: Color, textMeasurer: TextMeasurer, textSize: TextUnit, textColor: Color,
    isPortrait: Boolean, mainArrayId: String
) {
    for (arrow in arrows) {
        val fromArray = arrays.find { it.id == arrow.fromArrayId } ?: continue
        val toArray = arrays.find { it.id == arrow.toArrayId } ?: continue
        val fromArea = areas[arrow.fromArrayId] ?: continue
        val toArea = areas[arrow.toArrayId] ?: continue

        if (arrow.fromIndex !in fromArray.array.indices || arrow.toIndex !in toArray.array.indices) continue

        val color = arrow.color.takeIf { it != Color.Unspecified } ?: defaultColor

        if (isPortrait && arrow.fromArrayId != mainArrayId && arrow.toArrayId == mainArrayId) {
            // Вертикальная раскладка: стрелка от доп. массива к основному
            val startPos = getGlobalBarTop(fromArray.array, arrow.fromIndex, fromArea, textMeasurer, textSize, textColor)
            val endPos = getGlobalBarBottom(toArray.array, arrow.toIndex, toArea, textMeasurer, textSize, textColor)

            drawInterArrayArrowDoubleSided(startPos, endPos, color)
        } else if (!isPortrait) {
            // Горизонтальная раскладка: стрелка с изгибом
            val startPos = getGlobalBarTop(fromArray.array, arrow.fromIndex, fromArea, textMeasurer, textSize, textColor)
            val endPos = getGlobalBarTop(toArray.array, arrow.toIndex, toArea, textMeasurer, textSize, textColor)

            drawInterArrayArrowCurved(startPos, endPos, color)
        } else {
            // Фолбэк (например, от основного к доп. на вертикальной)
            val startPos = getGlobalBarTop(fromArray.array, arrow.fromIndex, fromArea, textMeasurer, textSize, textColor)
            val endPos = getGlobalBarTop(toArray.array, arrow.toIndex, toArea, textMeasurer, textSize, textColor)
            drawInterArrayArrowStraight(startPos, endPos, color)
        }
    }
}

private fun DrawScope.drawInterArrayArrowStraight(start: Offset, end: Offset, color: Color) {
    drawLine(color = color, start = start, end = end, strokeWidth = 4f, cap = StrokeCap.Round)
    drawArrowHead(end, start, color)
}

private fun DrawScope.drawInterArrayArrowDoubleSided(start: Offset, end: Offset, color: Color) {
    drawLine(color = color, start = start, end = end, strokeWidth = 4f, cap = StrokeCap.Round)
    drawArrowHead(end, start, color)
    drawArrowHead(start, end, color)
}

private fun DrawScope.drawInterArrayArrowCurved(start: Offset, end: Offset, color: Color) {
    val offset = 150f // Высота изгиба вверх
    // Контрольные точки с той же X, но меньшей Y дают вертикальные касательные на концах
    val control1 = Offset(start.x, start.y - offset)
    val control2 = Offset(end.x, end.y - offset)

    drawPath(
        path = Path().apply {
            moveTo(start.x, start.y)
            cubicTo(control1.x, control1.y, control2.x, control2.y, end.x, end.y)
        },
        color = color,
        style = Stroke(width = 4f, cap = StrokeCap.Round)
    )

    // Наконечник у end (касательная от control2 к end)
    drawArrowHead(end, control2, color)
}

private fun DrawScope.drawArrowHead(tip: Offset, base: Offset, color: Color) {
    val dx = tip.x - base.x
    val dy = tip.y - base.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist == 0f) return

    val angle = atan2(dy, dx)
    val arrowLength = 20f
    val arrowAngle = (PI / 6).toFloat() // 30 degrees

    val x1 = tip.x - arrowLength * cos(angle - arrowAngle)
    val y1 = tip.y - arrowLength * sin(angle - arrowAngle)
    val x2 = tip.x - arrowLength * cos(angle + arrowAngle)
    val y2 = tip.y - arrowLength * sin(angle + arrowAngle)

    drawPath(
        path = Path().apply { moveTo(tip.x, tip.y); lineTo(x1, y1); moveTo(tip.x, tip.y); lineTo(x2, y2) },
        color = color, style = Stroke(width = 4f, cap = StrokeCap.Round)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun PreviewSortVisualizerLight() {
    Surface(modifier = Modifier.fillMaxSize()) {
        val random = Random(42)
        val arrayLength = 10
        val array = Array(arrayLength) { i -> i + 1 }
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
    SortAppTheme(darkTheme = true) { PreviewSortVisualizerLight() }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview(showBackground = true, heightDp = 800, widthDp = 400, name = "Merge Sort - Portrait")
fun PreviewMergeSortVisualizerPortrait() {
    SortAppTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val mainArray = arrayOf(3, 1, 5, 4, 2, 8, 6, 7, 10, 9)
            val leftHalf = arrayOf(1, 3, 5)
            val rightHalf = arrayOf(2, 4)

            val arrays = listOf(
                ArrayState(
                    array = mainArray,
                    id = "main",
                    comparing = 2 to 4, // Сравниваем 5 и 2
                    comparingColor = MaterialTheme.colorScheme.error,
                    selected = setOf(0, 1), // Уже отсортированные 1 и 3
                    selectedColor = MaterialTheme.colorScheme.primary,
                    barsColor = MaterialTheme.colorScheme.inverseSurface
                ),
                ArrayState(
                    array = leftHalf,
                    id = "left",
                    selected = setOf(0, 1, 2), // Вся левая половина отсортирована
                    selectedColor = MaterialTheme.colorScheme.tertiary,
                    barsColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                ArrayState(
                    array = rightHalf,
                    id = "right",
                    comparing = 0 to 1, // Сравниваем 2 и 4
                    comparingColor = MaterialTheme.colorScheme.error,
                    barsColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            val arrows = listOf(
                ArrayArrow(
                    fromArrayId = "left", fromIndex = 2, // Берем 5 из левой половины
                    toArrayId = "main", toIndex = 2, // Кладем на позицию 2 в основном
                    color = MaterialTheme.colorScheme.primary
                ),
                ArrayArrow(
                    fromArrayId = "right", fromIndex = 0, // Берем 2 из правой половины
                    toArrayId = "main", toIndex = 0, // Кладем на позицию 0 в основном
                    color = MaterialTheme.colorScheme.tertiary
                )
            )

            ArrayVisualizer(
                arrays = arrays,
                arrows = arrows,
                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview(showBackground = true, heightDp = 400, widthDp = 800, name = "Merge Sort - Landscape")
fun PreviewMergeSortVisualizerLandscape() {
    SortAppTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val mainArray = arrayOf(3, 1, 5, 4, 2, 8, 6, 7, 10, 9)
            val leftHalf = arrayOf(1, 3, 5)
            val rightHalf = arrayOf(2, 4)

            val arrays = listOf(
                ArrayState(
                    array = mainArray,
                    id = "main",
                    comparing = 2 to 4,
                    comparingColor = MaterialTheme.colorScheme.error,
                    selected = setOf(0, 1),
                    selectedColor = MaterialTheme.colorScheme.primary,
                    barsColor = MaterialTheme.colorScheme.inverseSurface
                ),
                ArrayState(
                    array = leftHalf,
                    id = "left",
                    selected = setOf(0, 1, 2),
                    selectedColor = MaterialTheme.colorScheme.tertiary,
                    barsColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                ArrayState(
                    array = rightHalf,
                    id = "right",
                    comparing = 0 to 1,
                    comparingColor = MaterialTheme.colorScheme.error,
                    barsColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            val arrows = listOf(
                ArrayArrow(
                    fromArrayId = "left", fromIndex = 2,
                    toArrayId = "main", toIndex = 2,
                    color = MaterialTheme.colorScheme.primary
                ),
                ArrayArrow(
                    fromArrayId = "right", fromIndex = 0,
                    toArrayId = "main", toIndex = 0,
                    color = MaterialTheme.colorScheme.tertiary
                )
            )

            ArrayVisualizer(
                arrays = arrays,
                arrows = arrows,
                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
            )
        }
    }
}