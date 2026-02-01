package com.campus.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.trade.controller.ItemController.ItemUpdateRequest;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 后台物品管理控制器
 */
@Tag(name = "后台物品管理")
@RestController
@RequestMapping("/api/admin/items")
public class ItemManagementController {

    private final ItemService itemService;
    private final AdminService adminService;
    private final AuthService authService;

    public ItemManagementController(ItemService itemService, AdminService adminService, AuthService authService) {
        this.itemService = itemService;
        this.adminService = adminService;
        this.authService = authService;
    }

    @Operation(summary = "获取物品列表")
    @GetMapping
    public Result<Page<Item>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        verifyAdmin(authHeader);

        Page<Item> pageParam = new Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Item> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        // 默认排除已删除的物品
        wrapper.ne(Item::getStatus, 0);
        
        if (type != null) {
            wrapper.eq(Item::getType, type);
        }
        
        if (status != null) {
            wrapper.eq(Item::getStatus, status);
        }
        
        if (userId != null) {
            wrapper.eq(Item::getUserId, userId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Item::getTitle, keyword)
                            .or()
                            .like(Item::getDescription, keyword));
        }
        
        if (minPrice != null) {
            wrapper.ge(Item::getPrice, minPrice);
        }
        
        if (maxPrice != null) {
            wrapper.le(Item::getPrice, maxPrice);
        }
        
        wrapper.orderByDesc(Item::getCreatedAt);
        
        Page<Item> result = itemService.page(pageParam, wrapper);
        
        return Result.success(result);
    }

    @Operation(summary = "获取物品详情")
    @GetMapping("/{itemId}")
    public Result<Item> detail(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        
        verifyAdmin(authHeader);
        
        Item item = itemService.getById(itemId);
        if (item == null || (item.getStatus() != null && item.getStatus() == 0)) {
            return Result.error("物品不存在");
        }
        
        return Result.success(item);
    }

    @Operation(summary = "删除物品")
    @DeleteMapping("/{itemId}")
    public Result<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {

        verifyAdmin(authHeader);

        Item item = itemService.getById(itemId);
        if (item == null || (item.getStatus() != null && item.getStatus() == 0)) {
            return Result.error("物品不存在");
        }

        // Soft delete - set status to 0 (deleted)
        item.setStatus(0);
        itemService.updateById(item);

        return Result.success();
    }

    @Operation(summary = "更新物品")
    @PutMapping("/{itemId}")
    public Result<Item> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateRequest request) {

        verifyAdmin(authHeader);

        Item item = itemService.getById(itemId);
        if (item == null || (item.getStatus() != null && item.getStatus() == 0)) {
            return Result.error("物品不存在");
        }

        // 更新字段
        if (request.getTitle() != null) {
            item.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        if (request.getImages() != null) {
            item.setImages(request.getImages());
        }

        itemService.updateById(item);
        return Result.success(item);
    }

    @Operation(summary = "下架物品")
    @PutMapping("/{itemId}/offline")
    public Result<Void> offline(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        
        verifyAdmin(authHeader);
        
        Item item = itemService.getById(itemId);
        if (item == null || (item.getStatus() != null && item.getStatus() == 0)) {
            return Result.error("物品不存在");
        }
        
        item.setStatus(3); // 3 = 已下架
        itemService.updateById(item);
        
        return Result.success();
    }

    @Operation(summary = "获取物品统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestHeader("Authorization") String authHeader) {
        
        verifyAdmin(authHeader);
        
        long total = itemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Item>()
                .ne(Item::getStatus, 0)); // 排除已删除
        
        long selling = itemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Item>()
                .ne(Item::getStatus, 0)
                .eq(Item::getStatus, 1)); // 正常状态
        
        long completed = itemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, 2)); // 已完成
        
        long offline = itemService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, 3)); // 已下架
        
        Map<String, Object> result = Map.of(
            "total", total,
            "selling", selling,
            "completed", completed,
            "offline", offline
        );
        
        return Result.success(result);
    }

    /**
     * 验证管理员权限
     */
    private void verifyAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("未授权访问");
        }

        String token = authHeader.replace("Bearer ", "");
        Long adminId = adminService.getAdminIdFromToken(token);

        if (adminId == null || !adminService.isSuperAdmin(adminId)) {
            throw new SecurityException("权限不足");
        }
    }
}
