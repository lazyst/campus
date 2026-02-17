package com.campus.modules.forum.board;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.mapper.BoardMapper;
import com.campus.modules.forum.service.impl.BoardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BoardServiceTest {

    @Mock
    private BoardMapper boardMapper;

    @InjectMocks
    private BoardServiceImpl boardService;

    private Board testBoard;

    @BeforeEach
    void setUp() {
        setBaseMapper(boardService, boardMapper);

        testBoard = new Board();
        testBoard.setId(1L);
        testBoard.setName("学习交流");
        testBoard.setDescription("学习交流板块");
        testBoard.setIcon("book");
        testBoard.setSort(1);
        testBoard.setStatus(1);
    }

    private void setBaseMapper(Object service, Object baseMapper) {
        try {
            var field = service.getClass().getSuperclass().getDeclaredField("baseMapper");
            field.setAccessible(true);
            field.set(service, baseMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set baseMapper", e);
        }
    }

    @Nested
    @DisplayName("existsByName 方法测试")
    class ExistsByNameTests {

        @Test
        @DisplayName("板块名称存在时返回true")
        void shouldReturnTrueWhenNameExists() {
            when(boardMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            boolean result = boardService.existsByName("学习交流");

            assertTrue(result);
            verify(boardMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("板块名称不存在时返回false")
        void shouldReturnFalseWhenNameNotExists() {
            when(boardMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            boolean result = boardService.existsByName("不存在的板块");

            assertFalse(result);
            verify(boardMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("板块名称为空时也能正常查询")
        void shouldQueryWithEmptyName() {
            when(boardMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            boolean result = boardService.existsByName("");

            assertFalse(result);
            verify(boardMapper).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("IService 基础方法测试")
    class IServiceTests {

        @Test
        @DisplayName("根据ID查询板块")
        void shouldGetById() {
            when(boardMapper.selectById(1L)).thenReturn(testBoard);

            Board result = boardService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("学习交流", result.getName());
        }

        @Test
        @DisplayName("根据不存在的ID查询返回null")
        void shouldReturnNullWhenIdNotExists() {
            when(boardMapper.selectById(999L)).thenReturn(null);

            Board result = boardService.getById(999L);

            assertNull(result);
        }
    }
}
