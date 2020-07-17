package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.mapper.EntryEventResultMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/29
 */
@Service
public class EntryEventResultService extends ServiceImpl<EntryEventResultMapper, EntryEventResultEntity> implements IService<EntryEventResultEntity> {

	public int sumEventNetPoint(int event, int entry) {
		return this.baseMapper.sumEventNetPoint(event, entry);
	}

}
