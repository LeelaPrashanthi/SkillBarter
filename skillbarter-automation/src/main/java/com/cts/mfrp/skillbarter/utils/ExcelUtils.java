package com.cts.mfrp.skillbarter.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        String resolved = resolvePath(filePath);

        try (FileInputStream fis = new FileInputStream(resolved);
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
     * Writes a value to a cell, identified by sheet name + 1-based data row
     * index (1 = first row under the header) + column header. If the column
     * does not exist it is added at the end of the header row. Not
     * thread-safe — only call from sequential suites (thread-count="1").
     */
    public static synchronized void writeCellValue(String filePath,
                                                   String sheetName,
                                                   int dataRowIndex,
                                                   String columnHeader,
                                                   String value) {
        String resolved = resolvePath(filePath);
        try (FileInputStream fis = new FileInputStream(resolved);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                log.warn("writeCellValue: sheet '{}' not found in '{}'", sheetName, filePath);
                return;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                log.warn("writeCellValue: header row missing in sheet '{}'", sheetName);
                return;
            }

            int colIndex = -1;
            for (Cell h : headerRow) {
                if (columnHeader.equalsIgnoreCase(getCellValue(h))) {
                    colIndex = h.getColumnIndex();
                    break;
                }
            }
            if (colIndex == -1) {
                colIndex = headerRow.getLastCellNum();
                headerRow.createCell(colIndex).setCellValue(columnHeader);
            }

            Row row = sheet.getRow(dataRowIndex);
            if (row == null) row = sheet.createRow(dataRowIndex);
            Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(value);

            try (FileOutputStream fos = new FileOutputStream(resolved)) {
                wb.write(fos);
            }
            log.debug("Wrote '{}' → {}!R{}C{} ('{}') in {}",
                    value, sheetName, dataRowIndex, colIndex, columnHeader, resolved);

        } catch (IOException e) {
            log.error("writeCellValue failed for '{}'!R{}/'{}': {}",
                    sheetName, dataRowIndex, columnHeader, e.getMessage());
        }
    }

    /**
     * Resolve a (possibly relative) file path robustly:
     *   1. If the path is absolute or already resolves from CWD → use it.
     *   2. Walk up from System.getProperty("user.dir") looking for the
     *      file under each ancestor — this handles IDE runners that set
     *      the working directory to a sub-folder of the project.
     * Returns the original path as a last resort so the caller sees a
     * meaningful error message.
     */
    private static String resolvePath(String filePath) {
        java.io.File f = new java.io.File(filePath);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        java.io.File cwd = new java.io.File(System.getProperty("user.dir"));
        while (cwd != null) {
            java.io.File candidate = new java.io.File(cwd, filePath);
            if (candidate.exists()) {
                return candidate.getAbsolutePath();
            }
            cwd = cwd.getParentFile();
        }
        return filePath;
    }
}
