package com.campus.modules.common.controller;

import com.campus.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Tag(name = "文件上传", description = "图片上传相关接口")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final String UPLOAD_PATH = "./uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过10MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return Result.error("只支持 JPG/PNG/GIF/WEBP 格式的图片");
        }

        try {
            // 创建日期目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path uploadDir = Paths.get(UPLOAD_PATH, dateDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 返回访问URL
            String imageUrl = "/uploads/" + dateDir + "/" + filename;
            return Result.success(imageUrl);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传多张图片")
    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        if (files.size() > 9) {
            return Result.error("最多只能上传9张图片");
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.error("文件大小不能超过10MB");
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                return Result.error("只支持 JPG/PNG/GIF/WEBP 格式的图片");
            }

            try {
                String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                Path uploadDir = Paths.get(UPLOAD_PATH, dateDir);
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String originalFilename = file.getOriginalFilename();
                String ext = originalFilename != null && originalFilename.contains(".") 
                        ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                        : ".jpg";
                String filename = UUID.randomUUID().toString().replace("-", "") + ext;

                Path filePath = uploadDir.resolve(filename);
                Files.copy(file.getInputStream(), filePath);

                String imageUrl = "/uploads/" + dateDir + "/" + filename;
                imageUrls.add(imageUrl);

            } catch (IOException e) {
                return Result.error("文件上传失败: " + e.getMessage());
            }
        }

        return Result.success(imageUrls);
    }
}
