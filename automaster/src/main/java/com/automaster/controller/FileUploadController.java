package com.automaster.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 图片上传控制器
 */
@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传接口", description = "车辆图片上传接口")
public class FileUploadController {

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 上传车辆图片
     */
    @PostMapping("/car-image")
    @Operation(
            summary = "上传车辆图片",
            description = "上传图片到服务器本地，返回可访问的图片URL",
            responses = {
                    @ApiResponse(responseCode = "200", description = "上传成功，返回图片URL"),
                    @ApiResponse(responseCode = "400", description = "文件为空或格式错误"),
                    @ApiResponse(responseCode = "500", description = "上传失败")
            }
    )
    public ResponseEntity<String> uploadCarImage(
            @Parameter(description = "车辆图片文件（支持jpg/png/jpeg）", required = true)
            @RequestParam("file") MultipartFile file) {
        // 1. 校验文件
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("上传文件不能为空");
        }
        // 2. 校验文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches("^.+\\.(jpg|png|jpeg)$")) {
            return ResponseEntity.badRequest().body("仅支持jpg/png/jpeg格式的图片");
        }
        // 3. 创建存储目录（不存在则创建）
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean mkdirs = uploadDir.mkdirs();
            if (!mkdirs) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建上传目录失败");
            }
        }
        // 4. 生成唯一文件名（避免重复）
        String uniqueFileName = UUID.randomUUID().toString() +
                originalFilename.substring(originalFilename.lastIndexOf("."));
        String filePath = uploadPath + File.separator + uniqueFileName;
        // 5. 保存文件到本地
        try {
            File destFile = new File(filePath);
            file.transferTo(destFile);
            // 6. 返回可访问的URL（前端可直接访问）
            String imageUrl = "http://localhost:8080/uploads/" + uniqueFileName;
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("图片上传失败：" + e.getMessage());
        }
    }
}