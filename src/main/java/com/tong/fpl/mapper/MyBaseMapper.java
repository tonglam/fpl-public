package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Create by tong on 2021/8/19
 */
public interface MyBaseMapper<T> extends BaseMapper<T> {

    void truncate();

}
