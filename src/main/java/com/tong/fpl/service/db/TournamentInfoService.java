package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.mapper.TournamentInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/6/23
 */
@Service
public class TournamentInfoService extends ServiceImpl<TournamentInfoMapper, TournamentInfoEntity> implements IService<TournamentInfoEntity> {

	public List<TournamentInfoEntity> getAllKnockoutTournamentsByEvent(int event) {
		return this.baseMapper.getAllKnockoutTournamentsByEvent(event);
	}

	public List<TournamentInfoEntity> getAllPointsRaceGroupByEvent(int event) {
		return this.baseMapper.getAllPointsRaceGroupByEvent(event);
	}

	public List<TournamentInfoEntity> getAllBattleRaceGroupByEvent(int event) {
		return this.baseMapper.getAllBattleRaceGroupByEvent(event);
	}

}
