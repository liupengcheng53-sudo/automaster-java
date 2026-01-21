package com.automaster.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用错误响应DTO（适配Swagger注解类型要求）
 */
@Data
@Schema(description = "通用错误响应体")
public class ErrorResponse {
    @Schema(description = "错误码", example = "VIN_DUPLICATE")
    private String code;

    @Schema(description = "错误提示信息", example = "VIN码已存在，请检查后重新录入")
    private String message;
}