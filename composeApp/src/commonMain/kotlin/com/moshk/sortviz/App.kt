package com.moshk.sortviz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moshk.sortviz.ui.ListAndSortScaffold
import com.moshk.sortviz.ui.theme.SortAppTheme

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App() {
    SortAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SortVizScaffold()
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SortVizScaffold() {
    SortAppTheme {
        ListAndSortScaffold()
    }
}