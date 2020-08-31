package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.service.IPageQueryService;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.TournamentInfoService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/8/28
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PageQueryServiceImpl implements IPageQueryService {

	private final IQuerySerivce querySerivce;
	private final PlayerService playerService;
	private final TournamentInfoService tournamentInfoService;

	@Cacheable(value = "pagePlayerDataList", key = "#current+'::'+#size", unless = "#result.records.size() eq 0")
	@Override
	public Page<PlayerInfoData> qryPagePlayerDataList(long current, long size) {
		List<PlayerInfoData> list = Lists.newArrayList();

		Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
				new Page<>(current, size, this.setSearchTotal(current)), new QueryWrapper<>());
		playerPage.getRecords().forEach(o -> list.add(BeanUtil.copyProperties(this.querySerivce.initPlayerInfo(o), PlayerInfoData.class)));
		Page<PlayerInfoData> page = new Page<>(current, size, playerPage.getTotal());
		page.setRecords(list);
		return page;
	}

	private boolean setSearchTotal(long current) {
		return current == 1;
	}

	@Override
	public Page<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
		List<TournamentInfoData> list = Lists.newArrayList();
		LambdaQueryWrapper<TournamentInfoEntity> queryWrapper = new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getState, 1);
		if (StringUtils.isNotBlank(param.getName())) {
			queryWrapper.eq(TournamentInfoEntity::getName, param.getName());
		} else {
			if (StringUtils.isNotBlank(param.getCreator())) {
				queryWrapper.eq(TournamentInfoEntity::getCreator, param.getCreator());
			} else if (param.getLeagueId() > 0) {
				queryWrapper.eq(TournamentInfoEntity::getLeagueId, param.getLeagueId());
			}
		}
		if (queryWrapper.getExpression().getNormal().size() == 0) {
			return new Page<>();
		}
		long current = param.getCurrent();
		long size = param.getSize();
		Page<TournamentInfoEntity> playerPage = this.tournamentInfoService.getBaseMapper().selectPage(
				new Page<>(current, size, false), queryWrapper);
		playerPage.getRecords().forEach(o -> {
			TournamentInfoData tournamentInfoData = new TournamentInfoData();
			BeanUtil.copyProperties(o, tournamentInfoData, CopyOptions.create().ignoreNullValue());
			tournamentInfoData.setGroupMode(GroupMode.valueOf(o.getGroupMode()).getModeName())
					.setGroupStartGw(CommonUtils.setRealGw(o.getGroupStartGw()))
					.setGroupEndGw(CommonUtils.setRealGw(o.getGroupEndGw()))
					.setKnockoutMode(KnockoutMode.valueOf(o.getKnockoutMode()).getModeName())
					.setKnockoutStartGw(CommonUtils.setRealGw(o.getKnockoutStartGw()))
					.setKnockoutEndGw(CommonUtils.setRealGw(o.getKnockoutEndGw()))
					.setGroupFillAverage(o.isGroupFillAverage() ? "是" : "否");
			list.add(tournamentInfoData);
		});
		Page<TournamentInfoData> page = new Page<>(current, size, playerPage.getTotal());
		page.setRecords(list);
		return page;
	}

}
