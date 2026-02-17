package com.campus.modules.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.modules.forum.dto.BoardCreateRequest;
import com.campus.modules.forum.dto.BoardUpdateRequest;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.service.BoardService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    private static final String BASE_URL = "/api/boards";

    // ==================== GET /api/boards - 获取所有启用板块列表 ====================

    @Nested
    @DisplayName("GET /api/boards - 获取所有启用板块列表")
    class GetBoardsListTests {

        @Test
        @DisplayName("获取启用板块列表成功")
        void shouldGetEnabledBoardsSuccessfully() throws Exception {
            Board board1 = createTestBoard(1L, "学习交流", "学习讨论区", 1);
            Board board2 = createTestBoard(2L, "生活分享", "生活话题区", 2);

            when(boardService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(board1, board2));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("学习交流"))
                    .andExpect(jsonPath("$.data[1].name").value("生活分享"));

            verify(boardService).list(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("获取板块列表 - 无启用板块返回空列表")
        void shouldReturnEmptyListWhenNoEnabledBoards() throws Exception {
            when(boardService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));

            verify(boardService).list(any(LambdaQueryWrapper.class));
        }
    }

    // ==================== GET /api/boards/page - 分页获取板块列表 ====================

    @Nested
    @DisplayName("GET /api/boards/page - 分页获取板块列表")
    class GetBoardsPageTests {

        @Test
        @DisplayName("分页获取板块列表成功")
        void shouldGetBoardsPageSuccessfully() throws Exception {
            Board board1 = createTestBoard(1L, "学习交流", "学习讨论区", 1);
            Board board2 = createTestBoard(2L, "生活分享", "生活话题区", 2);

            Page<Board> boardPage = new Page<>(1, 10);
            boardPage.setRecords(Arrays.asList(board1, board2));
            boardPage.setTotal(2);

            lenient().when(boardService.page(any(Page.class), any()))
                    .thenAnswer(invocation -> {
                        Page<Board> page = invocation.getArgument(0);
                        page.setRecords(Arrays.asList(board1, board2));
                        page.setTotal(2);
                        return page;
                    });

            mockMvc.perform(get(BASE_URL + "/page")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records.length()").value(2))
                    .andExpect(jsonPath("$.data.total").value(2));
        }

        @Test
        @DisplayName("分页获取板块列表 - 空结果")
        void shouldReturnEmptyPage() throws Exception {
            Page<Board> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);

            lenient().when(boardService.page(any(Page.class), any()))
                    .thenAnswer(invocation -> {
                        Page<Board> page = invocation.getArgument(0);
                        page.setRecords(Collections.emptyList());
                        page.setTotal(0);
                        return page;
                    });

            mockMvc.perform(get(BASE_URL + "/page")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").value(0));
        }
    }

    // ==================== GET /api/boards/{id} - 获取板块详情 ====================

    @Nested
    @DisplayName("GET /api/boards/{id} - 获取板块详情")
    class GetBoardDetailTests {

        @Test
        @DisplayName("获取板块详情成功")
        void shouldGetBoardDetailSuccessfully() throws Exception {
            Board board = createTestBoard(1L, "学习交流", "学习讨论区", 1);

            when(boardService.getById(1L)).thenReturn(board);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("学习交流"))
                    .andExpect(jsonPath("$.data.description").value("学习讨论区"));

            verify(boardService).getById(1L);
        }

        @Test
        @DisplayName("获取板块详情失败 - 板块不存在")
        void shouldFailGetBoardWhenNotFound() throws Exception {
            when(boardService.getById(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块不存在"));

            verify(boardService).getById(999L);
        }
    }

    // ==================== POST /api/boards - 创建板块 ====================

    @Nested
    @DisplayName("POST /api/boards - 创建板块")
    class CreateBoardTests {

        @Test
        @DisplayName("创建板块成功")
        void shouldCreateBoardSuccessfully() throws Exception {
            BoardCreateRequest request = new BoardCreateRequest();
            request.setName("新板块");
            request.setDescription("新板块描述");
            request.setSort(1);

            Board savedBoard = createTestBoard(1L, "新板块", "新板块描述", 1);

            when(boardService.existsByName("新板块")).thenReturn(false);
            when(boardService.save(any(Board.class))).thenReturn(true);
            when(boardService.getById(1L)).thenReturn(savedBoard);

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("新板块"));

            verify(boardService).existsByName("新板块");
            verify(boardService).save(any(Board.class));
        }

        @Test
        @DisplayName("创建板块失败 - 板块名称已存在")
        void shouldFailCreateBoardWhenNameExists() throws Exception {
            BoardCreateRequest request = new BoardCreateRequest();
            request.setName("已存在板块");

            when(boardService.existsByName("已存在板块")).thenReturn(true);

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块名称已存在"));

            verify(boardService).existsByName("已存在板块");
            verify(boardService, never()).save(any(Board.class));
        }

        @Test
        @DisplayName("创建板块失败 - 名称为空")
        void shouldFailCreateBoardWhenNameEmpty() throws Exception {
            BoardCreateRequest request = new BoardCreateRequest();
            request.setName("");

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== PUT /api/boards/{id} - 更新板块 ====================

    @Nested
    @DisplayName("PUT /api/boards/{id} - 更新板块")
    class UpdateBoardTests {

        @Test
        @DisplayName("更新板块成功")
        void shouldUpdateBoardSuccessfully() throws Exception {
            BoardUpdateRequest request = new BoardUpdateRequest();
            request.setName("更新后名称");
            request.setDescription("更新后描述");

            Board existingBoard = createTestBoard(1L, "原名称", "原描述", 1);
            Board updatedBoard = createTestBoard(1L, "更新后名称", "更新后描述", 1);

            when(boardService.getById(1L)).thenReturn(existingBoard);
            when(boardService.existsByName("更新后名称")).thenReturn(false);
            when(boardService.updateById(any(Board.class))).thenReturn(true);
            when(boardService.getById(1L)).thenReturn(updatedBoard);

            mockMvc.perform(put(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("更新后名称"));

            verify(boardService).getById(1L);
            verify(boardService).updateById(any(Board.class));
        }

        @Test
        @DisplayName("更新板块失败 - 板块不存在")
        void shouldFailUpdateBoardWhenNotFound() throws Exception {
            BoardUpdateRequest request = new BoardUpdateRequest();
            request.setName("新名称");

            when(boardService.getById(999L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块不存在"));

            verify(boardService).getById(999L);
        }

        @Test
        @DisplayName("更新板块失败 - 板块名称已存在")
        void shouldFailUpdateBoardWhenNameExists() throws Exception {
            BoardUpdateRequest request = new BoardUpdateRequest();
            request.setName("已存在名称");

            Board existingBoard = createTestBoard(1L, "原名称", "描述", 1);

            when(boardService.getById(1L)).thenReturn(existingBoard);
            when(boardService.existsByName("已存在名称")).thenReturn(true);

            mockMvc.perform(put(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块名称已存在"));

            verify(boardService).getById(1L);
            verify(boardService).existsByName("已存在名称");
        }
    }

    // ==================== DELETE /api/boards/{id} - 删除板块 ====================

    @Nested
    @DisplayName("DELETE /api/boards/{id} - 删除板块")
    class DeleteBoardTests {

        @Test
        @DisplayName("删除板块成功")
        void shouldDeleteBoardSuccessfully() throws Exception {
            Board board = createTestBoard(1L, "测试板块", "描述", 1);

            when(boardService.getById(1L)).thenReturn(board);
            when(boardService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));

            verify(boardService).getById(1L);
            verify(boardService).removeById(1L);
        }

        @Test
        @DisplayName("删除板块失败 - 板块不存在")
        void shouldFailDeleteBoardWhenNotFound() throws Exception {
            when(boardService.getById(999L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块不存在"));

            verify(boardService).getById(999L);
        }
    }

    // ==================== Helper Methods ====================

    private Board createTestBoard(Long id, String name, String description, Integer sort) {
        Board board = new Board();
        board.setId(id);
        board.setName(name);
        board.setDescription(description);
        board.setIcon(null);
        board.setSort(sort);
        board.setStatus(1);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        return board;
    }
}
