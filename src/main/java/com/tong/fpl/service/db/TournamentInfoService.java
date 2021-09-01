package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.mapper.TournamentInfoMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/23
 */
@Service
public class TournamentInfoService extends ServiceImpl<TournamentInfoMapper, TournamentInfoEntity> implements IService<TournamentInfoEntity> {

}
