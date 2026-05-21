package com.moshk.sortviz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moshk.sortviz.ui.theme.SortAppTheme

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ThreePaneScaffoldPaneScope.SortInfoPane(
    onBack: () -> Unit,
    onHideExtra: () -> Unit,
    isBackVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedPane(modifier = modifier) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SortInfo(
                onBack,
                onHideExtra,
                isBackVisible,
                modifier
            )
        }
    }
}

@Composable
fun SortInfo(
    onBack: () -> Unit,
    onHideExtra: () -> Unit,
    isBackVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp, 32.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            if (isBackVisible) {
                Button(onClick = onBack) {
                    Text("Назад")
                }
            } else {
                Button(onClick = onHideExtra) {
                    Text("Скрыть")
                }
            }
        }

        Column(modifier = Modifier.padding(32.dp)) {
            Text("Дополнительная информация", style = MaterialTheme.typography.headlineSmall)
            Text("Здесь отображаются детали, настройки или контекстные данные.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
@Preview
fun PreviewSortInfoLight() {
    Surface(modifier = Modifier.fillMaxSize()) {
        SortInfo(
            {},
            {},
            false,
            Modifier
        )
    }
}

@Composable
@Preview
fun PreviewSortInfoDark() {
    SortAppTheme(darkTheme = true) {
        PreviewSortInfoLight()
    }
}
