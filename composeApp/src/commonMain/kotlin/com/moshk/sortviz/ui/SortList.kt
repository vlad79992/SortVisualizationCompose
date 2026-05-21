package com.moshk.sortviz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moshk.sortviz.ui.theme.SortAppTheme


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ThreePaneScaffoldPaneScope.SortListPane(
    onItemSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedPane(modifier = modifier) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SortList(
                onItemSelected,
                modifier
            )
        }
    }
}

@Composable
fun SortList(
    onItemSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Сортировки",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Start
        )
        LazyColumn (
            modifier = Modifier.fillMaxSize()
        ) {
            items(10) { index ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier
                        .height(100.dp)
                        .padding(8.dp, 4.dp)
                        .fillMaxWidth(1f),
                    onClick = { onItemSelected() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeContentPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "sort $index",
                            modifier = Modifier,
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun PreviewSortListLight() {
    Surface(modifier = Modifier.fillMaxSize()) {
        SortList(
            {},
            Modifier
        )
    }
}

@Composable
@Preview
fun PreviewSortListDark() {
    SortAppTheme(darkTheme = true) {
        PreviewSortListLight()
    }
}
