package com.campus.modules.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.service.ItemService;
import com.campus.modules.chat.service.ChatService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物品控制器
 */
@Tag(name = "闲置物品管理")
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final AuthService authService;
    private final UserService userService;
    private final ChatService chatService;

    public ItemController(ItemService itemService, AuthService authService, UserService userService, ChatService chatService) {
        this.itemService = itemService;
        this.authService = authService;
        this.userService = userService;
        this.chatService = chatService;
    }

    @Operation(summary = "发布物品")
    @PostMapping
    @CacheEvict(value = "items", allEntries = true)
    public Result<Item> create(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ItemCreateRequest request) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        Item item = new Item();
        item.setUserId(userId);
        item.setType(request.getType());
        item.setCategory(request.getCategory());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setImages(request.getImages());
        item.setLocation(request.getLocation());
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
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {

        Page<Item> pageParam = new Page<>(page, size);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Item> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        wrapper.eq(Item::getDeleted, false);

        if (type != null) {
            wrapper.eq(Item::getType, type);
        }
        if (status != null) {
            wrapper.eq(Item::getStatus, status);
        } else {
            // 默认排除已完成的物品（status=2）和已下架的物品（status=3）
            wrapper.ne(Item::getStatus, 2);
            wrapper.ne(Item::getStatus, 3);
        }

        // 排序
        switch (sortBy) {
            case "price_asc":
                wrapper.orderByAsc(Item::getPrice);
                break;
            case "price_desc":
                wrapper.orderByDesc(Item::getPrice);
                break;
            case "newest":
            default:
                wrapper.orderByDesc(Item::getCreatedAt);
                break;
        }

        Page<Item> result = itemService.page(pageParam, wrapper);

        // 填充用户信息
        enrichItemsWithUserInfo(result.getRecords());

        return Result.success(result);
    }

    @Operation(summary = "搜索物品")
    @GetMapping("/search")
    public Result<Page<Item>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {

        Page<Item> pageParam = new Page<>(page, size);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Item> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        wrapper.eq(Item::getDeleted, false)
               .and(wrapper1 -> wrapper1
                   .like(Item::getTitle, keyword)
                   .or()
                   .like(Item::getDescription, keyword))
               .ne(Item::getStatus, 2)
               .ne(Item::getStatus, 3);

        if (type != null) {
            wrapper.eq(Item::getType, type);
        }

        // 排序
        switch (sortBy) {
            case "price_asc":
                wrapper.orderByAsc(Item::getPrice);
                break;
            case "price_desc":
                wrapper.orderByDesc(Item::getPrice);
                break;
            case "newest":
            default:
                wrapper.orderByDesc(Item::getCreatedAt);
                break;
        }

        Page<Item> result = itemService.page(pageParam, wrapper);

        // 填充用户信息
        enrichItemsWithUserInfo(result.getRecords());

        return Result.success(result);
    }

    @Operation(summary = "获取物品详情")
    @GetMapping("/{itemId}")
    public Result<Item> detail(@PathVariable Long itemId) {
        Item item = itemService.getDetail(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

        // 填充用户信息
        enrichItemsWithUserInfo(Collections.singletonList(item));

        return Result.success(item);
    }

    @Operation(summary = "获取我发布的物品")
    @GetMapping("/my")
    public Result<List<Item>> myItems(@RequestHeader("Authorization") String authHeader) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        List<Item> items = itemService.getByUserId(userId);

        // 填充用户信息
        enrichItemsWithUserInfo(items);

        return Result.success(items);
    }

    @Operation(summary = "按用户ID获取物品列表")
    @GetMapping("/user/{userId}")
    public Result<List<Item>> getItemsByUserId(@PathVariable Long userId) {
        List<Item> items = itemService.getByUserId(userId);

        // 填充用户信息
        enrichItemsWithUserInfo(items);

        return Result.success(items);
    }

    @Operation(summary = "更新物品")
    @PutMapping("/{itemId}")
    @CacheEvict(value = "items", allEntries = true)
    public Result<Item> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateRequest request) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

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
    @CacheEvict(value = "items", allEntries = true)
    public Result<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

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
    @CacheEvict(value = "items", allEntries = true)
    public Result<Void> online(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

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
    @CacheEvict(value = "items", allEntries = true)
    public Result<Void> offline(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

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
    @CacheEvict(value = "items", allEntries = true)
    public Result<Void> complete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

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
    @Transactional
    public Result<Map<String, Object>> contact(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        Item item = itemService.getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

        // Cannot contact own item
        if (item.getUserId().equals(userId)) {
            return Result.error("不能联系自己");
        }

        Long sellerId = item.getUserId();
        
        // 创建或获取会话
        chatService.getOrCreateConversation(userId, sellerId);
        
        // 增加联系次数
        itemService.incrementContactCount(itemId);
        
        // 返回卖家用户ID（简化返回格式）
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("sellerId", sellerId);
        result.put("sellerName", "用户" + sellerId);
        
        return Result.success(result);
    }

    /**
     * 创建物品请求DTO
     */
    public static class ItemCreateRequest {
        @NotNull(message = "物品类型不能为空")
        @Min(value = 1, message = "物品类型不正确")
        @Max(value = 2, message = "物品类型不正确")
        private Integer type;

        @NotBlank(message = "分类不能为空")
        private String category;

        @NotBlank(message = "标题不能为空")
        @Size(max = 100, message = "标题不能超过100个字符")
        private String title;

        @Size(max = 2000, message = "描述不能超过2000个字符")
        private String description;

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        @DecimalMax(value = "999999.99", message = "价格不能超过999999.99")
        private BigDecimal price;

        private String images;

        @Size(max = 100, message = "位置不能超过100个字符")
        private String location;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
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

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    /**
     * 更新物品请求DTO
     */
    public static class ItemUpdateRequest {
        @Size(max = 100, message = "标题不能超过100个字符")
        private String title;

        @Size(max = 2000, message = "描述不能超过2000个字符")
        private String description;

        @DecimalMin(value = "0.01", message = "价格必须大于0")
        @DecimalMax(value = "999999.99", message = "价格不能超过999999.99")
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
                    // 兼容前端字段名
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
