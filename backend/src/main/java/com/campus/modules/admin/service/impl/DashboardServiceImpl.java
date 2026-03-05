package com.campus.modules.admin.service.impl;
import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.campus.modules.admin.service.DashboardService;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.BoardService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.service.ItemService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Dashboard服务实现
 */
@Service
@DS("slave")
public class DashboardServiceImpl implements DashboardService {

    private final UserService userService;
    private final PostService postService;
    private final ItemService itemService;
    private final BoardService boardService;

    // 应用启动时间（使用JVM启动时间）
    private static final long APP_START_MILLIS = System.currentTimeMillis();

    public DashboardServiceImpl(UserService userService, PostService postService,
                                 ItemService itemService, BoardService boardService) {
        this.userService = userService;
        this.postService = postService;
        this.itemService = itemService;
        this.boardService = boardService;
    }

    @Override
    public Map<String, Object> getStats() {
        // 用户统计
        long userTotal = userService.count();
        long userNormal = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, false)
                .eq(User::getStatus, 1));
        long userBanned = userTotal - userNormal;

        // 帖子统计
        long postTotal = postService.count();
        long postToday = postService.count(new LambdaQueryWrapper<Post>()
                .ge(Post::getCreatedAt, LocalDate.now().atStartOfDay()));
        long postThisWeek = postService.count(new LambdaQueryWrapper<Post>()
                .ge(Post::getCreatedAt, LocalDate.now().minusDays(7).atStartOfDay()));

        // 物品统计
        long itemTotal = itemService.count();
        long itemSelling = itemService.count(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, 1));
        long itemCompleted = itemService.count(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, 2));

        // 板块统计
        long boardTotal = boardService.count();
        long boardActive = boardService.count(new LambdaQueryWrapper<Board>()
                .eq(Board::getStatus, 1));

        Map<String, Object> stats = new LinkedHashMap<>();

        Map<String, Object> users = new LinkedHashMap<>();
        users.put("total", userTotal);
        users.put("normal", userNormal);
        users.put("banned", userBanned);
        stats.put("users", users);

        Map<String, Object> posts = new LinkedHashMap<>();
        posts.put("total", postTotal);
        posts.put("today", postToday);
        posts.put("thisWeek", postThisWeek);
        stats.put("posts", posts);

        Map<String, Object> items = new LinkedHashMap<>();
        items.put("total", itemTotal);
        items.put("selling", itemSelling);
        items.put("completed", itemCompleted);
        stats.put("items", items);

        Map<String, Object> boards = new LinkedHashMap<>();
        boards.put("total", boardTotal);
        boards.put("active", boardActive);
        stats.put("boards", boards);

        return stats;
    }

    @Override
    public Map<String, Object> getTrendData() {
        Map<String, Object> trend = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>();
        List<Long> userRegistrations = new ArrayList<>();
        List<Long> postCreations = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));

            // 每日新增用户
            long users = userService.count(new LambdaQueryWrapper<User>()
                    .ge(User::getCreatedAt, date.atStartOfDay())
                    .lt(User::getCreatedAt, date.plusDays(1).atStartOfDay()));
            userRegistrations.add(users);

            // 每日新增帖子
            long posts = postService.count(new LambdaQueryWrapper<Post>()
                    .ge(Post::getCreatedAt, date.atStartOfDay())
                    .lt(Post::getCreatedAt, date.plusDays(1).atStartOfDay()));
            postCreations.add(posts);
        }

        trend.put("dates", dates);
        trend.put("userRegistrations", userRegistrations);
        trend.put("postCreations", postCreations);

        return trend;
    }

    @Override
    public Map<String, Object> getRecentActivity() {
        Map<String, Object> recent = new LinkedHashMap<>();

        // 最近10条帖子
        List<Post> recentPosts = postService.list(new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreatedAt)
                .last("LIMIT 10"));

        List<Map<String, Object>> posts = new ArrayList<>();
        for (Post post : recentPosts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", post.getId());
            item.put("title", post.getTitle() != null ?
                    (post.getTitle().length() > 20 ? post.getTitle().substring(0, 20) + "..." : post.getTitle())
                    : "无标题");
            item.put("authorId", post.getUserId());
            item.put("createdAt", post.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
            posts.add(item);
        }
        recent.put("posts", posts);

        // 最近10个新用户
        List<User> recentUsers = userService.list(new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt)
                .last("LIMIT 10"));

        List<Map<String, Object>> users = new ArrayList<>();
        for (User user : recentUsers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", user.getId());
            item.put("nickname", user.getNickname() != null ? user.getNickname() : "用户" + user.getId());
            item.put("avatar", user.getAvatar());
            item.put("createdAt", user.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
            users.add(item);
        }
        recent.put("users", users);

        return recent;
    }

    @Override
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        // 服务器状态
        Map<String, Object> server = new LinkedHashMap<>();

        // 计算运行时间（基于JVM启动时间）
        long uptimeMillis = System.currentTimeMillis() - APP_START_MILLIS;
        long days = uptimeMillis / (24 * 60 * 60 * 1000);
        long hours = (uptimeMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (uptimeMillis % (60 * 60 * 1000)) / (60 * 1000);
        String uptime = String.format("%d天 %d小时 %d分", days, hours, minutes);
        server.put("uptime", uptime);

        // 服务器状态
        server.put("serverStatus", "正常运行");
        status.put("server", server);

        return status;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("stats", getStats());
        overview.put("trend", getTrendData());
        overview.put("recent", getRecentActivity());
        overview.put("status", getSystemStatus());
        return overview;
    }
}
