package com.moshk.sortviz.core

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.moshk.sortviz.ui.ArrayVisualizer
import kotlin.also
import kotlin.collections.forEach

abstract class SortingAlgorithm<S : Any>(
    open val customVisualizer: (@Composable (step: SortStep<S>) -> Unit)? = null,
    open val customDescription: (@Composable () -> Unit)? = null
) {
    abstract val name: String
    abstract val description: String

    private val historyStack = ArrayDeque<SortStep<S>>()
    private val forwardStack = ArrayDeque<SortStep<S>>()
    var currentStep by mutableStateOf<SortStep<S>?>(null)
        protected set

    var canGoBack by mutableStateOf(false)
        private set

    var canGoForward by mutableStateOf(false)
        private set

    private fun updateNavigationState() {
        canGoBack = historyStack.isNotEmpty()
        canGoForward = stepIterator?.hasNext() == true
    }

    private var stepIterator: Iterator<SortStep<S>>? = null
    private var initialData: List<Int>? = null

    abstract fun generateSteps(initialData: List<Int>): Sequence<SortStep<S>>

    fun initialize(initialData: List<Int>) {
        this@SortingAlgorithm.initialData = initialData
        historyStack.clear()
        forwardStack.clear()
        currentStep = null
        stepIterator = generateSteps(initialData).iterator()
        updateNavigationState()
    }

    fun next(): Boolean {
        return if (forwardStack.isNotEmpty()) {
            val step = forwardStack.removeLast()
            historyStack.addLast(step)
            currentStep = step
            updateNavigationState()
            true
        } else {
            val it = stepIterator ?: return false
            if (it.hasNext()) {
                val step = it.next()
                historyStack.addLast(step)
                currentStep = step
                updateNavigationState()
                true
            } else false
        }
    }

    fun previous(): Boolean {
        return when {
            historyStack.size > 1 -> {
                val step = historyStack.removeLast()
                forwardStack.addLast(step)

                @Suppress("UNCHECKED_CAST")
                currentStep = step.copy(
                    swappingIndices = step.swappingIndices.map { (first, second) -> second to first }.toSet(),
                    state = ((step.state as Array<Int>).clone().apply {
                        step.swappingIndices.forEach { (a, b) ->
                            if (a is SortIndex.Single && b is SortIndex.Single) {
                                val i = a.index
                                val j = b.index
                                this[i] = this[j].also { this[j] = this[i] }
                            }
                        }
                    }) as S,
                    selectedIndices = step.selectedIndices,
                    comparingIndices = step.comparingIndices
                )

                updateNavigationState()
                true
            }
            historyStack.size == 1 -> {
                val step = historyStack.removeLast()
                forwardStack.addLast(step)
                currentStep = null
                updateNavigationState()
                true
            }
            else -> false
        }
    }

    @Suppress("unused")
    fun reset() {
        initialData?.let { initialize(it) }
    }
    fun reset(initialData: List<Int>) {
        initialize(initialData)
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun RenderVisualizer(
        modifier: Modifier = Modifier,
        animationSpec: AnimationSpec<Float> = MaterialTheme.motionScheme.slowSpatialSpec()
    ) {
        val step = currentStep

        if (step != null) {
            if (customVisualizer != null) {
                customVisualizer!!(step)
            } else {
                @Suppress("UNCHECKED_CAST")
                val array = step.state as? Array<Int>
                    ?: error("Default visualizer requires S == Array<Int>.")

                val stdComparing = when (val cmp = step.comparingIndices) {
                    null -> null
                    else -> when (cmp.first) {
                        is SortIndex.Single -> ((cmp.first as SortIndex.Single).index to (cmp.second as SortIndex.Single).index)
                        is SortIndex.Composite -> null
                    }
                }

                val stdSwapping = step.swappingIndices.mapNotNull { swp ->
                    when (swp.first) {
                        is SortIndex.Single -> ((swp.first as SortIndex.Single).index to (swp.second as SortIndex.Single).index)
                        is SortIndex.Composite -> null
                    }
                }.toSet()

                ArrayVisualizer(
                    array = array,
                    comparing = stdComparing,
                    selected = step.selectedIndices.mapNotNull { (it as? SortIndex.Single)?.index }.toSet(),
                    swapping = stdSwapping,
                    animationSpec = animationSpec,
                    modifier = modifier
                )
            }
        } else {
            val initial = initialData ?: return
            val array = initial.toTypedArray()
            ArrayVisualizer(
                array = array,
                comparing = null,
                selected = emptySet(),
                animationSpec = animationSpec,
                modifier = modifier
            )
        }
    }

    @Suppress("unused")
    @Composable
    fun RenderDescription() {
        if (customDescription != null) customDescription!!() else Text(description)
    }
}