package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.LeagueResultStatEntity;
import com.tong.fpl.mapper.LeagueResultStatMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/9/16
 */
@Service
public class LeagueResultStatService extends ServiceImpl<LeagueResultStatMapper, LeagueResultStatEntity> implements IService<LeagueResultStatEntity> {
}
