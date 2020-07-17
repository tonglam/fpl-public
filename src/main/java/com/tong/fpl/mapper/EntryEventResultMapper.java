package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import org.apache.ibatis.annotations.Select;

/**
 * Create by tong on 2020/6/29
 */
public interface EntryEventResultMapper extends BaseMapper<EntryEventResultEntity> {

	@Select("SELECT IFNULL(SUM(event_net_points), 0) FROM entry_event_result WHERE entry = #{entry} And `event` <= #{event} ")
	int sumEventNetPoint(int event, int entry);

}
