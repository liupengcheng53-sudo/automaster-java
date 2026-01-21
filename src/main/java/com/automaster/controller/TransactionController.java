package com.automaster.controller;

import com.automaster.entity.Transaction;
import com.automaster.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 交易订单管理控制器
 * 接口路径：/api/transactions，与前端完全适配
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "交易订单接口", description = "交易订单的新增、查询、删除接口，创建交易自动更新车辆状态为售出")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 查询所有交易订单（关联车辆/客户信息）
     */
    @GetMapping
    @Operation(
            summary = "查询所有交易订单",
            description = "获取所有交易订单，并关联返回车辆、客户详情（用于前端展示）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回交易订单列表"),
                    @ApiResponse(responseCode = "204", description = "暂无交易数据")
            }
    )
    public ResponseEntity<List<Transaction>> getAll() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return transactions.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(transactions);
    }

    /**
     * 新增交易订单（核心：自动改车辆状态为SOLD）
     */
    @PostMapping
    @Operation(
            summary = "新增交易订单",
            description = "创建销售订单，自动将关联车辆状态改为SOLD，请求体需包含carId、customerId、price等必填字段",
            responses = {
                    @ApiResponse(responseCode = "201", description = "创建成功，返回交易订单信息（含关联车辆/客户）"),
                    @ApiResponse(responseCode = "400", description = "参数错误（如车辆已售出/客户不存在）"),
                    @ApiResponse(responseCode = "404", description = "关联车辆/客户不存在")
            }
    )
    public ResponseEntity<?> add(
            @Parameter(description = "交易订单信息，carId、customerId、price为必填项", required = true)
            @RequestBody Transaction transaction
    ) {
        try {
            Transaction saved = transactionService.saveTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            // 业务异常返回400+错误信息
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除交易订单
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除交易订单",
            description = "根据订单ID删除交易记录（注意：删除后不会自动恢复车辆状态）",
            responses = {
                    @ApiResponse(responseCode = "204", description = "删除成功，无返回内容"),
                    @ApiResponse(responseCode = "404", description = "订单ID不存在")
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "要删除的订单ID，不能为空且必须存在", required = true)
            @PathVariable String id
    ) {
        boolean deleted = transactionService.deleteTransaction(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * 按ID查询交易订单
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "按ID查询交易订单",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回订单详情"),
                    @ApiResponse(responseCode = "404", description = "订单ID不存在")
            }
    )
    public ResponseEntity<Transaction> getById(
            @Parameter(description = "交易订单ID", required = true)
            @PathVariable String id
    ) {
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 完成预定交易（预定转销售）
     */
    @PutMapping("/{id}/complete")
    @Operation(
            summary = "完成预定交易",
            description = "将预定状态的交易转为已完成，填写最终成交价，同时更新车辆状态为Sold",
            responses = {
                    @ApiResponse(responseCode = "200", description = "完成成功，返回更新后的交易记录"),
                    @ApiResponse(responseCode = "400", description = "业务错误（如交易不是预定状态）"),
                    @ApiResponse(responseCode = "404", description = "交易记录不存在")
            }
    )
    public ResponseEntity<?> completeTransaction(
            @Parameter(description = "交易订单ID", required = true)
            @PathVariable String id,
            @Parameter(description = "最终成交价（元）", required = true)
            @RequestParam Integer finalPrice
    ) {
        try {
            Transaction completed = transactionService.completeTransaction(id, finalPrice);
            return ResponseEntity.ok(completed);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
