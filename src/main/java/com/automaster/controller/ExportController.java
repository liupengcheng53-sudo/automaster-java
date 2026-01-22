package com.automaster.controller;

import com.automaster.entity.Transaction;
import com.automaster.entity.Car;
import com.automaster.entity.Customer;
import com.automaster.repository.TransactionRepository;
import com.automaster.repository.CarRepository;
import com.automaster.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 导出控制器
 * 提供订单导出 Excel 功能
 */
@RestController
@RequestMapping("/api/export")
@Tag(name = "导出管理", description = "数据导出相关接口")
@CrossOrigin(origins = "*")
public class ExportController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * 导出所有交易订单为 Excel
     */
    @GetMapping("/transactions")
    @Operation(summary = "导出交易订单", description = "导出所有交易订单到 Excel 文件")
    public ResponseEntity<byte[]> exportTransactions() {
        try {
            // 查询所有交易记录
            List<Transaction> transactions = transactionRepository.findAll();

            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("交易订单");

            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"订单号", "订单状态", "车辆信息", "客户姓名", "客户电话", "定金", "成交价", "交易日期", "操作人"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建数据行
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);

                // 订单号
                row.createCell(0).setCellValue(transaction.getId());

                // 订单状态
                String status = "PENDING".equals(transaction.getStatus()) ? "预定中" : "已完成";
                row.createCell(1).setCellValue(status);

                // 车辆信息
                Car car = carRepository.findById(transaction.getCarId()).orElse(null);
                String carInfo = car != null ? car.getYear() + " " + car.getMake() + " " + car.getModel() : "未知";
                row.createCell(2).setCellValue(carInfo);

                // 客户信息
                Customer customer = customerRepository.findById(transaction.getCustomerId()).orElse(null);
                row.createCell(3).setCellValue(customer != null ? customer.getName() : "未知");
                row.createCell(4).setCellValue(customer != null ? customer.getPhone() : "未知");

                // 定金
                row.createCell(5).setCellValue(transaction.getDeposit() != null ? transaction.getDeposit() : 0);

                // 成交价
                row.createCell(6).setCellValue(transaction.getFinalPrice() != null ? transaction.getFinalPrice() : transaction.getPrice());

                // 交易日期
                row.createCell(7).setCellValue(transaction.getDate() != null ? sdf.format(transaction.getDate()) : "");

                // 操作人
                row.createCell(8).setCellValue(transaction.getHandledByUserId() != null ? transaction.getHandledByUserId() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小宽度
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3000));
            }

            // 将工作簿写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 设置响应头
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", "transactions_" + System.currentTimeMillis() + ".xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
