package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TournamentEntryEntity;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.EntryTournamentData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.TournamentEntryService;
import com.tong.fpl.service.db.TournamentInfoService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/8/28
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TableQueryServiceImpl implements ITableQueryService {

	private final IQuerySerivce querySerivce;
	private final PlayerService playerService;
	private final EntryInfoService entryInfoService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentEntryService tournamentEntryService;

	@Override
	public TableData<PlayerInfoData> qryPagePlayerDataList(long current, long size) {
		List<PlayerInfoData> list = Lists.newArrayList();
		Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
				new Page<>(current, size, this.setSearchTotal(current)), new QueryWrapper<>());
		playerPage.getRecords().forEach(o -> list.add(BeanUtil.copyProperties(this.querySerivce.initPlayerInfo(o), PlayerInfoData.class)));
		Page<PlayerInfoData> page = new Page<>(current, size, playerPage.getTotal());
		page.setRecords(list);
		return new TableData<>(page);
	}

	private boolean setSearchTotal(long current) {
		return current == 1;
	}

	@Override
	public TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
		List<TournamentInfoData> list = Lists.newArrayList();
		// get tournament info
		LambdaQueryWrapper<TournamentInfoEntity> queryWrapper = new QueryWrapper<TournamentInfoEntity>().lambda();
		if (StringUtils.isNotBlank(param.getName())) {
			queryWrapper.eq(TournamentInfoEntity::getName, param.getName());
		} else {
			if (StringUtils.isNotBlank(param.getCreator())) {
				queryWrapper.eq(TournamentInfoEntity::getCreator, param.getCreator());
			} else if (param.getLeagueId() > 0) {
				queryWrapper.eq(TournamentInfoEntity::getLeagueId, param.getLeagueId());
			} else if (StringUtils.isNotBlank(param.getCreateTime())) {
				queryWrapper.gt(TournamentInfoEntity::getCreateTime, param.getCreateTime());
				queryWrapper.lt(TournamentInfoEntity::getCreateTime, LocalDate.parse(param.getCreateTime()).plusDays(1).format(DateTimeFormatter.ofPattern(Constant.DATE)));
			}
		}
		if (queryWrapper.getExpression().getNormal().size() == 0) {
			return new TableData<>();
		}
		queryWrapper.eq(TournamentInfoEntity::getState, 1);
		// return
		this.tournamentInfoService.list(queryWrapper).forEach(o -> {
			TournamentInfoData tournamentInfoData = new TournamentInfoData();
			BeanUtil.copyProperties(o, tournamentInfoData, CopyOptions.create().ignoreNullValue());
			tournamentInfoData.setGroupMode(GroupMode.valueOf(o.getGroupMode()).getModeName())
					.setGroupStartGw(CommonUtils.setRealGw(o.getGroupStartGw()))
					.setGroupEndGw(CommonUtils.setRealGw(o.getGroupEndGw()))
					.setKnockoutMode(KnockoutMode.valueOf(o.getKnockoutMode()).getModeName())
					.setKnockoutStartGw(CommonUtils.setRealGw(o.getKnockoutStartGw()))
					.setKnockoutEndGw(CommonUtils.setRealGw(o.getKnockoutEndGw()))
					.setGroupFillAverage(o.isGroupFillAverage() ? "是" : "否")
					.setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "));
			list.add(tournamentInfoData);
		});
		return new TableData<>(list);
	}

	@Override
	public TableData<EntryTournamentData> qryEntryTournamenList(TournamentQueryParam param) {
		List<EntryTournamentData> list = Lists.newArrayList();
		// entry required
		int entry = param.getEntry();
		if (entry == 0) {
			return new TableData<>();
		}
		// get tournament_list
		List<Integer> entryTournamentList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
				.eq(TournamentEntryEntity::getEntry, entry))
				.stream()
				.map(TournamentEntryEntity::getTournamentId)
				.collect(Collectors.toList());
		// get tournament info
		LambdaQueryWrapper<TournamentInfoEntity> queryWrapper = new QueryWrapper<TournamentInfoEntity>().lambda();
		if (StringUtils.isNotBlank(param.getName())) {
			queryWrapper.eq(TournamentInfoEntity::getName, param.getName());
		} else {
			if (StringUtils.isNotBlank(param.getCreator())) {
				queryWrapper.eq(TournamentInfoEntity::getCreator, param.getCreator());
			} else if (param.getLeagueId() > 0) {
				queryWrapper.eq(TournamentInfoEntity::getLeagueId, param.getLeagueId());
			} else if (StringUtils.isNotBlank(param.getCreateTime())) {
				queryWrapper.gt(TournamentInfoEntity::getCreateTime, param.getCreateTime());
				queryWrapper.lt(TournamentInfoEntity::getCreateTime, LocalDate.parse(param.getCreateTime()).plusDays(1).format(DateTimeFormatter.ofPattern(Constant.DATE)));
			}
		}
		if (queryWrapper.getExpression().getNormal().size() == 0) {
			return new TableData<>();
		}
		queryWrapper.eq(TournamentInfoEntity::getState, 1);
		queryWrapper.in(TournamentInfoEntity::getId, entryTournamentList);
		// return
		this.tournamentInfoService.list(queryWrapper).forEach(o -> list.add(new EntryTournamentData()
				.setEntry(entry)
				.setTournamentId(o.getId())
				.setName(o.getName())
				.setCreator(o.getCreator())
				.setSeason(o.getSeason())
				.setLeagueType(o.getLeagueType())
				.setLeagueId(o.getLeagueId())
				.setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "))
		));
		return new TableData<>(list);
	}

}
