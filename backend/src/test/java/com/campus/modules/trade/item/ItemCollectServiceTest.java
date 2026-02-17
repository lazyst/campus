package com.campus.modules.trade.item;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.config.TestUtils;
import com.campus.modules.trade.entity.ItemCollect;
import com.campus.modules.trade.mapper.ItemCollectMapper;
import com.campus.modules.trade.service.ItemCollectService;
import com.campus.modules.trade.service.impl.ItemCollectServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 物品收藏服务单元测试 - JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemCollectServiceTest {

    @Mock
    private ItemCollectMapper itemCollectMapper;

    @Mock
    private com.campus.modules.trade.service.ItemService itemService;

    @InjectMocks
    private ItemCollectServiceImpl itemCollectService;

    private ItemCollect testCollect;

    @BeforeEach
    void setUp() {
        // 注入baseMapper
        TestUtils.setBaseMapper(itemCollectService, itemCollectMapper);

        testCollect = new ItemCollect();
        testCollect.setId(1L);
        testCollect.setUserId(1L);
        testCollect.setItemId(100L);
        testCollect.setDeleted(0);
        testCollect.setCreatedAt(LocalDateTime.now());
        testCollect.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("hasCollected 方法测试")
    class HasCollectedTests {

        @Test
        @DisplayName("用户已收藏物品时返回true")
        void shouldReturnTrueWhenUserHasCollected() {
            when(itemCollectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            boolean result = itemCollectService.hasCollected(1L, 100L);

            assertTrue(result);
            verify(itemCollectMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("用户未收藏物品时返回false")
        void shouldReturnFalseWhenUserHasNotCollected() {
            when(itemCollectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            boolean result = itemCollectService.hasCollected(1L, 100L);

            assertFalse(result);
        }

        @Test
        @DisplayName("不同用户的收藏记录不干扰")
        void shouldNotAffectOtherUsersCollect() {
            when(itemCollectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            boolean result = itemCollectService.hasCollected(2L, 100L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("toggleCollect 方法测试")
    class ToggleCollectTests {

        @Test
        @DisplayName("未收藏时添加收藏返回true")
        void shouldAddCollectWhenNotCollected() {
            when(itemCollectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(itemCollectMapper.insert(any(ItemCollect.class))).thenReturn(1);

            boolean result = itemCollectService.toggleCollect(1L, 100L);

            assertTrue(result);
            verify(itemCollectMapper).insert(any(ItemCollect.class));
        }

        @Test
        @DisplayName("已收藏时取消收藏返回false")
        void shouldRemoveCollectWhenAlreadyCollected() {
            when(itemCollectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
            when(itemCollectMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

            boolean result = itemCollectService.toggleCollect(1L, 100L);

            assertFalse(result);
            verify(itemCollectMapper).delete(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("添加收藏时设置正确的用户ID和物品ID")
        void shouldSetCorrectUserIdAndItemIdWhenAdding() {
            when(itemCollectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(itemCollectMapper.insert(any(ItemCollect.class))).thenReturn(1);

            itemCollectService.toggleCollect(1L, 100L);

            verify(itemCollectMapper).insert(argThat(collect ->
                    collect.getUserId().equals(1L) && collect.getItemId().equals(100L)
            ));
        }
    }

    @Nested
    @DisplayName("getByUserId 方法测试")
    class GetByUserIdTests {

        @Test
        @DisplayName("获取用户的收藏列表")
        void shouldGetCollectsByUserId() {
            List<ItemCollect> collects = Arrays.asList(testCollect);
            when(itemCollectMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(collects);

            List<ItemCollect> result = itemCollectService.getByUserId(1L);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getUserId());
            verify(itemCollectMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("用户无收藏时返回空列表")
        void shouldReturnEmptyListWhenNoCollects() {
            when(itemCollectMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList());

            List<ItemCollect> result = itemCollectService.getByUserId(999L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("收藏列表按创建时间倒序排列")
        void shouldReturnCollectsOrderedByCreatedAtDesc() {
            ItemCollect olderCollect = new ItemCollect();
            olderCollect.setId(2L);
            olderCollect.setUserId(1L);
            olderCollect.setItemId(200L);
            olderCollect.setDeleted(0);
            olderCollect.setCreatedAt(LocalDateTime.now().minusDays(1));

            when(itemCollectMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(testCollect, olderCollect));

            List<ItemCollect> result = itemCollectService.getByUserId(1L);

            assertEquals(2, result.size());
            // 验证按创建时间倒序排列
            assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
        }
    }

    @Nested
    @DisplayName("IService 基础方法测试")
    class IServiceTests {

        @Test
        @DisplayName("根据ID查询收藏记录")
        void shouldGetById() {
            when(itemCollectMapper.selectById(1L)).thenReturn(testCollect);

            ItemCollect result = itemCollectService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(1L, result.getUserId());
            assertEquals(100L, result.getItemId());
        }

        @Test
        @DisplayName("统计收藏数量")
        void shouldCountCollects() {
            when(itemCollectMapper.selectCount(any())).thenReturn(10L);

            long count = itemCollectService.count();

            assertEquals(10L, count);
        }
    }
}
