package com.moshk.sortviz.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
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
fun SortVizAndInfoScaffold(
    parentNavigator: ThreePaneScaffoldNavigator<Any>,
    modifier: Modifier = Modifier
) {
    val supportingNavigator = rememberSupportingPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()
    val supportingNavEventState = rememberNavigationEventState(NavigationEventInfo.None)

    val isInfoVisible = supportingNavigator.scaffoldValue[SupportingPaneScaffoldRole.Supporting] == PaneAdaptedValue.Expanded

    // НОВОЕ: Проверяем, видна ли главная панель.
    // Если она видна, значит экран большой и панели расположены рядом (side-by-side).
    // Если не видна, значит экран маленький и Info открыта на весь экран.
    val isMainPaneVisible = supportingNavigator.scaffoldValue[SupportingPaneScaffoldRole.Main] == PaneAdaptedValue.Expanded

    if (isInfoVisible) {
        NavigationBackHandler(
            state = supportingNavEventState,
            isBackEnabled = true,
            onBackCompleted = {
                scope.launch { supportingNavigator.navigateBack() }
            }
        )
    }

    SupportingPaneScaffold(
        modifier = modifier,
        directive = supportingNavigator.scaffoldDirective,
        value = supportingNavigator.scaffoldValue,
        mainPane = {
            SortScreenPane(
                onShowExtra = {
                    scope.launch { supportingNavigator.navigateTo(SupportingPaneScaffoldRole.Supporting) }
                },
                onBack = {
                    if (parentNavigator.canNavigateBack()) {
                        scope.launch { parentNavigator.navigateBack() }
                    }
                },
                isBackVisible = true,
                shouldShowExtraButton = true
            )
        },
        supportingPane = {
            SortInfoPane(
                onBack = {
                    scope.launch { supportingNavigator.navigateBack() }
                },
                onHideExtra = {
                    scope.launch { supportingNavigator.navigateBack() }
                },
                isBackVisible = !isMainPaneVisible
            )
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@PreviewScreenSizes
@Composable
fun PreviewSortVizAndInfoScaffoldLight() {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()

    val maxPartitions = when {
        windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 3
        else -> 2
    }

    val directive = PaneScaffoldDirective.Default.copy(
        maxHorizontalPartitions = maxPartitions
    )
    val parentNavigator = rememberListDetailPaneScaffoldNavigator(scaffoldDirective = directive)

    // Создаем мок NavigationEventDispatcherOwner специально для Preview
    val dispatcherOwner = remember {
        object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = NavigationEventDispatcher()
        }
    }

    // Предоставляем его через CompositionLocalProvider
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides dispatcherOwner
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SortVizAndInfoScaffold(
                parentNavigator = parentNavigator
            )
        }
    }
}

@PreviewScreenSizes
@Composable
fun PreviewSortVizAndInfoScaffoldDark() {
    SortAppTheme(darkTheme = true) {
        PreviewSortVizAndInfoScaffoldLight()
    }
}