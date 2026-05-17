package com.moshk.sortviz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.window.Popup
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.window.core.layout.WindowSizeClass
import com.moshk.sortviz.ui.SortInfoPane
import com.moshk.sortviz.ui.SortListPane
import com.moshk.sortviz.ui.SortScreenPane
import com.moshk.sortviz.ui.theme.SortAppTheme
import kotlinx.coroutines.NonCancellable.key
import kotlinx.coroutines.launch

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
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val showExtraOnExpanded = remember { mutableStateOf(true) }

    val maxPartitions = when {
        windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            if (showExtraOnExpanded.value) 3 else 2
        windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 1
        else -> 1
    }

    val directive = PaneScaffoldDirective.Default.copy(
        maxHorizontalPartitions = maxPartitions
    )

    val navigator = rememberListDetailPaneScaffoldNavigator(scaffoldDirective = directive)
    val scope = rememberCoroutineScope()
    val backBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange
    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    // key можно убрать, но тогда анимации сломаются, если переходить в полноэкранный режим
    // не из третьего экрана
    key(maxPartitions) {
        NavigationBackHandler(
            state = navEventState,
            isBackEnabled = navigator.canNavigateBack(),
            onBackCancelled = {
                // Жест отменён (палец убран до завершения).
                // Можно сбросить кастомные анимации, если используете predictive back.
            },
            onBackCompleted = {
                // Жест завершён или нажата системная кнопка/ESC
                scope.launch {
                    navigator.navigateBack(backBehavior)
                }
            }
        )

        LaunchedEffect(navEventState.transitionState) {
            val state = navEventState.transitionState
            if (state is NavigationEventTransitionState.InProgress) {
                val progress = state.latestEvent.progress // 0.0f .. 1.0f
                // Здесь можно драйвить кастомные анимации, если нужно.
                // ListDetailPaneScaffold уже анимирует переходы панелей автоматически.
            }
        }

        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                SortListPane(
                    onItemSelected = {
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                    }
                )
            },
            detailPane = {
                SortScreenPane(
                    shouldShowExtraButton = navigator.scaffoldValue[ListDetailPaneScaffoldRole.Extra] == PaneAdaptedValue.Hidden,
                    onShowExtra = {
                        showExtraOnExpanded.value = true
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
                    },
                    onBack = {
                        if (navigator.canNavigateBack()) {
                            scope.launch { navigator.navigateBack(backBehavior) }
                        } else {
                            scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.List) }
                        }
                    },
                    isBackVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden
                )
            },
            extraPane = {
                SortInfoPane(
                    onBack = {
                        if (navigator.canNavigateBack()) {
                            scope.launch { navigator.navigateBack(backBehavior) }
                        } else {
                            scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                        }
                    },
                    onHideExtra = {
                        scope.launch {
                            showExtraOnExpanded.value = false
                            navigator.navigateBack()
                        }
                    },
                    isBackVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Hidden
                )
            }
        )
    }
}



@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@PreviewScreenSizes
@Composable
fun PreviewAppLight() {
    Surface(modifier = Modifier.fillMaxSize()) {
        App()
    }
}

@PreviewScreenSizes
@Composable
fun PreviewAppDark() {
    SortAppTheme(darkTheme = true) {
        PreviewAppLight()
    }
}