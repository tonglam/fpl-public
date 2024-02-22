package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.RerunRecordEntity;
import com.tong.fpl.mapper.RerunRecordMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2022/8/17
 */
@Service
public class RerunRecordService extends ServiceImpl<RerunRecordMapper, RerunRecordEntity> implements IService<RerunRecordEntity> {

}
