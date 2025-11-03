package com.test_task.n_minimal.util;

import com.test_task.n_minimal.exception.LinkProcessingException;
import com.test_task.n_minimal.exception.ValueNProcessingException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XlsxToListConverterTest {

    @TempDir
    Path tempDir;

    private Workbook workbook;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("TestSheet");
    }

    @AfterEach
    void tearDown() throws IOException {
        workbook.close();
    }

    // Вспомогательный метод для создания временного файла
    private File createTempXlsx(String filename) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (var out = new java.io.FileOutputStream(file)) {
            workbook.write(out);
        }
        return file;
    }

    @Test
    @DisplayName("Должен извлекать числа из первого столбца (NUMERIC)")
    void shouldExtractNumericValuesFromFirstColumn() throws IOException {
        // Given
        for (int i = 0; i < 3; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(i + 10L);
        }
        File file = createTempXlsx("numbers.xlsx");

        // When
        List<Long> result = XlsxToListConverter.convert(file.getAbsolutePath());

        // Then
        assertEquals(List.of(10L, 11L, 12L), result);
    }

    @Test
    @DisplayName("Должен извлекать числа из строкового представления")
    void shouldExtractNumbersFromStringCells() throws IOException {
        // Given
        String[] values = {"123", " 456 ", "789 "};
        for (int i = 0; i < values.length; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(values[i]);
        }
        File file = createTempXlsx("strings.xlsx");

        // When
        List<Long> result = XlsxToListConverter.convert(file.getAbsolutePath());

        // Then
        assertEquals(List.of(123L, 456L, 789L), result);
    }

    @Test
    @DisplayName("Должен игнорировать пустые и нецелочисленные значения")
    void shouldIgnoreInvalidStringCells() throws IOException {
        // Given
        String[] values = {"123", "abc", "", "  ", "45.67", "12 345"}; // 12 345 -> "12345" -> Long
        // Примечание: "12 345" -> убираем пробелы -> "12345" -> OK
        // Но "45.67" -> не Long -> null

        for (int i = 0; i < values.length; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(values[i]);
        }
        File file = createTempXlsx("mixed_strings.xlsx");

        // When
        List<Long> result = XlsxToListConverter.convert(file.getAbsolutePath());

        // Then
        assertEquals(List.of(123L, 12345L), result); // "12 345" -> 12345
    }

    @Test
    @DisplayName("Должен обрабатывать NUMERIC как double (например, 123.9) -> truncate в long")
    void shouldTruncateDoubleValuesToLong() throws IOException {
        // Given
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(123.999); // должно стать 123

        File file = createTempXlsx("double.xlsx");

        // When
        List<Long> result = XlsxToListConverter.convert(file.getAbsolutePath());

        // Then
        assertEquals(List.of(123L), result);
    }

    @Test
    @DisplayName("Должен игнорировать пустые ячейки")
    void shouldIgnoreNullCells() throws IOException {
        // Given
        Row row1 = sheet.createRow(0);
        row1.createCell(0).setCellValue(100L);

        // Пропущенная ячейка в строке 1
        sheet.createRow(1); // без ячейки

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue(200L);

        File file = createTempXlsx("null_cells.xlsx");

        // When
        List<Long> result = XlsxToListConverter.convert(file.getAbsolutePath());

        // Then
        assertEquals(List.of(100L, 200L), result);
    }

    @Test
    @DisplayName("Должен игнорировать другие типы ячеек (например, BOOLEAN)")
    void shouldIgnoreNonNumericAndNonStringCells() throws IOException {
        // Given
        Row row1 = sheet.createRow(0);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue(true);
        cell1.setCellType(CellType.BOOLEAN);

        Row row2 = sheet.createRow(1);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue(123L);

        File file = createTempXlsx("boolean.xlsx");

        // When
        List<Long> result = XlsxToListConverter.convert(file.getAbsolutePath());

        // Then
        assertEquals(List.of(123L), result);
    }

    @Test
    @DisplayName("Должен выбрасывать ValueNProcessingException, если нет чисел в первом столбце")
    void shouldThrowWhenNoNumbersInFirstColumn() throws IOException {
        // Given
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("abc");

        File file = createTempXlsx("no_numbers.xlsx");

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> XlsxToListConverter.convert(file.getAbsolutePath())
        );
        assertEquals("No numbers found in first column", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать LinkProcessingException, если файл не Excel")
    void shouldThrowOnInvalidFormat() {
        // Given
        File fakeFile = tempDir.resolve("not_excel.txt").toFile();
        try {
            fakeFile.createNewFile();
            // Записываем какой-то контент в файл, чтобы он не был пустым
            Files.writeString(fakeFile.toPath(), "dummy content");
        } catch (IOException e) {
            fail("Cannot create test file");
        }

        // When & Then
        LinkProcessingException exception = assertThrows(
                LinkProcessingException.class,
                () -> XlsxToListConverter.convert(fakeFile.getAbsolutePath())
        );
        assertTrue(exception.getMessage().contains("Invalid Excel file format"));
    }

    @Test
    @DisplayName("Должен выбрасывать LinkProcessingException, если файл не существует")
    void shouldThrowWhenFileNotFound() {
        // Given
        String nonExistentPath = tempDir.resolve("missing.xlsx").toString();

        // When & Then
        LinkProcessingException exception = assertThrows(
                LinkProcessingException.class,
                () -> XlsxToListConverter.convert(nonExistentPath)
        );
        assertTrue(exception.getMessage().contains("Error reading Excel file"));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать большие числа (в пределах Long)")
    void shouldHandleLargeNumbers() throws IOException {
        // Given
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(Long.MAX_VALUE);

        File file = createTempXlsx("large.xlsx");

        // When
        List<Long> result = XlsxToListConverter.convert(file.getAbsolutePath());

        // Then
        assertEquals(List.of(Long.MAX_VALUE), result);
    }

    @Test
    @DisplayName("Должен обрабатывать пустой первый столбец (другие столбцы не учитываются)")
    void shouldIgnoreOtherColumns() throws IOException {
        // Given
        Row row = sheet.createRow(0);
        row.createCell(1).setCellValue(123L); // во втором столбце

        File file = createTempXlsx("other_column.xlsx");

        // When & Then
        ValueNProcessingException exception = assertThrows(
                ValueNProcessingException.class,
                () -> XlsxToListConverter.convert(file.getAbsolutePath())
        );
        assertEquals("No numbers found in first column", exception.getMessage());
    }
}
