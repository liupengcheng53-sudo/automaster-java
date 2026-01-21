package com.automaster.controller;

import com.automaster.entity.Customer;
import com.automaster.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 客户管理控制器（适配前端 + 注入Service）
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "客户管理接口", description = "客户的新增、查询、修改、删除、搜索接口，包含未购车客户专用查询")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * 查询所有客户
     */
    @GetMapping
    @Operation(
            summary = "查询所有客户",
            description = "获取数据库中所有客户信息，按录入时间倒序排列",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回客户列表",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "204", description = "暂无客户数据", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<List<Customer>> getAll() {
        List<Customer> customers = customerService.getAllCustomers();
        return customers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(customers);
    }

    /**
     * 搜索客户（姓名/电话）
     */
    @GetMapping("/search")
    @Operation(
            summary = "按关键词搜索客户",
            description = "按客户姓名或手机号进行模糊匹配搜索，返回所有匹配的客户信息",
            parameters = {
                    @Parameter(name = "keyword", description = "搜索关键词（姓名/手机号）", required = true,
                            example = "张三", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "搜索成功，返回匹配的客户列表",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "204", description = "无匹配的客户数据", content = @Content),
                    @ApiResponse(responseCode = "400", description = "参数错误（关键词为空）", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<List<Customer>> search(
            @Parameter(description = "搜索关键词（姓名/手机号）", required = true)
            @RequestParam String keyword
    ) {
        List<Customer> customers = customerService.searchCustomers(keyword);
        return customers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(customers);
    }

    /**
     * 未购车客户专用接口（适配前端下拉菜单）
     */
    @GetMapping("/unpurchased")
    @Operation(
            summary = "查询未购车客户",
            description = "默认返回最新10个未购车客户（无购车订单），支持按姓名模糊搜索未购车客户",
            parameters = {
                    @Parameter(name = "keyword", description = "搜索关键词（姓名或手机号，可为空）", required = false,
                            example = "李四", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回未购车客户列表",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "204", description = "暂无未购车客户", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<List<Customer>> getUnpurchasedCustomers(
            @Parameter(description = "搜索关键词（姓名或手机号，可为空）")
            @RequestParam(required = false) String keyword
    ) {
        List<Customer> customers = customerService.searchUnpurchasedCustomers(keyword);
        return customers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(customers);
    }

    /**
     * 新增客户
     */
    @PostMapping
    @Operation(
            summary = "新增客户",
            description = "添加新客户信息，姓名、手机号、客户类型为必填项，手机号唯一",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "客户信息实体", required = true,
                    content = @Content(schema = @Schema(implementation = Customer.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "新增成功，返回新增的客户信息",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误（必填项为空/手机号重复）",
                            content = @Content(schema = @Schema(implementation = com.automaster.dto.ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<Customer> add(
            @Parameter(description = "客户信息", required = true)
            @RequestBody Customer customer
    ) {
        try {
            Customer saved = customerService.saveCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 修改客户
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "修改客户信息",
            description = "根据客户ID更新客户信息，ID不存在则返回404，手机号修改后需保证唯一",
            parameters = {
                    @Parameter(name = "id", description = "客户ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "新的客户信息", required = true,
                    content = @Content(schema = @Schema(implementation = Customer.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "修改成功，返回修改后的客户信息",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误（必填项为空/手机号重复）",
                            content = @Content(schema = @Schema(implementation = com.automaster.dto.ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "客户ID不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<Customer> update(
            @Parameter(description = "客户ID", required = true)
            @PathVariable String id,
            @Parameter(description = "新的客户信息", required = true)
            @RequestBody Customer customer
    ) {
        Optional<Customer> updated = customerService.updateCustomer(id, customer);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除客户",
            description = "根据客户ID删除客户信息，删除后不可恢复，若客户关联订单可能删除失败",
            parameters = {
                    @Parameter(name = "id", description = "客户ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "删除成功，无返回内容", content = @Content),
                    @ApiResponse(responseCode = "404", description = "客户ID不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误（如客户关联订单）", content = @Content)
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "客户ID", required = true)
            @PathVariable String id
    ) {
        boolean deleted = customerService.deleteCustomer(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * 按ID查询客户（用于编辑回显）
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "按ID查询客户",
            description = "根据客户ID查询单个客户的完整信息，用于编辑回显等场景",
            parameters = {
                    @Parameter(name = "id", description = "客户ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回客户详细信息",
                            content = @Content(schema = @Schema(implementation = Customer.class))),
                    @ApiResponse(responseCode = "404", description = "客户ID不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<Customer> getById(
            @Parameter(description = "客户ID", required = true)
            @PathVariable String id
    ) {
        Optional<Customer> customer = customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}