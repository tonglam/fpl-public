package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.LeagueEventReportEntity;
import com.tong.fpl.mapper.LeagueEventReportMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/11/6
 */
@Service
public class LeagueEventReportService extends ServiceImpl<LeagueEventReportMapper, LeagueEventReportEntity> implements IService<LeagueEventReportEntity> {

}
