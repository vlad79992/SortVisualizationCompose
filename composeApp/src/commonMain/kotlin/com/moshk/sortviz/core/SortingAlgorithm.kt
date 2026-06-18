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
import com.moshk.sortviz.ui.ArrayArrow
import com.moshk.sortviz.ui.ArrayState
import com.moshk.sortviz.ui.ArrayVisualizer
import kotlin.also

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

                val stateClone: S = when (val st = step.state) {
                    is Array<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        ((st as Array<Int>).clone().apply {
                            step.swappingIndices.forEach { (a, b) ->
                                if (a is SortIndex.Single && b is SortIndex.Single) {
                                    val i = a.index
                                    val j = b.index
                                    this[i] = this[j].also { this[j] = this[i] }
                                }
                            }
                        }) as S
                    }
                    else -> step.state // Для сложных состояний (Map/List) возвращаем оригинал
                }

                currentStep = step.copy(
                    swappingIndices = step.swappingIndices.map { (first, second) -> second to first }.toSet(),
                    state = stateClone,
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
    fun reset() { initialData?.let { initialize(it) } }

    fun reset(initialData: List<Int>) { initialize(initialData) }

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
                val stateMap = step.state as? Map<Int, Array<Int>>
                val stateList = step.state as? List<Array<Int>>
                val singleArray = step.state as? Array<Int>

                val arrayStates = mutableListOf<ArrayState>()
                val arrayArrows = mutableListOf<ArrayArrow>()

                when {
                    stateMap != null -> {
                        stateMap.forEach { (bufferId, array) ->
                            arrayStates.add(
                                ArrayState(
                                    array = array, id = bufferId.toString(),
                                    comparing = extractComparing(step.comparingIndices, bufferId),
                                    selected = extractSelected(step.selectedIndices, bufferId),
                                    swapping = extractSwapping(step.swappingIndices, bufferId)
                                )
                            )
                        }
                        arrayArrows.addAll(extractArrows(step.comparingIndices, step.swappingIndices))
                    }
                    stateList != null -> {
                        stateList.forEachIndexed { index, array ->
                            arrayStates.add(
                                ArrayState(
                                    array = array, id = index.toString(),
                                    comparing = extractComparing(step.comparingIndices, index),
                                    selected = extractSelected(step.selectedIndices, index),
                                    swapping = extractSwapping(step.swappingIndices, index)
                                )
                            )
                        }
                        arrayArrows.addAll(extractArrows(step.comparingIndices, step.swappingIndices))
                    }
                    singleArray != null -> {
                        arrayStates.add(
                            ArrayState(
                                array = singleArray, id = "0",
                                comparing = extractSingleComparing(step.comparingIndices),
                                selected = step.selectedIndices.mapNotNull { (it as? SortIndex.Single)?.index }.toSet(),
                                swapping = extractSingleSwapping(step.swappingIndices)
                            )
                        )
                    }
                    else -> error("Default visualizer requires S == Array<Int>, List<Array<Int>>, or Map<Int, Array<Int>>.")
                }

                ArrayVisualizer(
                    arrays = arrayStates,
                    arrows = arrayArrows,
                    animationSpec = animationSpec,
                    modifier = modifier
                )
            }
        } else {
            val initial = initialData ?: return
            val array = initial.toTypedArray()
            ArrayVisualizer(
                array = array, comparing = null, selected = emptySet(),
                animationSpec = animationSpec, modifier = modifier
            )
        }
    }

    @Suppress("unused")
    @Composable
    fun RenderDescription() {
        if (customDescription != null) customDescription!!() else Text(description)
    }

    // --- Вспомогательные методы для извлечения состояний из SortIndex ---

    private fun extractComparing(comparingIndices: Pair<SortIndex, SortIndex>?, bufferId: Int): Pair<Int, Int>? {
        if (comparingIndices == null) return null
        val (first, second) = comparingIndices
        val f = when (first) {
            is SortIndex.Single -> if (bufferId == 0) first.index else null
            is SortIndex.Composite -> if (first.bufferId == bufferId) first.index else null
        }
        val s = when (second) {
            is SortIndex.Single -> if (bufferId == 0) second.index else null
            is SortIndex.Composite -> if (second.bufferId == bufferId) second.index else null
        }
        return if (f != null && s != null) f to s else null
    }

    private fun extractSelected(selectedIndices: Set<SortIndex>, bufferId: Int): Set<Int> {
        return selectedIndices.mapNotNull { idx ->
            when (idx) {
                is SortIndex.Single -> if (bufferId == 0) idx.index else null
                is SortIndex.Composite -> if (idx.bufferId == bufferId) idx.index else null
            }
        }.toSet()
    }

    private fun extractSwapping(swappingIndices: Set<Pair<SortIndex, SortIndex>>, bufferId: Int): Set<Pair<Int, Int>> {
        return swappingIndices.mapNotNull { pair ->
            val (first, second) = pair
            val f = when (first) {
                is SortIndex.Single -> if (bufferId == 0) first.index else null
                is SortIndex.Composite -> if (first.bufferId == bufferId) first.index else null
            }
            val s = when (second) {
                is SortIndex.Single -> if (bufferId == 0) second.index else null
                is SortIndex.Composite -> if (second.bufferId == bufferId) second.index else null
            }
            if (f != null && s != null) f to s else null
        }.toSet()
    }

    private fun extractArrows(comparingIndices: Pair<SortIndex, SortIndex>?, swappingIndices: Set<Pair<SortIndex, SortIndex>>): List<ArrayArrow> {
        val arrows = mutableListOf<ArrayArrow>()

        comparingIndices?.let { cmp ->
            val fId = when (val f = cmp.first) { is SortIndex.Single -> "0"; is SortIndex.Composite -> f.bufferId.toString() }
            val fIdx = when (val f = cmp.first) { is SortIndex.Single -> f.index; is SortIndex.Composite -> f.index }
            val sId = when (val s = cmp.second) { is SortIndex.Single -> "0"; is SortIndex.Composite -> s.bufferId.toString() }
            val sIdx = when (val s = cmp.second) { is SortIndex.Single -> s.index; is SortIndex.Composite -> s.index }
            if (fId != sId) arrows.add(ArrayArrow(fId, fIdx, sId, sIdx))
        }

        swappingIndices.forEach { pair ->
            val (first, second) = pair
            val fId = when (first) { is SortIndex.Single -> "0"; is SortIndex.Composite -> first.bufferId.toString() }
            val fIdx = when (first) { is SortIndex.Single -> first.index; is SortIndex.Composite -> first.index }
            val sId = when (second) { is SortIndex.Single -> "0"; is SortIndex.Composite -> second.bufferId.toString() }
            val sIdx = when (second) { is SortIndex.Single -> second.index; is SortIndex.Composite -> second.index }
            if (fId != sId) arrows.add(ArrayArrow(fId, fIdx, sId, sIdx))
        }

        return arrows
    }

    private fun extractSingleComparing(comparingIndices: Pair<SortIndex, SortIndex>?): Pair<Int, Int>? {
        if (comparingIndices == null) return null
        val f = (comparingIndices.first as? SortIndex.Single)?.index ?: return null
        val s = (comparingIndices.second as? SortIndex.Single)?.index ?: return null
        return f to s
    }

    private fun extractSingleSwapping(swappingIndices: Set<Pair<SortIndex, SortIndex>>): Set<Pair<Int, Int>> {
        return swappingIndices.mapNotNull { pair ->
            val f = (pair.first as? SortIndex.Single)?.index ?: return@mapNotNull null
            val s = (pair.second as? SortIndex.Single)?.index ?: return@mapNotNull null
            f to s
        }.toSet()
    }
}