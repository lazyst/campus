package com.campus.modules.trade.controller;

import com.campus.common.Result;
import com.campus.common.ResultCode;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.entity.ItemCollect;
import com.campus.modules.trade.service.ItemCollectService;
import com.campus.modules.trade.service.ItemService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物品收藏控制器
 */
@Slf4j
@Tag(name = "物品收藏管理")
@RestController
@RequestMapping("/api/items")
public class ItemCollectController {

    private final ItemCollectService itemCollectService;
    private final ItemService itemService;
    private final AuthService authService;
    private final UserService userService;

    public ItemCollectController(ItemCollectService itemCollectService, ItemService itemService, AuthService authService, UserService userService) {
        this.itemCollectService = itemCollectService;
        this.itemService = itemService;
        this.authService = authService;
        this.userService = userService;
    }

    @Operation(summary = "收藏/取消收藏物品")
    @PostMapping("/{itemId}/collect")
    public Result<Boolean> toggleCollect(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long itemId) {
        // 处理未登录
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED, "请先登录后再收藏");
        }

        try {
            Long userId = authService.getUserIdFromAuthHeader(authHeader);

            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
            }

            // Verify item exists
            if (itemService.getById(itemId) == null) {
                return Result.error("物品不存在");
            }

            boolean isCollected = itemCollectService.toggleCollect(userId, itemId);
            return Result.success(isCollected);
        } catch (Exception e) {
            log.error("物品收藏操作失败: {}", e.getMessage());
            return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
    }

    @Operation(summary = "检查用户是否已收藏物品")
    @GetMapping("/{itemId}/collect/check")
    public Result<Boolean> checkCollected(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long itemId) {
        // 处理未登录
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.success(false);
        }

        try {
            Long userId = authService.getUserIdFromAuthHeader(authHeader);
            boolean hasCollected = itemCollectService.hasCollected(userId, itemId);
            return Result.success(hasCollected);
        } catch (Exception e) {
            log.error("检查物品收藏状态失败: {}", e.getMessage());
            return Result.success(false);
        }
    }

    @Operation(summary = "获取我收藏的物品")
    @GetMapping("/collected")
    public Result<List<Item>> getCollectedItems(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 处理未登录
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED, "请先登录");
        }

        try {
            Long userId = authService.getUserIdFromAuthHeader(authHeader);

            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
            }

            List<ItemCollect> collects = itemCollectService.getByUserId(userId);
            
            if (collects.isEmpty()) {
                return Result.success(List.of());
            }

            List<Long> itemIds = collects.stream()
                    .map(ItemCollect::getItemId)
                    .collect(Collectors.toList());

            List<Item> items = itemService.listByIds(itemIds).stream()
                    .filter(item -> item != null && item.getDeleted() == 0)
                    .collect(Collectors.toList());

            enrichItemsWithUserInfo(items);

            return Result.success(items);
        } catch (Exception e) {
            log.error("获取收藏物品列表失败: {}", e.getMessage());
            return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
    }

    /**
     * 填充物品的用户信息（昵称和头像）
     */
    private void enrichItemsWithUserInfo(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        // 收集所有userId
        Set<Long> userIds = items.stream()
                .map(Item::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return;
        }

        // 批量查询用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 填充到每个物品
        for (Item item : items) {
            if (item.getUserId() != null) {
                User user = userMap.get(item.getUserId());
                if (user != null) {
                    item.setUserNickname(user.getNickname());
                    item.setUserAvatar(user.getAvatar());
                    item.setSellerName(user.getNickname());
                    item.setSellerAvatar(user.getAvatar());
                } else {
                    item.setUserNickname("匿名用户");
                    item.setSellerName("匿名用户");
                }
            } else {
                item.setUserNickname("匿名用户");
                item.setSellerName("匿名用户");
            }
        }
    }
}
