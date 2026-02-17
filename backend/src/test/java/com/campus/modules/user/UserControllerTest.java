package com.campus.modules.user;

import com.campus.modules.auth.service.AuthService;
import com.campus.modules.user.controller.UserController;
import com.campus.modules.user.dto.UpdateProfileRequest;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private static final String BASE_URL = "/api/user";

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800000001");
        user.setNickname("测试用户");
        user.setGender(1);
        user.setBio("这是我的个人简介");
        user.setAvatar("/uploads/avatars/test.jpg");
        user.setGrade("大三");
        user.setMajor("计算机科学与技术");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Nested
    @DisplayName("GET /api/user/profile - 获取当前用户信息")
    class GetProfileTests {

        @Test
        @DisplayName("获取用户信息成功")
        void shouldGetProfileSuccessfully() throws Exception {
            User user = createTestUser();
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(userService.getById(1L)).thenReturn(user);

            mockMvc.perform(get(BASE_URL + "/profile")
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.nickname").value("测试用户"));
        }

        @Test
        @DisplayName("未登录时返回null")
        void shouldReturnNullWhenNotLoggedIn() throws Exception {
            mockMvc.perform(get(BASE_URL + "/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("Token无效时返回null")
        void shouldReturnNullWhenTokenInvalid() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/profile")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("用户不存在时返回null")
        void shouldReturnNullWhenUserNotFound() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(userService.getById(1L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/profile")
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    @Nested
    @DisplayName("PUT /api/user/profile - 更新用户信息")
    class UpdateProfileTests {

        @Test
        @DisplayName("更新用户信息成功")
        void shouldUpdateProfileSuccessfully() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            doNothing().when(userService).updateProfile(anyLong(), anyString(), any(), any(), any(), any(), any());

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setNickname("新昵称");
            request.setGender(1);
            request.setBio("新的个人简介");
            request.setGrade("大四");
            request.setMajor("软件工程");

            mockMvc.perform(put(BASE_URL + "/profile")
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));
        }

        @Test
        @DisplayName("Token无效时更新失败")
        void shouldFailUpdateWhenTokenInvalid() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(null);

            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setNickname("新昵称");

            mockMvc.perform(put(BASE_URL + "/profile")
                    .header("Authorization", "Bearer invalid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无效的认证令牌"));
        }
    }

    @Nested
    @DisplayName("GET /api/user/public/{userId} - 获取用户公开信息")
    class GetUserPublicInfoTests {

        @Test
        @DisplayName("获取用户公开信息成功")
        void shouldGetUserPublicInfoSuccessfully() throws Exception {
            User user = createTestUser();
            when(userService.getById(1L)).thenReturn(user);

            mockMvc.perform(get(BASE_URL + "/public/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                    .andExpect(jsonPath("$.data.bio").value("这是我的个人简介"));
        }

        @Test
        @DisplayName("用户不存在时返回错误")
        void shouldReturnErrorWhenUserNotFound() throws Exception {
            when(userService.getById(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/public/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("GET /api/user/profile/{userId} - 获取用户详细信息")
    class GetUserDetailInfoTests {

        @Test
        @DisplayName("获取用户详细信息成功")
        void shouldGetUserDetailInfoSuccessfully() throws Exception {
            User user = createTestUser();
            when(userService.getById(1L)).thenReturn(user);

            mockMvc.perform(get(BASE_URL + "/profile/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                    .andExpect(jsonPath("$.data.grade").value("大三"))
                    .andExpect(jsonPath("$.data.major").value("计算机科学与技术"));
        }

        @Test
        @DisplayName("用户不存在时返回错误")
        void shouldReturnErrorWhenUserNotFound() throws Exception {
            when(userService.getById(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/profile/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/user/account - 注销账号")
    class DeactivateAccountTests {

        @Test
        @DisplayName("注销账号成功")
        void shouldDeactivateAccountSuccessfully() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            doNothing().when(userService).deactivateAccount(1L);

            mockMvc.perform(delete(BASE_URL + "/account")
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));
        }

        @Test
        @DisplayName("Token无效时注销失败")
        void shouldFailDeactivateWhenTokenInvalid() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/account")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无效的认证令牌"));
        }
    }

    @Nested
    @DisplayName("POST /api/user/avatar - 上传头像")
    class UploadAvatarTests {

        @Test
        @DisplayName("上传头像成功")
        void shouldUploadAvatarSuccessfully() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            doNothing().when(userService).updateAvatar(anyLong(), anyString());

            MockMultipartFile mockFile = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "test image content".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/avatar")
                    .file(mockFile)
                    .header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));
        }

        @Test
        @DisplayName("Token无效时上传失败")
        void shouldFailUploadWhenTokenInvalid() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(null);

            MockMultipartFile mockFile = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "test image content".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/avatar")
                    .file(mockFile)
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无效的认证令牌"));
        }
    }
}
