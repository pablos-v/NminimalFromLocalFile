package com.test_task.n_minimal.util;

import com.test_task.n_minimal.exception.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Класс валидации входных данных
 */
@Component
public class Validator {

    // Статические константы для сообщений об ошибках
    private static final String INVALID_CHARS = "Invalid characters in file path";
    private static final String NOT_XLSX_FILE = "File is not an Excel .xlsx file";
    private static final String FILE_NOT_FOUND = "File not found";
    private static final String N_NOT_INTEGER = "N value is not a valid integer";
    private static final String N_BELOW_ZERO = "N value must be positive, starting from 1";
    private static final String PATH_IS_NOT_A_FILE = "Path is not a file";
    private static final String N_EXCEEDS_NUMBERS_COUNT = "N exceeds the number of values in first column";


    public void validateInput(String link, String N) {
        // 1. Проверка что link не null
        if (link == null || link.isEmpty()) {
            throw new LinkNotFoundException("File link cannot be null");
        }

        // 2. Проверка что N не null
        if (N == null || N.isEmpty()) {
            throw new ValueNNotFoundException("N value cannot be null");
        }

        // 3. Проверка что в пути нет запрещённых символов для Windows и Linux
        if (link.matches(".*[<>\"|?*].*")) {
            throw new LinkProcessingException(INVALID_CHARS);
        }

        // 4. Проверка что файл существует
        File file = new File(link);
        if (!file.exists()) {
            throw new FileProcessingException(FILE_NOT_FOUND);
        }

        // 5. Проверка что это xlsx файл

        if (!file.isFile()) {
            throw new LinkProcessingException(PATH_IS_NOT_A_FILE);
        }

        if (!link.toLowerCase().endsWith(".xlsx")) {
            throw new FileProcessingException(NOT_XLSX_FILE);
        }

        // 6. Проверка что N является integer
        int valueN;
        try {
            valueN = Integer.parseInt(N);
        } catch (NumberFormatException e) {
            throw new ValueNProcessingException(N_NOT_INTEGER);
        }

        // 7. Проверка что N больше 0
        if (valueN < 1) {
            throw new ValueNProcessingException(N_BELOW_ZERO);
        }
    }

    // Проверка что количество цифр в 1 столбце >= N
    public void validateNWithListSize(List<Long> numbers, int N) {
        if (numbers.size() < N) {
            throw new ValueNProcessingException(N_EXCEEDS_NUMBERS_COUNT);
        }
    }

}
