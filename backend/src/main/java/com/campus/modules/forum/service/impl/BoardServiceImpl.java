package com.campus.modules.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.mapper.BoardMapper;
import com.campus.modules.forum.service.BoardService;
import org.springframework.stereotype.Service;

/**
 * 板块服务实现类
 */
@Service
public class BoardServiceImpl extends ServiceImpl<BoardMapper, Board> implements BoardService {

    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<Board> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Board::getName, name);
        return this.count(wrapper) > 0;
    }
}
