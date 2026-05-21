package com.cts.mfrp.skillbarter.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Utility class to read test data from Excel (.xlsx) via Apache POI.
 * Usage:
 *   List<Map<String,String>> rows = ExcelUtils.getSheetData("src/.../TestData.xlsx", "LoginData");
 */
public class ExcelUtils {

    private static final Logger log = LogManager.getLogger(ExcelUtils.class);

    private ExcelUtils() {}

    /**
     * Returns all data rows from the specified sheet as a list of column-name → value maps.
     * First row is treated as the header.
     */
    public static List<Map<String, String>> getSheetData(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                log.error("Sheet '{}' not found in '{}'", sheetName, filePath);
                return data;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return data;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell));
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowMap.put(headers.get(j), getCellValue(cell));
                }
                data.add(rowMap);
            }

            log.info("Loaded {} rows from sheet '{}' in '{}'", data.size(), sheetName, filePath);

        } catch (IOException e) {
            log.error("Failed to read Excel file '{}': {}", filePath, e.getMessage());
        }

        return data;
    }

    /**
     * Returns a 2-D Object array suitable for TestNG @DataProvider.
     */
    public static Object[][] getDataProviderArray(String filePath, String sheetName) {
        List<Map<String, String>> rows = getSheetData(filePath, sheetName);
        Object[][] result = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            result[i][0] = rows.get(i);
        }
        return result;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Writes a string value to (sheetName, dataRowIndex, columnName) and saves
     * the workbook back to disk. dataRowIndex is 1-based against DATA rows
     * (row 0 in the sheet is the header), matching what the data providers
     * pass in. If columnName isn't present in the header row it's appended
     * to the right. Silently swallows IO errors so a failure to write the
     * result doesn't mask the underlying test outcome.
     *
     * Used by data-driven tests to write PASS/FAIL back to the source xlsx.
     */
    public static synchronized void writeCellValue(String filePath, String sheetName,
                                                   int dataRowIndex, String columnName, String value) {
        Workbook wb = null;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            wb = new XSSFWorkbook(fis);
            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                log.warn("writeCellValue: sheet '{}' not found in '{}'", sheetName, filePath);
                return;
            }

            Row header = sheet.getRow(0);
            if (header == null) {
                log.warn("writeCellValue: header row missing in sheet '{}'", sheetName);
                return;
            }

            // Find (or create) the column whose header matches columnName.
            int colIndex = -1;
            for (Cell c : header) {
                if (columnName.equalsIgnoreCase(getCellValue(c))) {
                    colIndex = c.getColumnIndex();
                    break;
                }
            }
            if (colIndex == -1) {
                colIndex = header.getLastCellNum();
                if (colIndex < 0) colIndex = 0;
                header.createCell(colIndex).setCellValue(columnName);
            }

            int sheetRowIndex = dataRowIndex; // header is row 0, data row 1 = sheet row 1
            Row row = sheet.getRow(sheetRowIndex);
            if (row == null) row = sheet.createRow(sheetRowIndex);

            Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(value);
        } catch (IOException e) {
            log.warn("writeCellValue failed on '{}'!{} row={} col='{}': {}",
                    filePath, sheetName, dataRowIndex, columnName, e.getMessage());
            return;
        } catch (Exception e) {
            log.warn("writeCellValue unexpected error on '{}'!{} row={} col='{}': {}",
                    filePath, sheetName, dataRowIndex, columnName, e.getMessage());
            return;
        }

        // Save back to disk (separate try/with so we never write a partially-mutated workbook on read failure).
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
        } catch (IOException e) {
            log.warn("writeCellValue: failed to save '{}': {}", filePath, e.getMessage());
        } finally {
            try { wb.close(); } catch (IOException ignored) {}
        }
    }
}
