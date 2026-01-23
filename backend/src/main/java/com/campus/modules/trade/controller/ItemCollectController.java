package com.campus.modules.trade.controller;

import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.entity.ItemCollect;
import com.campus.modules.trade.service.ItemCollectService;
import com.campus.modules.trade.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 物品收藏控制器
 */
@Tag(name = "物品收藏管理")
@RestController
@RequestMapping("/api/items")
public class ItemCollectController {

    private final ItemCollectService itemCollectService;
    private final ItemService itemService;
    private final AuthService authService;

    public ItemCollectController(ItemCollectService itemCollectService, ItemService itemService, AuthService authService) {
        this.itemCollectService = itemCollectService;
        this.itemService = itemService;
        this.authService = authService;
    }

    @Operation(summary = "收藏/取消收藏物品")
    @PostMapping("/{itemId}/collect")
    public Result<Boolean> toggleCollect(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify item exists
        if (itemService.getById(itemId) == null) {
            return Result.error("物品不存在");
        }

        boolean isCollected = itemCollectService.toggleCollect(userId, itemId);
        return Result.success(isCollected);
    }

    @Operation(summary = "检查用户是否已收藏物品")
    @GetMapping("/{itemId}/collect/check")
    public Result<Boolean> checkCollected(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        boolean hasCollected = itemCollectService.hasCollected(userId, itemId);
        return Result.success(hasCollected);
    }

    @Operation(summary = "获取我收藏的物品")
    @GetMapping("/collected")
    public Result<List<Item>> getCollectedItems(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        List<ItemCollect> collects = itemCollectService.getByUserId(userId);
        List<Item> items = collects.stream()
                .map(collect -> itemService.getById(collect.getItemId()))
                .filter(item -> item != null && item.getDeleted() == 0)
                .collect(Collectors.toList());

        return Result.success(items);
    }
}
