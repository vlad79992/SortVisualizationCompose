package com.moshk.sortviz.core

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.moshk.sortviz.ui.ArrayVisualizer

abstract class SortingAlgorithm<S : Any>(
    open val customVisualizer: (@Composable (step: SortStep<S>) -> Unit)? = null,
    open val customDescription: (@Composable () -> Unit)? = null
) {
    abstract val name: String
    abstract val description: String

    private val historyStack = ArrayDeque<SortStep<S>>()
    var currentStep by mutableStateOf<SortStep<S>?>(null)
        protected set

    private var stepIterator: Iterator<SortStep<S>>? = null
    private var initialData: List<Int>? = null

    abstract fun generateSteps(initialData: List<Int>): Sequence<SortStep<S>>

    fun initialize(initialData: List<Int>) {
        this@SortingAlgorithm.initialData = initialData
        historyStack.clear()
        currentStep = null
        stepIterator = generateSteps(initialData).iterator()
    }

    fun next(): Boolean {
        val it = stepIterator ?: return false
        return if (it.hasNext()) {
            val step = it.next()
            historyStack.addLast(step)
            currentStep = step
            true
        } else false
    }

    fun previous(): Boolean {
        return when {
            historyStack.size > 1 -> {
                historyStack.removeLast()
                currentStep = historyStack.last()
                true
            }
            historyStack.size == 1 -> {
                historyStack.clear()
                currentStep = null
                true
            }
            else -> false
        }
    }

    fun reset() {
        initialData?.let { initialize(it) }
    }

    val currentComparingIndices: Pair<SortIndex, SortIndex>? get() = currentStep?.comparingIndices

    @Composable
    fun RenderVisualizer(modifier: Modifier = Modifier) {
        val step = currentStep ?: return
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
                    is SortIndex.Composite -> null // Со стандартным визуализатором не заработает
                }
            }

            ArrayVisualizer(
                array = array,
                comparing = stdComparing,
                selected = step.selectedIndices.mapNotNull { (it as? SortIndex.Single)?.index }.toSet(),
                modifier = modifier
            )
        }
    }

    @Composable
    fun RenderDescription() {
        if (customDescription != null) customDescription!!() else Text(description)
    }
}