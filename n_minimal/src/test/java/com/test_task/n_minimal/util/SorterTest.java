package com.test_task.n_minimal.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SorterTest {

    @Autowired
    private Sorter sorter;

    // === Тесты для sort(List<Long> unsorted) ===

    @Test
    @DisplayName("Должен возвращать пустой список, если входной список пустой")
    void shouldReturnEmptyListWhenInputIsEmpty() {
        // Given
        List<Long> empty = Collections.emptyList();

        // When
        List<Long> result = sorter.sort(empty);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Должен возвращать список из одного элемента без изменений")
    void shouldReturnSameListWhenSingleElement() {
        // Given
        List<Long> single = List.of(42L);

        // When
        List<Long> result = sorter.sort(single);

        // Then
        assertEquals(List.of(42L), result);
    }

    @Test
    @DisplayName("Должен корректно сортировать список из двух элементов (уже отсортирован)")
    void shouldSortTwoElementsAlreadySorted() {
        // Given
        List<Long> input = List.of(1L, 2L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        assertEquals(List.of(1L, 2L), result);
    }

    @Test
    @DisplayName("Должен корректно сортировать список из двух элементов (в обратном порядке)")
    void shouldSortTwoElementsReversed() {
        // Given
        List<Long> input = List.of(2L, 1L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        assertEquals(List.of(1L, 2L), result);
    }

    @Test
    @DisplayName("Должен корректно сортировать обычный неотсортированный список")
    void shouldSortUnsortedList() {
        // Given
        List<Long> input = Arrays.asList(5L, 2L, 8L, 1L, 9L, 3L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        assertEquals(List.of(1L, 2L, 3L, 5L, 8L, 9L), result);
    }

    @Test
    @DisplayName("Должен корректно сортировать уже отсортированный список")
    void shouldHandleAlreadySortedList() {
        // Given
        List<Long> input = List.of(1L, 2L, 3L, 4L, 5L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), result);
    }

    @Test
    @DisplayName("Должен корректно сортировать список в обратном порядке")
    void shouldSortReverseOrderedList() {
        // Given
        List<Long> input = Arrays.asList(5L, 4L, 3L, 2L, 1L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), result);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать дубликаты")
    void shouldHandleDuplicates() {
        // Given
        List<Long> input = Arrays.asList(3L, 1L, 4L, 1L, 5L, 9L, 2L, 6L, 5L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        assertEquals(List.of(1L, 1L, 2L, 3L, 4L, 5L, 5L, 6L, 9L), result);
    }

    @Test
    @DisplayName("Должен корректно сортировать список с отрицательными числами")
    void shouldSortWithNegativeNumbers() {
        // Given
        List<Long> input = Arrays.asList(-5L, 3L, -1L, 0L, 2L, -10L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        assertEquals(List.of(-10L, -5L, -1L, 0L, 2L, 3L), result);
    }

    @Test
    @DisplayName("Должен корректно сортировать список с минимальным и максимальным Long")
    void shouldSortWithExtremeValues() {
        // Given
        List<Long> input = Arrays.asList(Long.MAX_VALUE, Long.MIN_VALUE, 0L, -1L, 1L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        List<Long> expected = Arrays.asList(Long.MIN_VALUE, -1L, 0L, 1L, Long.MAX_VALUE);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Должен возвращать копию, не изменяя оригинальный список")
    void shouldNotModifyOriginalList() {
        // Given
        List<Long> original = new ArrayList<>(List.of(3L, 1L, 4L, 1L, 5L));
        List<Long> originalCopy = new ArrayList<>(original); // сохраняем копию для проверки

        // When
        List<Long> result = sorter.sort(original);

        // Then
        assertEquals(List.of(1L, 1L, 3L, 4L, 5L), result);
        assertEquals(originalCopy, original); // оригинал не изменился
        assertNotSame(original, result); // возвращена новая коллекция
    }

    @Test
    @DisplayName("Должен выбрасывать NullPointerException при null-входе")
    void shouldThrowNullPointerExceptionWhenInputIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> sorter.sort(null));
    }

    @Test
    @DisplayName("Должен корректно работать с большими списками (10_000 элементов)")
    void shouldSortLargeList() {
        // Given
        Random random = new Random(42);
        List<Long> largeList = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            largeList.add(random.nextLong() % 1_000_000);
        }

        // When
        List<Long> result = sorter.sort(largeList);

        // Then
        assertTrue(isSorted(result), "Результат должен быть отсортирован");
        assertEquals(largeList.size(), result.size(), "Размер должен сохраняться");
        assertNotSame(largeList, result, "Должна быть создана новая копия");
    }

    @Test
    @DisplayName("Должен корректно сортировать список, где все элементы одинаковы")
    void shouldSortAllEqualElements() {
        // Given
        List<Long> input = Arrays.asList(42L, 42L, 42L, 42L);

        // When
        List<Long> result = sorter.sort(input);

        // Then
        assertEquals(List.of(42L, 42L, 42L, 42L), result);
    }

    @Test
    @DisplayName("Должен корректно работать с коллекциями разных типов")
    void shouldWorkWithDifferentCollectionTypes() {
        // Given
        Set<Long> set = new HashSet<>(Arrays.asList(5L, 2L, 8L, 1L));
        LinkedList<Long> linkedList = new LinkedList<>(Arrays.asList(3L, 1L, 4L));

        // When
        List<Long> setResult = sorter.sort(new ArrayList<>(set));
        List<Long> linkedListResult = sorter.sort(new ArrayList<>(linkedList));

        // Then
        assertEquals(List.of(1L, 2L, 5L, 8L), setResult);
        assertEquals(List.of(1L, 3L, 4L), linkedListResult);
    }

    // === Вспомогательные методы ===

    /**
     * Проверяет, что список отсортирован по возрастанию.
     */
    private boolean isSorted(List<Long> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1) > list.get(i)) {
                return false;
            }
        }
        return true;
    }
}