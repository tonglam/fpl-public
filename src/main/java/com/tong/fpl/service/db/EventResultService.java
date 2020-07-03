package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EventResultEntity;
import com.tong.fpl.mapper.EventResultMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/29
 */
@Service
public class EventResultService extends ServiceImpl<EventResultMapper, EventResultEntity> implements IService<EventResultEntity> {
}
