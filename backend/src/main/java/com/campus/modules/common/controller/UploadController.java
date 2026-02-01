package com.campus.modules.common.controller;

import com.campus.common.Result;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件上传控制器
 */
@Tag(name = "文件上传", description = "图片上传相关接口")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final String UPLOAD_PATH = "./uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

    @Autowired
    private com.campus.modules.forum.mapper.PostMapper postMapper;

    @Autowired
    private com.campus.modules.trade.mapper.ItemMapper itemMapper;

    @Autowired
    private com.campus.modules.user.service.UserService userService;

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

    @Operation(summary = "删除图片")
    @DeleteMapping("/image")
    public Result<Void> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            // 解析图片路径
            String relativePath = imageUrl;
            if (imageUrl.startsWith("/")) {
                relativePath = imageUrl.substring(1);
            }

            // 防止路径遍历攻击
            if (relativePath.contains("..") || !relativePath.startsWith("uploads/")) {
                return Result.error("无效的文件路径");
            }

            Path filePath = Paths.get(relativePath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return Result.success();
            } else {
                return Result.error("文件不存在");
            }
        } catch (IOException e) {
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量删除图片")
    @DeleteMapping("/images/batch")
    public Result<Map<String, Object>> deleteImages(@RequestBody List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Result.error("请选择要删除的文件");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> failedUrls = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            try {
                String relativePath = imageUrl;
                if (imageUrl.startsWith("/")) {
                    relativePath = imageUrl.substring(1);
                }

                if (relativePath.contains("..") || !relativePath.startsWith("uploads/")) {
                    failCount++;
                    failedUrls.add(imageUrl);
                    continue;
                }

                Path filePath = Paths.get(relativePath);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    successCount++;
                } else {
                    failCount++;
                    failedUrls.add(imageUrl);
                }
            } catch (IOException e) {
                failCount++;
                failedUrls.add(imageUrl);
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("successCount", successCount);
        resultMap.put("failCount", failCount);
        resultMap.put("failedUrls", failedUrls);

        if (failCount > 0) {
            Result<Map<String, Object>> resultObj = Result.success(resultMap);
            resultObj.setMessage("部分文件删除失败");
            return resultObj;
        }
        return Result.success(resultMap);
    }

    @Operation(summary = "获取上传目录文件列表（分页）")
    @GetMapping("/list")
    public Result<Map<String, Object>> listImages(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String dateDir,
            @RequestParam(required = false) String keyword) {
        
        Path listDir;
        if (dateDir != null && !dateDir.isEmpty()) {
            listDir = Paths.get(UPLOAD_PATH, dateDir);
        } else {
            listDir = Paths.get(UPLOAD_PATH);
        }

        // 获取所有文件
        List<FileInfo> allFiles = new ArrayList<>();
        try {
            if (Files.exists(listDir)) {
                List<Path> dateDirs = Files.list(listDir)
                        .filter(Files::isDirectory)
                        .collect(Collectors.toList());
                
                for (Path dateDirPath : dateDirs) {
                    try {
                        List<Path> files = Files.list(dateDirPath)
                                .filter(Files::isRegularFile)
                                .collect(Collectors.toList());
                        
                        for (Path filePath : files) {
                            FileInfo info = buildFileInfo(dateDirPath.getFileName().toString(), filePath);
                            allFiles.add(info);
                        }
                    } catch (IOException e) {
                        // 忽略目录读取错误
                    }
                }
            }
        } catch (IOException e) {
            return Result.error("获取文件列表失败: " + e.getMessage());
        }

        // 关键词过滤
        if (keyword != null && !keyword.isEmpty()) {
            allFiles = allFiles.stream()
                    .filter(f -> f.getFileName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 按日期和文件名排序
        allFiles.sort((a, b) -> {
            int dateCompare = b.getDateDir().compareTo(a.getDateDir());
            if (dateCompare != 0) return dateCompare;
            return b.getFileName().compareTo(a.getFileName());
        });

        // 分页
        int total = allFiles.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<FileInfo> pagedFiles = start < total ? allFiles.subList(start, end) : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("records", pagedFiles);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("pages", (total + pageSize - 1) / pageSize);

        return Result.success(result);
    }

    @Operation(summary = "获取所有日期目录")
    @GetMapping("/dates")
    public Result<List<String>> listDateDirs() {
        try {
            Path uploadDir = Paths.get(UPLOAD_PATH);
            if (!Files.exists(uploadDir)) {
                return Result.success(Collections.emptyList());
            }

            List<String> dates = Files.list(uploadDir)
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());

            return Result.success(dates);
        } catch (IOException e) {
            return Result.error("获取日期目录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取未使用的图片")
    @GetMapping("/unused")
    public Result<Map<String, Object>> listUnusedImages(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            // 获取所有被引用的图片URL
            Set<String> usedUrls = new HashSet<>();

            // 从用户表获取头像 - 使用MyBatis-Plus查询
            List<User> users = userService.list();
            System.out.println("=== DEBUG: 查询到 " + users.size() + " 个用户");
            for (User user : users) {
                String avatar = user.getAvatar();
                System.out.println("=== DEBUG: 用户 " + user.getId() + " 的头像: " + avatar);
                if (avatar != null && !avatar.isEmpty()) {
                    // 规范化URL：去除协议和域名，只保留路径部分
                    String normalizedUrl = normalizeUrl(avatar);
                    System.out.println("=== DEBUG: 规范化后: " + normalizedUrl);
                    usedUrls.add(normalizedUrl);
                }
            }
            System.out.println("=== DEBUG: 总共 " + usedUrls.size() + " 个被使用的URL");

            // 从帖子表获取
            List<Post> posts = postMapper.selectList(null);
            for (Post post : posts) {
                if (post.getImages() != null) {
                    usedUrls.addAll(parseImageUrls(post.getImages()));
                }
                if (post.getThumbnail() != null) {
                    usedUrls.add(normalizeUrl(post.getThumbnail()));
                }
            }

            // 从闲置物品表获取
            List<Item> items = itemMapper.selectList(null);
            for (Item item : items) {
                if (item.getImages() != null) {
                    usedUrls.addAll(parseImageUrls(item.getImages()));
                }
                if (item.getThumbnail() != null) {
                    usedUrls.add(normalizeUrl(item.getThumbnail()));
                }
            }

            // 获取所有文件（包含日期目录和avatars目录）
            Path uploadDir = Paths.get(UPLOAD_PATH);
            if (!Files.exists(uploadDir)) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("total", 0);
                emptyResult.put("records", Collections.emptyList());
                emptyResult.put("pages", 0);
                return Result.success(emptyResult);
            }

            List<FileInfo> allFiles = new ArrayList<>();

            // 遍历所有子目录（日期目录和avatars等）
            Files.list(uploadDir)
                    .filter(Files::isDirectory)
                    .forEach(dateFolder -> {
                        try {
                            Files.list(dateFolder)
                                    .filter(Files::isRegularFile)
                                    .forEach(filePath -> {
                                        FileInfo info = buildFileInfo(dateFolder.getFileName().toString(), filePath);
                                        allFiles.add(info);
                                    });
                        } catch (IOException e) {
                            // 忽略目录读取错误
                        }
                    });

            // 筛选未使用的文件
            List<FileInfo> unusedFiles = allFiles.stream()
                    .filter(f -> !usedUrls.contains(f.getUrl()))
                    .sorted((a, b) -> {
                        int dateCompare = b.getDateDir().compareTo(a.getDateDir());
                        if (dateCompare != 0) return dateCompare;
                        return b.getFileName().compareTo(a.getFileName());
                    })
                    .collect(Collectors.toList());

            // 分页
            int total = unusedFiles.size();
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            List<FileInfo> pagedFiles = start < total ? unusedFiles.subList(start, end) : Collections.emptyList();

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("usedCount", allFiles.size() - total);
            result.put("records", pagedFiles);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", (total + pageSize - 1) / pageSize);

            return Result.success(result);
        } catch (IOException e) {
            return Result.error("获取未使用图片失败: " + e.getMessage());
        }
    }

    @Operation(summary = "清理未使用的图片")
    @DeleteMapping("/unused/clean")
    public Result<Map<String, Object>> cleanUnusedImages() {
        try {
            // 获取所有被引用的图片URL
            Set<String> usedUrls = new HashSet<>();

            // 从用户表获取头像 - 使用MyBatis-Plus查询
            List<User> users = userService.list();
            for (User user : users) {
                String avatar = user.getAvatar();
                if (avatar != null && !avatar.isEmpty()) {
                    usedUrls.add(normalizeUrl(avatar));
                }
            }

            List<Post> posts = postMapper.selectList(null);
            for (Post post : posts) {
                if (post.getImages() != null) {
                    usedUrls.addAll(parseImageUrls(post.getImages()));
                }
                if (post.getThumbnail() != null) {
                    usedUrls.add(normalizeUrl(post.getThumbnail()));
                }
            }

            List<Item> items = itemMapper.selectList(null);
            for (Item item : items) {
                if (item.getImages() != null) {
                    usedUrls.addAll(parseImageUrls(item.getImages()));
                }
                if (item.getThumbnail() != null) {
                    usedUrls.add(normalizeUrl(item.getThumbnail()));
                }
            }

            Path uploadDir = Paths.get(UPLOAD_PATH);
            if (!Files.exists(uploadDir)) {
                Map<String, Object> result = new HashMap<>();
                result.put("deletedCount", 0);
                result.put("totalSize", 0L);
                return Result.success(result);
            }

            int deletedCount = 0;
            long deletedSize = 0;

            List<Path> dirsToCheck = Files.list(uploadDir)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());

            for (Path dateFolder : dirsToCheck) {
                List<Path> files = Files.list(dateFolder)
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());

                for (Path filePath : files) {
                    String dateDir = dateFolder.getFileName().toString();
                    String fileName = filePath.getFileName().toString();
                    String url = "/uploads/" + dateDir + "/" + fileName;

                    if (!usedUrls.contains(url)) {
                        long size = Files.size(filePath);
                        Files.delete(filePath);
                        deletedCount++;
                        deletedSize += size;
                    }
                }

                // 如果日期目录为空，删除目录
                if (Files.list(dateFolder).count() == 0) {
                    Files.delete(dateFolder);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("deletedCount", deletedCount);
            result.put("totalSize", deletedSize);
            result.put("formattedSize", formatFileSize(deletedSize));

            return Result.success(result);
        } catch (IOException e) {
            return Result.error("清理未使用图片失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取存储统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStorageStats() {
        try {
            Path uploadDir = Paths.get(UPLOAD_PATH);
            if (!Files.exists(uploadDir)) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("totalFiles", 0);
                emptyResult.put("totalSize", 0L);
                emptyResult.put("formattedSize", "0 B");
                emptyResult.put("dateCount", 0);
                emptyResult.put("usedFiles", 0);
                emptyResult.put("unusedFiles", 0);
                return Result.success(emptyResult);
            }

            // 获取所有被引用的图片URL
            Set<String> usedUrls = new HashSet<>();

            // 从用户表获取头像 - 使用MyBatis-Plus查询
            List<User> users = userService.list();
            for (User user : users) {
                String avatar = user.getAvatar();
                if (avatar != null && !avatar.isEmpty()) {
                    usedUrls.add(normalizeUrl(avatar));
                }
            }

            List<Post> posts = postMapper.selectList(null);
            for (Post post : posts) {
                if (post.getImages() != null) {
                    usedUrls.addAll(parseImageUrls(post.getImages()));
                }
                if (post.getThumbnail() != null) {
                    usedUrls.add(normalizeUrl(post.getThumbnail()));
                }
            }

            List<Item> items = itemMapper.selectList(null);
            for (Item item : items) {
                if (item.getImages() != null) {
                    usedUrls.addAll(parseImageUrls(item.getImages()));
                }
                if (item.getThumbnail() != null) {
                    usedUrls.add(normalizeUrl(item.getThumbnail()));
                }
            }

            // 统计文件
            int totalFiles = 0;
            long totalSize = 0;
            int dateCount = 0;
            int unusedFiles = 0;

            List<Path> dateDirs = Files.list(uploadDir)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            dateCount = dateDirs.size();

            for (Path dateFolder : dateDirs) {
                List<Path> files = Files.list(dateFolder)
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());

                for (Path filePath : files) {
                    totalFiles++;
                    totalSize += Files.size(filePath);

                    String dateDir = dateFolder.getFileName().toString();
                    String fileName = filePath.getFileName().toString();
                    String url = "/uploads/" + dateDir + "/" + fileName;

                    if (!usedUrls.contains(url)) {
                        unusedFiles++;
                    }
                }
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalFiles", totalFiles);
            stats.put("totalSize", totalSize);
            stats.put("formattedSize", formatFileSize(totalSize));
            stats.put("dateCount", dateCount);
            stats.put("usedFiles", totalFiles - unusedFiles);
            stats.put("unusedFiles", unusedFiles);

            return Result.success(stats);
        } catch (IOException e) {
            return Result.error("获取存储统计失败: " + e.getMessage());
        }
    }

    private FileInfo buildFileInfo(String dateDir, Path filePath) {
        FileInfo info = new FileInfo();
        String fileName = filePath.getFileName().toString();
        info.setDateDir(dateDir);
        info.setFileName(fileName);
        info.setUrl("/uploads/" + dateDir + "/" + fileName);

        try {
            info.setFileSize(Files.size(filePath));
            info.setFormattedSize(formatFileSize(info.getFileSize()));
        } catch (IOException e) {
            info.setFileSize(0L);
            info.setFormattedSize("0 B");
        }

        // 获取文件类型
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "";
        info.setFileType(ext);

        // 获取创建时间
        try {
            info.setCreateTime(Files.getAttribute(filePath, "creationTime").toString());
        } catch (IOException e) {
            info.setCreateTime("");
        }

        return info;
    }

    private List<String> parseImageUrls(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }

        // 尝试解析为JSON数组
        if (imagesJson.startsWith("[")) {
            try {
                List<?> urls = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(imagesJson, List.class);
                return urls.stream()
                        .map(Object::toString)
                        .map(this::normalizeUrl)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // 解析失败，返回空列表
            }
        }

        // 逗号分隔的URL
        return Arrays.stream(imagesJson.split(","))
                .map(this::normalizeUrl)
                .collect(Collectors.toList());
    }

    /**
     * 规范化URL：去除协议和域名，只保留路径部分
     * 例如：http://localhost:8080/uploads/xxx.jpg -> /uploads/xxx.jpg
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        // 去除 http://、https:// 前缀
        url = url.replaceFirst("^https?://[^/]+", "");
        // 确保以 / 开头
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return url;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @Data
    public static class FileInfo {
        private String dateDir;
        private String fileName;
        private String url;
        private Long fileSize;
        private String formattedSize;
        private String fileType;
        private String createTime;
    }
}
