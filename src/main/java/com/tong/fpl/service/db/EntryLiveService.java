package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.db.entity.EntryLiveEntity;
import com.tong.fpl.mapper.EntryLiveMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/11
 */
@Service
public class EntryLiveService extends ServiceImpl<EntryLiveMapper, EntryLiveEntity> implements IService<EntryLiveEntity> {
}
