package com.moshk.sortviz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moshk.sortviz.ui.theme.SortAppTheme
import kotlin.random.Random

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

@Composable
fun SortScreen(
    shouldShowExtraButton: Boolean,
    onShowExtra: () -> Unit,
    onBack: () -> Unit,
    isBackVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(16.dp),
    ) {
        Row (Modifier.height(64.dp)) {
            if (isBackVisible) {
                Button(
                    onClick = onBack,
                    Modifier.padding(end = 8.dp)
                ) {
                    Text("Назад к списку", style = MaterialTheme.typography.bodySmall)
                    // заменить на иконку
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
            val arrayLength = 10
            val array = Array(arrayLength) { i -> i + 1}
            val random = Random(42)
            array.shuffle(random)
            ArrayVisualizer(
                array = array,
                selected = random.nextInt(0, 10) to random.nextInt(0, 10),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    //.background(Color.Magenta)
            )

            Row() {
                Button(
                    onClick = {},
                    Modifier.width(80.dp).height(75.dp).padding(2.dp, 8.dp)
                ) {
                    Text("1", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {},
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