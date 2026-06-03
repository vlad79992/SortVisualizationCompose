package com.moshk.sortviz.core

/**
 * Состояние алгоритма на конкретном шаге.
 * @param S Тип состояния для визуализации (массив, массив массивов, список и т.д.)
 */
data class SortStep<S> (
    val state: S,
    val comparingIndices: Pair<SortIndex, SortIndex>? = null,
    val selectedIndices: Set<SortIndex> = emptySet()
)