package com.campus.modules.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.forum.entity.Board;

/**
 * 板块服务接口
 */
public interface BoardService extends IService<Board> {

    /**
     * 检查板块名称是否存在
     */
    boolean existsByName(String name);
}
