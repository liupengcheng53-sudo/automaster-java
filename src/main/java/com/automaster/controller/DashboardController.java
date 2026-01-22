package com.automaster.controller;

import com.automaster.dto.DashboardStats;
import com.automaster.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j; // 新增日志注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j // 启用日志，需确保项目引入lombok依赖
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "仪表盘统计接口")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取仪表盘统计数据
     * 
     * @return 统计数据
     */
    @GetMapping("/stats")
    @Operation(
            summary = "获取仪表盘统计数据",
            description = "返回库存货值、销售额、利润、客户数等核心经营指标",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @Content(schema = @Schema(implementation = DashboardStats.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "服务器内部错误",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<DashboardStats> getDashboardStats() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            // 核心修改：打印异常栈，定位具体错误
            log.error("获取仪表盘统计数据失败：", e);
            // 服务端异常返回500，符合HTTP语义
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取近半年销售趋势数据
     * 
     * @return 每月销售额列表
     */
    @GetMapping("/sales-trend")
    @Operation(
            summary = "获取近半年销售趋势",
            description = "返回近6个月每月的销售额统计数据",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "服务器内部错误",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<?> getSalesTrend() {
        try {
            return ResponseEntity.ok(dashboardService.getSalesTrend());
        } catch (Exception e) {
            log.error("获取销售趋势数据失败：", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}