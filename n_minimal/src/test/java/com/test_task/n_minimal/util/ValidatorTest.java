package com.test_task.n_minimal.util;

import com.test_task.n_minimal.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @TempDir
    Path tempDir;

    private File validFile;

    @BeforeEach
    void setUp() throws IOException {
        // Создаём временный .xlsx файл (даже пустой — нам не нужно читать содержимое)
        validFile = tempDir.resolve("test.xlsx").toFile();
        assertTrue(validFile.createNewFile(), "Не удалось создать временный файл");
    }

    // === Тесты для validateInput(String link, String N) ===

    @Test
    @DisplayName("Должен выбрасывать LinkNotFoundException при link = null")
    void shouldThrowLinkNotFoundExceptionWhenLinkIsNull() {
        // When & Then
        LinkNotFoundException exception = assertThrows(
                LinkNotFoundException.class,
                () -> Validator.validateInput(null, "1")
        );
        assertEquals("File link cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValueNNotFoundException при N = null")
    void shouldThrowValueNNotFoundExceptionWhenNIsNull() {
        // When & Then
        ValueNNotFoundException exception = assertThrows(
                ValueNNotFoundException.class,
                () -> Validator.validateInput(validFile.getAbsolutePath(), null)
        );
        assertEquals("N value cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать LinkProcessingException при наличии запрещённых символов")
    void shouldThrowLinkProcessingExceptionOnInvalidChars() {
        for (String ch : List.of("<", ">", "\"", "|", "?", "*")) {

            LinkProcessingException exception = assertThrows(LinkProcessingException.class,
                    () -> Validator.validateInput(ch, "1"));
            assertEquals("Invalid characters in file path", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Должен выбрасывать FileProcessingException, если файл не существует")
    void shouldThrowFileProcessingExceptionWhenFileNotFound() {
        File nonExistent = new File(tempDir.toFile(), "missing.xlsx");

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> Validator.validateInput(nonExistent.getAbsolutePath(), "1")
        );
        assertEquals("File not found", exception.getMessage());
    }

    @DisplayName("Должен выбрасывать FileProcessingException, если расширение не .xlsx")
    @ParameterizedTest(name = "Расширение: {0}")
    @ValueSource(strings = {".xls", ".xlsm", ".txt", ".docx", ""})
    void shouldThrowFileProcessingExceptionOnInvalidExtension(String ext) {
        File file = new File(tempDir.toFile(), "test" + ext);
        try {
            file.createNewFile();
        } catch (IOException e) {
            fail("Не удалось создать файл");
        }

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> Validator.validateInput(file.getAbsolutePath(), "1")
        );
        assertEquals("File is not an Excel .xlsx file", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать LinkProcessingException, если путь — это директория")
    void shouldThrowLinkProcessingExceptionWhenPathIsDirectory() {
        File dir = tempDir.resolve("dir").toFile();
        assertTrue(dir.mkdir());

        LinkProcessingException exception = assertThrows(
                LinkProcessingException.class,
                () -> Validator.validateInput(dir.getAbsolutePath(), "1")
        );
        assertEquals("Path is not a file", exception.getMessage());
    }

    @DisplayName("Должен выбрасывать ValueNProcessingException, если N — не целое число")
    @ParameterizedTest(name = "N = {0}")
    @ValueSource(strings = {"abc", "1.5", "", " ", "12a", "++", "--"})
    void shouldThrowValueNProcessingExceptionOnNonIntegerN(String invalidN) {
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> Validator.validateInput(validFile.getAbsolutePath(), invalidN)
        );
        assertEquals("N value is not a valid integer", exception.getMessage());
    }

    @DisplayName("Должен выбрасывать ValueNProcessingException, если N < 1")
    @ParameterizedTest(name = "N = {0}")
    @ValueSource(ints = {0, -1, -100})
    void shouldThrowValueNProcessingExceptionOnNBelowOne(int invalidN) {
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> Validator.validateInput(validFile.getAbsolutePath(), String.valueOf(invalidN))
        );
        assertEquals("N value must be positive, starting from 1", exception.getMessage());
    }

    @Test
    @DisplayName("Должен успешно валидировать корректные входные данные")
    void shouldValidateCorrectInput() {
        // When
        Validator.validateInput(validFile.getAbsolutePath(), "1");

        // Then
        assertDoesNotThrow(() -> {});
        assertEquals(1, Validator.VALUE_N);
    }

    @Test
    @DisplayName("Должен корректно парсить граничные значения N (1, 2, Integer.MAX_VALUE)")
    void shouldParseValidNValues() {
        // N = 1
        Validator.validateInput(validFile.getAbsolutePath(), "1");
        assertEquals(1, Validator.VALUE_N);

        // N = 2
        Validator.validateInput(validFile.getAbsolutePath(), "2");
        assertEquals(2, Validator.VALUE_N);

        // N = Integer.MAX_VALUE
        Validator.validateInput(validFile.getAbsolutePath(), String.valueOf(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, Validator.VALUE_N);
    }

    // === Тесты для validateNWithListSize(List<Long> numbers) ===

    @Test
    @DisplayName("Должен выбрасывать ValueNProcessingException, если размер списка < VALUE_N")
    void shouldThrowWhenListSizeLessThanN() {
        // Given
        Validator.validateInput(validFile.getAbsolutePath(), "5"); // Устанавливаем VALUE_N = 5
        List<Long> numbers = List.of(1L, 2L, 3L, 4L); // 4 элемента

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> Validator.validateNWithListSize(numbers)
        );
        assertEquals("N exceeds the number of values in first column", exception.getMessage());
    }

    @Test
    @DisplayName("Должен пройти, если размер списка == VALUE_N")
    void shouldPassWhenListSizeEqualsN() {
        // Given
        Validator.validateInput(validFile.getAbsolutePath(), "3");
        List<Long> numbers = List.of(1L, 2L, 3L);

        // When & Then
        assertDoesNotThrow(() -> Validator.validateNWithListSize(numbers));
    }

    @Test
    @DisplayName("Должен пройти, если размер списка > VALUE_N")
    void shouldPassWhenListSizeGreaterThanN() {
        // Given
        Validator.validateInput(validFile.getAbsolutePath(), "2");
        List<Long> numbers = List.of(1L, 2L, 3L, 4L);

        // When & Then
        assertDoesNotThrow(() -> Validator.validateNWithListSize(numbers));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если список пустой, а N > 0")
    void shouldThrowWhenListEmptyAndNPositive() {
        // Given
        Validator.validateInput(validFile.getAbsolutePath(), "1");
        List<Long> empty = List.of();

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> Validator.validateNWithListSize(empty)
        );
        assertEquals("N exceeds the number of values in first column", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать NPE, если список null")
    void shouldThrowNullPointerExceptionWhenListIsNull() {
        // Given
        Validator.validateInput(validFile.getAbsolutePath(), "1");

        // When & Then
        assertThrows(NullPointerException.class, () -> Validator.validateNWithListSize(null));
    }
}