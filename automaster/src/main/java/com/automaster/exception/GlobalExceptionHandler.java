package com.automaster.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器：统一返回结构化错误信息给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理数据完整性异常（如唯一键冲突）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Map<String, String> error = new HashMap<>();
        String msg = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();

        if (msg.contains("vin")) {
            error.put("code", "VIN_DUPLICATE");
            error.put("message", "VIN码已存在，请检查后重新录入");
        } else if (msg.contains("phone")) {
            error.put("code", "PHONE_DUPLICATE");
            error.put("message", "手机号已存在");
        } else {
            error.put("code", "DATA_ERROR");
            error.put("message", "数据校验失败：" + msg);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理通用参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("code", "PARAM_ERROR");
        error.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("code", "SYSTEM_ERROR");
        error.put("message", "系统异常：" + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}