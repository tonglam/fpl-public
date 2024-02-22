package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.EventLiveEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2020/7/9
 */
public interface EventLiveMapper extends BaseMapper<EventLiveEntity> {

    @Select("TRUNCATE TABLE event_live; ")
    void truncate();

}
