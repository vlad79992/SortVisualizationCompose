package com.moshk.sortviz.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.*
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.window.core.layout.WindowSizeClass
import com.moshk.sortviz.core.SortingAlgorithm
import com.moshk.sortviz.ui.theme.SortAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListAndSortScaffold() {
    val scope = rememberCoroutineScope()

    // Указываем тип контента, который будет передаваться при навигации
    val navigator = rememberListDetailPaneScaffoldNavigator<SortingAlgorithm<*>>(
        scaffoldDirective = PaneScaffoldDirective.Default.copy(
            maxHorizontalPartitions = if (
                currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
                )
            ) 2 else 1
        )
    )

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = navigator.canNavigateBack(),
        onBackCompleted = { scope.launch { navigator.navigateBack() } }
    )

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            SortListPane(
                onItemSelected = { algorithm ->
                    // Передаем выбранный алгоритм в Detail Pane
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, algorithm) }
                }
            )
        },
        detailPane = {
            AnimatedPane(modifier = Modifier.fillMaxSize()) {
                // Получаем алгоритм из навигатора
                val algorithm = navigator.currentDestination?.contentKey
                if (algorithm != null) {
                    SortVizAndInfoScaffold(
                        modifier = Modifier.fillMaxSize(),
                        parentNavigator = navigator,
                        algorithm = algorithm
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@PreviewScreenSizes
@Composable
fun PreviewListAndSortScaffoldLight() {
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides remember {
            object : NavigationEventDispatcherOwner {
                override val navigationEventDispatcher = NavigationEventDispatcher()
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ListAndSortScaffold()
        }
    }
}

@PreviewScreenSizes
@Composable
fun PreviewListAndSortScaffoldDark() {
    SortAppTheme(darkTheme = true) {
        PreviewListAndSortScaffoldLight()
    }
}