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
}
