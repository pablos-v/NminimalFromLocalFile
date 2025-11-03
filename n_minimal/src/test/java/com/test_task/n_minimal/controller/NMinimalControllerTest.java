// src/test/java/com/test_task/n_minimal/controller/NMinimalControllerTest.java
package com.test_task.n_minimal.controller;

import com.test_task.n_minimal.exception.LinkNotFoundException;
import com.test_task.n_minimal.exception.LinkProcessingException;
import com.test_task.n_minimal.exception.ValueNNotFoundException;
import com.test_task.n_minimal.exception.ValueNProcessingException;
import com.test_task.n_minimal.service.NMinimalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NMinimalControllerTest {

    @Mock
    private NMinimalService service;

    @InjectMocks
    private NMinimalController controller;

    @BeforeEach
    void setUp() {
        reset(service);
    }

    // === Успешный сценарий ===

    @Test
    void getNthMinimal_validInput_returnsOkWithResult() {
        // Given
        String fileLink = "  data/file.xlsx  ";
        String N = "  5  ";
        Long expectedValue = 42L;

        when(service.getNthMinimal("data/file.xlsx", "5")).thenReturn(expectedValue);

        // When
        ResponseEntity<Long> response = controller.getNthMinimal(fileLink, N);

        // Then
        assertNotNull(response);
        assertEquals(ResponseEntity.ok(expectedValue), response);
        verify(service).getNthMinimal(eq("data/file.xlsx"), eq("5"));
    }

    // === Пограничные случаи: пустые и пробельные строки ===

    @Test
    void getNthMinimal_emptyFileLinkAndN_callsServiceWithEmptyStrings() {
        // Given
        String fileLink = "   ";
        String N = "   ";

        when(service.getNthMinimal("", "")).thenReturn(100L);

        // When
        ResponseEntity<Long> response = controller.getNthMinimal(fileLink, N);

        // Then
        assertEquals(ResponseEntity.ok(100L), response);
        verify(service).getNthMinimal(eq(""), eq(""));
    }

    @Test
    void getNthMinimal_trimmedToEmptyFileLink_throwsLinkNotFoundException() {
        // Given
        String fileLink = "   ";
        String N = "5";

        when(service.getNthMinimal("", "5")).thenThrow(new LinkNotFoundException("Link is empty"));

        // When & Then
        LinkNotFoundException thrown = assertThrows(LinkNotFoundException.class, () ->
                controller.getNthMinimal(fileLink, N)
        );
        assertEquals("Link is empty", thrown.getMessage());
        verify(service).getNthMinimal(eq(""), eq("5"));
    }

    @Test
    void getNthMinimal_trimmedToEmptyN_throwsValueNNotFoundException() {
        // Given
        String fileLink = "file.txt";
        String N = "   ";

        when(service.getNthMinimal("file.txt", "")).thenThrow(new ValueNNotFoundException("N is empty"));

        // When & Then
        ValueNNotFoundException thrown = assertThrows(ValueNNotFoundException.class, () ->
                controller.getNthMinimal(fileLink, N)
        );
        assertEquals("N is empty", thrown.getMessage());
        verify(service).getNthMinimal(eq("file.txt"), eq(""));
    }

    // === Обработка исключений (через ExceptionHandlingService) ===

    @Test
    void getNthMinimal_invalidFileLinkFormat_throwsLinkProcessingException() {
        // Given
        String fileLink = "invalid|path";
        String N = "5";

        when(service.getNthMinimal("invalid|path", "5"))
                .thenThrow(new LinkProcessingException("Invalid character in path"));

        // When & Then
        LinkProcessingException thrown = assertThrows(LinkProcessingException.class, () ->
                controller.getNthMinimal(fileLink, N)
        );
        assertEquals("Invalid character in path", thrown.getMessage());
    }

    @Test
    void getNthMinimal_invalidNFormat_throwsValueNProcessingException() {
        // Given
        String fileLink = "file.txt";
        String N = "abc";

        when(service.getNthMinimal("file.txt", "abc"))
                .thenThrow(new ValueNProcessingException("N must be a number"));

        // When & Then
        ValueNProcessingException thrown = assertThrows(ValueNProcessingException.class, () ->
                controller.getNthMinimal(fileLink, N)
        );
        assertEquals("N must be a number", thrown.getMessage());
    }

    @Test
    void getNthMinimal_fileNotFound_throwsLinkNotFoundException() {
        // Given
        String fileLink = "missing.xlsx";
        String N = "3";

        when(service.getNthMinimal("missing.xlsx", "3"))
                .thenThrow(new LinkNotFoundException("File not found"));

        // When & Then
        LinkNotFoundException thrown = assertThrows(LinkNotFoundException.class, () ->
                controller.getNthMinimal(fileLink, N)
        );
        assertEquals("File not found", thrown.getMessage());
    }

    @Test
    void getNthMinimal_nOutOfRange_throwsValueNNotFoundException() {
        // Given
        String fileLink = "data.xlsx";
        String N = "100";
        when(service.getNthMinimal("data.xlsx", "100"))
                .thenThrow(new ValueNNotFoundException("N exceeds number of values"));

        // When & Then
        ValueNNotFoundException thrown = assertThrows(ValueNNotFoundException.class, () ->
                controller.getNthMinimal(fileLink, N)
        );
        assertEquals("N exceeds number of values", thrown.getMessage());
    }

    // === Пограничные значения N: 0, отрицательные, очень большие ===

    @Test
    void getNthMinimal_nIsZero_callsServiceWithZero() {
        // Given
        String fileLink = "data.xlsx";
        String N = "0";

        when(service.getNthMinimal("data.xlsx", "0"))
                .thenThrow(new ValueNProcessingException("N must be positive"));

        // When & Then
        ValueNProcessingException thrown = assertThrows(ValueNProcessingException.class, () ->
                controller.getNthMinimal(fileLink, N)
        );
        assertEquals("N must be positive", thrown.getMessage());
    }

    @Test
    void getNthMinimal_nIsNegative_callsServiceWithNegative() {
        // Given
        String fileLink = "data.xlsx";
        String N = " -5 ";

        when(service.getNthMinimal("data.xlsx", "-5"))
                .thenThrow(new ValueNProcessingException("N must be a natural number"));

        // When & Then
        ValueNProcessingException thrown = assertThrows(ValueNProcessingException.class, () ->
                controller.getNthMinimal(fileLink, N)
        );
        assertEquals("N must be a natural number", thrown.getMessage());
    }

    // === Проверка, что trim() действительно вызывается ===

    @Test
    void getNthMinimal_inputWithSpaces_callsServiceWithoutSpaces() {
        // Given
        String fileLink = "  path/to/file.xlsx  ";
        String N = "  10  ";

        when(service.getNthMinimal("path/to/file.xlsx", "10")).thenReturn(999L);

        // When
        ResponseEntity<Long> response = controller.getNthMinimal(fileLink, N);

        // Then
        assertEquals(ResponseEntity.ok(999L), response);
        verify(service).getNthMinimal(eq("path/to/file.xlsx"), eq("10"));
    }
}
