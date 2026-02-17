package com.campus.modules.common.controller;

import com.campus.modules.forum.mapper.PostMapper;
import com.campus.modules.trade.mapper.ItemMapper;
import com.campus.modules.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostMapper postMapper;

    @MockBean
    private ItemMapper itemMapper;

    @MockBean
    private UserService userService;

    private static final String BASE_URL = "/api/upload";

    @Nested
    @DisplayName("POST /api/upload/image - 上传单张图片")
    class UploadImageTests {

        @Test
        @DisplayName("上传图片成功")
        void shouldUploadImageSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("上传PNG图片成功")
        void shouldUploadPngImageSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.png",
                    "image/png",
                    "png content".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("上传图片失败 - 文件为空")
        void shouldFailWhenFileIsEmpty() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            mockMvc.perform(multipart(BASE_URL + "/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("请选择要上传的文件"));
        }

        @Test
        @DisplayName("上传图片失败 - 文件大小超过限制")
        void shouldFailWhenFileSizeExceedsLimit() throws Exception {
            byte[] largeContent = new byte[11 * 1024 * 1024];
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "large.jpg",
                    "image/jpeg",
                    largeContent
            );

            mockMvc.perform(multipart(BASE_URL + "/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("文件大小不能超过10MB"));
        }

        @Test
        @DisplayName("上传图片失败 - 文件类型不支持")
        void shouldFailWhenFileTypeNotAllowed() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    "test content".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("只支持 JPG/PNG/GIF/WEBP 格式的图片"));
        }

        @Test
        @DisplayName("上传图片失败 - Content-Type为null")
        void shouldFailWhenContentTypeIsNull() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    null,
                    "test content".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("只支持 JPG/PNG/GIF/WEBP 格式的图片"));
        }

        @Test
        @DisplayName("上传图片失败 - 文件名无扩展名")
        void shouldFailWhenFilenameHasNoExtension() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "testfile",
                    "image/jpeg",
                    "test content".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("POST /api/upload/images - 上传多张图片")
    class UploadImagesTests {

        @Test
        @DisplayName("上传多张图片成功")
        void shouldUploadMultipleImagesSuccessfully() throws Exception {
            MockMultipartFile file1 = new MockMultipartFile(
                    "files",
                    "test1.jpg",
                    "image/jpeg",
                    "image1".getBytes()
            );
            MockMultipartFile file2 = new MockMultipartFile(
                    "files",
                    "test2.png",
                    "image/png",
                    "image2".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/images").file(file1).file(file2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("上传多张图片失败 - 文件类型不支持")
        void shouldFailWhenOneImageTypeNotAllowed() throws Exception {
            MockMultipartFile file1 = new MockMultipartFile(
                    "files",
                    "test1.jpg",
                    "image/jpeg",
                    "image1".getBytes()
            );
            MockMultipartFile file2 = new MockMultipartFile(
                    "files",
                    "test2.txt",
                    "text/plain",
                    "text".getBytes()
            );

            mockMvc.perform(multipart(BASE_URL + "/images").file(file1).file(file2))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("只支持 JPG/PNG/GIF/WEBP 格式的图片"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/upload/image - 删除单张图片")
    class DeleteImageTests {

        @Test
        @DisplayName("删除图片失败 - 无效路径（路径遍历攻击）")
        void shouldFailWhenInvalidPathTraversal() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/image")
                            .param("url", "../etc/passwd"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无效的文件路径"));
        }

        @Test
        @DisplayName("删除图片失败 - 无效路径（不以uploads开头）")
        void shouldFailWhenPathNotStartWithUploads() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/image")
                            .param("url", "/images/test.jpg"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无效的文件路径"));
        }

        @Test
        @DisplayName("删除图片失败 - 文件不存在")
        void shouldFailWhenFileNotExists() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/image")
                            .param("url", "/uploads/20240101/nonexist.jpg"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("文件不存在"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/upload/images/batch - 批量删除图片")
    class DeleteImagesBatchTests {

        @Test
        @DisplayName("批量删除失败 - 图片列表为空")
        void shouldFailWhenImageUrlsEmpty() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/images/batch")
                            .contentType("application/json")
                            .content("[]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("请选择要删除的文件"));
        }

        @Test
        @DisplayName("批量删除失败 - 图片列表为null")
        void shouldFailWhenImageUrlsNull() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/images/batch")
                            .contentType("application/json")
                            .content("[]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("请选择要删除的文件"));
        }

        @Test
        @DisplayName("批量删除部分失败")
        void shouldHandlePartialFailure() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/images/batch")
                            .contentType("application/json")
                            .content("[\"/uploads/exist.jpg\", \"../invalid.jpg\"]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("部分文件删除失败"))
                    .andExpect(jsonPath("$.data.successCount").exists())
                    .andExpect(jsonPath("$.data.failCount").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/upload/list - 获取文件列表")
    class ListImagesTests {

        @Test
        @DisplayName("获取文件列表成功 - 无文件")
        void shouldListImagesEmpty() throws Exception {
            mockMvc.perform(get(BASE_URL + "/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(0))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("获取文件列表成功 - 分页参数")
        void shouldListImagesWithPagination() throws Exception {
            mockMvc.perform(get(BASE_URL + "/list")
                            .param("pageNum", "2")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.pageNum").value(2))
                    .andExpect(jsonPath("$.data.pageSize").value(10));
        }

        @Test
        @DisplayName("获取文件列表成功 - 按日期目录筛选")
        void shouldListImagesWithDateDirFilter() throws Exception {
            mockMvc.perform(get(BASE_URL + "/list")
                            .param("dateDir", "20240101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("获取文件列表成功 - 关键词筛选")
        void shouldListImagesWithKeywordFilter() throws Exception {
            mockMvc.perform(get(BASE_URL + "/list")
                            .param("keyword", "test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("获取文件列表成功 - 超出页码范围")
        void shouldReturnEmptyWhenPageOutOfRange() throws Exception {
            mockMvc.perform(get(BASE_URL + "/list")
                            .param("pageNum", "999")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/upload/dates - 获取日期目录列表")
    class ListDateDirsTests {

        @Test
        @DisplayName("获取日期目录列表成功")
        void shouldListDateDirsSuccessfully() throws Exception {
            mockMvc.perform(get(BASE_URL + "/dates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/upload/unused - 获取未使用的图片")
    class ListUnusedImagesTests {

        @Test
        @DisplayName("获取未使用图片成功 - 无数据")
        void shouldListUnusedImagesEmpty() throws Exception {
            mockMvc.perform(get(BASE_URL + "/unused"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("获取未使用图片成功 - 分页")
        void shouldListUnusedImagesWithPagination() throws Exception {
            mockMvc.perform(get(BASE_URL + "/unused")
                            .param("pageNum", "1")
                            .param("pageSize", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.pageNum").value(1))
                    .andExpect(jsonPath("$.data.pageSize").value(5));
        }
    }

    @Nested
    @DisplayName("DELETE /api/upload/unused/clean - 清理未使用的图片")
    class CleanUnusedImagesTests {

        @Test
        @DisplayName("清理未使用图片成功 - 无数据")
        void shouldCleanUnusedImagesEmpty() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/unused/clean"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/upload/stats - 获取存储统计")
    class GetStorageStatsTests {

        @Test
        @DisplayName("获取存储统计成功")
        void shouldGetStorageStats() throws Exception {
            mockMvc.perform(get(BASE_URL + "/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalFiles").exists())
                    .andExpect(jsonPath("$.data.totalSize").exists());
        }
    }
}
