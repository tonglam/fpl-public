package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.LeagueEventStatEntity;
import com.tong.fpl.mapper.LeagueEventStatMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/9/17
 */
@Service
public class LeagueEventStatService extends ServiceImpl<LeagueEventStatMapper, LeagueEventStatEntity> implements IService<LeagueEventStatEntity> {
}
