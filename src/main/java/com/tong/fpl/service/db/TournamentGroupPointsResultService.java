package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TournamentGroupPointsResultEntity;
import com.tong.fpl.mapper.TournamentGroupPointsResultMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/7/17
 */
@Service
public class TournamentGroupPointsResultService extends ServiceImpl<TournamentGroupPointsResultMapper, TournamentGroupPointsResultEntity> implements IService<TournamentGroupPointsResultEntity> {
}
