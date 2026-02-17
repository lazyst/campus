package com.campus.modules.admin.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.modules.admin.service.impl.DashboardServiceImpl;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.BoardService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.service.ItemService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DashboardService 单元测试")
class DashboardServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @Mock
    private ItemService itemService;

    @Mock
    private BoardService boardService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dashboardService, "uploadPath", "./uploads");
    }

    @Nested
    @DisplayName("getStats 方法测试")
    class GetStatsTests {

        @Test
        @DisplayName("获取统计数据成功")
        void shouldGetStatsSuccessfully() {
            when(userService.count()).thenReturn(100L);
            when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(95L);

            when(postService.count()).thenReturn(500L);
            when(postService.count(any(LambdaQueryWrapper.class))).thenReturn(10L);

            when(itemService.count()).thenReturn(200L);
            when(itemService.count(any(LambdaQueryWrapper.class))).thenReturn(150L);

            when(boardService.count()).thenReturn(20L);
            when(boardService.count(any(LambdaQueryWrapper.class))).thenReturn(18L);

            Map<String, Object> stats = dashboardService.getStats();

            assertNotNull(stats);
            assertTrue(stats.containsKey("users"));
            assertTrue(stats.containsKey("posts"));
            assertTrue(stats.containsKey("items"));
            assertTrue(stats.containsKey("boards"));

            @SuppressWarnings("unchecked")
            Map<String, Object> users = (Map<String, Object>) stats.get("users");
            assertEquals(100L, users.get("total"));
            assertEquals(95L, users.get("normal"));
            assertEquals(5L, users.get("banned"));

            @SuppressWarnings("unchecked")
            Map<String, Object> posts = (Map<String, Object>) stats.get("posts");
            assertEquals(500L, posts.get("total"));
            assertEquals(10L, posts.get("today"));

            verify(userService).count();
            verify(userService, atLeastOnce()).count(any(LambdaQueryWrapper.class));
            verify(postService).count();
            verify(postService, atLeastOnce()).count(any(LambdaQueryWrapper.class));
            verify(itemService).count();
            verify(itemService, atLeastOnce()).count(any(LambdaQueryWrapper.class));
            verify(boardService).count();
            verify(boardService, atLeastOnce()).count(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("统计数据全部为零")
        void shouldReturnZeroStats() {
            when(userService.count()).thenReturn(0L);
            when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(postService.count()).thenReturn(0L);
            when(postService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(itemService.count()).thenReturn(0L);
            when(itemService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(boardService.count()).thenReturn(0L);
            when(boardService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

            Map<String, Object> stats = dashboardService.getStats();

            assertNotNull(stats);
            @SuppressWarnings("unchecked")
            Map<String, Object> users = (Map<String, Object>) stats.get("users");
            assertEquals(0L, users.get("total"));
            assertEquals(0L, users.get("normal"));
            assertEquals(0L, users.get("banned"));
        }
    }

    @Nested
    @DisplayName("getTrendData 方法测试")
    class GetTrendDataTests {

        @Test
        @DisplayName("获取7天趋势数据成功")
        void shouldGetTrendDataSuccessfully() {
            when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(5L);
            when(postService.count(any(LambdaQueryWrapper.class))).thenReturn(3L);

            Map<String, Object> trend = dashboardService.getTrendData();

            assertNotNull(trend);
            assertTrue(trend.containsKey("dates"));
            assertTrue(trend.containsKey("userRegistrations"));
            assertTrue(trend.containsKey("postCreations"));

            @SuppressWarnings("unchecked")
            List<String> dates = (List<String>) trend.get("dates");
            assertEquals(7, dates.size());

            @SuppressWarnings("unchecked")
            List<Long> userRegistrations = (List<Long>) trend.get("userRegistrations");
            assertEquals(7, userRegistrations.size());
            assertTrue(userRegistrations.stream().allMatch(v -> v == 5L));

            verify(userService, times(7)).count(any(LambdaQueryWrapper.class));
            verify(postService, times(7)).count(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("趋势数据日期格式正确")
        void shouldReturnCorrectDateFormat() {
            when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(postService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

            Map<String, Object> trend = dashboardService.getTrendData();

            @SuppressWarnings("unchecked")
            List<String> dates = (List<String>) trend.get("dates");
            assertEquals(7, dates.size());
            assertTrue(dates.get(0).matches("\\d{2}-\\d{2}"));
        }
    }

    @Nested
    @DisplayName("getRecentActivity 方法测试")
    class GetRecentActivityTests {

        @Test
        @DisplayName("获取最近活动数据成功")
        void shouldGetRecentActivitySuccessfully() {
            Post post = new Post();
            post.setId(1L);
            post.setTitle("测试帖子标题");
            post.setUserId(100L);
            post.setCreatedAt(LocalDateTime.now());

            List<Post> posts = new ArrayList<>();
            posts.add(post);
            when(postService.list(any(LambdaQueryWrapper.class))).thenReturn(posts);

            User user = new User();
            user.setId(1L);
            user.setNickname("测试用户");
            user.setAvatar("/avatar/default.jpg");
            user.setCreatedAt(LocalDateTime.now());

            List<User> users = new ArrayList<>();
            users.add(user);
            when(userService.list(any(LambdaQueryWrapper.class))).thenReturn(users);

            Map<String, Object> recent = dashboardService.getRecentActivity();

            assertNotNull(recent);
            assertTrue(recent.containsKey("posts"));
            assertTrue(recent.containsKey("users"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recentPosts = (List<Map<String, Object>>) recent.get("posts");
            assertEquals(1, recentPosts.size());
            assertEquals(1L, recentPosts.get(0).get("id"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recentUsers = (List<Map<String, Object>>) recent.get("users");
            assertEquals(1, recentUsers.size());
            assertEquals("测试用户", recentUsers.get(0).get("nickname"));
        }

        @Test
        @DisplayName("空数据时返回空列表")
        void shouldReturnEmptyListsWhenNoData() {
            when(postService.list(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(userService.list(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            Map<String, Object> recent = dashboardService.getRecentActivity();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> posts = (List<Map<String, Object>>) recent.get("posts");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users = (List<Map<String, Object>>) recent.get("users");

            assertNotNull(posts);
            assertNotNull(users);
            assertTrue(posts.isEmpty());
            assertTrue(users.isEmpty());
        }
    }

    @Nested
    @DisplayName("getSystemStatus 方法测试")
    class GetSystemStatusTests {

        @Test
        @DisplayName("获取系统状态成功")
        void shouldGetSystemStatusSuccessfully() {
            Map<String, Object> status = dashboardService.getSystemStatus();

            assertNotNull(status);
            assertTrue(status.containsKey("storage"));
            assertTrue(status.containsKey("server"));

            @SuppressWarnings("unchecked")
            Map<String, Object> storage = (Map<String, Object>) status.get("storage");
            assertTrue(storage.containsKey("used"));
            assertTrue(storage.containsKey("total"));
            assertTrue(storage.containsKey("percentage"));

            @SuppressWarnings("unchecked")
            Map<String, Object> server = (Map<String, Object>) status.get("server");
            assertTrue(server.containsKey("uptime"));
            assertTrue(server.containsKey("serverStatus"));
            assertEquals("正常运行", server.get("serverStatus"));
        }
    }

    @Nested
    @DisplayName("getOverview 方法测试")
    class GetOverviewTests {

        @Test
        @DisplayName("获取完整首页数据成功")
        void shouldGetOverviewSuccessfully() {
            when(userService.count()).thenReturn(100L);
            when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(95L);
            when(postService.count()).thenReturn(500L);
            when(postService.count(any(LambdaQueryWrapper.class))).thenReturn(10L);
            when(itemService.count()).thenReturn(200L);
            when(itemService.count(any(LambdaQueryWrapper.class))).thenReturn(150L);
            when(boardService.count()).thenReturn(20L);
            when(boardService.count(any(LambdaQueryWrapper.class))).thenReturn(18L);
            when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(5L);
            when(postService.list(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(userService.list(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            Map<String, Object> overview = dashboardService.getOverview();

            assertNotNull(overview);
            assertTrue(overview.containsKey("stats"));
            assertTrue(overview.containsKey("trend"));
            assertTrue(overview.containsKey("recent"));
            assertTrue(overview.containsKey("status"));

            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) overview.get("stats");
            assertNotNull(stats);

            @SuppressWarnings("unchecked")
            Map<String, Object> trend = (Map<String, Object>) overview.get("trend");
            assertNotNull(trend);

            @SuppressWarnings("unchecked")
            Map<String, Object> recent = (Map<String, Object>) overview.get("recent");
            assertNotNull(recent);

            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) overview.get("status");
            assertNotNull(status);
        }

        @Test
        @DisplayName("Overview 聚合所有方法结果")
        void shouldAggregateAllMethodResults() {
            when(userService.count()).thenReturn(10L);
            when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(9L);
            when(postService.count()).thenReturn(50L);
            when(postService.count(any(LambdaQueryWrapper.class))).thenReturn(5L);
            when(itemService.count()).thenReturn(30L);
            when(itemService.count(any(LambdaQueryWrapper.class))).thenReturn(25L);
            when(boardService.count()).thenReturn(5L);
            when(boardService.count(any(LambdaQueryWrapper.class))).thenReturn(4L);
            when(postService.list(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
            when(userService.list(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            Map<String, Object> overview = dashboardService.getOverview();

            assertEquals(4, overview.size());
            assertNotNull(overview.get("stats"));
            assertNotNull(overview.get("trend"));
            assertNotNull(overview.get("recent"));
            assertNotNull(overview.get("status"));
        }
    }
}
