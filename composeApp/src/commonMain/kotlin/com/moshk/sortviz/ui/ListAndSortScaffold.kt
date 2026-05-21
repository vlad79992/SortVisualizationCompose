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
import com.moshk.sortviz.ui.theme.SortAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListAndSortScaffold() {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()

    val maxPartitions = when {
        windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 2
        else -> 1
    }

    val directive = PaneScaffoldDirective.Default.copy(
        maxHorizontalPartitions = maxPartitions
    )

    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator(scaffoldDirective = directive)
    val scope = rememberCoroutineScope()

    // Создаем УНИКАЛЬНЫЙ state для этого уровня навигации
    // NavigationEventInfo.None означает, что у нас нет специфичных данных для этого хендлера,
    // но он нужен для корректной работы системы предиктивного бэка.
    val listNavEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = listNavEventState,
        isBackEnabled = listDetailNavigator.canNavigateBack(),
        onBackCancelled = {
            // Жест отменен
        },
        onBackCompleted = {
            scope.launch {
                listDetailNavigator.navigateBack()
            }
        }
    )

    ListDetailPaneScaffold(
        directive = listDetailNavigator.scaffoldDirective,
        value = listDetailNavigator.scaffoldValue,
        listPane = {
            SortListPane(
                onItemSelected = {
                    scope.launch {
                        listDetailNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                    }
                }
            )
        },
        detailPane = {
            // Вложенный скэффолд получает свой собственный контекст
            AnimatedPane (modifier = Modifier.fillMaxSize()) {
                SortVizAndInfoScaffold(
                    modifier = Modifier.fillMaxSize(),
                    parentNavigator = listDetailNavigator
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@PreviewScreenSizes
@Composable
fun PreviewListAndSortScaffoldLight() {
    // Создаем мок NavigationEventDispatcherOwner специально для Preview
    val dispatcherOwner = remember {
        object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = NavigationEventDispatcher()
        }
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides dispatcherOwner
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