package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TeamSelectStatEntity;
import com.tong.fpl.mapper.TeamSelectStatMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/9/17
 */
@Service
public class TeamSelectStatService extends ServiceImpl<TeamSelectStatMapper, TeamSelectStatEntity> implements IService<TeamSelectStatEntity> {
}
