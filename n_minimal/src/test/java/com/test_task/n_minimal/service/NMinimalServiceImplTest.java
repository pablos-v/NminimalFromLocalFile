package com.test_task.n_minimal.service;

import com.test_task.n_minimal.exception.*;
import com.test_task.n_minimal.util.Sorter;
import com.test_task.n_minimal.util.Validator;
import com.test_task.n_minimal.util.XlsxToListConverter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NMinimalServiceImplTest {

    @Mock
    private Validator validator;

    @Mock
    private XlsxToListConverter converter;

    @Mock
    private Sorter sorter;

    @InjectMocks
    private NMinimalServiceImpl service;

    // === Успешные сценарии ===

    @Test
    @DisplayName("Должен вернуть N-й минимальный элемент при корректных входных данных")
    void shouldReturnNthMinimalWhenValidInput() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = "2";
        List<Long> unsorted = List.of(5L, 2L, 8L, 1L, 9L);
        List<Long> sorted = List.of(1L, 2L, 5L, 8L, 9L);

        // Моки
        doNothing().when(validator).validateInput(fileLink, N);
        when(converter.convert(fileLink)).thenReturn(unsorted);
        doNothing().when(validator).validateNWithListSize(unsorted, 2);
        when(sorter.sort(unsorted)).thenReturn(sorted);

        // When
        Long result = service.getNthMinimal(fileLink, N);

        // Then
        assertEquals(2L, result); // sorted.get(2 - 1) = sorted.get(1) = 2L
        verify(validator).validateInput(fileLink, N);
        verify(converter).convert(fileLink);
        verify(validator).validateNWithListSize(unsorted, 2);
        verify(sorter).sort(unsorted);
    }

    @Test
    @DisplayName("Должен вернуть первый минимальный элемент (N = 1)")
    void shouldReturnFirstMinimalWhenNIsOne() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = "1";
        List<Long> unsorted = List.of(3L, 1L, 4L, 2L);
        List<Long> sorted = List.of(1L, 2L, 3L, 4L);

        doNothing().when(validator).validateInput(fileLink, N);
        when(converter.convert(fileLink)).thenReturn(unsorted);
        doNothing().when(validator).validateNWithListSize(unsorted, 1);
        when(sorter.sort(unsorted)).thenReturn(sorted);

        // When
        Long result = service.getNthMinimal(fileLink, N);

        // Then
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("Должен вернуть последний элемент при N = size")
    void shouldReturnLastMinimalWhenNIsSize() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = "4";
        List<Long> unsorted = List.of(3L, 1L, 4L, 2L);
        List<Long> sorted = List.of(1L, 2L, 3L, 4L);

        doNothing().when(validator).validateInput(fileLink, N);
        when(converter.convert(fileLink)).thenReturn(unsorted);
        doNothing().when(validator).validateNWithListSize(unsorted, 4);
        when(sorter.sort(unsorted)).thenReturn(sorted);

        // When
        Long result = service.getNthMinimal(fileLink, N);

        // Then
        assertEquals(4L, result);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать дубликаты")
    void shouldHandleDuplicatesCorrectly() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = "2";
        List<Long> unsorted = List.of(3L, 1L, 4L, 1L, 5L);
        List<Long> sorted = List.of(1L, 1L, 3L, 4L, 5L);

        doNothing().when(validator).validateInput(fileLink, N);
        when(converter.convert(fileLink)).thenReturn(unsorted);
        doNothing().when(validator).validateNWithListSize(unsorted, 2);
        when(sorter.sort(unsorted)).thenReturn(sorted);

        // When
        Long result = service.getNthMinimal(fileLink, N);

        // Then
        assertEquals(1L, result); // второй элемент — тоже 1
    }

    // === Исключения: validateInput ===

    @Test
    @DisplayName("Должен пробрасывать LinkNotFoundException при null-ссылке")
    void shouldThrowLinkNotFoundExceptionWhenLinkIsNull() {
        // Given
        String fileLink = null;
        String N = "1";

        doThrow(new LinkNotFoundException("File link cannot be null"))
                .when(validator).validateInput(fileLink, N);

        // When & Then
        LinkNotFoundException exception = assertThrows(
                LinkNotFoundException.class,
                () -> service.getNthMinimal(fileLink, N)
        );
        assertEquals("File link cannot be null", exception.getMessage());
        verify(validator).validateInput(fileLink, N);
        verifyNoMoreInteractions(converter, sorter);
    }

    @Test
    @DisplayName("Должен пробрасывать ValueNNotFoundException при N = null")
    void shouldThrowValueNNotFoundExceptionWhenNIsNull() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = null;

        doThrow(new ValueNNotFoundException("N value cannot be null"))
                .when(validator).validateInput(fileLink, N);

        // When & Then
        ValueNNotFoundException exception = assertThrows(
                ValueNNotFoundException.class,
                () -> service.getNthMinimal(fileLink, N)
        );
        assertEquals("N value cannot be null", exception.getMessage());
        verify(validator).validateInput(fileLink, N);
        verifyNoMoreInteractions(converter, sorter);
    }

    @Test
    @DisplayName("Должен пробрасывать LinkProcessingException при недопустимых символах в пути")
    void shouldThrowLinkProcessingExceptionOnInvalidPath() {
        // Given
        String fileLink = "<invalid>.xlsx";
        String N = "1";

        doThrow(new LinkProcessingException("Invalid characters in file path"))
                .when(validator).validateInput(fileLink, N);

        // When & Then
        LinkProcessingException exception = assertThrows(
                LinkProcessingException.class,
                () -> service.getNthMinimal(fileLink, N)
        );
        assertEquals("Invalid characters in file path", exception.getMessage());
        verify(validator).validateInput(fileLink, N);
        verifyNoMoreInteractions(converter, sorter);
    }

    @Test
    @DisplayName("Должен пробрасывать ValueNProcessingException при N не integer")
    void shouldThrowValueNProcessingExceptionOnInvalidN() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = "abc";

        doThrow(new ValueNProcessingException("N value is not a valid integer"))
                .when(validator).validateInput(fileLink, N);

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> service.getNthMinimal(fileLink, N)
        );
        assertEquals("N value is not a valid integer", exception.getMessage());
        verify(validator).validateInput(fileLink, N);
        verifyNoMoreInteractions(converter, sorter);
    }

    // === Исключения: convert и validateNWithListSize ===

    @Test
    @DisplayName("Должен пробрасывать LinkProcessingException при ошибке чтения файла")
    void shouldThrowLinkProcessingExceptionWhenFileReadFails() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = "1";

        doNothing().when(validator).validateInput(fileLink, N);
        doThrow(new LinkProcessingException("Error reading Excel file"))
                .when(converter).convert(fileLink);

        // When & Then
        LinkProcessingException exception = assertThrows(
                LinkProcessingException.class,
                () -> service.getNthMinimal(fileLink, N)
        );
        assertEquals("Error reading Excel file", exception.getMessage());
        verify(validator).validateInput(fileLink, N);
        verify(converter).convert(fileLink);
        verifyNoMoreInteractions(sorter);
    }

    @Test
    @DisplayName("Должен пробрасывать ValueNProcessingException, если N > количества чисел в файле")
    void shouldThrowValueNProcessingExceptionWhenNExceedsListSize() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = "5";
        List<Long> numbers = List.of(1L, 2L, 3L); // только 3 числа

        doNothing().when(validator).validateInput(fileLink, N);
        when(converter.convert(fileLink)).thenReturn(numbers);
        doThrow(new ValueNProcessingException("N exceeds the number of values in first column"))
                .when(validator).validateNWithListSize(numbers, 5);

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> service.getNthMinimal(fileLink, N)
        );
        assertEquals("N exceeds the number of values in first column", exception.getMessage());
        verify(validator).validateInput(fileLink, N);
        verify(converter).convert(fileLink);
        verify(validator).validateNWithListSize(numbers, 5);
        verifyNoMoreInteractions(sorter);
    }

    // === Пограничные случаи ===

    @Test
    @DisplayName("Должен корректно обрабатывать пустой список чисел")
    void shouldThrowWhenListIsEmpty() {
        // Given
        String fileLink = "/data/test.xlsx";
        String N = "1";
        List<Long> empty = List.of();

        doNothing().when(validator).validateInput(fileLink, N);
        when(converter.convert(fileLink)).thenReturn(empty);
        doThrow(new ValueNProcessingException("N exceeds the number of values in first column"))
                .when(validator).validateNWithListSize(empty, 1);

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> service.getNthMinimal(fileLink, N)
        );
        assertEquals("N exceeds the number of values in first column", exception.getMessage());
    }

}

