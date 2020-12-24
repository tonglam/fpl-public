package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import com.tong.fpl.mapper.PlayerStatMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/26
 */
@Service
public class PlayerStatService extends ServiceImpl<PlayerStatMapper, PlayerStatEntity> implements IService<PlayerStatEntity> {

}
