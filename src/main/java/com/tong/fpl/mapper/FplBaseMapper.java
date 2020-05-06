package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface FplBaseMapper<T> extends BaseMapper<T> {

    void truncateTable();

    void batchInsert(List<T> batchList);

}