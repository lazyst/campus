package com.campus.modules.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物品控制器
 */
@Tag(name = "闲置物品管理")
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final AuthService authService;

    public ItemController(ItemService itemService, AuthService authService) {
        this.itemService = itemService;
        this.authService = authService;
    }

    @Operation(summary = "发布物品")
    @PostMapping
    public Result<Item> create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ItemCreateRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        Item item = new Item();
        item.setUserId(userId);
        item.setType(request.getType());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setImages(request.getImages());
        item.setStatus(1);  // 默认正常状态
        item.setViewCount(0);
        item.setContactCount(0);

        itemService.save(item);
        return Result.success(item);
    }

    @Operation(summary = "获取物品列表")
    @GetMapping
    public Result<Page<Item>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        
        Page<Item> pageParam = new Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Item> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        wrapper.eq(Item::getDeleted, false);
        
        if (type != null) {
            wrapper.eq(Item::getType, type);
        }
        if (status != null) {
            wrapper.eq(Item::getStatus, status);
        }
        
        wrapper.orderByDesc(Item::getCreatedAt);
        
        Page<Item> result = itemService.page(pageParam, wrapper);
        return Result.success(result);
    }

    @Operation(summary = "获取物品详情")
    @GetMapping("/{itemId}")
    public Result<Item> detail(@PathVariable Long itemId) {
        Item item = itemService.getDetail(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }
        return Result.success(item);
    }

    @Operation(summary = "获取我发布的物品")
    @GetMapping("/my")
    public Result<List<Item>> myItems(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        List<Item> items = itemService.getByUserId(userId);
        return Result.success(items);
    }

    @Operation(summary = "更新物品")
    @PutMapping("/{itemId}")
    public Result<Item> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify ownership
        if (!itemService.isAuthor(userId, itemId)) {
            return Result.error("无权操作");
        }

        Item item = itemService.getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

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

    @Operation(summary = "删除物品")
    @DeleteMapping("/{itemId}")
    public Result<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify ownership
        if (!itemService.isAuthor(userId, itemId)) {
            return Result.error("无权操作");
        }

        Item item = itemService.getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

        // Soft delete using removeById (MyBatis-Plus @TableLogic will handle it)
        boolean success = itemService.removeById(itemId);
        if (!success) {
            return Result.error("删除失败");
        }
        return Result.success();
    }

    @Operation(summary = "上架物品")
    @PutMapping("/{itemId}/online")
    public Result<Void> online(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify ownership
        if (!itemService.isAuthor(userId, itemId)) {
            return Result.error("无权操作");
        }

        Item item = itemService.getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

        itemService.online(itemId);
        return Result.success();
    }

    @Operation(summary = "下架物品")
    @PutMapping("/{itemId}/offline")
    public Result<Void> offline(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify ownership
        if (!itemService.isAuthor(userId, itemId)) {
            return Result.error("无权操作");
        }

        Item item = itemService.getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

        itemService.offline(itemId);
        return Result.success();
    }

    @Operation(summary = "标记为已完成")
    @PutMapping("/{itemId}/complete")
    public Result<Void> complete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify ownership
        if (!itemService.isAuthor(userId, itemId)) {
            return Result.error("无权操作");
        }

        Item item = itemService.getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

        itemService.complete(itemId);
        return Result.success();
    }

    @Operation(summary = "联系物品发布者")
    @PostMapping("/{itemId}/contact")
    public Result<Void> contact(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        Item item = itemService.getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

        // Cannot contact own item
        if (item.getUserId().equals(userId)) {
            return Result.error("不能联系自己");
        }

        itemService.incrementContactCount(itemId);
        return Result.success();
    }

    /**
     * 创建物品请求DTO
     */
    public static class ItemCreateRequest {
        private Integer type;
        private String title;
        private String description;
        private BigDecimal price;
        private String images;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getImages() {
            return images;
        }

        public void setImages(String images) {
            this.images = images;
        }
    }

    /**
     * 更新物品请求DTO
     */
    public static class ItemUpdateRequest {
        private String title;
        private String description;
        private BigDecimal price;
        private String images;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getImages() {
            return images;
        }

        public void setImages(String images) {
            this.images = images;
        }
    }
}
