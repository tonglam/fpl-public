package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TeamNameEntity;
import com.tong.fpl.mapper.TeamNameMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/22
 */
@Service
public class TeamNameService extends ServiceImpl<TeamNameMapper, TeamNameEntity> implements IService<TeamNameEntity> {

}
