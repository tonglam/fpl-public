package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EventLiveSummaryEntity;
import com.tong.fpl.mapper.EventLiveSummaryMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/5/24
 */
@Service
public class EventLiveSummaryService extends ServiceImpl<EventLiveSummaryMapper, EventLiveSummaryEntity> implements IService<EventLiveSummaryEntity> {

}
