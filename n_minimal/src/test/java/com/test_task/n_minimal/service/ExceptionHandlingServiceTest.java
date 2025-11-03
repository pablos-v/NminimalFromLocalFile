package com.test_task.n_minimal.service;

import com.test_task.n_minimal.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlingServiceTest {

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<Throwable> throwableCaptor;

    private ExceptionHandlingService exceptionHandlingService;

    @BeforeEach
    void setUp() {
        exceptionHandlingService = new ExceptionHandlingService();
        // Заменяем логгер через рефлексию, т.к. он приватный и нет сеттера
        try {
            var field = ExceptionHandlingService.class.getDeclaredField("logger");
            field.setAccessible(true);
            field.set(exceptionHandlingService, logger);
        } catch (Exception e) {
            fail("Не удалось установить мок логгера: " + e.getMessage());
        }
    }

    // === Тесты для handleProcessingException (BAD_REQUEST) ===

    @Test
    void handleProcessingException_withLinkProcessingException_returnsMessageAndLogs() {
        String expectedMessage = "Ошибка обработки ссылки";
        LinkProcessingException exception = new LinkProcessingException(expectedMessage);

        String result = exceptionHandlingService.handleProcessingException(exception);

        assertEquals(expectedMessage, result);
        verify(logger).error(eq(expectedMessage), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    @Test
    void handleProcessingException_withValueNProcessingException_returnsMessageAndLogs() {
        String expectedMessage = "Ошибка обработки значения N";
        ValueNProcessingException exception = new ValueNProcessingException(expectedMessage);

        String result = exceptionHandlingService.handleProcessingException(exception);

        assertEquals(expectedMessage, result);
        verify(logger).error(eq(expectedMessage), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    @Test
    void handleProcessingException_withFileProcessingException_returnsMessageAndLogs() {
        String expectedMessage = "Ошибка обработки файла";
        FileProcessingException exception = new FileProcessingException(expectedMessage);

        String result = exceptionHandlingService.handleProcessingException(exception);

        assertEquals(expectedMessage, result);
        verify(logger).error(eq(expectedMessage), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    @Test
    void handleProcessingException_withNullMessage_logsAndReturnsNull() {
        LinkProcessingException exception = new LinkProcessingException(null);

        String result = exceptionHandlingService.handleProcessingException(exception);

        assertNull(result);
        verify(logger).error(isNull(), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    @Test
    void handleProcessingException_withEmptyMessage_returnsEmptyStringAndLogs() {
        String expectedMessage = "";
        LinkProcessingException exception = new LinkProcessingException(expectedMessage);

        String result = exceptionHandlingService.handleProcessingException(exception);

        assertEquals(expectedMessage, result);
        verify(logger).error(eq(expectedMessage), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    // === Тесты для handleNotFoundException (NOT_FOUND) ===

    @Test
    void handleNotFoundException_withLinkNotFoundException_returnsMessageAndLogs() {
        String expectedMessage = "Ссылка не найдена";
        LinkNotFoundException exception = new LinkNotFoundException(expectedMessage);

        String result = exceptionHandlingService.handleNotFoundException(exception);

        assertEquals(expectedMessage, result);
        verify(logger).error(eq(expectedMessage), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    @Test
    void handleNotFoundException_withValueNNotFoundException_returnsMessageAndLogs() {
        String expectedMessage = "Значение N не найдено";
        ValueNNotFoundException exception = new ValueNNotFoundException(expectedMessage);

        String result = exceptionHandlingService.handleNotFoundException(exception);

        assertEquals(expectedMessage, result);
        verify(logger).error(eq(expectedMessage), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    @Test
    void handleNotFoundException_withNullMessage_logsAndReturnsNull() {
        LinkNotFoundException exception = new LinkNotFoundException(null);

        String result = exceptionHandlingService.handleNotFoundException(exception);

        assertNull(result);
        verify(logger).error(isNull(), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    @Test
    void handleNotFoundException_withEmptyMessage_returnsEmptyStringAndLogs() {
        String expectedMessage = "";
        LinkNotFoundException exception = new LinkNotFoundException(expectedMessage);

        String result = exceptionHandlingService.handleNotFoundException(exception);

        assertEquals(expectedMessage, result);
        verify(logger).error(eq(expectedMessage), throwableCaptor.capture());
        assertSame(exception, throwableCaptor.getValue());
    }

    // === Проверка аннотаций (опционально, через рефлексию) ===

    @Test
    void handleProcessingException_hasCorrectResponseStatus() {
        ResponseStatus annotation = ExceptionHandlingService.class
                .getDeclaredMethods()[0]
                .getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.BAD_REQUEST, annotation.value());
    }

    @Test
    void handleNotFoundException_hasCorrectResponseStatus() {
        ResponseStatus annotation = ExceptionHandlingService.class
                .getDeclaredMethods()[1]
                .getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }
}
