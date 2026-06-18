package com.moshk.sortviz.core.sortAlgorithms

import com.moshk.sortviz.core.SortIndex
import com.moshk.sortviz.core.SortStep
import com.moshk.sortviz.core.SortingAlgorithm

@com.moshk.sortviz.core.annotations.SortingAlgorithm
class MergeSortAlgorithm : SortingAlgorithm<Map<Int, Array<Int>>>() {
    override val name = "Merge Sort (Сортировка слиянием)"
    override val description = "Эффективный алгоритм сортировки со сложностью O(n log n). " +
            "Массив рекурсивно делится пополам до элементарных подмассивов, затем " +
            "отсортированные половины сливаются (merge) в один. Для процесса слияния " +
            "используются два вспомогательных буфера — левый и правый."

    private lateinit var fullArray: Array<Int>

    override fun generateSteps(initialData: List<Int>): Sequence<SortStep<Map<Int, Array<Int>>>> {
        fullArray = initialData.toTypedArray()
        return mergeSortSteps(0, fullArray.size - 1) + finalStep()
    }

    private fun mergeSortSteps(
        left: Int,
        right: Int
    ): Sequence<SortStep<Map<Int, Array<Int>>>> = sequence {
        if (left >= right) return@sequence
        val mid = (left + right) / 2

        // Шаг 1: Показываем активный диапазон и выделяем левую половину (готовимся к спуску)
        val activeRange = fullArray.sliceArray(left..right)
        val leftHalfIndices = (0..mid - left).map { SortIndex.Composite(0, it) }.toSet()

        yield(SortStep(
            state = mapOf(0 to activeRange.copyOf()),
            selectedIndices = leftHalfIndices
        ))

        // Рекурсивный спуск в левую половину
        yieldAll(mergeSortSteps(left, mid))

        // После возврата из левой рекурсии показываем активный диапазон с выделением правой половины
        val activeRangeAfterLeft = fullArray.sliceArray(left..right)
        val rightHalfIndices = (mid - left + 1..right - left).map { SortIndex.Composite(0, it) }.toSet()

        yield(SortStep(
            state = mapOf(0 to activeRangeAfterLeft.copyOf()),
            selectedIndices = rightHalfIndices
        ))

        // Рекурсивный спуск в правую половину
        yieldAll(mergeSortSteps(mid + 1, right))

        // Слияние двух отсортированных половин
        yieldAll(merge(left, mid, right))
    }

    private fun merge(
        left: Int,
        mid: Int,
        right: Int
    ): Sequence<SortStep<Map<Int, Array<Int>>>> = sequence {
        val lSize = mid - left + 1
        val rSize = right - mid

        // Создаем два буфера из отсортированных половин
        var leftBuf = Array(lSize) { fullArray[left + it] }
        var rightBuf = Array(rSize) { fullArray[mid + 1 + it] }

        // Результат начинается пустым
        var result = emptyArray<Int>()

        // Шаг: Показываем пустой результат + два отсортированных подмассива
        yield(SortStep(
            state = mapOf(
                0 to result.copyOf(),
                1 to leftBuf.copyOf(),
                2 to rightBuf.copyOf()
            )
        ))

        var i = 0
        var j = 0

        while (i < lSize && j < rSize) {
            // Шаг: Сравниваем текущие элементы из левого и правого буферов
            // Добавляем selectedIndices для подсветки текущих позиций в буферах
            yield(SortStep(
                state = mapOf(
                    0 to result.copyOf(),
                    1 to leftBuf.copyOf(),
                    2 to rightBuf.copyOf()
                ),
                comparingIndices = SortIndex.Composite(1, i) to SortIndex.Composite(2, j),
                selectedIndices = setOf(
                    SortIndex.Composite(1, i),
                    SortIndex.Composite(2, j)
                )
            ))

            if (leftBuf[i] <= rightBuf[j]) {
                // Добавляем элемент из левого буфера в результат
                result = result + leftBuf[i]

                yield(SortStep(
                    state = mapOf(
                        0 to result.copyOf(),
                        1 to leftBuf.copyOf(),
                        2 to rightBuf.copyOf()
                    ),
                    swappingIndices = setOf(SortIndex.Composite(1, i) to SortIndex.Composite(0, result.size - 1)),
                    // Подсвечиваем переносимый элемент из левого буфера
                    selectedIndices = setOf(SortIndex.Composite(1, i))
                ))
                i++
            } else {
                // Добавляем элемент из правого буфера в результат
                result = result + rightBuf[j]

                yield(SortStep(
                    state = mapOf(
                        0 to result.copyOf(),
                        1 to leftBuf.copyOf(),
                        2 to rightBuf.copyOf()
                    ),
                    swappingIndices = setOf(SortIndex.Composite(2, j) to SortIndex.Composite(0, result.size - 1)),
                    // Подсвечиваем переносимый элемент из правого буфера
                    selectedIndices = setOf(SortIndex.Composite(2, j))
                ))
                j++
            }
        }

        // Докопировать оставшиеся элементы из левого буфера
        while (i < lSize) {
            result = result + leftBuf[i]
            yield(SortStep(
                state = mapOf(
                    0 to result.copyOf(),
                    1 to leftBuf.copyOf(),
                    2 to rightBuf.copyOf()
                ),
                swappingIndices = setOf(SortIndex.Composite(1, i) to SortIndex.Composite(0, result.size - 1)),
                selectedIndices = setOf(SortIndex.Composite(1, i))
            ))
            i++
        }

        // Докопировать оставшиеся элементы из правого буфера
        while (j < rSize) {
            result = result + rightBuf[j]
            yield(SortStep(
                state = mapOf(
                    0 to result.copyOf(),
                    1 to leftBuf.copyOf(),
                    2 to rightBuf.copyOf()
                ),
                swappingIndices = setOf(SortIndex.Composite(2, j) to SortIndex.Composite(0, result.size - 1)),
                selectedIndices = setOf(SortIndex.Composite(2, j))
            ))
            j++
        }

        // Шаг: Показываем готовый отсортированный результат (буферы исчезают)
        yield(SortStep(
            state = mapOf(0 to result.copyOf())
        ))

        // Обновляем полный массив для последующих операций
        for (k in left..right) {
            fullArray[k] = result[k - left]
        }
    }

    private fun finalStep(): SortStep<Map<Int, Array<Int>>> {
        val sortedIndices = (0 until fullArray.size).map { SortIndex.Composite(0, it) }.toSet()
        return SortStep(
            state = mapOf(0 to fullArray.copyOf()),
            selectedIndices = sortedIndices
        )
    }
}