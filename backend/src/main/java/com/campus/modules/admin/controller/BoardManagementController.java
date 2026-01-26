package com.campus.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.config.JwtConfig;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台板块管理控制器
 */
@Tag(name = "后台板块管理")
@RestController
@RequestMapping("/api/admin/boards")
public class BoardManagementController {

    private final BoardService boardService;
    private final AdminService adminService;
    private final JwtConfig jwtConfig;

    public BoardManagementController(BoardService boardService, AdminService adminService, JwtConfig jwtConfig) {
        this.boardService = boardService;
        this.adminService = adminService;
        this.jwtConfig = jwtConfig;
    }

    @Operation(summary = "获取板块列表")
    @GetMapping
    public Result<Page<Board>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        verifyAdmin(authHeader);

        Page<Board> pageParam = new Page<>(page, size);
        Page<Board> result = boardService.page(pageParam);
        return Result.success(result);
    }

    @Operation(summary = "获取板块详情")
    @GetMapping("/{boardId}")
    public Result<Board> detail(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long boardId) {
        
        verifyAdmin(authHeader);
        
        Board board = boardService.getById(boardId);
        if (board == null || (board.getDeleted() != null && board.getDeleted() == 1)) {
            return Result.error("板块不存在");
        }
        return Result.success(board);
    }

    @Operation(summary = "创建板块")
    @PostMapping
    public Result<Board> create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BoardCreateRequest request) {
        
        verifyAdmin(authHeader);
        
        Board board = new Board();
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setIcon(request.getIcon());
        board.setSort(0);
        
        boardService.save(board);
        return Result.success(board);
    }

    @Operation(summary = "更新板块")
    @PutMapping("/{boardId}")
    public Result<Board> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long boardId,
            @RequestBody BoardUpdateRequest request) {
        
        verifyAdmin(authHeader);
        
        Board board = boardService.getById(boardId);
        if (board == null || (board.getDeleted() != null && board.getDeleted() == 1)) {
            return Result.error("板块不存在");
        }
        
        if (request.getName() != null) {
            board.setName(request.getName());
        }
        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            board.setIcon(request.getIcon());
        }
        if (request.getSort() != null) {
            board.setSort(request.getSort());
        }
        
        boardService.updateById(board);
        return Result.success(board);
    }

    @Operation(summary = "删除板块")
    @DeleteMapping("/{boardId}")
    public Result<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long boardId) {

        verifyAdmin(authHeader);

        Board board = boardService.getById(boardId);
        if (board == null || (board.getDeleted() != null && board.getDeleted() == 1)) {
            return Result.error("板块不存在");
        }

        // Soft delete using removeById (MyBatis-Plus @TableLogic will handle it)
        boolean success = boardService.removeById(boardId);
        if (!success) {
            return Result.error("删除失败");
        }
        return Result.success();
    }

    @Operation(summary = "获取板块统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestHeader("Authorization") String authHeader) {
        
        verifyAdmin(authHeader);
        
        long total = boardService.count();
        long active = boardService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Board>()
                .eq(Board::getDeleted, false));
        
        Map<String, Object> result = Map.of(
            "total", total,
            "active", active
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

        // 从token中提取admin ID和role
        String subject = jwtConfig.getUsernameFromToken(token);
        Integer role = jwtConfig.getRoleFromToken(token);

        if (subject == null) {
            throw new SecurityException("权限验证失败: 无效的token");
        }

        // 验证是否为管理员token (role == 1 或 2)
        if (role == null || (role != 1 && role != 2)) {
            throw new SecurityException("权限不足: 非管理员token");
        }

        Long adminId;
        try {
            adminId = Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new SecurityException("权限验证失败: 无效的管理员ID");
        }

        if (!adminService.isSuperAdmin(adminId)) {
            throw new SecurityException("权限不足: 非超级管理员");
        }
    }

    /**
     * 创建板块请求DTO
     */
    public static class BoardCreateRequest {
        private String name;
        private String description;
        private String icon;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }

    /**
     * 更新板块请求DTO
     */
    public static class BoardUpdateRequest {
        private String name;
        private String description;
        private String icon;
        private Integer sort;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public Integer getSort() {
            return sort;
        }

        public void setSort(Integer sort) {
            this.sort = sort;
        }
    }
}
