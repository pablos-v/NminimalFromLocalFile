package com.test_task.n_minimal.util;

import com.test_task.n_minimal.exception.LinkProcessingException;
import com.test_task.n_minimal.exception.ValueNProcessingException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс конвернтации Excel файла в список чисел
 */
@Component
public class XlsxToListConverter {

    private static final String NO_SHEETS_IN_FILE = "Excel file contains no sheets";
    private static final String NO_NUMBERS_IN_FIRST_COLUMN = "No numbers found in first column";

    public List<Long> convert(String link) {
        List<Long> numbers = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(new File(link))) {

            // Проверка что файл содержит хотя бы 1 лист
            if (workbook.getNumberOfSheets() == 0) {
                throw new LinkProcessingException(NO_SHEETS_IN_FILE);
            }

            // Получаем первый лист
            Sheet sheet = workbook.getSheetAt(0);

            // Проходим по всем строкам первого столбца
            for (Row row : sheet) {
                Cell cell = row.getCell(0);

                if (cell != null) {
                    Long value = extractLong(cell);
                    if (value != null) {
                        numbers.add(value);
                    }
                }
            }

            // Проверка что файл содержит хотя бы 1 число в первом столбце
            if (numbers.isEmpty()) {
                throw new ValueNProcessingException(NO_NUMBERS_IN_FIRST_COLUMN);
            }

        } catch (NotOfficeXmlFileException | InvalidFormatException e) {
            throw new LinkProcessingException("Invalid Excel file format" + e.getMessage());
        } catch (InvalidOperationException | IOException e) {
            throw new LinkProcessingException("Error reading Excel file: " + e.getMessage());
        }
        return numbers;
    }

    private static Long extractLong(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                return (long) numericValue;

            case STRING:
                String stringValue = cell.getStringCellValue().trim();
                return parseStringToLong(stringValue);

            default:
                return null;
        }
    }

    private static Long parseStringToLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            String cleanedValue = value.replace(" ", "");
            return Long.parseLong(cleanedValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}