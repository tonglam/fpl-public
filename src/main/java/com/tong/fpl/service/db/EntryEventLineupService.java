package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryEventLineupEntity;
import com.tong.fpl.mapper.EntryEventLineupMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/1/11
 */
@Service
public class EntryEventLineupService extends ServiceImpl<EntryEventLineupMapper, EntryEventLineupEntity> implements IService<EntryEventLineupEntity> {
}
