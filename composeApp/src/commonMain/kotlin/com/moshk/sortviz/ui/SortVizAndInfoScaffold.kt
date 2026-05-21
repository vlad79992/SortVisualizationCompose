package com.moshk.sortviz.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    val navigator = rememberSupportingPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()

    var isSupportingForceHidden by remember { mutableStateOf(false) }

    val isMainVisible = navigator.scaffoldValue[SupportingPaneScaffoldRole.Main] == PaneAdaptedValue.Expanded
    val isActuallyVisible = navigator.scaffoldValue[SupportingPaneScaffoldRole.Supporting] == PaneAdaptedValue.Expanded && !isSupportingForceHidden

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = isActuallyVisible && !isMainVisible,
        onBackCompleted = { scope.launch { navigator.navigateBack() } }
    )

    SupportingPaneScaffold(
        modifier = modifier,
        directive = navigator.scaffoldDirective,
        value = if (isSupportingForceHidden) {
            ThreePaneScaffoldValue(
                primary = PaneAdaptedValue.Expanded,
                secondary = PaneAdaptedValue.Hidden,
                tertiary = PaneAdaptedValue.Hidden
            )
        } else {
            navigator.scaffoldValue
        },
        mainPane = {
            SortScreenPane(
                onShowExtra = {
                    isSupportingForceHidden = false
                    scope.launch { navigator.navigateTo(SupportingPaneScaffoldRole.Supporting) }
                },
                onBack = {
                    if (parentNavigator.canNavigateBack()) {
                        scope.launch { parentNavigator.navigateBack() }
                    } else {
                        scope.launch { parentNavigator.navigateTo(ThreePaneScaffoldRole.Primary) }
                    }
                },
                isBackVisible = !currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND),
                shouldShowExtraButton = !isActuallyVisible
            )
        },
        supportingPane = {
            SortInfoPane(
                onBack = { scope.launch { navigator.navigateBack() } },
                onHideExtra = {
                    if (isMainVisible) {
                        isSupportingForceHidden = true
                    } else {
                        scope.launch { navigator.navigateBack() }
                    }
                },
                isBackVisible = !isMainVisible
            )
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@PreviewScreenSizes
@Composable
fun PreviewSortVizAndInfoScaffoldLight() {
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides remember {
            object : NavigationEventDispatcherOwner {
                override val navigationEventDispatcher = NavigationEventDispatcher()
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SortVizAndInfoScaffold(
                parentNavigator = rememberListDetailPaneScaffoldNavigator(
                    scaffoldDirective = PaneScaffoldDirective.Default.copy(
                        maxHorizontalPartitions = if (
                            currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
                        ) 3 else 2
                    )
                )
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