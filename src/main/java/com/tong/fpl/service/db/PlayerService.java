package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.mapper.PlayerMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/11
 */
@Service
public class PlayerService extends ServiceImpl<PlayerMapper, PlayerEntity> implements IService<PlayerEntity> {

}
