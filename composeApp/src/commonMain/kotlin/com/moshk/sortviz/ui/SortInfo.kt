package com.moshk.sortviz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.moshk.sortviz.ui.icons.arrow_back
import com.moshk.sortviz.ui.icons.visibility_off
import com.moshk.sortviz.ui.theme.SortAppTheme

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ThreePaneScaffoldPaneScope.SortInfoPane(
    onBack: () -> Unit,
    onHideExtra: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedPane(modifier = modifier) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SortInfo(
                onBack,
                onHideExtra,
                modifier
            )
        }
    }
}

@Composable
fun SortInfo(
    onBack: () -> Unit,
    onHideExtra: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBackVisible = currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp)
            .safeContentPadding()
    ) {
        Row(Modifier.height(64.dp)) {
            if (!isBackVisible) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        arrow_back,
                        "назад"
                    )
                }
            } else {
                Button(
                    onClick = onHideExtra,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        visibility_off,
                        "скрыть"
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(32.dp)) {
            Text("Дополнительная информация", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Здесь отображаются детали, настройки или контекстные данные.",
                style = MaterialTheme.typography.bodyMedium
            )
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
