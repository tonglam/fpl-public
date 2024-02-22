package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.EventLiveExplainEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2021/9/8
 */
public interface EventLiveExplainMapper extends BaseMapper<EventLiveExplainEntity> {

    @Select("TRUNCATE TABLE event_live_explain; ")
    void truncate();

}
