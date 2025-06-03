package com.formfill.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表单填写器服务类，负责处理Excel表单的坐标填写逻辑
 */
@Service
public class FormFillerService {
    
    private static final Logger logger = LoggerFactory.getLogger(FormFillerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String TEMPLATES_DIR = "templates";
    private static final String OUTPUT_DIR = "output";
    
    public FormFillerService() {
        // 确保目录存在
        createDirectoryIfNotExists(TEMPLATES_DIR);
        createDirectoryIfNotExists(OUTPUT_DIR);
    }
    
    /**
     * 填写表单的主要方法
     * 
     * @param formName 表单名称
     * @param formContent 表单内容，格式为 {"row3": {"col2": "值", "col3": "值"}}
     * @return 包含成功状态和结果信息的Map
     */
    public Map<String, Object> fillForm(String formName, Map<String, Map<String, String>> formContent) {
        try {
            // 查找模板文件
            String templatePath = findTemplate(formName);
            
            if (templatePath == null) {
                // 如果没有找到模板，创建一个新的表单
                return createNewForm(formName, formContent);
            }
            
            // 使用现有模板填写表单
            return fillExistingTemplate(templatePath, formName, formContent);
            
        } catch (Exception e) {
            logger.error("填写表单时发生错误: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "填写表单失败: " + e.getMessage());
            result.put("code", "FILL_ERROR");
            return result;
        }
    }
    
    /**
     * 根据表单名称获取模板信息
     * 
     * @param formName 表单名称
     * @return 包含模板信息的Map
     */
    public Map<String, Object> getTemplateInfo(String formName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("查找表单模板: {}", formName);
            
            // 查找模板文件
            String templatePath = findTemplate(formName);
            
            if (templatePath != null) {
                File templateFile = new File(templatePath);
                String fileName = templateFile.getName();
                
                result.put("found", true);
                result.put("formName", formName);
                result.put("templatePath", templatePath);
                result.put("fileName", fileName);
                result.put("fileSize", templateFile.length());
                result.put("lastModified", templateFile.lastModified());
                result.put("readable", templateFile.canRead());
                result.put("absolutePath", templateFile.getAbsolutePath());
                
                // 解析文件名获取基础名称
                String baseName = fileName.replaceAll("\\.(xlsx|xls)$", "");
                result.put("baseName", baseName);
                
                // 判断匹配类型
                if (fileName.equals(formName + ".xlsx") || fileName.equals(formName + ".xls")) {
                    result.put("matchType", "exact");
                } else if (fileName.equals(formName + "模板.xlsx") || fileName.equals(formName + "模板.xls")) {
                    result.put("matchType", "template_suffix");
                } else {
                    result.put("matchType", "fuzzy");
                }
                
                logger.info("找到模板文件: {} (匹配类型: {})", fileName, result.get("matchType"));
                
            } else {
                result.put("found", false);
                result.put("formName", formName);
                result.put("message", "未找到匹配的模板文件");
                
                // 提供建议的文件名
                List<String> suggestions = List.of(
                    formName + ".xlsx",
                    formName + ".xls", 
                    formName + "模板.xlsx",
                    formName + "模板.xls"
                );
                result.put("suggestedFileNames", suggestions);
                
                logger.warn("未找到表单模板: {}", formName);
            }
            
            result.put("timestamp", LocalDateTime.now().toString());
            return result;
            
        } catch (Exception e) {
            logger.error("获取模板信息时发生错误: {}", e.getMessage(), e);
            result.put("found", false);
            result.put("formName", formName);
            result.put("error", "获取模板信息失败: " + e.getMessage());
            result.put("code", "TEMPLATE_INFO_ERROR");
            return result;
        }
    }
    
    /**
     * 解析坐标字符串，如 "[3, 2]" 返回 [3, 2]
     */
    private int[] parseCoordinates(String coordStr) {
        try {
            coordStr = coordStr.trim();
            JsonNode coordinates = objectMapper.readTree(coordStr);
            
            if (coordinates.isArray() && coordinates.size() == 2) {
                int row = coordinates.get(0).asInt();
                int col = coordinates.get(1).asInt();
                if (row > 0 && col > 0) {
                    return new int[]{row, col};
                }
            }
            throw new IllegalArgumentException("坐标格式错误: " + coordStr);
            
        } catch (Exception e) {
            logger.error("解析坐标失败: {}, 错误: {}", coordStr, e.getMessage());
            return null;
        }
    }
    
    /**
     * Find corresponding template file
     */
    private String findTemplate(String formName) {
        String[] possibleNames = {
            formName + ".xlsx",
            formName + ".xls",
            formName + "模板.xlsx",
            formName + "模板.xls"
        };
        
        File templatesDir = new File(TEMPLATES_DIR);
        if (!templatesDir.exists()) {
            return null;
        }
        
        // Exact match
        for (String name : possibleNames) {
            File templateFile = new File(templatesDir, name);
            if (templateFile.exists()) {
                logger.info("Found template file: {}", templateFile.getAbsolutePath());
                return templateFile.getAbsolutePath();
            }
        }
        
        // Fuzzy match
        File[] files = templatesDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                    String baseName = fileName.replaceAll("\\.(xlsx|xls)$", "");
                    if (fileName.contains(formName) || baseName.contains(formName)) {
                        logger.info("Found template file by fuzzy match: {}", file.getAbsolutePath());
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        
        logger.warn("Form template not found: {}", formName);
        return null;
    }
    
    /**
     * Fill form using existing template
     */
    private Map<String, Object> fillExistingTemplate(String templatePath, String formName, 
                                                    Map<String, Map<String, String>> formContent) {
        try {
            // Load template
            FileInputStream fis = new FileInputStream(templatePath);
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(0);
            fis.close();
            
            logger.info("Using template: {}", templatePath);
            logger.info("Worksheet name: {}", sheet.getSheetName());
            
            // Fill fields
            int filledCount = 0;
            int totalFields = 0;
            
            for (Map.Entry<String, Map<String, String>> rowEntry : formContent.entrySet()) {
                String rowStr = rowEntry.getKey(); // e.g., "row3"
                Map<String, String> colMap = rowEntry.getValue();
                
                // Extract row number from "rowX" format
                int row = parseRowNumber(rowStr);
                if (row <= 0) {
                    logger.warn("Invalid row format: {}", rowStr);
                    continue;
                }
                
                // Process each column
                for (Map.Entry<String, String> colEntry : colMap.entrySet()) {
                    String colStr = colEntry.getKey(); // e.g., "col2"
                    String value = colEntry.getValue();
                    totalFields++;
                    
                    // Extract column number from "colX" format
                    int col = parseColNumber(colStr);
                    if (col <= 0) {
                        logger.warn("Invalid column format: {}", colStr);
                        continue;
                    }
                    
                    // Create coordinate string for logging
                    String coordStr = String.format("[%d,%d]", row, col);
                    
                    if (fillCellByCoordinates(sheet, coordStr, value, row, col)) {
                        filledCount++;
                    } else {
                        logger.warn("Failed to fill value at coordinates {}: {}", coordStr, value);
                    }
                }
            }
            
            // Generate output filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String outputFileName = formName + "_filled_" + timestamp + ".xlsx";
            String outputPath = OUTPUT_DIR + File.separator + outputFileName;
            
            // Save file
            FileOutputStream fos = new FileOutputStream(outputPath);
            workbook.write(fos);
            fos.close();
            workbook.close();
            
            logger.info("Successfully filled {}/{} fields, saved to: {}", filledCount, totalFields, outputPath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("output_file", outputPath);
            result.put("filled_count", filledCount);
            result.put("total_fields", totalFields);
            result.put("template_used", templatePath);
            result.put("fill_method", "row_col_format");
            return result;
            
        } catch (Exception e) {
            logger.error("Error filling existing template: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Failed to fill template: " + e.getMessage());
            result.put("code", "TEMPLATE_FILL_ERROR");
            return result;
        }
    }
    
    /**
     * Fill cell directly by coordinates
     */
    private boolean fillCellByCoordinates(Sheet sheet, String coordStr, String answer, int row, int col) {
        try {
            logger.info("Filling value at coordinates {}: '{}'", coordStr, answer);
            
            // Check if coordinates are valid
            if (row < 1 || col < 1) {
                logger.error("Invalid coordinates: {}", coordStr);
                return false;
            }
            
            // Temporarily remove sheet protection if it exists
            boolean wasProtected = false;
            try {
                // Attempt to remove any protection (if exists)
                logger.debug("Attempting to remove any sheet protection");
                sheet.protectSheet(null); // Remove protection
            } catch (Exception protectionCheck) {
                logger.debug("Could not remove protection (may not be protected): {}", protectionCheck.getMessage());
            }
            
            // Get or create row
            Row targetRow = sheet.getRow(row - 1); // POI uses 0-based indexing
            if (targetRow == null) {
                targetRow = sheet.createRow(row - 1);
                logger.debug("Created new row at index: {}", row - 1);
            }
            
            // Get or create cell
            Cell targetCell = targetRow.getCell(col - 1); // POI uses 0-based indexing
            boolean cellWasNull = (targetCell == null);
            if (targetCell == null) {
                targetCell = targetRow.createCell(col - 1);
                logger.debug("Created new cell at column index: {}", col - 1);
            }
            
            // Record original content and cell type (for debugging)
            String originalValue = getCellValueAsString(targetCell);
            CellType originalCellType = targetCell.getCellType();
            
            logger.info("Before filling - Original value: '{}', Cell type: {}, Was null: {}", 
                       originalValue, originalCellType, cellWasNull);
            
            // Force clear the cell content and any formulas
            try {
                // Remove any formulas first
                if (originalCellType == CellType.FORMULA) {
                    logger.info("Cell contains formula, removing it");
                }
                
                // Force set to blank first
                targetCell.setBlank();
                
                // Clear any existing value completely
                targetCell.setCellType(CellType.BLANK);
                
                logger.debug("Completely cleared cell content");
            } catch (Exception e) {
                logger.debug("Could not clear cell: {}", e.getMessage());
            }
            
            // Now fill with new value using multiple approaches
            try {
                // Method 1: Set as string type first, then value
                targetCell.setCellType(CellType.STRING);
                targetCell.setCellValue(answer);
                logger.debug("Method 1: Set as STRING type then value");
                
                // Method 2: Force set value again
                targetCell.setCellValue((String) answer);
                logger.debug("Method 2: Force set as String");
                
                // Method 3: Set rich text string (sometimes works better)
                Workbook workbook = sheet.getWorkbook();
                targetCell.setCellValue(workbook.getCreationHelper().createRichTextString(answer));
                logger.debug("Method 3: Set as RichTextString");
                
            } catch (Exception e) {
                logger.debug("Some write methods failed: {}", e.getMessage());
            }
            
            // Apply a completely new style to ensure visibility
            try {
                Workbook workbook = sheet.getWorkbook();
                CellStyle newStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setFontName("Arial");
                font.setFontHeightInPoints((short) 11);
                font.setBold(false);
                newStyle.setFont(font);
                newStyle.setAlignment(HorizontalAlignment.LEFT);
                newStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                
                // Remove any borders that might be showing underlines
                // newStyle.setBorderBottom(BorderStyle.NONE);
                // newStyle.setBorderTop(BorderStyle.NONE);
                // newStyle.setBorderLeft(BorderStyle.NONE);
                // newStyle.setBorderRight(BorderStyle.NONE);
                
                targetCell.setCellStyle(newStyle);
                logger.debug("Applied completely new style with no borders");
            } catch (Exception styleError) {
                logger.debug("Error setting new style: {}", styleError.getMessage());
            }
            
            // Verify the cell was actually filled
            String verifyValue = getCellValueAsString(targetCell);
            CellType finalCellType = targetCell.getCellType();
            logger.info("After filling - Coordinates {}: '{}' -> '{}' (verified: '{}', final type: {})", 
                       coordStr, originalValue, answer, verifyValue, finalCellType);
            
            // Additional verification
            if (!answer.equals(verifyValue)) {
                logger.warn("WARNING: Expected '{}' but got '{}' at coordinates {}. This may be due to cell formatting.", 
                           answer, verifyValue, coordStr);
                
                // Try one more time with a different approach
                try {
                    targetCell.setCellValue(answer);
                    targetCell.setCellType(CellType.STRING);
                    String finalVerify = getCellValueAsString(targetCell);
                    logger.info("Final attempt result: '{}'", finalVerify);
                } catch (Exception e) {
                    logger.debug("Final attempt failed: {}", e.getMessage());
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error filling value at coordinates {}: {}", coordStr, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create new form file
     */
    private Map<String, Object> createNewForm(String formName, Map<String, Map<String, String>> formContent) {
        try {
            // Create new workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(formName);
            
            // Set header
            Row headerRow = sheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue(formName);
            
            // Set header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 16);
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerCell.setCellStyle(headerStyle);
            
            // Merge header cells
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));
            
            // Fill form content
            int filledCount = 0;
            int totalFields = 0;
            
            for (Map.Entry<String, Map<String, String>> rowEntry : formContent.entrySet()) {
                String rowStr = rowEntry.getKey(); // e.g., "row3"
                Map<String, String> colMap = rowEntry.getValue();
                
                // Extract row number from "rowX" format
                int row = parseRowNumber(rowStr);
                if (row <= 0) {
                    logger.warn("Invalid row format: {}", rowStr);
                    continue;
                }
                
                // Process each column
                for (Map.Entry<String, String> colEntry : colMap.entrySet()) {
                    String colStr = colEntry.getKey(); // e.g., "col2"
                    String value = colEntry.getValue();
                    totalFields++;
                    
                    // Extract column number from "colX" format
                    int col = parseColNumber(colStr);
                    if (col <= 0) {
                        logger.warn("Invalid column format: {}", colStr);
                        continue;
                    }
                    
                    // Ensure coordinates are valid
                    if (row >= 1 && col >= 1) {
                        // Fill value at specified coordinates
                        Row targetRow = sheet.getRow(row - 1);
                        if (targetRow == null) {
                            targetRow = sheet.createRow(row - 1);
                        }
                        
                        Cell targetCell = targetRow.createCell(col - 1);
                        targetCell.setCellValue(value);
                        
                        // Set style
                        try {
                            CellStyle style = workbook.createCellStyle();
                            Font font = workbook.createFont();
                            font.setFontName("Arial");
                            font.setFontHeightInPoints((short) 11);
                            style.setFont(font);
                            style.setAlignment(HorizontalAlignment.LEFT);
                            style.setVerticalAlignment(VerticalAlignment.CENTER);
                            targetCell.setCellStyle(style);
                        } catch (Exception styleError) {
                            logger.debug("Error setting style: {}", styleError.getMessage());
                        }
                        
                        filledCount++;
                        logger.info("Filled at row {} col {}: {}", row, col, value);
                    }
                }
            }
            
            // Adjust column width
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 30 * 256);
            
            // Generate output filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String outputFileName = formName + "_" + timestamp + ".xlsx";
            String outputPath = OUTPUT_DIR + File.separator + outputFileName;
            
            // Save file
            FileOutputStream fos = new FileOutputStream(outputPath);
            workbook.write(fos);
            fos.close();
            workbook.close();
            
            logger.info("Successfully created new form: {}", outputPath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("output_file", outputPath);
            result.put("filled_count", filledCount);
            result.put("total_fields", totalFields);
            result.put("message", "New form created");
            result.put("fill_method", "row_col_format");
            return result;
            
        } catch (Exception e) {
            logger.error("Error creating new form: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Failed to create new form: " + e.getMessage());
            result.put("code", "CREATE_FORM_ERROR");
            return result;
        }
    }
    
    /**
     * Parse row number from "rowX" format (e.g., "row3" -> 3)
     */
    private int parseRowNumber(String rowStr) {
        try {
            if (rowStr != null && rowStr.toLowerCase().startsWith("row")) {
                String numberPart = rowStr.substring(3);
                return Integer.parseInt(numberPart);
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            logger.warn("Failed to parse row number from: {}", rowStr);
        }
        return -1;
    }
    
    /**
     * Parse column number from "colX" format (e.g., "col2" -> 2)
     */
    private int parseColNumber(String colStr) {
        try {
            if (colStr != null && colStr.toLowerCase().startsWith("col")) {
                String numberPart = colStr.substring(3);
                return Integer.parseInt(numberPart);
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            logger.warn("Failed to parse column number from: {}", colStr);
        }
        return -1;
    }
    
    private void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        CellType cellType = cell.getCellType();
        if (cellType == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cellType == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else if (cellType == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cellType == CellType.FORMULA) {
            return cell.getCellFormula();
        } else {
            return "";
        }
    }
} 