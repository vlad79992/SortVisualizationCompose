package com.moshk.sortviz.core.sortAlgorithms

import com.moshk.sortviz.core.SortIndex
import com.moshk.sortviz.core.SortStep
import com.moshk.sortviz.core.SortingAlgorithm

class BubbleSortAlgorithm : SortingAlgorithm<Array<Int>>() {
    override val name = "Bubble Sort"
    override val description = "Простой алгоритм сортировки. Многократно проходит по массиву, сравнивая соседние элементы и меняя их местами, если они стоят в неправильном порядке. После каждого прохода наибольший элемент «всплывает» в конец."

    override fun generateSteps(initialData: List<Int>): Sequence<SortStep<Array<Int>>> = sequence {
        val arr = initialData.toTypedArray()
        val n = arr.size
        val sortedIndices = mutableSetOf<SortIndex>()

        for (i in 0 until n - 1) {
            var swapped = false
            for (j in 0 until n - i - 1) {
                // 1. Шаг сравнения
                yield(SortStep(
                    state = arr.copyOf(),
                    comparingIndices = SortIndex.Single(j) to SortIndex.Single(j + 1),
                    selectedIndices = sortedIndices.toSet()
                ))

                // 2. Обмен при нарушении порядка
                if (arr[j] > arr[j + 1]) {
                    val tmp = arr[j]
                    arr[j] = arr[j + 1]
                    arr[j + 1] = tmp
                    swapped = true

                    // Фиксируем состояние после обмена (даёт плавную анимацию перестановки)
                    yield(SortStep(
                        state = arr.copyOf(),
                        comparingIndices = SortIndex.Single(j) to SortIndex.Single(j + 1),
                        selectedIndices = sortedIndices.toSet(),
                        swappingIndices = setOf(SortIndex.Single(j) to SortIndex.Single(j + 1))
                    ))
                }
            }

            // Элемент занял финальную позицию
            sortedIndices.add(SortIndex.Single(n - i - 1))

            // Оптимизация: если обменов не было, массив уже отсортирован
            if (!swapped) break
        }

        // Помечаем оставшийся элемент
        sortedIndices.add(SortIndex.Single(0))

        // Финальный кадр без подсветки сравнения
        yield(SortStep(
            state = arr.copyOf(),
            comparingIndices = null,
            selectedIndices = sortedIndices.toSet()
        ))
    }
}