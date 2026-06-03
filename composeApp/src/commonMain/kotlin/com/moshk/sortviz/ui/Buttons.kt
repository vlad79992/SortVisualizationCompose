package com.moshk.sortviz.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moshk.sortviz.ui.icons.speed
import com.moshk.sortviz.ui.icons.pause
import com.moshk.sortviz.ui.icons.play_arrow
import com.moshk.sortviz.ui.icons.redo
import com.moshk.sortviz.ui.icons.undo

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Buttons(

) {
    Box(
        modifier = Modifier
            .widthIn(max = 550.dp)
            .heightIn(max = 64.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        ControlButtonsGroup(
            { println("prev click")},
            { println("play/pause click") },
            { println("change speed click") },
            { println("next click") }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ControlButtonsGroup(
    onPrevClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onChangeSpeedClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val isPlaying = remember { mutableStateOf(false) }
    val selectedSpeed = remember { mutableStateOf("1x") }
    val speeds = listOf("1x", "2x", "3x", "4x")
    val largeSize = ButtonDefaults.LargeContainerHeight

    ButtonGroup(
        modifier = Modifier.fillMaxWidth().heightIn(min = largeSize),
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState)
        }
    ) {
        customItem(
            buttonGroupContent = {
                val interactionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = onPrevClick,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .animateWidth(interactionSource),
                    contentPadding = PaddingValues(0.dp),
                    shapes = ButtonDefaults.shapesFor(largeSize),
                ) {
                    Icon(undo, contentDescription = "предыдущий", modifier = Modifier.size(24.dp))
                }
            },
            menuContent = {}
        )

        customItem(
            buttonGroupContent = {
                val interactionSource = remember { MutableInteractionSource() }
                ToggleButton(
                    checked = isPlaying.value,
                    onCheckedChange = {
                            isPlaying.value = it
                            onPlayPauseClick()
                        },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .animateWidth(interactionSource),
                    contentPadding = PaddingValues(0.dp),
                    shapes = ToggleButtonDefaults.shapesFor(largeSize),
                ) {
                    Icon(
                        if (isPlaying.value) pause else play_arrow,
                        contentDescription = if (isPlaying.value) "пауза" else "запустить",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            menuContent = {}
        )

        customItem(
            buttonGroupContent = {
                val interactionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = onNextClick,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .animateWidth(interactionSource),
                    contentPadding = PaddingValues(0.dp),
                    shapes = ButtonDefaults.shapesFor(largeSize),
                ) {
                    Icon(redo, contentDescription = "вперед", modifier = Modifier.size(24.dp))
                }
            },
            menuContent = {}
        )

        customItem(
            buttonGroupContent = {
                val interactionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        val currentIndex = speeds.indexOf(selectedSpeed.value)
                        selectedSpeed.value = speeds[(currentIndex + 1) % speeds.size]
                        onChangeSpeedClick()
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
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
                    Text(selectedSpeed.value, style = MaterialTheme.typography.labelLarge)
                }
            },
            menuContent = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 150)
@Composable
fun AnimatedButtonsPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Buttons()
        }
    }
}