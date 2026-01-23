package com.campus.modules.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.forum.dto.BoardCreateRequest;
import com.campus.modules.forum.dto.BoardUpdateRequest;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 板块控制器
 */
@Tag(name = "板块管理")
@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @Operation(summary = "获取所有启用板块列表")
    @GetMapping
    public Result<List<Board>> listBoards() {
        LambdaQueryWrapper<Board> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Board::getStatus, 1)
               .orderByAsc(Board::getSort);
        List<Board> boards = boardService.list(wrapper);
        return Result.success(boards);
    }

    @Operation(summary = "分页获取板块列表")
    @GetMapping("/page")
    public Result<Page<Board>> pageBoards(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Board> boardPage = new Page<>(page, size);
        LambdaQueryWrapper<Board> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Board::getSort);
        boardService.page(boardPage, wrapper);
        return Result.success(boardPage);
    }

    @Operation(summary = "获取板块详情")
    @GetMapping("/{id}")
    public Result<Board> getBoard(@PathVariable Long id) {
        Board board = boardService.getById(id);
        if (board == null) {
            return Result.error("板块不存在");
        }
        return Result.success(board);
    }

    @Operation(summary = "创建板块")
    @PostMapping
    public Result<Board> createBoard(@Valid @RequestBody BoardCreateRequest request) {
        // Check if name already exists
        if (boardService.existsByName(request.getName())) {
            return Result.error("板块名称已存在");
        }

        Board board = new Board();
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setIcon(request.getIcon());
        board.setSort(request.getSort() != null ? request.getSort() : 0);
        board.setStatus(1);

        boardService.save(board);
        return Result.success(board);
    }

    @Operation(summary = "更新板块")
    @PutMapping("/{id}")
    public Result<Board> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest request) {
        Board board = boardService.getById(id);
        if (board == null) {
            return Result.error("板块不存在");
        }

        // Check if name already exists (excluding current board)
        if (request.getName() != null && !request.getName().equals(board.getName())) {
            if (boardService.existsByName(request.getName())) {
                return Result.error("板块名称已存在");
            }
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
        if (request.getStatus() != null) {
            board.setStatus(request.getStatus());
        }

        boardService.updateById(board);
        return Result.success(board);
    }

    @Operation(summary = "删除板块")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBoard(@PathVariable Long id) {
        Board board = boardService.getById(id);
        if (board == null) {
            return Result.error("板块不存在");
        }
        boardService.removeById(id);
        return Result.success();
    }
}
