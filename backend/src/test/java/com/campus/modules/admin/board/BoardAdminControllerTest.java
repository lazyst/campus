package com.campus.modules.admin.board;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.config.JwtConfig;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.service.BoardService;
import com.campus.modules.admin.controller.BoardManagementController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BoardAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtConfig jwtConfig;

    private String validAdminToken;

    @BeforeEach
    void setUp() {
        String jwtSecret = "campus-helping-platform-jwt-secret-key-2024-very-long-and-secure-123";
        validAdminToken = io.jsonwebtoken.Jwts.builder()
            .subject("1")
            .claim("role", 1)
            .claim("username", "admin")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .compact();
        
        when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
        when(adminService.isSuperAdmin(anyLong())).thenReturn(true);
        when(jwtConfig.getUsernameFromToken(anyString())).thenReturn("1");
        when(jwtConfig.getRoleFromToken(anyString())).thenReturn(1);
    }

    private static final String BASE_URL = "/api/admin/boards";

    private String getAuthHeader() {
        return "Bearer " + validAdminToken;
    }

    @Nested
    @DisplayName("GET /api/admin/boards - 获取板块列表")
    class GetBoardListTests {

        @Test
        @DisplayName("获取板块列表成功")
        void shouldGetBoardListSuccessfully() throws Exception {
            Page<Board> page = new Page<>(1, 20);
            Board board = createTestBoard(1L, "学习交流", "学习交流板块");
            page.setRecords(List.of(board));
            page.setTotal(1);

            when(boardService.page(any(Page.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records[0].name").value("学习交流"));
        }

        @Test
        @DisplayName("获取板块列表失败 - 未授权")
        void shouldFailGetBoardListWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/boards - 创建板块")
    class CreateBoardTests {

        @Test
        @DisplayName("创建板块成功")
        void shouldCreateBoardSuccessfully() throws Exception {
            BoardManagementController.BoardCreateRequest request = 
                    new BoardManagementController.BoardCreateRequest();
            request.setName("新板块");
            request.setDescription("这是一个新板块");
            request.setIcon("icon");

            Board board = createTestBoard(1L, "新板块", "这是一个新板块");
            when(boardService.save(any(Board.class))).thenReturn(true);

            mockMvc.perform(post(BASE_URL)
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("新板块"));
        }

        @Test
        @DisplayName("创建板块失败 - 未授权")
        void shouldFailCreateBoardWhenNotAuthorized() throws Exception {
            BoardManagementController.BoardCreateRequest request = 
                    new BoardManagementController.BoardCreateRequest();
            request.setName("新板块");

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/boards/{id} - 更新板块")
    class UpdateBoardTests {

        @Test
        @DisplayName("更新板块成功")
        void shouldUpdateBoardSuccessfully() throws Exception {
            Board existingBoard = createTestBoard(1L, "学习交流", "学习交流板块");
            existingBoard.setDeleted(0);

            BoardManagementController.BoardUpdateRequest request = 
                    new BoardManagementController.BoardUpdateRequest();
            request.setName("学习交流更新");
            request.setDescription("更新后的描述");

            when(boardService.getById(1L)).thenReturn(existingBoard);
            when(boardService.updateById(any(Board.class))).thenReturn(true);

            mockMvc.perform(put(BASE_URL + "/1")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新板块失败 - 板块不存在")
        void shouldFailUpdateBoardWhenNotFound() throws Exception {
            BoardManagementController.BoardUpdateRequest request = 
                    new BoardManagementController.BoardUpdateRequest();
            request.setName("不存在的板块");

            when(boardService.getById(99L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/99")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块不存在"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/boards/{id} - 删除板块")
    class DeleteBoardTests {

        @Test
        @DisplayName("删除板块成功")
        void shouldDeleteBoardSuccessfully() throws Exception {
            Board board = createTestBoard(1L, "学习交流", "学习交流板块");
            board.setDeleted(0);

            when(boardService.getById(1L)).thenReturn(board);
            when(boardService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除板块失败 - 板块不存在")
        void shouldFailDeleteBoardWhenNotFound() throws Exception {
            when(boardService.getById(99L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块不存在"));
        }

        @Test
        @DisplayName("删除板块失败 - 未授权")
        void shouldFailDeleteBoardWhenNotAuthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
        }
    }

    private Board createTestBoard(Long id, String name, String description) {
        Board board = new Board();
        board.setId(id);
        board.setName(name);
        board.setDescription(description);
        board.setIcon("default-icon");
        board.setSort(0);
        board.setStatus(1);
        board.setDeleted(0);
        return board;
    }
}
