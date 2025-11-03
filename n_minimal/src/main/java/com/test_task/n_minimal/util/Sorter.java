package com.test_task.n_minimal.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс сортировки
 */
@Component
public class Sorter {

    public List<Long> sort(List<Long> unsorted) {
        List<Long> list = new ArrayList<>(unsorted);
        quickSort(list, 0, list.size() - 1);

        return list;
    }

    private static void quickSort(List<Long> list, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(list, low, high);
            quickSort(list, low, pivotIndex - 1);
            quickSort(list, pivotIndex + 1, high);
        }
    }

    private static int partition(List<Long> list, int low, int high) {
        Long pivot = list.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (list.get(j) <= pivot) {
                i++;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }
}
