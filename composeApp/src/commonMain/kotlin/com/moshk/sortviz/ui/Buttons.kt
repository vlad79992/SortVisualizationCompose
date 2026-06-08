package com.moshk.sortviz.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moshk.sortviz.ui.icons.pause
import com.moshk.sortviz.ui.icons.play_arrow
import com.moshk.sortviz.ui.icons.redo
import com.moshk.sortviz.ui.icons.restart_alt
import com.moshk.sortviz.ui.icons.speed
import com.moshk.sortviz.ui.icons.undo
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Buttons(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onPlayPauseClick: (Boolean) -> Unit,
    selectedSpeed: String,
    onChangeSpeedClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onResetClick: () -> Unit,
    canGoBack: Boolean = true,
    canGoForward: Boolean = true
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ControlButtonsGroup(
            isPlaying,
            onPlayPauseClick,
            selectedSpeed,
            onChangeSpeedClick,
            onPrevClick,
            onNextClick,
            onResetClick,
            canGoBack,
            canGoForward
        )
    }
}

private const val minVisibility = 0.01f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ControlButtonsGroup(
    isPlaying: Boolean,
    onPlayPauseClick: (Boolean) -> Unit,
    selectedSpeed: String,
    onChangeSpeedClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onResetClick: () -> Unit,
    canGoBack: Boolean = true,
    canGoForward: Boolean = true
) {
    val largeSize = ButtonDefaults.LargeContainerHeight

    val prevWeightAnim = remember { Animatable(if (canGoBack) 1f else 0f) }
    val nextWeightAnim = remember { Animatable(if (canGoForward) 1f else 0f) }

    val animationSpec = spring<Float>(stiffness = Spring.StiffnessMediumLow)

    LaunchedEffect(canGoBack) {
        val target = if (canGoBack) 1f else 0f
        if (prevWeightAnim.value != target) {
            delay(100L)
            prevWeightAnim.animateTo(target, animationSpec)
        }
    }

    LaunchedEffect(canGoForward) {
        val target = if (canGoForward) 1f else 0f
        if (nextWeightAnim.value != target) {
            delay(100L)
            nextWeightAnim.animateTo(target, animationSpec)
        }
    }

    val animatedPrevWeight = prevWeightAnim.value
    val animatedNextWeight = nextWeightAnim.value

    val playButtonWeight = 1.5f + (1f - animatedPrevWeight) + (1f - animatedNextWeight)

    ButtonGroup(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState)
        }
    ) {
        if (animatedPrevWeight > minVisibility) {
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = onPrevClick,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .weight(animatedPrevWeight)
                            .fillMaxHeight()
                            .animateWidth(interactionSource),
                        contentPadding = PaddingValues(0.dp),
                        shapes = ButtonDefaults.shapesFor(largeSize),
                        enabled = canGoBack
                    ) {
                        Icon(
                            undo,
                            contentDescription = "предыдущий",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                menuContent = {}
            )
        }

        customItem(
            buttonGroupContent = {
                val interactionSource = remember { MutableInteractionSource() }
                ToggleButton(
                    checked = isPlaying && canGoForward,
                    onCheckedChange = {
                        if (canGoForward)
                            onPlayPauseClick(it)
                        else
                            onResetClick()
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(playButtonWeight)
                        .fillMaxHeight()
                        .animateWidth(interactionSource),
                    contentPadding = PaddingValues(0.dp),
                    shapes = ToggleButtonDefaults.shapesFor(largeSize),
                    enabled = (animatedPrevWeight !in minVisibility..0.5f
                            && animatedNextWeight !in minVisibility..0.5f)
                ) {
                    Icon(
                        if (!canGoForward) {
                            restart_alt
                        } else if (isPlaying) {
                            pause
                        } else {
                            play_arrow
                        },
                        contentDescription = if (!canGoForward) {
                            "перезапустить"
                        } else if (isPlaying) {
                            "пауза"
                        } else {
                            "запустить"
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            menuContent = {}
        )

        if (animatedNextWeight > minVisibility) {
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = onNextClick,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .weight(animatedNextWeight)
                            .fillMaxHeight()
                            .animateWidth(interactionSource),
                        contentPadding = PaddingValues(0.dp),
                        shapes = ButtonDefaults.shapesFor(largeSize),
                        enabled = canGoForward
                    ) {
                        Icon(redo, contentDescription = "вперед", modifier = Modifier.size(24.dp))
                    }
                },
                menuContent = {}
            )
        }

        customItem(
            buttonGroupContent = {
                val interactionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = onChangeSpeedClick,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(0.75f)
                        .fillMaxHeight()
                        .animateWidth(interactionSource),
                    contentPadding = PaddingValues(0.dp),
                    shapes = ButtonDefaults.shapesFor(largeSize),
                ) {
                    Icon(
                        speed,
                        contentDescription = "изменить скорость",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(selectedSpeed, style = MaterialTheme.typography.labelLarge)
                }
            },
            menuContent = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 150)
@Composable
fun AnimatedButtonsPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Buttons(
                modifier = Modifier
                    .widthIn(max = 550.dp)
                    .heightIn(max = 64.dp)
                    .fillMaxWidth(),
                isPlaying = false,
                onPlayPauseClick = {},
                selectedSpeed = "1x",
                onChangeSpeedClick = {},
                onPrevClick = {},
                onNextClick = {},
                onResetClick = {},
                canGoBack = true,
                canGoForward = true
            )
        }
    }
}