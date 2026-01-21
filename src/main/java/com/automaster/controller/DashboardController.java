package com.automaster.controller;

import com.automaster.dto.DashboardStats;
import com.automaster.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘统计控制器
 * 提供经营数据统计接口
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
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
            return ResponseEntity.internalServerError().build();
        }
    }
}
