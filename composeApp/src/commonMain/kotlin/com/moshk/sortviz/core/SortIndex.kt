package com.moshk.sortviz.core

sealed interface SortIndex {
    data class Single(val index: Int) : SortIndex
    data class Composite(val bufferId: Int, val index: Int) : SortIndex
}