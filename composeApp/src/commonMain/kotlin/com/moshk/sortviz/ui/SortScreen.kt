package com.moshk.sortviz.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moshk.sortviz.core.sortAlgorithms.BubbleSortAlgorithm
import com.moshk.sortviz.ui.icons.arrow_back
import com.moshk.sortviz.ui.theme.SortAppTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ThreePaneScaffoldPaneScope.SortScreenPane(
    shouldShowExtraButton: Boolean,
    onShowExtra: () -> Unit,
    onBack: () -> Unit,
    isBackVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedPane(modifier = modifier) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SortScreen(
                shouldShowExtraButton,
                onShowExtra,
                onBack,
                isBackVisible,
                modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SortScreen(
    shouldShowExtraButton: Boolean,
    onShowExtra: () -> Unit,
    onBack: () -> Unit,
    isBackVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val arrayLength = 10
    var initialData = List(arrayLength) { i -> i + 1}.shuffled()
    val algorithm = remember {
        BubbleSortAlgorithm().apply {
            initialize(initialData)
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 1f, 2f, 4f)
    var speedIndex by remember { mutableStateOf(1) }
    val selectedSpeedString = speeds[speedIndex].toString().removeSuffix(".0").removePrefix("0") + "x"
    val delayMillis = (1000L / speeds[speedIndex]).toLong()

    val animationSpec: AnimationSpec<Float> = remember(speedIndex) {
        val stiffness = Spring.StiffnessMedium * speeds[speedIndex]

        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = stiffness.coerceIn(Spring.StiffnessLow, Spring.StiffnessHigh),
            visibilityThreshold = 0.005f
        )
    }

    LaunchedEffect(isPlaying, speedIndex) {
        if (isPlaying) {
            while (isPlaying) {
                val hasNext = algorithm.next()
                if (!hasNext) {
                    isPlaying = false
                    break
                }
                delay(delayMillis)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .safeContentPadding(),
    ) {
        Row (Modifier.height(64.dp)) {
            if (isBackVisible) {
                Button(
                    onClick = onBack,
                    Modifier
                        .padding(end = 8.dp)
                        .fillMaxHeight()
                ) {
                    Icon(
                        arrow_back,
                        "назад"
                    )
                }
            }
            Column (
                Modifier.clickable(onClick = onShowExtra, enabled = shouldShowExtraButton)
                    .safeContentPadding()
                    .fillMaxWidth()
            ){
                Text(
                    "SORT",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black)
                if (shouldShowExtraButton) {
                    Text(
                        "Подробнее",
                        Modifier.padding(0.dp, 0.dp, 8.dp, 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

        }


        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            algorithm.RenderVisualizer(
                modifier = Modifier
                    //.background(Color.Magenta)
                    .fillMaxWidth()
                    .weight(1f),
                animationSpec = animationSpec
            )

            Buttons(
                modifier = Modifier
                    .widthIn(max = 550.dp)
                    .heightIn(max = ButtonDefaults.LargeContainerHeight)
                    .fillMaxWidth(),
                isPlaying = isPlaying,
                onPlayPauseClick = { isPlaying = it },
                selectedSpeed = selectedSpeedString,
                onChangeSpeedClick = {
                    speedIndex = (speedIndex + 1) % speeds.size
                },
                onPrevClick = {
                    isPlaying = false
                    algorithm.previous()
                },
                onNextClick = {
                    isPlaying = false
                    algorithm.next()
                },
                onResetClick = {
                    isPlaying = false
                    initialData = initialData.shuffled()
                    algorithm.reset(initialData)
                },
                canGoBack = algorithm.canGoBack,
                canGoForward = algorithm.canGoForward
            )
        }
    }
}

@Composable
@Preview
fun PreviewSortScreenLight() {
    Surface(modifier = Modifier.fillMaxSize()) {
        SortScreen(
            true,
            {},
            {},
            true,
            Modifier
        )
    }
}

@Composable
@Preview
fun PreviewSortScreenDark() {
    SortAppTheme(darkTheme = true) {
        PreviewSortScreenLight()
    }
}