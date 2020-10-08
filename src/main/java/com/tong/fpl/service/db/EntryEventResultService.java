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

    public int sumEventPoints(int startGw, int endGw, int entry) {
        return this.baseMapper.sumEventPoints(startGw, endGw, entry);
    }

    public int sumEventNetPoints(int startGw, int endGw, int entry) {
        return this.baseMapper.sumEventNetPoints(startGw, endGw, entry);
    }

}
