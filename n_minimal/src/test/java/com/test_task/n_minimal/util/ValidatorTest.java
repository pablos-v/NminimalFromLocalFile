package com.test_task.n_minimal.util;

import com.test_task.n_minimal.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ValidatorTest {

    @Autowired
    private Validator validator;

    @TempDir
    Path tempDir;

    private File validFile;

    @BeforeEach
    void setUp() throws IOException {
        // Создаём временный .xlsx файл
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
                () -> validator.validateInput(null, "1")
        );
        assertEquals("File link cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValueNNotFoundException при N = null")
    void shouldThrowValueNNotFoundExceptionWhenNIsNull() {
        // When & Then
        ValueNNotFoundException exception = assertThrows(
                ValueNNotFoundException.class,
                () -> validator.validateInput(validFile.getAbsolutePath(), null)
        );
        assertEquals("N value cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать LinkProcessingException при наличии запрещённых символов")
    void shouldThrowLinkProcessingExceptionOnInvalidChars() {
        for (String ch : List.of("<", ">", "\"", "|", "?", "*")) {
            LinkProcessingException exception = assertThrows(
                    LinkProcessingException.class,
                    () -> validator.validateInput(ch, "1")
            );
            assertEquals("Invalid characters in file path", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Должен выбрасывать FileProcessingException, если файл не существует")
    void shouldThrowFileProcessingExceptionWhenFileNotFound() {
        File nonExistent = new File(tempDir.toFile(), "missing.xlsx");

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> validator.validateInput(nonExistent.getAbsolutePath(), "1")
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
                () -> validator.validateInput(file.getAbsolutePath(), "1")
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
                () -> validator.validateInput(dir.getAbsolutePath(), "1")
        );
        assertEquals("Path is not a file", exception.getMessage());
    }

    @DisplayName("Должен выбрасывать ValueNProcessingException, если N — не целое число")
    @ParameterizedTest(name = "N = {0}")
    @ValueSource(strings = {"abc", "1.5", "", " ", "12a", "++", "--"})
    void shouldThrowValueNProcessingExceptionOnNonIntegerN(String invalidN) {
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> validator.validateInput(validFile.getAbsolutePath(), invalidN)
        );
        assertEquals("N value is not a valid integer", exception.getMessage());
    }

    @DisplayName("Должен выбрасывать ValueNProcessingException, если N < 1")
    @ParameterizedTest(name = "N = {0}")
    @ValueSource(ints = {0, -1, -100})
    void shouldThrowValueNProcessingExceptionOnNBelowOne(int invalidN) {
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> validator.validateInput(validFile.getAbsolutePath(), String.valueOf(invalidN))
        );
        assertEquals("N value must be positive, starting from 1", exception.getMessage());
    }

    @Test
    @DisplayName("Должен успешно валидировать корректные входные данные")
    void shouldValidateCorrectInput() {
        // When & Then
        assertDoesNotThrow(() -> validator.validateInput(validFile.getAbsolutePath(), "1"));
    }

    @Test
    @DisplayName("Должен корректно парсить граничные значения N (1, 2, Integer.MAX_VALUE)")
    void shouldParseValidNValues() {
        // N = 1
        assertDoesNotThrow(() -> validator.validateInput(validFile.getAbsolutePath(), "1"));

        // N = 2
        assertDoesNotThrow(() -> validator.validateInput(validFile.getAbsolutePath(), "2"));

        // N = Integer.MAX_VALUE
        assertDoesNotThrow(() ->
                validator.validateInput(validFile.getAbsolutePath(), String.valueOf(Integer.MAX_VALUE))
        );
    }

    // === Тесты для validateNWithListSize(List<Long> numbers) ===

    @Test
    @DisplayName("Должен выбрасывать ValueNProcessingException, если размер списка < N")
    void shouldThrowWhenListSizeLessThanN() {
        // Given
        String N = "5";
        List<Long> numbers = List.of(1L, 2L, 3L, 4L); // 4 элемента

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> {
                    validator.validateInput(validFile.getAbsolutePath(), N);
                    validator.validateNWithListSize(numbers, Integer.parseInt(N));
                }
        );
        assertEquals("N exceeds the number of values in first column", exception.getMessage());
    }

    @Test
    @DisplayName("Должен пройти, если размер списка == N")
    void shouldPassWhenListSizeEqualsN() {
        // Given
        String N = "3";
        List<Long> numbers = List.of(1L, 2L, 3L);

        // When & Then
        assertDoesNotThrow(() -> {
            validator.validateInput(validFile.getAbsolutePath(), N);
            validator.validateNWithListSize(numbers,  Integer.parseInt(N));
        });
    }

    @Test
    @DisplayName("Должен пройти, если размер списка > N")
    void shouldPassWhenListSizeGreaterThanN() {
        // Given
        String N = "2";
        List<Long> numbers = List.of(1L, 2L, 3L, 4L);

        // When & Then
        assertDoesNotThrow(() -> {
            validator.validateInput(validFile.getAbsolutePath(), N);
            validator.validateNWithListSize(numbers, Integer.parseInt(N));
        });
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если список пустой, а N > 0")
    void shouldThrowWhenListEmptyAndNPositive() {
        // Given
        String N = "1";
        List<Long> empty = List.of();

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> {
                    validator.validateInput(validFile.getAbsolutePath(), N);
                    validator.validateNWithListSize(empty, Integer.parseInt(N));
                }
        );
        assertEquals("N exceeds the number of values in first column", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать NPE, если список null")
    void shouldThrowNullPointerExceptionWhenListIsNull() {
        // Given
        String N = "1";

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            validator.validateInput(validFile.getAbsolutePath(), N);
            validator.validateNWithListSize(null,  Integer.parseInt(N));
        });
    }
}