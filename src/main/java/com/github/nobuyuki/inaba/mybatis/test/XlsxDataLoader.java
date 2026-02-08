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
                String tableName = sheet.getSheetName();
                
                if (sheet.getPhysicalNumberOfRows() == 0) {
                    continue;
                }
                
                // Read header row
                Row headerRow = sheet.getRow(0);
                List<String> columns = new ArrayList<>();
                for (Cell cell : headerRow) {
                    columns.add(cell.getStringCellValue());
                }
                
                // Clear existing data
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + tableName)) {
                    stmt.execute();
                }
                
                // Insert data rows
                for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null) continue;
                    
                    insertRow(conn, tableName, columns, row);
                }
            }
        }
    }

    private void insertRow(Connection conn, String tableName, List<String> columns, Row row) throws Exception {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");
        sql.append(String.join(", ", columns));
        sql.append(") VALUES (");
        sql.append(String.join(", ", columns.stream().map(c -> "?").toArray(String[]::new)));
        sql.append(")");
        
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = row.getCell(i);
                if (cell == null) {
                    stmt.setNull(i + 1, java.sql.Types.VARCHAR);
                    continue;
                }
                
                switch (cell.getCellType()) {
                    case STRING:
                        stmt.setString(i + 1, cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            stmt.setDate(i + 1, new java.sql.Date(cell.getDateCellValue().getTime()));
                        } else {
                            double numValue = cell.getNumericCellValue();
                            if (numValue == (long) numValue) {
                                stmt.setLong(i + 1, (long) numValue);
                            } else {
                                stmt.setDouble(i + 1, numValue);
                            }
                        }
                        break;
                    case BOOLEAN:
                        stmt.setBoolean(i + 1, cell.getBooleanCellValue());
                        break;
                    case BLANK:
                        stmt.setNull(i + 1, java.sql.Types.VARCHAR);
                        break;
                    default:
                        stmt.setString(i + 1, cell.toString());
                }
            }
            stmt.executeUpdate();
        }
    }
}
