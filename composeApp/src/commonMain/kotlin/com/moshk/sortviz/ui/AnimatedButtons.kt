package com.moshk.sortviz.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
/* TODO переделать нейрослоп */
@Composable
fun AnimatedButtonsExample() {
    // Состояние: false = 4 кнопки, true = 2 кнопки
    var showSecondSet by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = showSecondSet,
        label = "ButtonsSwitch",
        contentAlignment = androidx.compose.ui.Alignment.CenterStart,
        // Кастомная анимация (можно убрать для стандартной)
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
        }
    ) { isSecond ->
        Row {
            if (isSecond) {
                Button(
                    onClick = { showSecondSet = false },
                    Modifier.width(120.dp).height(75.dp).padding(2.dp, 8.dp)
                ) {
                    Text("A", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {},
                    Modifier.width(120.dp).height(75.dp).padding(2.dp, 8.dp)
                ) {
                    Text("B", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Button(
                    onClick = {},
                    Modifier.width(80.dp).height(75.dp).padding(2.dp, 8.dp)
                ) {
                    Text("1", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { showSecondSet = true },
                    Modifier.width(80.dp).height(75.dp).padding(2.dp, 8.dp)
                ) {
                    Text("2", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {},
                    Modifier.width(80.dp).height(75.dp).padding(2.dp, 8.dp)
                ) {
                    Text("3", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {},
                    Modifier.width(40.dp).height(75.dp).padding(2.dp, 8.dp)
                ) {
                    Text("4", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 150)
@Composable
fun AnimatedButtonsPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedButtonsExample()
        }
    }
}