package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface FplBaseMapper<T> extends BaseMapper<T> {

    void truncateTable();

}