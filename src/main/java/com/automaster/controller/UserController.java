package com.automaster.controller;

import com.automaster.dto.ErrorResponse;
import com.automaster.entity.User;
import com.automaster.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
 * 提供用户的增删改查、角色管理、状态管理等接口
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理接口", description = "用户的新增、查询、修改、删除、角色管理、状态管理接口")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 查询所有用户
     */
    @GetMapping
    @Operation(
            summary = "查询所有用户",
            description = "获取数据库中所有用户信息，按创建时间倒序排列",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回用户列表",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "204", description = "暂无用户数据", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAllUsers();
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "按ID查询用户",
            description = "根据用户ID查询单个用户的完整信息，用于编辑回显等场景",
            parameters = {
                    @Parameter(name = "id", description = "用户ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回用户详细信息",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "用户ID不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<User> getById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable String id
    ) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 新增用户
     */
    @PostMapping
    @Operation(
            summary = "新增用户",
            description = "添加新用户信息，用户名、姓名、密码、角色为必填项，用户名唯一",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "用户信息实体", required = true,
                    content = @Content(schema = @Schema(implementation = User.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "新增成功，返回新增的用户信息",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误（必填项为空/用户名重复）",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "用户名重复",
                                                    value = "{\"code\":\"USERNAME_DUPLICATE\",\"message\":\"用户名已存在\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "参数为空",
                                                    value = "{\"code\":\"PARAM_ERROR\",\"message\":\"用户名不能为空\"}"
                                            )
                                    }
                            )),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<?> add(
            @Parameter(description = "用户信息", required = true)
            @RequestBody User user
    ) {
        try {
            User saved = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("code", "PARAM_ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 修改用户信息
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "修改用户信息",
            description = "根据用户ID更新用户信息，ID不存在则返回404，用户名修改后需保证唯一",
            parameters = {
                    @Parameter(name = "id", description = "用户ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "新的用户信息", required = true,
                    content = @Content(schema = @Schema(implementation = User.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "修改成功，返回修改后的用户信息",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误（必填项为空/用户名重复）",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "用户ID不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<?> update(
            @Parameter(description = "用户ID", required = true)
            @PathVariable String id,
            @Parameter(description = "新的用户信息", required = true)
            @RequestBody User user
    ) {
        try {
            Optional<User> updated = userService.updateUser(id, user);
            if (updated.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated.get());
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("code", "PARAM_ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除用户",
            description = "根据用户ID删除用户信息，删除后不可恢复，若用户关联交易记录可能删除失败",
            parameters = {
                    @Parameter(name = "id", description = "用户ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "删除成功，无返回内容", content = @Content),
                    @ApiResponse(responseCode = "404", description = "用户ID不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误（如用户关联交易记录）", content = @Content)
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "用户ID", required = true)
            @PathVariable String id
    ) {
        boolean deleted = userService.deleteUser(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * 按角色查询用户
     */
    @GetMapping("/by-role")
    @Operation(
            summary = "按角色查询用户",
            description = "根据用户角色筛选用户（Admin-管理员，Sales-销售）",
            parameters = {
                    @Parameter(name = "role", description = "用户角色", required = true,
                            example = "Sales", schema = @Schema(type = "string",
                            allowableValues = {"Admin", "Sales"}))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回对应角色的用户列表",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "204", description = "暂无对应角色的用户数据", content = @Content),
                    @ApiResponse(responseCode = "400", description = "角色参数为空或不合法", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<List<User>> getUsersByRole(
            @Parameter(description = "用户角色（Admin/Sales）", required = true)
            @RequestParam String role
    ) {
        if (role == null || role.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<User> users = userService.getUsersByRole(role);
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }

    /**
     * 按姓名搜索用户
     */
    @GetMapping("/search")
    @Operation(
            summary = "按姓名搜索用户",
            description = "按用户姓名进行模糊匹配搜索，返回所有匹配的用户信息",
            parameters = {
                    @Parameter(name = "name", description = "搜索关键词（姓名）", required = true,
                            example = "张三", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "搜索成功，返回匹配的用户列表",
                            content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "204", description = "无匹配的用户数据", content = @Content),
                    @ApiResponse(responseCode = "400", description = "参数错误（关键词为空）", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<List<User>> search(
            @Parameter(description = "搜索关键词（姓名）", required = true)
            @RequestParam String name
    ) {
        List<User> users = userService.searchUsersByName(name);
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }

    /**
     * 更新用户状态
     */
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "更新用户状态",
            description = "启用或禁用用户账号（ACTIVE-正常，DISABLED-禁用）",
            parameters = {
                    @Parameter(name = "id", description = "用户ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string")),
                    @Parameter(name = "status", description = "账号状态", required = true,
                            example = "ACTIVE", schema = @Schema(type = "string",
                            allowableValues = {"ACTIVE", "DISABLED"}))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "状态更新成功", content = @Content),
                    @ApiResponse(responseCode = "400", description = "参数错误（状态值不合法）", content = @Content),
                    @ApiResponse(responseCode = "404", description = "用户ID不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<?> updateStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable String id,
            @Parameter(description = "账号状态（ACTIVE/DISABLED）", required = true)
            @RequestParam String status
    ) {
        try {
            boolean updated = userService.updateUserStatus(id, status);
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "状态更新成功");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("code", "PARAM_ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
