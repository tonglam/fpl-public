package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.HistorySeason;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/31
 */
@Slf4j
@Valid
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueryServiceImpl implements IQuerySerivce {

	private final IRedisCacheSerive redisCacheSerive;
	private final PlayerService playerService;
	private final PlayerStatService playerStatService;
	private final EntryInfoService entryInfoService;
	private final EventLiveService eventLiveService;
	private final EntryEventResultService entryEventResultService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentKnockoutService tournamentKnockoutService;
	private final TournamentKnockoutResultService tournamentKnockoutResultService;

	@Override
	public Map<Integer, PlayerEntity> qryAllPlayerList(String season) {
		MybatisPlusConfig.season.set(season);
		List<PlayerEntity> list = this.playerService.list();
		MybatisPlusConfig.season.remove();
		return list.stream().collect(Collectors.toMap(PlayerEntity::getElement, v -> v));
	}

	@Override
	public Map<Integer, PlayerStatEntity> qryAllPlayerStatList(String season) {
		MybatisPlusConfig.season.set(season);
		List<PlayerStatEntity> list = this.playerStatService.list();
		MybatisPlusConfig.season.remove();
		return list.stream().collect(Collectors.toMap(PlayerStatEntity::getElement, v -> v));
	}

	@Cacheable(value = "qryPlayerElementByCode", key = "#season+'::'+#code", cacheManager = "apiCacheManager")
	@Override
	public int qryPlayerElementByCode(String season, int code) {
		MybatisPlusConfig.season.set(season);
		PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getCode, code));
		MybatisPlusConfig.season.remove();
		return playerEntity == null ? 0 : playerEntity.getElement();
	}

	@Cacheable(value = "qryPlayerElementByWebName", key = "#season+'::'+#webName", cacheManager = "apiCacheManager")
	@Override
	public int qryPlayerElementByWebName(String season, String webName) throws Exception {
		MybatisPlusConfig.season.set(season);
		List<PlayerEntity> playerList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getWebName, webName));
		MybatisPlusConfig.season.remove();
		if (CollectionUtils.isEmpty(playerList)) {
			return 0;
		} else if (playerList.size() > 1) {
			throw new Exception("webname不止一个球员，请用element或code查询!");
		}
		return playerList.get(0) == null ? 0 : playerList.get(0).getElement();
	}

	@Cacheable(value = "qryEntryInfoData", key = "#season+'::'+#entry", cacheManager = "apiCacheManager")
	@Override
	public EntryEventData qryEntryInfoData(String season, int entry) {
		EntryEventData entryEventData = new EntryEventData();
		MybatisPlusConfig.season.set(season);
		EntryInfoEntity entryInfoEntity = this.entryInfoService.getOne(new QueryWrapper<EntryInfoEntity>().lambda().
				eq(EntryInfoEntity::getEntry, entry));
		MybatisPlusConfig.season.remove();
		if (entryInfoEntity == null) {
			return entryEventData;
		}
		BeanUtil.copyProperties(entryInfoEntity, entryEventData, CopyOptions.create().ignoreNullValue());
		return entryEventData;
	}

	@Cacheable(value = "qryEntryResult", key = "#season+'::'+#entry", cacheManager = "apiCacheManager")
	@Override
	public List<EntryEventResultData> qryEntryResult(String season, int entry) {
		List<EntryEventResultData> list = Lists.newArrayList();
		if (StringUtils.equals(season, "1920")) {
			IntStream.range(1, 48).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
		} else {
			IntStream.range(1, 39).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
		}
		return list;
	}

	@Cacheable(value = "qryEntryEventResult", key = "#season+'::'+#event+'::'+#entry", cacheManager = "apiCacheManager")
	@Override
	public EntryEventResultData qryEntryEventResult(String season, int event, int entry) {
		return this.setEntryEventResult(season, event, entry);
	}

	private EntryEventResultData setEntryEventResult(String season, int event, int entry) {
		EntryEventResultData entryEventResultData = new EntryEventResultData();
		MybatisPlusConfig.season.set(season);
		EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
		MybatisPlusConfig.season.remove();
		if (entryEventResultEntity == null) {
			return entryEventResultData;
		}
		entryEventResultData
				.setEntry(entry)
				.setEvent(event)
				.setPoints(entryEventResultEntity.getEventPoints())
				.setTransfers(entryEventResultEntity.getEventTransfers())
				.setTransfersCost(entryEventResultEntity.getEventTransfersCost())
				.setNetPoints(entryEventResultEntity.getEventNetPoints())
				.setBenchPoints(entryEventResultEntity.getEventBenchPoints())
				.setRank(entryEventResultEntity.getEventRank())
				.setChip(entryEventResultEntity.getEventChip())
				.setPicks(this.qryPickListFromPicks(season, entryEventResultEntity.getEventPicks()));
		return entryEventResultData;
	}

	@Cacheable(value = "qryEventLiveAll", key = "#season+'::'+#element", cacheManager = "apiCacheManager")
	@Override
	public List<EventLiveEntity> qryEventLiveAll(String season, int element) {
		MybatisPlusConfig.season.set(season);
		List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getElement, element));
		MybatisPlusConfig.season.remove();
		return list;
	}

	@Cacheable(value = "qryEventLive", key = "#season+'::'+#event+'::'+#element", cacheManager = "apiCacheManager")
	@Override
	public List<EventLiveEntity> qryEventLive(String season, int event, int element) {
		MybatisPlusConfig.season.set(season);
		List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event).eq(EventLiveEntity::getElement, element));
		MybatisPlusConfig.season.remove();
		return list;
	}

	@Cacheable(value = "qryPlayerData", key = "#element", cacheManager = "apiCacheManager")
	@Override
	public PlayerData qryPlayerData(int element) {
		PlayerEntity playerEntity = this.redisCacheSerive.getPlayerByElememt(element);
		if (playerEntity == null) {
			return null;
		}
		PlayerData playerData = new PlayerData();
		// info
		playerData.setInfoData(this.initPlayerInfo(CommonUtils.getCurrentSeason(), playerEntity));
		// fixture, next 5 gw
		playerData.setFixtureDataList(this.setPlayerFixture(playerEntity.getTeamId()));
		// current season data
		playerData.setCurrentSeason(this.setSeasonData(CommonUtils.getCurrentSeason(), playerEntity.getCode()));
		// history season data（use code as unique index）
		playerData.setHistorySeasonList(this.setHistorySeasonData(playerEntity.getCode()));
		return playerData;
	}

	@Cacheable(value = "initPlayerInfo", key = "#playerEntity.element", condition = "#playerEntity.element gt 0")
	@Override
	public PlayerInfoData initPlayerInfo(String season, PlayerEntity playerEntity) {
		Map<Integer, String> teamNameMap = this.redisCacheSerive.getTeamNameMap(season);
		Map<Integer, String> positionMap = CommonUtils.getPositonMap();
		return new PlayerInfoData()
				.setElement(playerEntity.getElement())
				.setCode(playerEntity.getCode())
				.setWebName(playerEntity.getWebName())
				.setElementType(playerEntity.getElementType())
				.setElementTypeName(positionMap.get(playerEntity.getElementType()))
				.setTeamId(playerEntity.getTeamId())
				.setTeamName(teamNameMap.get(playerEntity.getTeamId()))
				.setPrice(NumberUtil.div(playerEntity.getPrice(), 10, 2));
	}

	private List<PlayerFixtureData> setPlayerFixture(int teamId) {
		List<PlayerFixtureData> playerFixtureList = Lists.newArrayList();
		int currentEvent = this.redisCacheSerive.getCurrentEvent();
		Map<Integer, String> teamNameMap = this.redisCacheSerive.getTeamNameMap();
		Map<Integer, String> teamShortNameMap = this.redisCacheSerive.getTeamShortNameMap();
		this.redisCacheSerive.getEventFixtureByTeamId(teamId).subList(currentEvent - 1, currentEvent + 4).forEach(o -> {
					o.setAgainstTeamName(teamNameMap.get(o.getAgainstTeamId()));
					o.setAgainstTeamShortName(teamShortNameMap.get(o.getAgainstTeamId()));
					playerFixtureList.add(o);
				}
		);
		return playerFixtureList;
	}

	private PlayerDetailData setSeasonData(String season, int code) {
		int element = this.qryPlayerElementByCode(season, code);
		PlayerDetailData playerDetailData = new PlayerDetailData().setSeason(season);
		PlayerStatEntity playerStatEntity = this.redisCacheSerive.getPlayerStatByElement(season, element);
		if (playerStatEntity == null) {
			return playerDetailData;
		}
		BeanUtil.copyProperties(playerStatEntity, playerDetailData, CopyOptions.create().ignoreNullValue());
		return playerDetailData;
	}

	private List<PlayerDetailData> setHistorySeasonData(int code) {
		List<PlayerDetailData> historySeasonList = Lists.newArrayList();
		Arrays.stream(HistorySeason.values()).forEach(o ->
				historySeasonList.add(this.setSeasonData(o.getSeason(), code)));
		return historySeasonList;
	}

	@Cacheable(value = "qryAllPlayers", key = "#season", cacheManager = "apiCacheManager")
	@Override
	public List<PlayerInfoData> qryAllPlayers(String season) {
		List<PlayerInfoData> list = Lists.newArrayList();
		this.qryAllPlayerList(season).values()
				.forEach(o -> list.add(initPlayerInfo(season, o)));
		return list;
	}

	@Override
	public List<EntryPickData> qryPickListFromPicks(String season, @NotNull String picks) {
		List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
		if (CollectionUtils.isEmpty(pickList)) {
			return Lists.newArrayList();
		}
		Map<Integer, String> positonMap = CommonUtils.getPositonMap();
		pickList.forEach(pick -> {
			PlayerEntity playerEntity = this.redisCacheSerive.getPlayerByElememt(season, pick.getElement());
			if (playerEntity != null) {
				pick.setElementTypeName(positonMap.get(playerEntity.getElementType()))
						.setWebName(playerEntity.getWebName());
			}
		});
		return pickList;
	}

	//	@Cacheable(value = "qryEntryInfo")
	@Override
	public EntryInfoEntity qryEntryInfo(int entry) {
		return this.entryInfoService.getById(entry);
	}

	@Cacheable(value = "qryTournamentInfoById")
	@Override
	public TournamentInfoEntity qryTournamentInfoById(int tournamentId) {
		return this.tournamentInfoService.getById(tournamentId);
	}

	@Cacheable(value = "qryKnockoutListByTournamentId")
	@Override
	public List<TournamentKnockoutEntity> qryKnockoutListByTournamentId(int tournamentId) {
		return this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId));
	}

	//	@Cacheable(value = "qryKnockoutResultByTournament")
	@Override
	public List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId) {
		List<TournamentKnockoutResultData> knockoutResultDataList = Lists.newArrayList();
		// knockout
		Map<Integer, TournamentKnockoutEntity> knockoutMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutEntity::getRound, 1))
				.stream()
				.collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, v -> v));
		if (CollectionUtils.isEmpty(knockoutMap)) {
			return knockoutResultDataList;
		}
		// knouckout_result, every match_id return a knockoutResultData
		knockoutMap.keySet().forEach(matchId -> {
			List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
					.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
					.eq(TournamentKnockoutResultEntity::getMatchId, matchId));
			// knockoutResultData
			TournamentKnockoutResultData knockoutResultData = new TournamentKnockoutResultData();
			TournamentKnockoutResultEntity o = knockoutResultList.get(0);
			knockoutResultData
					.setTournamentId(tournamentId)
					.setRound(knockoutMap.get(o.getMatchId()).getRound())
					.setEvent(o.getEvent())
					.setPlayAgainstId(o.getPlayAginstId())
					.setMatchId(o.getMatchId())
					.setHomeEntry(o.getHomeEntry())
					.setAwayEntry(o.getAwayEntry())
					.setHomeEntryName(this.getKnockoutResultEntryName(o.getHomeEntry()))
					.setAwayEntryName(this.getKnockoutResultEntryName(o.getAwayEntry()))
					.setHomeEntryNetPoint(this.calcKnockoutResultDataNetPont(knockoutResultList, "home"))
					.setAwayEntryNetPoint(this.calcKnockoutResultDataNetPont(knockoutResultList, "away"))
					.setHomeEntryRank(o.getHomeEntryRank())
					.setAwayEntryRank(o.getAwayEntryRank())
					.setMatchWinner(o.getMatchWinner());
			// match informantion
			Map<Integer, String> entryNameMap = ImmutableMap.of(knockoutResultData.getHomeEntry(), knockoutResultData.getHomeEntryName(),
					knockoutResultData.getAwayEntry(), knockoutResultData.getAwayEntryName());
			knockoutResultData.setMatchInfo(this.setRoundMatchInformation(knockoutResultList, entryNameMap));
			knockoutResultDataList.add(knockoutResultData);
		});
		return knockoutResultDataList;
	}

	private String getKnockoutResultEntryName(int entry) {
		if (entry < 0) {
			return "BYE";
		}
		EntryInfoEntity entryInfoEntity = this.qryEntryInfo(entry);
		if (entryInfoEntity == null) {
			return "";
		}
		return entryInfoEntity.getEntryName();
	}

	private int calcKnockoutResultDataNetPont(List<TournamentKnockoutResultEntity> knockoutResultList, String type) {
		if (StringUtils.equals(type, "home")) {
			return knockoutResultList.stream().mapToInt(TournamentKnockoutResultEntity::getHomeEntryNetPoints).sum();
		} else if (StringUtils.equals(type, "away")) {
			return knockoutResultList.stream().mapToInt(TournamentKnockoutResultEntity::getAwayEntryNetPoints).sum();
		}
		return 0;
	}

	private String setRoundMatchInformation(List<TournamentKnockoutResultEntity> knockoutResultList, Map<Integer, String> entryNameMap) {
		StringBuilder builder = new StringBuilder();
		knockoutResultList.forEach(o ->
				builder.append("GW").append(o.getEvent()).append(": ")
						.append(entryNameMap.get(o.getHomeEntry()))
						.append("（").append(o.getHomeEntryNetPoints()).append("）")
						.append("- ")
						.append(entryNameMap.get(o.getAwayEntry()))
						.append("（").append(o.getAwayEntryNetPoints()).append("）")
		);
		return builder.toString();
	}

}
