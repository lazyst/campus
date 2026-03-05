package com.campus.modules.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * 图床服务 - 使用 img.scdn.io 图床
 */
@Slf4j
@Service
public class ImgBedService {

    private static final String UPLOAD_URL = "https://img.scdn.io/api/v1.php";
    private static final int TIMEOUT = 30000;
    private static final int BUFFER_SIZE = 8192;

    /**
     * 上传图片到图床
     *
     * @param imageData 图片字节数据
     * @param fileName  文件名
     * @return 图床URL，失败返回null
     */
    public String uploadImage(byte[] imageData, String fileName) {
        if (imageData == null || imageData.length == 0) {
            log.warn("图片数据为空");
            return null;
        }

        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
        String CRLF = "\r\n";

        try {
            URL url = new URL(UPLOAD_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

                // 文件部分
                writer.append("--").append(boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(fileName).append("\"").append(CRLF);
                writer.append("Content-Type: image/").append(getImageType(fileName)).append(CRLF);
                writer.append(CRLF);
                writer.flush();

                output.write(imageData);
                output.flush();

                writer.append(CRLF);
                writer.append("--").append(boundary).append("--").append(CRLF);
                writer.flush();
            }

            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                // 解析JSON响应
                String responseStr = response.toString();
                log.info("图床响应: {}", responseStr);

                // 尝试提取URL - 支持多种响应格式
                String imgUrl = extractUrlFromResponse(responseStr);
                if (imgUrl != null) {
                    log.info("图片上传成功: {}", imgUrl);
                    return imgUrl;
                } else {
                    log.error("无法从响应中提取URL: {}", responseStr);
                    return null;
                }
            } else {
                log.error("图床请求失败，响应码: {}", responseCode);
                return null;
            }
        } catch (Exception e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 上传文件流到图床
     *
     * @param inputStream 图片输入流
     * @param fileName    文件名
     * @return 图床URL，失败返回null
     */
    public String uploadImage(InputStream inputStream, String fileName) {
        try {
            byte[] imageData = readAllBytes(inputStream);
            return uploadImage(imageData, fileName);
        } catch (IOException e) {
            log.error("读取图片数据失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从响应中提取URL
     */
    private String extractUrlFromResponse(String response) {
        try {
            // 使用Jackson解析JSON
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> json = mapper.readValue(response, Map.class);

            // 直接从顶层获取url
            if (json.containsKey("url")) {
                Object urlObj = json.get("url");
                if (urlObj != null) {
                    return urlObj.toString();
                }
            }

            // 从data中获取url
            if (json.containsKey("data")) {
                Object dataObj = json.get("data");
                if (dataObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    if (data.containsKey("url")) {
                        Object urlObj = data.get("url");
                        if (urlObj != null) {
                            return urlObj.toString();
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.warn("JSON解析失败，尝试文本提取: {}", e.getMessage());
            // 回退到文本解析
            return extractUrlFromResponseFallback(response);
        }
    }

    /**
     * 回退的URL提取方法
     */
    private String extractUrlFromResponseFallback(String response) {
        // 尝试正则表达式匹配
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
        java.util.regex.Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 获取图片类型
     */
    private String getImageType(String fileName) {
        if (fileName == null) {
            return "jpeg";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "png";
        } else if (lower.endsWith(".gif")) {
            return "gif";
        } else if (lower.endsWith(".webp")) {
            return "webp";
        } else if (lower.endsWith(".bmp")) {
            return "bmp";
        }
        return "jpeg";
    }

    /**
     * 读取输入流所有字节
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, BUFFER_SIZE)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }
}
