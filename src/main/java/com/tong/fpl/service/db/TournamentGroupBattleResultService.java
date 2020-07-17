package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TournamentGroupBattleResultEntity;
import com.tong.fpl.mapper.TournamentGroupBattleResultMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/30
 */
@Service
public class TournamentGroupBattleResultService extends ServiceImpl<TournamentGroupBattleResultMapper, TournamentGroupBattleResultEntity> implements IService<TournamentGroupBattleResultEntity> {
}
