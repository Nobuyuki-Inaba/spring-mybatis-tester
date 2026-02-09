package com.github.nobuyuki.inaba.mybatis.test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads test data from XLSX files into the database.
 * 
 * <p>XLSX format:</p>
 * <ul>
 *   <li>Each sheet represents a table</li>
 *   <li>First row contains column names</li>
 *   <li>Subsequent rows contain data</li>
 * </ul>
 * 
 * <p>NULL vs Empty String Handling:</p>
 * <ul>
 *   <li><b>Blank/missing cells</b> → NULL in database</li>
 *   <li><b>Empty string ("")</b> → Empty string in database</li>
 *   <li><b>String "NULL" (case-insensitive)</b> → NULL in database (explicit NULL marker)</li>
 *   <li><b>Any other value</b> → The actual value in database</li>
 * </ul>
 * 
 * <p>This allows precise control over NULL vs empty string values, which is important
 * for databases that distinguish between them (e.g., Oracle, PostgreSQL).</p>
 */
public class XlsxDataLoader {

    /**
     * Loads data from an XLSX file into the database.
     * 
     * @param filePath path to XLSX file (relative to classpath)
     * @param dataSource database connection
     * @throws Exception if loading fails
     */
    public void loadData(String filePath, DataSource dataSource) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
             Workbook workbook = new XSSFWorkbook(is);
             Connection conn = dataSource.getConnection()) {
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String tableName = sanitizeIdentifier(sheet.getSheetName());
                
                if (sheet.getPhysicalNumberOfRows() == 0) {
                    continue;
                }
                
                Row headerRow = sheet.getRow(0);
                List<String> columns = new ArrayList<>();
                for (Cell cell : headerRow) {
                    columns.add(sanitizeIdentifier(cell.getStringCellValue()));
                }
                
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + tableName)) {
                    stmt.execute();
                }
                
                for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null) continue;
                    
                    insertRow(conn, tableName, columns, row);
                }
            }
        }
    }

    /**
     * Sanitizes SQL identifiers to prevent SQL injection.
     * Only allows alphanumeric characters and underscores.
     */
    private String sanitizeIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        
        // Only allow alphanumeric and underscore
        if (!identifier.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException(
                "Invalid identifier: " + identifier + 
                ". Only alphanumeric characters and underscores are allowed."
            );
        }
        
        return identifier;
    }

    private void insertRow(Connection conn, String tableName, List<String> columns, Row row) throws Exception {
        String placeholders = String.join(", ", columns.stream().map(c -> "?").toArray(String[]::new));
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", 
            tableName, 
            String.join(", ", columns), 
            placeholders);
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int colIndex = 1; colIndex <= columns.size(); colIndex++) {
                Cell cell = row.getCell(colIndex - 1);
                if (cell == null) {
                    stmt.setNull(colIndex, java.sql.Types.VARCHAR);
                    continue;
                }
                
                switch (cell.getCellType()) {
                    case STRING:
                        String strValue = cell.getStringCellValue();
                        if ("NULL".equalsIgnoreCase(strValue)) {
                            stmt.setNull(colIndex, java.sql.Types.VARCHAR);
                        } else {
                            stmt.setString(colIndex, strValue);
                        }
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            stmt.setDate(colIndex, new java.sql.Date(cell.getDateCellValue().getTime()));
                        } else {
                            double numValue = cell.getNumericCellValue();
                            if (numValue == (long) numValue) {
                                stmt.setLong(colIndex, (long) numValue);
                            } else {
                                stmt.setDouble(colIndex, numValue);
                            }
                        }
                        break;
                    case BOOLEAN:
                        stmt.setBoolean(colIndex, cell.getBooleanCellValue());
                        break;
                    case BLANK:
                        stmt.setNull(colIndex, java.sql.Types.VARCHAR);
                        break;
                    default:
                        stmt.setString(colIndex, cell.toString());
                }
            }
            stmt.executeUpdate();
        }
    }
}
