package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.*;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventTransferData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.StepDetailData;
import com.tong.fpl.domain.letletme.global.StepsData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.*;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.scout.ScoutPlayerData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.ILiveService;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/28
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TableQueryServiceImpl implements ITableQueryService {

	private final IQuerySerivce queryService;
	private final IRedisCacheSerive redisCacheService;
	private final ILiveService liveService;
	private final PlayerService playerService;
	private final PlayerValueService playerValueService;
	private final EventLiveService eventLiveService;
	private final EntryInfoService entryInfoService;
	private final EntryEventResultService entryEventResultService;
	private final EntryEventTransferService entryEventTransferService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentGroupService tournamentGroupService;
	private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
	private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
	private final TournamentKnockoutService tournamentKnockoutService;
	private final ZjTournamentResultService zjTournamentResultService;
	private final LeagueEventReportService leagueEventReportService;
	private final ScoutService scoutService;

	/**
	 * @apiNote player
	 */
	@Override
	public TableData<PlayerInfoData> qryPlayerList(String season) {
		List<PlayerInfoData> list = this.queryService.qryAllPlayers(season);
		list = list.stream().sorted(Comparator.comparing(PlayerInfoData::getPrice).reversed()).collect(Collectors.toList());
		return new TableData<>(list);
	}

	@Cacheable(value = "qryPagePlayerDataList", key = "#page+'::'+#limit", unless = "#result == null")
	@Override
	public TableData<PlayerInfoData> qryPagePlayerDataList(int page, int limit) {
		List<PlayerInfoData> list = Lists.newArrayList();
		Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
				new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<>());
		playerPage.getRecords().forEach(o ->
				list.add(BeanUtil.copyProperties(this.queryService.initPlayerInfo(CommonUtils.getCurrentSeason(), o), PlayerInfoData.class)));
		Page<PlayerInfoData> pageResult = new Page<>(page, limit, playerPage.getTotal());
		pageResult.setRecords(list);
		return new TableData<>(pageResult);
	}

	@Cacheable(value = "qryPriceChangeList")
	public TableData<PlayerValueData> qryPriceChangeList() {
		// prepare
		Map<Integer, PlayerEntity> playerMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
		Map<Integer, String> teamNameMap = Maps.newHashMap();
		this.queryService.getTeamNameMap().forEach((k, v) -> teamNameMap.put(Integer.valueOf(k), v));
		Map<Integer, String> teamShortNameMap = Maps.newHashMap();
		this.queryService.getTeamShortNameMap().forEach((k, v) -> teamShortNameMap.put(Integer.valueOf(k), v));
		Map<Integer, String> positionMap = Maps.newHashMap();
		this.queryService.getPositionMap().forEach((k, v) -> positionMap.put(Integer.valueOf(k), v));
		// player value
		List<PlayerValueData> list = Lists.newArrayList();
		this.playerValueService.list().forEach(o -> {
			PlayerValueData playerValueData = new PlayerValueData();
			BeanUtil.copyProperties(o, playerValueData, CopyOptions.create().ignoreNullValue());
			PlayerEntity playerEntity = playerMap.get(o.getElement());
			if (playerEntity != null) {
				int teamId = playerEntity.getTeamId();
				playerValueData
						.setWebName(playerEntity.getWebName())
						.setTeamName(teamNameMap.getOrDefault(teamId, ""))
						.setTeamShortName(teamShortNameMap.getOrDefault(teamId, ""))
						.setElementTypeName(positionMap.getOrDefault(o.getElementType(), ""));
			}
			list.add(playerValueData);
		});
		return new TableData<>(list);
	}

	@Cacheable(value = "qryScoutPlayerList", key = "#elementType")
	public TableData<ScoutPlayerData> qryScoutPlayerList(int elementType) {
		// event_live
		Multimap<Integer, EventLiveEntity> eventLiveMap = HashMultimap.create();
		this.eventLiveService.list().forEach(o -> eventLiveMap.put(o.getElement(), o));
		// player_data
		List<PlayerEntity> playerEntityList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
				.eq(PlayerEntity::getElementType, elementType));
		List<CompletableFuture<ScoutPlayerData>> future = playerEntityList
				.stream()
				.map(o ->
						CompletableFuture.supplyAsync(() ->
								this.qryScoutPlayerData(o, eventLiveMap)))
				.collect(Collectors.toList());
		List<ScoutPlayerData> list = future
				.stream()
				.map(CompletableFuture::join)
				.collect(Collectors.toList());
		return new TableData<>(list
				.stream()
				.sorted(Comparator.comparing(ScoutPlayerData::getTotalPoints).reversed())
				.collect(Collectors.toList())
		);
	}

	private ScoutPlayerData qryScoutPlayerData(PlayerEntity playerEntity, Multimap<Integer, EventLiveEntity> eventLiveMap) {
		int element = playerEntity.getElement();
		ScoutPlayerData scoutPlayerData = new ScoutPlayerData();
		// player info
		PlayerInfoData playerInfoData = this.queryService.initPlayerInfo(CommonUtils.getCurrentSeason(), playerEntity);
		if (playerInfoData == null) {
			return scoutPlayerData;
		}
		BeanUtil.copyProperties(playerInfoData, scoutPlayerData, CopyOptions.create().ignoreNullValue());
		// fixture
		List<PlayerFixtureData> fixtureDataList = this.queryService.setPlayerFixture(playerEntity.getTeamId());
		if (fixtureDataList.size() >= 5) {
			scoutPlayerData
					.setFixtureEvent1(fixtureDataList.get(2).getEvent())
					.setAgainstTeam1ShortName(fixtureDataList.get(2).getAgainstTeamShortName())
					.setDifficulty1(fixtureDataList.get(2).getDifficulty())
					.setWasHome1(fixtureDataList.get(2).isWasHome())
					.setFixtureEvent2(fixtureDataList.get(3).getEvent())
					.setAgainstTeam2ShortName(fixtureDataList.get(3).getAgainstTeamShortName())
					.setDifficulty2(fixtureDataList.get(3).getDifficulty())
					.setWasHome2(fixtureDataList.get(3).isWasHome())
					.setFixtureEvent3(fixtureDataList.get(4).getEvent())
					.setAgainstTeam3ShortName(fixtureDataList.get(4).getAgainstTeamShortName())
					.setDifficulty3(fixtureDataList.get(4).getDifficulty())
					.setWasHome3(fixtureDataList.get(4).isWasHome());
		}
		// event_live
		if (!eventLiveMap.containsKey(element)) {
			return scoutPlayerData;
		}
		scoutPlayerData.setTotalPoints(eventLiveMap.get(element)
				.stream()
				.mapToInt(EventLiveEntity::getTotalPoints)
				.sum()
		);
		// player_stat
		PlayerStatEntity playerStatEntity = this.queryService.getPlayerStatByElement(element);
		if (playerStatEntity != null) {
			scoutPlayerData
					.setSelectedByPercent(playerStatEntity.getSelectedByPercent())
					.setPointsPerGame(playerStatEntity.getPointsPerGame());
		}
		return scoutPlayerData;
	}

	/**
	 * @apiNote tournament
	 */
	@Override
	public TableData<TournamentInfoData> qryTournamentList(TournamentQueryParam param) {
		List<TournamentInfoData> list = Lists.newArrayList();
		// get tournament info
		LambdaQueryWrapper<TournamentInfoEntity> queryWrapper = new QueryWrapper<TournamentInfoEntity>().lambda();
		if (param.getEntry() > 0) {
			List<Integer> tournamentIdList = this.queryService.qryEntryTournamentList(param.getEntry());
			if (!CollectionUtils.isEmpty(tournamentIdList)) {
				queryWrapper.in(TournamentInfoEntity::getId, tournamentIdList);
			}
		}
		if (StringUtils.isNotBlank(param.getName())) {
			queryWrapper.like(TournamentInfoEntity::getName, param.getName());
		} else {
			if (StringUtils.isNotBlank(param.getCreator())) {
				queryWrapper.eq(TournamentInfoEntity::getCreator, param.getCreator());
			} else if (param.getLeagueId() > 0) {
				queryWrapper.eq(TournamentInfoEntity::getLeagueId, param.getLeagueId());
			} else if (StringUtils.isNotBlank(param.getCreateTime())) {
				queryWrapper.gt(TournamentInfoEntity::getCreateTime, param.getCreateTime());
				queryWrapper.lt(TournamentInfoEntity::getCreateTime, LocalDate.parse(param.getCreateTime()).plusDays(1).format(DateTimeFormatter.ofPattern(Constant.DATE)));
			} else if (StringUtils.isNotBlank(param.getSeason())) {
				queryWrapper.eq(TournamentInfoEntity::getSeason, param.getSeason());
			}
		}
		if (queryWrapper.getExpression().getNormal().size() == 0) {
			return new TableData<>();
		}
		queryWrapper.eq(TournamentInfoEntity::getState, 1)
				.orderByAsc(TournamentInfoEntity::getId);
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
					.setGroupFillAverage(o.getGroupFillAverage() ? "是" : "否")
					.setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "));
			list.add(tournamentInfoData);
		});
		return new TableData<>(list);
	}

	@Cacheable(value = "qryEntryTournamentList", key = "#entry")
	@Override
	public TableData<TournamentEntryData> qryEntryTournamentList(int entry) {
		List<TournamentEntryData> list = Lists.newArrayList();
		if (entry == 0) {
			return new TableData<>();
		}
		int currentEvent = this.queryService.getCurrentEvent();
		// get tournament_list
		List<Integer> tournamentList = this.queryService.qryEntryTournamentList(entry);
		if (CollectionUtils.isEmpty(tournamentList)) {
			return new TableData<>();
		}
		// stage_mode
		Map<String, GroupMode> groupModeMap = Arrays.stream(GroupMode.values()).collect(Collectors.toMap(Enum::name, v -> v));
		Map<String, KnockoutMode> knockModeMap = Arrays.stream(KnockoutMode.values()).collect(Collectors.toMap(Enum::name, v -> v));
		// return
		this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
				.in(TournamentInfoEntity::getId, tournamentList)
				.eq(TournamentInfoEntity::getState, 1))
				.forEach(o ->
						list.add(new TournamentEntryData()
								.setEntry(entry)
								.setTournamentId(o.getId())
								.setName(o.getName())
								.setCreator(o.getCreator())
								.setSeason(o.getSeason())
								.setLeagueType(o.getLeagueType())
								.setLeagueId(o.getLeagueId())
								.setTournamentMode(o.getTournamentMode())
								.setGroupMode(groupModeMap.get(o.getGroupMode()).getModeName())
								.setKnockoutMode(knockModeMap.get(o.getKnockoutMode()).getModeName())
								.setStadge(this.setCurrentStage(currentEvent, groupModeMap.get(o.getGroupMode()), o))
								.setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "))
						));
		return new TableData<>(list);
	}

	private String setCurrentStage(int currentEvent, GroupMode groupMode, TournamentInfoEntity tournamentInfoEntity) {
		switch (groupMode) {
			case No_group: {
				if (currentEvent > tournamentInfoEntity.getKnockoutStartGw()) {
					return "淘汰赛";
				} else if (currentEvent > tournamentInfoEntity.getKnockoutEndGw()) {
					return "已结束";
				}
				break;
			}
			case Points_race:
			case Battle_race: {
				int groupStartGw = tournamentInfoEntity.getGroupStartGw();
				int knockoutStartGw = tournamentInfoEntity.getKnockoutStartGw();
				int knockoutEndGw = tournamentInfoEntity.getKnockoutEndGw();
				if (groupStartGw > 0 && currentEvent > groupStartGw) {
					return "小组赛";
				} else if (knockoutStartGw > 0 && currentEvent > knockoutStartGw) {
					return "淘汰赛";
				} else if (knockoutEndGw > 0 && currentEvent > knockoutEndGw) {
					return "已结束";
				}
				break;
			}
		}
		return "未开始";
	}

	@Cacheable(value = "qryGroupInfoListByGroupId", key = "#tournamentId+'::'+#groupId")
	@Override
	public TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId) {
		List<TournamentGroupData> list = Lists.newArrayList();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return new TableData<>();
		}
		int groupNum = tournamentInfoEntity.getGroupNum();
		int current = this.queryService.getCurrentEvent();
		// tournament_group
		this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.eq(TournamentGroupEntity::getGroupId, groupId)
				.orderByAsc(TournamentGroupEntity::getGroupRank)
				.orderByAsc(TournamentGroupEntity::getGroupIndex))
				.forEach(o -> {
					int entry = o.getEntry();
					TournamentGroupData tournamentGroupData = new TournamentGroupData()
							.setEvent(current)
							.setGroupMode(tournamentInfoEntity.getGroupMode());
					BeanUtil.copyProperties(o, tournamentGroupData, CopyOptions.create().ignoreNullValue());
					tournamentGroupData
							.setStartGw(o.getStartGw())
							.setEndGw(o.getEndGw());
					if (entry < 0) {
						tournamentGroupData
								.setEntryName("平均分")
								.setPlayerName("平均分");
					} else {
						EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
						if (entryInfoEntity != null) {
							BeanUtil.copyProperties(entryInfoEntity, tournamentGroupData, CopyOptions.create().ignoreNullValue());
						}
					}
					// group name
					tournamentGroupData.setTournamentGroupNameMap(this.queryService.qryZjTournamentGroupNameMap(tournamentId));
					// pk entry
					TournamentKnockoutEntity tournamentKnockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
							.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
							.eq(TournamentKnockoutEntity::getRound, 1)
							.eq(TournamentKnockoutEntity::getHomeEntry, entry));
					if (tournamentKnockoutEntity != null) {
						int pkEntry = tournamentKnockoutEntity.getAwayEntry();
						if (pkEntry > 0) {
							tournamentGroupData
									.setPkDraw(true)
									.setPkEntry(pkEntry);
							// pk entry group
							TournamentGroupEntity tournamentGroupEntity = this.tournamentGroupService.getOne(new QueryWrapper<TournamentGroupEntity>().lambda()
									.eq(TournamentGroupEntity::getTournamentId, tournamentId)
									.le(TournamentGroupEntity::getGroupId, groupNum)
									.eq(TournamentGroupEntity::getEntry, pkEntry));
							if (tournamentGroupEntity != null) {
								tournamentGroupData.setPkGroupName(tournamentGroupEntity.getGroupName());
							}
							// pk entry_info
							EntryInfoEntity pkEntryInfo = this.queryService.qryEntryInfo(pkEntry);
							if (pkEntryInfo != null) {
								BeanUtil.copyProperties(pkEntryInfo, tournamentGroupData, CopyOptions.create().ignoreNullValue());

							}
						}
					} else {
						tournamentKnockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
								.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
								.eq(TournamentKnockoutEntity::getRound, 1)
								.eq(TournamentKnockoutEntity::getAwayEntry, entry));
						if (tournamentKnockoutEntity != null) {
							int pkEntry = tournamentKnockoutEntity.getHomeEntry();
							if (pkEntry > 0) {
								tournamentGroupData
										.setPkDraw(true)
										.setPkEntry(pkEntry);
								// pk entry group
								TournamentGroupEntity tournamentGroupEntity = this.tournamentGroupService.getOne(new QueryWrapper<TournamentGroupEntity>().lambda()
										.eq(TournamentGroupEntity::getTournamentId, tournamentId)
										.le(TournamentGroupEntity::getGroupId, groupNum)
										.eq(TournamentGroupEntity::getEntry, pkEntry));
								if (tournamentGroupEntity != null) {
									tournamentGroupData.setPkGroupName(tournamentGroupEntity.getGroupName());
								}
								// pk entry_info
								EntryInfoEntity pkEntryInfo = this.queryService.qryEntryInfo(pkEntry);
								if (pkEntryInfo != null) {
									BeanUtil.copyProperties(pkEntryInfo, tournamentGroupData, CopyOptions.create().ignoreNullValue());

								}
							}
						} else {
							tournamentGroupData
									.setPkDraw(false)
									.setPkEntry(0)
									.setPkGroupName("")
									.setPkEntryName("")
									.setPkPlayerName("");
						}
					}
					list.add(tournamentGroupData);
				});
		return new TableData<>(list);
	}

	@Override
	public TableData<TournamentGroupData> qrySeeableGroupInfoListByGroupId(int tournamentId, int currentGroupId, int groupId) {
		// group name
		Map<String, String> groupNameMap = this.queryService.qryZjTournamentGroupNameMap(tournamentId);
		// group entry name
		Map<String, String> groupEntryNameMap = this.queryService.qryZjTournamentGroupEntryGroupNameMap(tournamentId);
		// disclose entry list
		List<Integer> discloseList = this.redisCacheService.getDiscloseList(tournamentId, currentGroupId);
		// phase two tournament_group
		List<TournamentGroupData> list = Lists.newArrayList();
		this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.eq(TournamentGroupEntity::getGroupId, groupId)
				.orderByAsc(TournamentGroupEntity::getGroupRank)
				.orderByAsc(TournamentGroupEntity::getGroupIndex))
				.forEach(o -> {
					TournamentGroupData tournamentGroupData = new TournamentGroupData();
					BeanUtil.copyProperties(o, tournamentGroupData);
					int entry = o.getEntry();
					if (entry > 0) {
						tournamentGroupData.setDrawPhaseTwo(true);
						String currentGroupName = groupNameMap.getOrDefault(String.valueOf(currentGroupId), "");
						String entryGroupName = groupEntryNameMap.getOrDefault(String.valueOf(entry), "");
						if ((StringUtils.isEmpty(currentGroupName) || StringUtils.isEmpty(entryGroupName) || !StringUtils.equals(entryGroupName, currentGroupName)) &&
								!discloseList.contains(entry)) {
							tournamentGroupData.setEntry(-1);
						}
					} else {
						tournamentGroupData.setDrawPhaseTwo(false);
					}
					tournamentGroupData.setDiscloseList(discloseList);
					if (entry > 0) {
						EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(entry);
						if (entryInfoEntity != null) {
							BeanUtil.copyProperties(entryInfoEntity, tournamentGroupData, CopyOptions.create().ignoreNullValue());

						}
					}
					list.add(tournamentGroupData);
				});
		return new TableData<>(list);
	}

	@Cacheable(value = "qryPointsGroupChampion", key = "#tournamentId", unless = "#result == null")
	@Override
	public TableData<TournamentGroupEventChampionData> qryPointsGroupChampion(int tournamentId) {
		TournamentGroupEventChampionData data = new TournamentGroupEventChampionData();
		List<TournamentPointsGroupEventResultData> eventChampionResultList = Lists.newArrayList();
		List<TournamentPointsGroupEventResultData> eventRunnerUpResultList = Lists.newArrayList();
		List<TournamentPointsGroupEventResultData> eventSecondRunnerUpResultList = Lists.newArrayList();
		List<Multimap<Integer, TournamentPointsGroupEventResultData>> championCountList = Lists.newArrayList();
		// tournament_points_group_result
		int current = this.queryService.getCurrentEvent();
		IntStream.rangeClosed(1, current).forEach(event -> {
			List<TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityList = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
					.eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
					.eq(TournamentPointsGroupResultEntity::getEvent, event)
					.gt(TournamentPointsGroupResultEntity::getEventPoints, 0)
					.orderByDesc(TournamentPointsGroupResultEntity::getEventPoints));
			if (CollectionUtils.isEmpty(tournamentPointsGroupResultEntityList)) {
				return;
			}
			if (tournamentPointsGroupResultEntityList.size() < 3) {
				log.error("tournament:{}, event:{}, less than 3 entry, no champion then", tournamentId, event);
			}
			Multimap<Integer, TournamentPointsGroupEventResultData> eventChampionCountMap = this.getEventChampionCountMap(tournamentPointsGroupResultEntityList);
			eventChampionResultList.addAll(eventChampionCountMap.get(1));
			eventRunnerUpResultList.addAll(eventChampionCountMap.get(2));
			eventSecondRunnerUpResultList.addAll(eventChampionCountMap.get(3));
			championCountList.add(eventChampionCountMap);
		});
		// return
		data
				.setEventChampionResultList(eventChampionResultList)
				.setEventRunnerUpResultList(eventRunnerUpResultList)
				.setEventSecondRunnerUpResultList(eventSecondRunnerUpResultList)
				.setChampionCountList(this.setCountListByMultiMap(championCountList));
		return new TableData<>(data);
	}

	private Multimap<Integer, TournamentPointsGroupEventResultData> getEventChampionCountMap(List<TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityList) {
		Multimap<Integer, TournamentPointsGroupEventResultData> map = HashMultimap.create();
		int onePoints = tournamentPointsGroupResultEntityList.get(0).getEventPoints();
		map.put(1, this.initEventResultData(tournamentPointsGroupResultEntityList.get(0)));
		int twoPoints = tournamentPointsGroupResultEntityList.get(1).getEventPoints();
		if (twoPoints == onePoints) { // 1, 1, 2
			map.put(1, this.initEventResultData(tournamentPointsGroupResultEntityList.get(1)));
			map.put(2, this.initEventResultData(tournamentPointsGroupResultEntityList.get(2)));
		} else { // 1, 2, 2 or 1, 2, 3 or 1, 2, 3, 3
			map.put(2, this.initEventResultData(tournamentPointsGroupResultEntityList.get(1)));
			int threePoints = tournamentPointsGroupResultEntityList.get(2).getEventPoints();
			if (threePoints == twoPoints) { // 1, 2, 2
				map.put(2, this.initEventResultData(tournamentPointsGroupResultEntityList.get(2)));
			} else {
				map.put(3, this.initEventResultData(tournamentPointsGroupResultEntityList.get(2)));  // 1, 2, 3
				int fourPoints = tournamentPointsGroupResultEntityList.get(3).getEventPoints();
				if (fourPoints == threePoints) { // 1, 2, 3, 3
					map.put(3, this.initEventResultData(tournamentPointsGroupResultEntityList.get(3)));
				}
			}
		}
		return map;
	}

	private TournamentPointsGroupEventResultData initEventResultData(TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity) {
		TournamentPointsGroupEventResultData tournamentPointsGroupEventResultData = new TournamentPointsGroupEventResultData()
				.setTournamentId(tournamentPointsGroupResultEntity.getTournamentId())
				.setEvent(tournamentPointsGroupResultEntity.getEvent())
				.setPoints(tournamentPointsGroupResultEntity.getEventPoints())
				.setCost(tournamentPointsGroupResultEntity.getEventCost())
				.setNetPoints(tournamentPointsGroupResultEntity.getEventNetPoints())
				.setRank(tournamentPointsGroupResultEntity.getEventRank());
		// entry_info
		EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(tournamentPointsGroupResultEntity.getEntry());
		if (entryInfoEntity != null) {
			BeanUtil.copyProperties(entryInfoEntity, tournamentPointsGroupEventResultData, CopyOptions.create().ignoreNullValue());
		}
		return tournamentPointsGroupEventResultData;
	}

	private List<TournamentGroupChampionCountData> setCountListByMultiMap(List<Multimap<Integer, TournamentPointsGroupEventResultData>> championCountList) {
		Map<Integer, TournamentGroupChampionCountData> entryChampionCountMap = Maps.newHashMap();
		// champion
		List<TournamentPointsGroupEventResultData> championList = Lists.newArrayList();
		championCountList.forEach(o -> championList.addAll(o.get(1)));
		championList.forEach(o -> {
			int entry = o.getEntry();
			TournamentGroupChampionCountData entryChampionCountData = this.initChampionCountData(entry, entryChampionCountMap);
			entryChampionCountData.setChampionNum(entryChampionCountData.getChampionNum() + 1);
			entryChampionCountMap.put(entry, entryChampionCountData);
		});
		// runner_up
		List<TournamentPointsGroupEventResultData> runnerUpList = Lists.newArrayList();
		championCountList.forEach(o -> runnerUpList.addAll(o.get(2)));
		runnerUpList.forEach(o -> {
			int entry = o.getEntry();
			TournamentGroupChampionCountData entryChampionCountData = this.initChampionCountData(entry, entryChampionCountMap);
			entryChampionCountData.setRunnerUpNum(entryChampionCountData.getRunnerUpNum() + 1);
			entryChampionCountMap.put(entry, entryChampionCountData);
		});
		// second runner_up
		List<TournamentPointsGroupEventResultData> secondRunnerUpList = Lists.newArrayList();
		championCountList.forEach(o -> secondRunnerUpList.addAll(o.get(3)));
		secondRunnerUpList.forEach(o -> {
			int entry = o.getEntry();
			TournamentGroupChampionCountData entryChampionCountData = this.initChampionCountData(entry, entryChampionCountMap);
			entryChampionCountData.setSecondRunnerUpNum(entryChampionCountData.getSecondRunnerUpNum() + 1);
			entryChampionCountMap.put(entry, entryChampionCountData);
		});
		return entryChampionCountMap.values()
				.stream()
				.sorted(Comparator.comparing(TournamentGroupChampionCountData::getChampionNum)
						.thenComparing(TournamentGroupChampionCountData::getRunnerUpNum)
						.thenComparing(TournamentGroupChampionCountData::getSecondRunnerUpNum)
						.reversed())
				.collect(Collectors.toList());
	}

	private TournamentGroupChampionCountData initChampionCountData(int entry, Map<Integer, TournamentGroupChampionCountData> entryChampionCountMap) {
		TournamentGroupChampionCountData entryChampionCountData;
		if (entryChampionCountMap.containsKey(entry)) {
			entryChampionCountData = entryChampionCountMap.get(entry);
		} else {
			entryChampionCountData = new TournamentGroupChampionCountData().setEntry(entry);
			EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(entry);
			if (entryInfoEntity != null) {
				entryChampionCountData
						.setEntryName(entryInfoEntity.getEntryName())
						.setPlayerName(entryInfoEntity.getPlayerName());
			}
		}
		return entryChampionCountData;
	}

	@Cacheable(value = "qryPagePointsGroupResult", key = "#tournamentId+'::'+#groupId+'::'+#entry+'::'+#page+'::'+#limit", unless = "#result == null")
	@Override
	public TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
		List<TournamentPointsGroupEventResultData> list = Lists.newArrayList();
		// event_list
		int current = this.queryService.getCurrentEvent();
		List<Integer> eventList = Lists.newArrayList();
		IntStream.rangeClosed(1, current).forEach(eventList::add);
		if (CollectionUtils.isEmpty(eventList)) {
			return new TableData<>();
		}
		// tournament_points_group_result
		Page<TournamentPointsGroupResultEntity> pointsGroupResultPage = this.tournamentPointsGroupResultService.getBaseMapper().selectPage(
				new Page<>(page, limit, true), new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
						.eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
						.eq(TournamentPointsGroupResultEntity::getGroupId, groupId)
						.eq(TournamentPointsGroupResultEntity::getEntry, entry)
						.in(TournamentPointsGroupResultEntity::getEvent, eventList)
		);
		pointsGroupResultPage.getRecords().forEach(o -> {
			TournamentPointsGroupEventResultData tournamentPointsGroupEventResultData = new TournamentPointsGroupEventResultData()
					.setTournamentId(tournamentId)
					.setGroupId(groupId)
					.setEvent(o.getEvent())
					.setEntry(entry)
					.setGroupRank(o.getEventGroupRank())
					.setPoints(o.getEventPoints())
					.setCost(o.getEventCost())
					.setNetPoints(o.getEventNetPoints())
					.setRank(o.getEventRank());
			// entry_event_result
			EntryEventResultData entryEventResultData = this.queryService.qryEntryEventResult(o.getEvent(), entry);
			if (entryEventResultData != null) {
				tournamentPointsGroupEventResultData
						.setBenchPoints(entryEventResultData.getBenchPoints())
						.setChip(entryEventResultData.getChip());
			}
			list.add(tournamentPointsGroupEventResultData);
		});
		Page<TournamentPointsGroupEventResultData> pageResult = new Page<>(page, limit, pointsGroupResultPage.getTotal());
		pageResult.setRecords(list);
		return new TableData<>(pageResult);
	}

	@Cacheable(value = "qryPageBattleGroupResult", key = "#tournamentId+'::'+#groupId+'::'+#entry+'::'+#page+'::'+#limit")
	@Override
	public TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
		List<TournamentBattleGroupEventResultData> list = Lists.newArrayList();
		// event_list
		int current = this.queryService.getCurrentEvent();
		List<Integer> eventList = Lists.newArrayList();
		IntStream.rangeClosed(1, current).forEach(eventList::add);
		if (CollectionUtils.isEmpty(eventList)) {
			return new TableData<>();
		}
		// tournament_battle_group_result
		Page<TournamentBattleGroupResultEntity> battleGroupResultPage = this.tournamentBattleGroupResultService.getBaseMapper().selectPage(
				new Page<>(page, limit, true), new QueryWrapper<TournamentBattleGroupResultEntity>().lambda()
						.eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId)
						.eq(TournamentBattleGroupResultEntity::getGroupId, groupId)
						.in(TournamentBattleGroupResultEntity::getEvent, eventList)
						.and(o -> o.eq(TournamentBattleGroupResultEntity::getHomeEntry, entry)
								.or(i -> i.eq(TournamentBattleGroupResultEntity::getAwayEntry, entry)))
		);
		battleGroupResultPage.getRecords().forEach(o ->
				list.add(new TournamentBattleGroupEventResultData()
						.setTournamentId(tournamentId)
						.setGroupId(groupId)
						.setEvent(o.getEvent())
						.setHomeEntry(o.getHomeEntry())
						.setHomeEntryName(this.setBattleGroupEntryName(o.getHomeEntry()))
						.setHomeEntryNetPoints(o.getHomeEntryNetPoints())
						.setHomeEntryRank(o.getHomeEntryRank())
						.setAwayEntry(o.getAwayEntry())
						.setAwayEntryName(this.setBattleGroupEntryName(o.getAwayEntry()))
						.setAwayEntryNetPoints(o.getAwayEntryNetPoints())
						.setAwayEntryRank(o.getAwayEntryRank())
						.setScore(o.getHomeEntryNetPoints() + "-" + o.getAwayEntryNetPoints())
				));
		Page<TournamentBattleGroupEventResultData> pageResult = new Page<>(page, limit, battleGroupResultPage.getTotal());
		pageResult.setRecords(list);
		return new TableData<>(pageResult);
	}

	@Cacheable(value = "qryPageZjTournamentGroupResult", key = "#tournamentId+'::'+stage+'::'+#groupId+'::'+#entry+'::'+#page+'::'+#limit", unless = "#result == null")
	@Override
	public TableData<TournamentPointsGroupEventResultData> qryPageZjTournamentGroupResult(int tournamentId, int stage, int groupId, int entry, int page, int limit) {
		List<TournamentPointsGroupEventResultData> list = Lists.newArrayList();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return new TableData<>();
		}
		// stage
		List<Integer> eventList = Lists.newArrayList();
		if (stage == 1) { // phase one
			TournamentGroupEntity phaseOne = this.tournamentGroupService.getOne(new QueryWrapper<TournamentGroupEntity>().lambda()
					.eq(TournamentGroupEntity::getTournamentId, tournamentId)
					.eq(TournamentGroupEntity::getGroupId, 1)
					.eq(TournamentGroupEntity::getGroupIndex, 1));
			if (phaseOne == null) {
				return new TableData<>();
			}
			int phaseOneStartGw = phaseOne.getStartGw();
			int phaseOneEndGw = phaseOne.getEndGw();
			IntStream.rangeClosed(phaseOneStartGw, phaseOneEndGw).forEach(eventList::add);
		} else if (stage == 2) { // phase two
			TournamentGroupEntity phaseTwo = this.tournamentGroupService.getOne(new QueryWrapper<TournamentGroupEntity>().lambda()
					.eq(TournamentGroupEntity::getTournamentId, tournamentId)
					.eq(TournamentGroupEntity::getGroupId, tournamentInfoEntity.getGroupNum() + 1)
					.eq(TournamentGroupEntity::getGroupIndex, 1));
			if (phaseTwo == null) {
				return new TableData<>();
			}
			int phaseTwoStartGw = phaseTwo.getStartGw();
			int phaseTwoEndGw = phaseTwo.getEndGw();
			IntStream.rangeClosed(phaseTwoStartGw, phaseTwoEndGw).forEach(eventList::add);
		} else {
			return new TableData<>();
		}
		if (CollectionUtils.isEmpty(eventList)) {
			return new TableData<>();
		}
		// points_group_result
		Page<TournamentPointsGroupResultEntity> pointsGroupResultPage = this.tournamentPointsGroupResultService.getBaseMapper().selectPage(
				new Page<>(page, limit, true), new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
						.eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
						.eq(TournamentPointsGroupResultEntity::getEntry, entry)
						.in(TournamentPointsGroupResultEntity::getEvent, eventList)
		);
		pointsGroupResultPage.getRecords().forEach(o ->
				list.add(new TournamentPointsGroupEventResultData()
						.setTournamentId(tournamentId)
						.setGroupId(groupId)
						.setEvent(o.getEvent())
						.setEntry(entry)
						.setGroupRank(o.getEventGroupRank())
						.setPoints(o.getEventPoints())
						.setCost(o.getEventCost())
						.setNetPoints(o.getEventNetPoints())
						.setRank(o.getEventRank())
				));
		Page<TournamentPointsGroupEventResultData> pageResult = new Page<>(page, limit, pointsGroupResultPage.getTotal());
		pageResult.setRecords(list);
		return new TableData<>(list);
	}

	private String setBattleGroupEntryName(int entry) {
		if (entry < 0) {
			return "平均分";
		} else if (entry == 0) {
			return "轮空";
		}
		EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(entry);
		if (entryInfoEntity == null) {
			return "";
		}
		return entryInfoEntity.getEntryName();
	}

	@Cacheable(value = "qryZjTournamentResultById", key = "#tournamentId")
	@Override
	public TableData<ZjTournamentResultData> qryZjTournamentResultById(int tournamentId) {
		List<ZjTournamentResultData> list = Lists.newArrayList();
		this.zjTournamentResultService.list(new QueryWrapper<ZjTournamentResultEntity>().lambda()
				.eq(ZjTournamentResultEntity::getTournamentId, tournamentId)
				.orderByAsc(ZjTournamentResultEntity::getTournamentRank))
				.forEach(o -> {
					ZjTournamentResultData zjTournamentResultData = new ZjTournamentResultData();
					BeanUtil.copyProperties(o, zjTournamentResultData, CopyOptions.create().ignoreNullValue());
					zjTournamentResultData.setPhaseStep(this.setZjTournamentPhaseStep(zjTournamentResultData));
					list.add(zjTournamentResultData);
				});
		return new TableData<>(list);
	}

	private int setZjTournamentPhaseStep(ZjTournamentResultData zjTournamentResultData) {
		int step = -1;
		if (zjTournamentResultData.getPhaseOneTotalPoints() > 0) {
			step = 0;
		}
		if (zjTournamentResultData.getPhaseTwoTotalPoints() > 0) {
			step = 1;
		}
		if (zjTournamentResultData.getPkTotalPoints() > 0) {
			step = 2;
		}
		return step;
	}

	@Override
	public TableData<StepsData> qryZjTournamentPkPickSteps(int tournamentId) {
		// tournament_knockout
		List<TournamentKnockoutEntity> tournamentKnockoutEntityList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutEntity::getRound, 1)
				.orderByAsc(TournamentKnockoutEntity::getMatchId));
		if (CollectionUtils.isEmpty(tournamentKnockoutEntityList)) {
			return new TableData<>();
		}
		int matchNum = tournamentKnockoutEntityList.size();
		List<Integer> groupRankList = this.zjTournamentResultService.list(new QueryWrapper<ZjTournamentResultEntity>().lambda()
				.eq(ZjTournamentResultEntity::getTournamentId, tournamentId)
				.orderByAsc(ZjTournamentResultEntity::getTournamentRank))
				.stream()
				.map(ZjTournamentResultEntity::getGroupId)
				.collect(Collectors.toList());
		// group name
		Map<String, String> groupNameMap = this.queryService.qryZjTournamentGroupNameMap(tournamentId);
		// pick order
		List<StepDetailData> pickOrderList = Lists.newArrayList();
		IntStream.range(0, matchNum / groupRankList.size()).forEach(repeatTime -> {
			for (int i = 1; i < groupRankList.size() + 1; i++) {
				String groupName = groupNameMap.getOrDefault(String.valueOf(groupRankList.get(i - 1)), "");
				pickOrderList.add(new StepDetailData()
						.setTitle(groupName)
						.setDescription("")
				);
			}
		});
		// steps data
		int lasePickMatchId = tournamentKnockoutEntityList
				.stream()
				.filter(o -> o.getHomeEntry() < 0 && o.getAwayEntry() < 0)
				.map(TournamentKnockoutEntity::getMatchId)
				.findFirst()
				.orElse(0) - 1;
		int active = lasePickMatchId - 1;
		return new TableData<>(new StepsData().setDataList(pickOrderList).setActive(active));
	}

	@Override
	public TableData<TournamentGroupData> qryZjTournamentPkPickableList(int tournamentId, int currentGroupId) {
		List<TournamentGroupData> list = Lists.newArrayList();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return new TableData<>();
		}
		int groupNum = tournamentInfoEntity.getGroupNum();
		// group list
		List<Integer> groupList = Lists.newArrayList();
		IntStream.rangeClosed(1, groupNum).forEach(groupId -> {
			if (groupId != currentGroupId) {
				groupList.add(groupId);
			}
		});
		if (CollectionUtils.isEmpty(groupList)) {
			return new TableData<>();
		}
		// picked entry
		List<Integer> pickedEntryList = Lists.newArrayList();
		List<TournamentKnockoutEntity> pkPickedList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutEntity::getRound, 1)
				.gt(TournamentKnockoutEntity::getHomeEntry, 0)
				.gt(TournamentKnockoutEntity::getAwayEntry, 0));
		pickedEntryList.addAll(pkPickedList.stream().map(TournamentKnockoutEntity::getHomeEntry).collect(Collectors.toList()));
		pickedEntryList.addAll(pkPickedList.stream().map(TournamentKnockoutEntity::getAwayEntry).collect(Collectors.toList()));
		// tournament_group
		this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.in(TournamentGroupEntity::getGroupId, groupList)
				.notIn(TournamentGroupEntity::getEntry, pickedEntryList))
				.forEach(o -> {
					EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(o.getEntry());
					if (entryInfoEntity == null) {
						return;
					}
					list.add(new TournamentGroupData()
							.setGroupId(o.getGroupId())
							.setGroupName(o.getGroupName())
							.setEntry(o.getEntry())
							.setEntryName(entryInfoEntity.getEntryName())
							.setPlayerName(entryInfoEntity.getPlayerName())
					);
				});
		return new TableData<>(list);
	}

	/**
	 * @apiNote live
	 */
	@Override
	public TableData<LiveCalaData> qryEntryLivePoints(int entry) {
		int event = this.queryService.getCurrentEvent();
		LiveCalaData liveCalaData = this.liveService.calcLivePointsByEntry(event, entry);
		return new TableData<>(liveCalaData);
	}

	@Override
	public TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId) {
		int event = this.queryService.getCurrentEvent();
		List<LiveCalaData> liveCalcList = this.liveService.calcLivePointsByTournament(event, tournamentId);
		return new TableData<>(liveCalcList);
	}

	@Override
	public TableData<LiveMatchTeamData> qryLiveTeamDataList(int statusId) {
		List<LiveMatchTeamData> list = Lists.newArrayList();
		// prepare
		int event = this.queryService.getCurrentEvent();
		Collection<EventLiveEntity> eventLiveList = this.queryService.getEventLiveByEvent(event).values();
		Map<Integer, String> playerMap = this.getPlayerMap();
		Map<Integer, String> positionMap = this.getPositionMap();
		// live element event result
		List<Integer> teamIdList = Lists.newArrayList();
		this.queryService.qryLiveMatchList(statusId).forEach(o -> {
			teamIdList.add(o.getHomeTeamId());
			teamIdList.add(o.getAwayTeamId());
		});
		teamIdList.forEach(teamId -> list.add(this.qryLiveTeamData(teamId, eventLiveList, playerMap, positionMap)));
		return new TableData<>(list);
	}

	private LiveMatchTeamData qryLiveTeamData(int teamId, Collection<EventLiveEntity> eventLiveList, Map<Integer, String> playerMap, Map<Integer, String> positionMap) {
		LiveMatchTeamData data = new LiveMatchTeamData().setTeamId(teamId);
		List<ElementEventResultData> teamDataList = Lists.newArrayList();
		// team data
		Map<Integer, Integer> liveBonusMap = this.getLiveBonusMap(teamId);
		eventLiveList.forEach(o -> {
			if (o.getTeamId() != teamId || o.getMinutes() <= 0) {
				return;
			}
			ElementEventResultData elementEventResultData = new ElementEventResultData();
			elementEventResultData
					.setEvent(o.getEvent())
					.setElement(o.getElement())
					.setWebName(playerMap.getOrDefault(o.getElement(), ""))
					.setElementType(o.getElementType())
					.setElementTypeName(positionMap.getOrDefault(o.getElementType(), ""))
					.setMinutes(o.getMinutes())
					.setGoalsScored(o.getGoalsScored())
					.setAssists(o.getAssists())
					.setGoalsConceded(o.getGoalsConceded())
					.setOwnGoals(o.getOwnGoals())
					.setPenaltiesSaved(o.getPenaltiesSaved())
					.setPenaltiesMissed(o.getPenaltiesMissed())
					.setYellowCards(o.getYellowCards())
					.setRedCards(o.getRedCards())
					.setSaves(o.getSaves())
					.setBps(o.getBps())
					.setTotalPoints(o.getTotalPoints());
			if (o.getBonus() > 0) {
				elementEventResultData
						.setBonus(o.getBonus())
						.setTotalPoints(elementEventResultData.getTotalPoints());
			} else {
				elementEventResultData
						.setBonus(liveBonusMap.getOrDefault(o.getElement(), 0))
						.setTotalPoints(elementEventResultData.getTotalPoints() + elementEventResultData.getBonus());
			}
			teamDataList.add(elementEventResultData);
		});
		data
				.setElementEventResulList(teamDataList
						.stream()
						.sorted(Comparator.comparing(ElementEventResultData::getTotalPoints)
								.thenComparing(ElementEventResultData::getBps).reversed())
						.collect(Collectors.toList())
				);
		return data;
	}

	private Map<Integer, String> getPlayerMap() {
		return this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
	}

	private Map<Integer, String> getPositionMap() {
		Map<Integer, String> map = Maps.newHashMap();
		this.queryService.getPositionMap().forEach((k, v) -> map.put(Integer.valueOf(k), v));
		return map;
	}

	private Map<Integer, Integer> getLiveBonusMap(int teamId) {
		Map<Integer, Integer> map = Maps.newHashMap();
		this.queryService.getLiveBonusCacheMap().forEach((team, list) -> {
			if (!StringUtils.equals(team, String.valueOf(teamId))) {
				return;
			}
			list.forEach((element, bonus) -> map.put(Integer.valueOf(element), bonus));
		});
		return map;
	}

	/**
	 * @apiNote entry_result
	 */
	@Cacheable(value = "qryEntryEventResult", key = "#event+'::'+#entry", unless = "#result == null")
	@Override
	public TableData<EntryPickData> qryEntryEventResult(int event, int entry) {
		if (event == 0 || entry == 0) {
			return new TableData<>();
		}
		String picks = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEvent, event)
				.eq(EntryEventResultEntity::getEntry, entry))
				.getEventPicks();
		if (StringUtils.isEmpty(picks)) {
			return new TableData<>();
		}
		List<EntryPickData> list = this.queryService.qryPickListFromPicks(picks)
				.stream()
				.sorted(Comparator.comparing(EntryPickData::getPosition))
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(list)) {
			return new TableData<>();
		}
		return new TableData<>(list);
	}

	@Cacheable(value = "qryEntryResultList", key = "#entry", unless = "#result==null")
	@Override
	public TableData<EntryEventResultData> qryEntryResultList(int entry) {
		if (entry <= 0) {
			return new TableData<>();
		}
		// entry_event_result
		List<EntryEventResultData> list = Lists.newArrayList();
		this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEntry, entry)
				.orderByAsc(EntryEventResultEntity::getEvent))
				.forEach(o -> list.add(new EntryEventResultData()
						.setEntry(o.getEntry())
						.setEvent(o.getEvent())
						.setPoints(o.getEventPoints())
						.setTransfers(o.getEventTransfers())
						.setTransfersCost(o.getEventTransfersCost())
						.setNetPoints(o.getEventNetPoints())
						.setBenchPoints(o.getEventBenchPoints())
						.setRank(o.getEventRank())
						.setChip(o.getEventChip())
				));
		return new TableData<>(list);
	}

	@Cacheable(value = "qryElementEventResult", key = "#event+'::'+#element", unless = "#result==null")
	@Override
	public TableData<ElementEventResultData> qryElementEventResult(int event, int element) {
		EventLiveEntity eventLiveEntity = this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event)
				.eq(EventLiveEntity::getElement, element));
		if (eventLiveEntity == null) {
			return new TableData<>();
		}
		return new TableData<>(BeanUtil.copyProperties(eventLiveEntity, ElementEventResultData.class));
	}

	/**
	 * @apiNote report
	 */
	@Cacheable(value = "qryTeamSelectStatByName", key = "#leagueName+'::'+#event", unless = "#result==null")
	@Override
	public TableData<LeagueStatData> qryTeamSelectStatByName(String leagueName, int event) {
		LeagueStatData leagueStatData = new LeagueStatData().setName(leagueName).setEvent(event);
		// player info
		Map<Integer, PlayerEntity> playerMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
		// team select
		List<LeagueEventReportEntity> leagueEventReportList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueName, leagueName)
				.eq(LeagueEventReportEntity::getEvent, event));
		int teamSize = leagueEventReportList.size();
		if (CollectionUtils.isEmpty(leagueEventReportList)) {
			return new TableData<>(leagueStatData);
		}
		// most transfer in
		LinkedHashMap<String, String> mostTransferInMap = this.getMostTransferInMap(leagueName, event, leagueEventReportList, teamSize, playerMap);
		leagueStatData.setMostTransferIn(mostTransferInMap);
		// most transfer out
		LinkedHashMap<String, String> mostTransferOutMap = this.getMostTransferOutMap(leagueName, event, leagueEventReportList, teamSize, playerMap);
		leagueStatData.setMostTransferOut(mostTransferOutMap);
		// captain selected
		LinkedHashMap<String, String> captainSelectedMap = this.getCaptainSelectedMap(leagueEventReportList, teamSize, playerMap);
		leagueStatData.setCaptainSelectedMap(captainSelectedMap);
		// vice captain selected
		LinkedHashMap<String, String> viceCaptainSelectedMap = this.getViceCaptainSelectedMap(leagueEventReportList, teamSize, playerMap);
		leagueStatData.setViceCaptainSelectedMap(viceCaptainSelectedMap);
		// top selected player
		LinkedHashMap<String, String> topSelectedPlayerMap = this.getTopSelectedPlayerMap(leagueEventReportList, teamSize, playerMap);
		leagueStatData.setTopSelectedPlayerMap(topSelectedPlayerMap);
		// top selected team
		LinkedHashMap<Integer, Map<String, String>> topSelectedTeamMap = this.getTopSelectedTeamMap(leagueEventReportList, teamSize, playerMap);
		leagueStatData.setTopSelectedTeamMap(topSelectedTeamMap);
		return new TableData<>(leagueStatData);
	}

	private LinkedHashMap<String, String> getMostTransferInMap(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
		if (event <= 1) {
			return Maps.newLinkedHashMap();
		}
		// current gw
		Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(leagueEventReportList);
		// previous gw
		Map<Integer, List<Integer>> previousSelectMap = this.collectPreviousEntrySelectedMap(leagueName, event);
		// different
		List<Integer> elementList = Lists.newArrayList();
		currentSelectMap.keySet().forEach(entry -> {
			List<Integer> currentList = currentSelectMap.get(entry);
			List<Integer> previousList = previousSelectMap.getOrDefault(entry, Lists.newArrayList());
			currentList
					.stream()
					.filter(o -> !previousList.contains(o))
					.forEach(elementList::add);
		});
		return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
	}

	private LinkedHashMap<String, String> getMostTransferOutMap(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
		if (event <= 1) {
			return Maps.newLinkedHashMap();
		}
		// current gw
		Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(leagueEventReportList);
		// previous gw
		Map<Integer, List<Integer>> previousSelectMap = this.collectPreviousEntrySelectedMap(leagueName, event);
		// different
		List<Integer> elementList = Lists.newArrayList();
		previousSelectMap.keySet().forEach(entry -> {
			List<Integer> previousList = previousSelectMap.get(entry);
			List<Integer> currentList = currentSelectMap.getOrDefault(entry, Lists.newArrayList());
			previousList
					.stream()
					.filter(o -> !currentList.contains(o))
					.forEach(elementList::add);
		});
		return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
	}

	private Map<Integer, List<Integer>> collectPreviousEntrySelectedMap(String leagueName, int event) {
		List<LeagueEventReportEntity> previousSelectList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueName, leagueName)
				.eq(LeagueEventReportEntity::getEvent, event - 1));
		return this.collectEntrySelectedMap(previousSelectList);
	}

	private Map<Integer, List<Integer>> collectEntrySelectedMap(List<LeagueEventReportEntity> leagueEventReportList) {
		Map<Integer, List<Integer>> teamSelectMap = Maps.newHashMap();
		leagueEventReportList.forEach(o -> {
			List<Integer> elementList = Lists.newArrayList(
					o.getPosition1(), o.getPosition2(), o.getPosition3(), o.getPosition4(), o.getPosition5(),
					o.getPosition6(), o.getPosition7(), o.getPosition8(), o.getPosition9(), o.getPosition10(),
					o.getPosition11(), o.getPosition12(), o.getPosition13(), o.getPosition14(), o.getPosition15()
			);
			teamSelectMap.put(o.getEntry(), elementList);
		});
		return teamSelectMap;
	}

	private LinkedHashMap<String, String> getCaptainSelectedMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
		List<Integer> elementList = leagueEventReportList
				.stream()
				.map(LeagueEventReportEntity::getCaptain)
				.collect(Collectors.toList());
		return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
	}

	private LinkedHashMap<String, String> getViceCaptainSelectedMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
		// collect
		List<Integer> elementList = leagueEventReportList
				.stream()
				.map(LeagueEventReportEntity::getViceCaptain)
				.collect(Collectors.toList());
		return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
	}

	private LinkedHashMap<String, String> getTopSelectedPlayerMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
		List<Integer> elementList = Lists.newArrayList();
		leagueEventReportList.forEach(o -> {
			elementList.add(o.getPosition1());
			elementList.add(o.getPosition2());
			elementList.add(o.getPosition3());
			elementList.add(o.getPosition4());
			elementList.add(o.getPosition5());
			elementList.add(o.getPosition6());
			elementList.add(o.getPosition7());
			elementList.add(o.getPosition8());
			elementList.add(o.getPosition9());
			elementList.add(o.getPosition10());
			elementList.add(o.getPosition11());
			elementList.add(o.getPosition12());
			elementList.add(o.getPosition13());
			elementList.add(o.getPosition14());
			elementList.add(o.getPosition15());
		});
		return this.collectSelectedMap(elementList, teamSize, 20, playerMap);
	}

	private LinkedHashMap<Integer, Map<String, String>> getTopSelectedTeamMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
		// element list
		List<PlayerEntity> elementPlayerInfoList = Lists.newArrayList();
		leagueEventReportList.forEach(o -> {
			elementPlayerInfoList.add(playerMap.get(o.getPosition1()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition2()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition3()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition4()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition5()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition6()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition7()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition8()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition9()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition10()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition11()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition12()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition13()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition14()));
			elementPlayerInfoList.add(playerMap.get(o.getPosition15()));
		});
		// collect
		Map<Integer, Map<Integer, Long>> elementTypeCountMap = elementPlayerInfoList
				.stream()
				.collect(Collectors.groupingBy(PlayerEntity::getElementType, Collectors.groupingBy(PlayerEntity::getElement, Collectors.counting())));
		// sort by element type
		Map<Integer, Integer> playerSelectedMap = Maps.newHashMap(); // key:element -> value: count
		elementTypeCountMap.keySet().forEach(elementType -> {
			Map<Integer, Integer> result = elementTypeCountMap.get(elementType).entrySet()
					.stream()
					.sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
					.limit(this.getLimitByElementType(elementType))
					.collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
			result.forEach(playerSelectedMap::put);
		});
		// add key:element_type
		Map<Integer, Map<Integer, Integer>> elementTypeMap = this.collectPlayerSelectedMap(playerSelectedMap, playerMap); // key:element_type -> value: elementCountMap
		// sort by selected
		LinkedHashMap<Integer, Integer> elementSelectedSortMap = playerSelectedMap.entrySet() // key:element -> value: count (sort by count)
				.stream()
				.sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
		List<PlayerEntity> elementList = Lists.newArrayList();
		elementSelectedSortMap.forEach((k, v) -> elementList.add(playerMap.get(k)));
		// selected line up
		LinkedHashMap<Integer, Map<String, String>> map = Maps.newLinkedHashMap(); // key:element_type -> value:elementCountMap(key:element -> value:percent)
		LinkedHashMap<Integer, Integer> lineupMap = this.getLineupMapByElementList(elementTypeMap, elementList); // key:position -> value:element
		lineupMap.forEach((position, element) -> {
			long count = playerSelectedMap.get(element);
			PlayerEntity playerEntity = playerMap.get(element);
			int elementType = playerEntity.getElementType();
			Map<String, String> valueMap = Maps.newHashMap();
			if (map.containsKey(elementType)) {
				valueMap = map.get(elementType);
			}
			valueMap.put(playerEntity.getWebName(), NumberUtil.decimalFormat("#.##%", NumberUtil.div(count, teamSize)));
			map.put(elementType, valueMap);
		});
		return map;
	}

	private Map<Integer, Map<Integer, Integer>> collectPlayerSelectedMap(Map<Integer, Integer> playerSelectedMap, Map<Integer, PlayerEntity> playerMap) {
		Map<Integer, Map<Integer, Integer>> map = Maps.newHashMap();
		playerSelectedMap.forEach((k, v) -> {
			int elementType = playerMap.get(k).getElementType();
			Map<Integer, Integer> valueMap = Maps.newHashMap();
			if (map.containsKey(elementType)) {
				valueMap = map.get(elementType);
			}
			valueMap.put(k, v);
			map.put(elementType, valueMap);
		});
		return map;
	}

	private LinkedHashMap<Integer, Integer> getLineupMapByElementList(Map<Integer, Map<Integer, Integer>> elementTypeMap, List<PlayerEntity> elementList) {
		// gkp
		List<Integer> gkpList = Lists.newArrayList();
		elementTypeMap.get(1).entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
				.forEachOrdered(o -> gkpList.add(o.getKey()));
		// def
		List<Integer> defList = Lists.newArrayList();
		elementTypeMap.get(2).entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
				.forEachOrdered(o -> defList.add(o.getKey()));
		// mid
		List<Integer> midList = Lists.newArrayList();
		elementTypeMap.get(3).entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
				.forEachOrdered(o -> midList.add(o.getKey()));
		// fwd
		List<Integer> fwdList = Lists.newArrayList();
		elementTypeMap.get(4).entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
				.forEachOrdered(o -> fwdList.add(o.getKey()));
		// line up
		Map<String, Integer> formationMap = this.getFormationMap(elementList);
		List<Integer> positionList = Lists.newArrayList();
		// gkp line up
		positionList.add(gkpList.get(0));
		// def line up
		int defStartIndex = 2;
		int defEndIndex = defStartIndex + formationMap.get("def");
		IntStream.range(defStartIndex, defEndIndex).forEach(index -> positionList.add(defList.get(index - defStartIndex)));
		// mid line up
		int midEndIndex = defEndIndex + formationMap.get("mid");
		IntStream.range(defEndIndex, midEndIndex).forEach(index -> positionList.add(midList.get(index - defEndIndex)));
		// fwd line up
		int fwdEndIndex = midEndIndex + formationMap.get("fwd");
		IntStream.range(midEndIndex, fwdEndIndex).forEach(index -> positionList.add(fwdList.get(index - midEndIndex)));
		// return
		LinkedHashMap<Integer, Integer> map = Maps.newLinkedHashMap();
		for (int i = 0; i < positionList.size(); i++) {
			map.put(i + 1, positionList.get(i));
		}
		return map;
	}

	private Map<String, Integer> getFormationMap(List<PlayerEntity> elementList) {
		int def = 0;
		int mid = 0;
		int fwd = 0;
		List<PlayerEntity> standbyList = Lists.newArrayList();
		for (PlayerEntity playerEntity : elementList) {
			int elementType = playerEntity.getElementType();
			switch (elementType) {
				case 2: {
					if (def < 3) {
						def++;
						break;
					}
				}
				case 4: {
					if (fwd < 1) {
						fwd++;
						break;
					}
				}
				default:
					standbyList.add(playerEntity);
			}
		}
		for (PlayerEntity playerEntity : standbyList) {
			if (def + mid + fwd >= 10) {
				break;
			}
			int elementType = playerEntity.getElementType();
			switch (elementType) {
				case 2: {
					def++;
					break;
				}
				case 3: {
					mid++;
					break;
				}
				case 4: {
					fwd++;
				}
			}
		}
		Map<String, Integer> map = Maps.newHashMap();
		map.put("def", def);
		map.put("mid", mid);
		map.put("fwd", fwd);
		return map;
	}

	private int getLimitByElementType(int elementType) {
		switch (elementType) {
			case 1:
				return 2;
			case 2:
			case 3:
				return 5;
			case 4:
				return 3;
		}
		return 0;
	}

	@Cacheable(value = "qryLeagueCaptainReportStat", key = "#leagueId+'::'+#leagueType", unless = "#result==null")
	@Override
	public TableData<LeagueEventReportStatData> qryLeagueCaptainReportStat(int leagueId, String leagueType) {
		// prepare
		Map<Integer, String> webNameMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
		List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueId, leagueId)
				.eq(LeagueEventReportEntity::getLeagueType, leagueType)
				.orderByAsc(LeagueEventReportEntity::getEvent));
		Multimap<Integer, LeagueEventReportEntity> entryEventReportMap = HashMultimap.create();
		leagueEventReportEntityList.forEach(o -> entryEventReportMap.put(o.getEntry(), o));
		if (entryEventReportMap.size() == 0) {
			return new TableData<>();
		}
		// collect
		List<LeagueEventReportStatData> list = Lists.newArrayList();
		entryEventReportMap.keySet().forEach(entry -> {
			LeagueEventReportStatData leagueEventReportStatData = this.qryEntryCaptainReportStat(entryEventReportMap.get(entry), webNameMap);
			if (leagueEventReportStatData != null) {
				list.add(leagueEventReportStatData);
			}
		});
		return new TableData<>(list);
	}

	private LeagueEventReportStatData qryEntryCaptainReportStat(Collection<LeagueEventReportEntity> leagueEventReportEntities, Map<Integer, String> webNameMap) {
		int size = leagueEventReportEntities.size();
		if (size == 0) {
			return null;
		}
		LeagueEventReportEntity leagueEventReportEntity = leagueEventReportEntities
				.stream()
				.max(Comparator.comparing(LeagueEventReportEntity::getEvent))
				.orElse(null);
		LeagueEventReportStatData leagueEventReportStatData = new LeagueEventReportStatData();
		BeanUtil.copyProperties(leagueEventReportEntity, leagueEventReportStatData, CopyOptions.create().ignoreNullValue());
		// stat
		int overallPoints = leagueEventReportStatData.getOverallPoints();
		int captainTotalPoints = this.calcCaptainTotalPoint(leagueEventReportEntities);
		LinkedHashMap<Integer, Long> captainSelectMap = Maps.newLinkedHashMap();
		leagueEventReportEntities
				.stream()
				.collect(Collectors.groupingBy(LeagueEventReportEntity::getCaptain, Collectors.counting())).entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
				.forEachOrdered(o -> captainSelectMap.put(o.getKey(), o.getValue()));
		int mostSelectedCaptain = captainSelectMap.keySet()
				.stream()
				.findFirst()
				.orElse(0);
		LeagueEventReportEntity captainMax = leagueEventReportEntities
				.stream()
				.max(Comparator.comparing(LeagueEventReportEntity::getCaptainPoints))
				.orElse(new LeagueEventReportEntity());
		LeagueEventReportEntity captainMin = leagueEventReportEntities
				.stream()
				.min(Comparator.comparing(LeagueEventReportEntity::getCaptainPoints))
				.orElse(new LeagueEventReportEntity());
		// data
		LeagueEventCaptainData leagueEventCaptainData = new LeagueEventCaptainData()
				.setTotalPoints(captainTotalPoints)
				.setTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(captainTotalPoints, overallPoints), 1))
				.setMaxPointsEvent(captainMax.getEvent())
				.setMaxPoints(captainMax.getCaptainPoints())
				.setMaxPointsWebName(webNameMap.getOrDefault(captainMax.getCaptain(), ""))
				.setMinPointsEvent(captainMin.getEvent())
				.setMinPoints(captainMin.getCaptainPoints())
				.setMinPointsWebName(webNameMap.getOrDefault(captainMin.getCaptain(), ""))
				.setMostSelected(mostSelectedCaptain)
				.setMostSelectedWebName(webNameMap.getOrDefault(mostSelectedCaptain, ""))
				.setMostSelectedtimes(captainSelectMap.get(mostSelectedCaptain).intValue())
				.setBlankTimes((int) leagueEventReportEntities
						.stream()
						.filter(LeagueEventReportEntity::getCaptainBlank)
						.count())
				.setHitTimes((int) leagueEventReportEntities
						.stream()
						.filter(o -> o.getCaptain().equals(o.getHighestScore()))
						.count());
		leagueEventReportStatData.setCaptainData(leagueEventCaptainData);
		return leagueEventReportStatData;
	}

	private int calcCaptainTotalPoint(Collection<LeagueEventReportEntity> leagueEventReportEntities) {
		return leagueEventReportEntities
				.stream()
				.filter(o -> !StringUtils.equals(o.getEventChip(), Chip.TC.getValue()))
				.mapToInt(o -> 2 * o.getCaptainPoints())
				.sum() +
				leagueEventReportEntities
						.stream()
						.filter(o -> StringUtils.equals(o.getEventChip(), Chip.TC.getValue()))
						.mapToInt(o -> 3 * o.getCaptainPoints())
						.sum();
	}

	@Cacheable(value = "qryLeagueCaptainEventReportList", key = "#leagueId+'::'+#leagueType+'::'+#event", unless = "#result==null")
	@Override
	public TableData<LeagueEventReportData> qryLeagueCaptainEventReportList(int leagueId, String leagueType, int event) {
		List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueId, leagueId)
				.eq(LeagueEventReportEntity::getLeagueType, leagueType)
				.eq(LeagueEventReportEntity::getEvent, event));
		if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
			return new TableData<>();
		}
		List<LeagueEventReportData> list = Lists.newArrayList();
		// prepare
		Map<Integer, String> webNameMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
		Map<Integer, String> captainCountMap = this.getLeagueEventCaptainCountMap(leagueEventReportEntityList);
		Map<Integer, String> viceCaptainCountMap = this.getLeagueEventViceCaptainCountMap(leagueEventReportEntityList);
		Map<Integer, String> highestScoreCountMap = this.getLeagueEventHighestScoreCountMap(leagueEventReportEntityList);
		// collect
		leagueEventReportEntityList.forEach(o -> {
			LeagueEventReportData data = new LeagueEventReportData();
			BeanUtil.copyProperties(o, data);
			data.setCaptainPoints(StringUtils.equals(Chip.TC.getValue(), o.getEventChip()) ? 3 * o.getCaptainPoints() : 2 * o.getCaptainPoints());
			data
					.setCaptainWebName(webNameMap.getOrDefault(o.getCaptain(), ""))
					.setCaptainSelectedInLeague(captainCountMap.getOrDefault(o.getCaptain(), "0"))
					.setCaptainPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getCaptainPoints(), data.getEventPoints()), 1))
					.setViceCaptainWebName(webNameMap.getOrDefault(o.getViceCaptain(), ""))
					.setViceCaptainSelectedInLeague(viceCaptainCountMap.getOrDefault(o.getViceCaptain(), "0"))
					.setViceCaptainPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getViceCaptainPoints(), data.getEventPoints()), 1))
					.setHighestScoreWebName(webNameMap.getOrDefault(o.getHighestScore(), ""))
					.setHighestScoreSelectedInLeague(highestScoreCountMap.getOrDefault(o.getHighestScore(), "0"))
					.setHighestScorePointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getHighestScorePoints(), data.getEventPoints()), 1));
			list.add(data);
		});
		return new TableData<>(list
				.stream()
				.sorted(Comparator.comparing(LeagueEventReportData::getCaptainPoints)
						.thenComparing(LeagueEventReportData::getEventPoints)
						.reversed())
				.collect(Collectors.toList())
		);
	}

	private Map<Integer, String> getLeagueEventCaptainCountMap(List<LeagueEventReportEntity> leagueEventReportEntityList) {
		int size = leagueEventReportEntityList.size();
		Map<Integer, Long> selectMap = leagueEventReportEntityList
				.stream()
				.collect(Collectors.groupingBy(LeagueEventReportEntity::getCaptain, Collectors.counting()));
		Map<Integer, String> map = Maps.newHashMap();
		selectMap.forEach((element, count) ->
				map.put(element, NumberUtil.formatPercent(NumberUtil.div(count.intValue(), size), 1)));
		return map;
	}

	private Map<Integer, String> getLeagueEventViceCaptainCountMap(List<LeagueEventReportEntity> leagueEventReportEntityList) {
		int size = leagueEventReportEntityList.size();
		Map<Integer, Long> selectMap = leagueEventReportEntityList
				.stream()
				.collect(Collectors.groupingBy(LeagueEventReportEntity::getViceCaptain, Collectors.counting()));
		Map<Integer, String> map = Maps.newHashMap();
		selectMap.forEach((element, count) ->
				map.put(element, NumberUtil.formatPercent(NumberUtil.div(count.intValue(), size), 1)));
		return map;
	}

	private Map<Integer, String> getLeagueEventHighestScoreCountMap(List<LeagueEventReportEntity> leagueEventReportEntityList) {
		int size = leagueEventReportEntityList.size();
		Map<Integer, Long> selectMap = leagueEventReportEntityList
				.stream()
				.collect(Collectors.groupingBy(LeagueEventReportEntity::getHighestScore, Collectors.counting()));
		Map<Integer, String> map = Maps.newHashMap();
		selectMap.forEach((element, count) ->
				map.put(element, NumberUtil.formatPercent(NumberUtil.div(count.intValue(), size), 1)));
		return map;
	}

	@Cacheable(value = "qryEntryCaptainEventReportList", key = "#leagueId+'::'+#leagueType+'::'+#entry", unless = "#result==null")
	@Override
	public TableData<LeagueEventReportData> qryEntryCaptainEventReportList(int leagueId, String leagueType, int entry) {
		List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueId, leagueId)
				.eq(LeagueEventReportEntity::getLeagueType, leagueType)
				.eq(LeagueEventReportEntity::getEntry, entry)
				.orderByAsc(LeagueEventReportEntity::getEvent));
		if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
			return new TableData<>();
		}
		List<LeagueEventReportData> list = Lists.newArrayList();
		// prepare
		Map<Integer, String> webNameMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
		Map<Integer, String> captainCountMap = this.getLeagueEventCaptainCountMap(leagueEventReportEntityList);
		Map<Integer, String> viceCaptainCountMap = this.getLeagueEventViceCaptainCountMap(leagueEventReportEntityList);
		Map<Integer, String> highestScoreCountMap = this.getLeagueEventHighestScoreCountMap(leagueEventReportEntityList);
		// collect
		leagueEventReportEntityList.forEach(o -> {
			LeagueEventReportData data = new LeagueEventReportData();
			BeanUtil.copyProperties(o, data);
			data.setCaptainPoints(StringUtils.equals(Chip.TC.getValue(), o.getEventChip()) ? 3 * o.getCaptainPoints() : 2 * o.getCaptainPoints());
			data
					.setCaptainWebName(webNameMap.getOrDefault(o.getCaptain(), ""))
					.setCaptainSelectedInLeague(captainCountMap.getOrDefault(o.getCaptain(), "0"))
					.setCaptainPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getCaptainPoints(), data.getEventPoints()), 1))
					.setViceCaptainWebName(webNameMap.getOrDefault(o.getViceCaptain(), ""))
					.setViceCaptainSelectedInLeague(viceCaptainCountMap.getOrDefault(o.getViceCaptain(), "0"))
					.setViceCaptainPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getViceCaptainPoints(), data.getEventPoints()), 1))
					.setHighestScoreWebName(webNameMap.getOrDefault(o.getHighestScore(), ""))
					.setHighestScoreSelectedInLeague(highestScoreCountMap.getOrDefault(o.getHighestScore(), "0"))
					.setHighestScorePointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getHighestScorePoints(), data.getEventPoints()), 1));
			list.add(data);
		});
		return new TableData<>(list);
	}

	@Cacheable(value = "qryLeagueTransferReportStat", key = "#leagueId+'::'+#leagueType", unless = "#result==null")
	@Override
	public TableData<LeagueEventReportStatData> qryLeagueTransferReportStat(int leagueId, String leagueType) {
		// prepare
		Table<Integer, Integer, Integer> elementPointsTable = HashBasedTable.create(); // element -> event -> points
		this.eventLiveService.list().forEach(o -> elementPointsTable.put(o.getElement(), o.getEvent(), o.getTotalPoints()));
		Multimap<Integer, EntryEventTransferEntity> entryEventTransferMap = HashMultimap.create();
		this.entryEventTransferService.list().forEach(o -> entryEventTransferMap.put(o.getEntry(), o));
		List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueId, leagueId)
				.eq(LeagueEventReportEntity::getLeagueType, leagueType)
				.orderByAsc(LeagueEventReportEntity::getEvent));
		Multimap<Integer, LeagueEventReportEntity> entryEventReportMap = HashMultimap.create();
		leagueEventReportEntityList.forEach(o -> entryEventReportMap.put(o.getEntry(), o));
		if (entryEventReportMap.size() == 0) {
			return new TableData<>();
		}
		// collect
		List<LeagueEventReportStatData> list = Lists.newArrayList();
		entryEventReportMap.keySet().forEach(entry -> {
			LeagueEventReportStatData leagueEventReportStatData = this.qryEntryTransferReportStat(entry, entryEventReportMap.get(entry), elementPointsTable, entryEventTransferMap);
			if (leagueEventReportStatData != null) {
				list.add(leagueEventReportStatData);
			}
		});
		return new TableData<>(list);
	}

	private LeagueEventReportStatData qryEntryTransferReportStat(int entry, Collection<LeagueEventReportEntity> leagueEventReportEntities, Table<Integer, Integer, Integer> elementPointsTable, Multimap<Integer, EntryEventTransferEntity> entryEventTransferMap) {
		int size = leagueEventReportEntities.size();
		if (size == 0) {
			return null;
		}
		if (!entryEventTransferMap.containsKey(entry)) {
			return null;
		}
		Collection<EntryEventTransferEntity> entryEventTransferEntities = entryEventTransferMap.get(entry);
		LeagueEventReportEntity leagueEventReportEntity = leagueEventReportEntities
				.stream()
				.max(Comparator.comparing(LeagueEventReportEntity::getEvent))
				.orElse(null);
		LeagueEventReportStatData leagueEventReportStatData = new LeagueEventReportStatData();
		BeanUtil.copyProperties(leagueEventReportEntity, leagueEventReportStatData, CopyOptions.create().ignoreNullValue());
		leagueEventReportStatData
				.setTransfers(
						leagueEventReportEntities
								.stream()
								.mapToInt(LeagueEventReportEntity::getEventTransfers)
								.sum()
				)
				.setTransfersCost(
						leagueEventReportEntities
								.stream()
								.mapToInt(LeagueEventReportEntity::getEventTransfersCost)
								.sum()
				);
		// data
		LeagueEventTransferData data = new LeagueEventTransferData()
				.setTransferInTotalPoints(this.sumSeasonElementPoints(elementPointsTable, entryEventTransferEntities, "in"))
				.setTransferOutTotalPoints(this.sumSeasonElementPoints(elementPointsTable, entryEventTransferEntities, "out"))
				.setTransferInTotalValue(
						entryEventTransferEntities
								.stream()
								.mapToInt(EntryEventTransferEntity::getElementInCost)
								.sum()
				)
				.setTransferOutTotalValue(
						entryEventTransferEntities
								.stream()
								.mapToInt(EntryEventTransferEntity::getElementOutCost)
								.sum()
				);
		data
				.setTransferPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints())
				.setTransferNetPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints() - leagueEventReportStatData.getTransfersCost())
				.setTransferValue(data.getTransferInTotalValue() - data.getTransferOutTotalValue());
		leagueEventReportStatData.setTransferData(data);
		return leagueEventReportStatData;
	}

	private int sumSeasonElementPoints(Table<Integer, Integer, Integer> elementPointsTable, Collection<EntryEventTransferEntity> entryEventTransferEntities, String type) {
		if (StringUtils.equals("in", type)) {
			return entryEventTransferEntities
					.stream()
					.mapToInt(o -> elementPointsTable.get(o.getElementIn(), o.getEvent()))
					.sum();
		} else if (StringUtils.equals("out", type)) {
			return entryEventTransferEntities
					.stream()
					.mapToInt(o -> elementPointsTable.get(o.getElementOut(), o.getEvent()))
					.sum();
		} else {
			return 0;
		}
	}

	@Cacheable(value = "qryLeagueTransferEventReportList", key = "#leagueId+'::'+#leagueType+'::'+#event", unless = "#result==null")
	@Override
	public TableData<LeagueEventReportData> qryLeagueTransferEventReportList(int leagueId, String leagueType, int event) {
		List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueId, leagueId)
				.eq(LeagueEventReportEntity::getLeagueType, leagueType)
				.eq(LeagueEventReportEntity::getEvent, event));
		if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
			return new TableData<>();
		}
		List<Integer> entryList = leagueEventReportEntityList
				.stream()
				.map(LeagueEventReportEntity::getEntry)
				.collect(Collectors.toList());
		List<LeagueEventReportData> leagueEventReportDataList = Lists.newArrayList();
		// prepare
		Map<Integer, PlayerEntity> playerMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
		Map<Integer, String> positionMap = Maps.newHashMap();
		this.queryService.getPositionMap().forEach((k, v) -> positionMap.put(Integer.valueOf(k), v));
		Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event))
				.stream()
				.collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
		Multimap<Integer, EntryEventTransferEntity> entryEventTransferMap = HashMultimap.create();
		this.entryEventTransferService.list(new QueryWrapper<EntryEventTransferEntity>().lambda()
				.eq(EntryEventTransferEntity::getEvent, event)
				.in(EntryEventTransferEntity::getEntry, entryList))
				.forEach(o -> entryEventTransferMap.put(o.getEntry(), o));
		// collect
		leagueEventReportEntityList.forEach(o -> {
			int entry = o.getEntry();
			if (!entryEventTransferMap.containsKey(entry)) {
				return;
			}
			LeagueEventReportData data = new LeagueEventReportData();
			BeanUtil.copyProperties(o, data);
			Collection<EntryEventTransferEntity> entryEventTransferEntities = entryEventTransferMap.get(entry);
			data
					.setTransferInTotalPoints(this.sumElementPoints(elementPointsMap,
							entryEventTransferEntities
									.stream()
									.map(EntryEventTransferEntity::getElementIn)
									.collect(Collectors.toList())
					))
					.setTransferOutTotalPoints(this.sumElementPoints(elementPointsMap,
							entryEventTransferEntities
									.stream()
									.map(EntryEventTransferEntity::getElementOut)
									.collect(Collectors.toList())
					))
					.setTransferInTotalValue(
							entryEventTransferEntities
									.stream()
									.mapToInt(EntryEventTransferEntity::getElementInCost)
									.sum()
					)

					.setTransferOutTotalValue(
							entryEventTransferEntities
									.stream()
									.mapToInt(EntryEventTransferEntity::getElementOutCost)
									.sum()
					);
			data
					.setTransferPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints())
					.setTransferNetPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints() - data.getEventTransfersCost())
					.setTransferValue(data.getTransferInTotalValue() - data.getTransferOutTotalValue())
					.setEntryEventTransferList(this.setEntryTransferList(entryEventTransferEntities, playerMap, positionMap, elementPointsMap));
			if (StringUtils.equals(data.getEventChip(), Chip.WC.getValue()) || StringUtils.equals(data.getEventChip(), Chip.FH.getValue())) {
				data
						.setEventTransfers(entryEventTransferEntities.size())
						.setTransferInTotalValue(0)
						.setTransferOutTotalValue(0)
						.setTransferValue(0);
			}
			leagueEventReportDataList.add(data);
		});
		List<LeagueEventReportData> list = leagueEventReportDataList
				.stream()
				.sorted(Comparator.comparing(LeagueEventReportData::getTransferNetPoints)
						.thenComparing(LeagueEventReportData::getEventPoints)
						.reversed())
				.collect(Collectors.toList());
		for (int i = 1; i < list.size() + 1; i++) {
			list.get(i - 1).setRank(i);
		}
		return new TableData<>(list);
	}

	private int sumElementPoints(Map<Integer, Integer> elementPointsMap, List<Integer> elementList) {
		return elementList
				.stream()
				.mapToInt(o -> elementPointsMap.getOrDefault(o, 0))
				.sum();
	}

	private List<EntryEventTransferData> setEntryTransferList(Collection<EntryEventTransferEntity> entryEventTransferEntities, Map<Integer, PlayerEntity> playerMap, Map<Integer, String> positionMap, Map<Integer, Integer> elementPointsMap) {
		List<EntryEventTransferData> list = Lists.newArrayList();
		entryEventTransferEntities.forEach(o -> {
			EntryEventTransferData entryEventTransferData = new EntryEventTransferData();
			entryEventTransferData
					.setEntry(o.getEntry())
					.setEvent(o.getEvent())
					.setElementIn(o.getElementIn())
					.setElementInCost(o.getElementInCost())
					.setElementInPoints(elementPointsMap.getOrDefault(o.getElementIn(), 0))
					.setElementOut(o.getElementOut())
					.setElementOutCost(o.getElementOutCost())
					.setElementOutPoints(elementPointsMap.getOrDefault(o.getElementOut(), 0))
					.setTime(LocalDateTime.parse(StringUtils.substringBefore(o.getTime(), ".")).format(DateTimeFormatter.ofPattern(Constant.DATETIME)));
			int elementIn = o.getElementIn();
			if (playerMap.containsKey(elementIn)) {
				PlayerEntity elementInEntity = playerMap.get(elementIn);
				entryEventTransferData
						.setElementInName(elementInEntity.getWebName())
						.setElementInType(elementInEntity.getElementType())
						.setElementInTypeName(positionMap.getOrDefault(elementInEntity.getElementType(), ""));
			}
			int elementOut = o.getElementOut();
			if (playerMap.containsKey(elementOut)) {
				PlayerEntity elementOutEntity = playerMap.get(elementOut);
				entryEventTransferData
						.setElementOutName(elementOutEntity.getWebName())
						.setElementOutType(elementOutEntity.getElementType())
						.setElementOutTypeName(positionMap.getOrDefault(elementOutEntity.getElementType(), ""));
			}
			list.add(entryEventTransferData);
		});
		return list
				.stream()
				.sorted(Comparator.comparing(EntryEventTransferData::getTime))
				.collect(Collectors.toList());
	}

	@Cacheable(value = "qryEntryTransferEventReportList", key = "#leagueId+'::'+#leagueType+'::'+#entry", unless = "#result==null")
	@Override
	public TableData<LeagueEventReportData> qryEntryTransferEventReportList(int leagueId, String leagueType, int entry) {
		List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueId, leagueId)
				.eq(LeagueEventReportEntity::getLeagueType, leagueType)
				.eq(LeagueEventReportEntity::getEntry, entry)
				.orderByAsc(LeagueEventReportEntity::getEvent));
		if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
			return new TableData<>();
		}
		List<LeagueEventReportData> list = Lists.newArrayList();
		// prepare
		Map<Integer, PlayerEntity> playerMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
		Map<Integer, String> positionMap = Maps.newHashMap();
		this.queryService.getPositionMap().forEach((k, v) -> positionMap.put(Integer.valueOf(k), v));
		Multimap<Integer, EntryEventTransferEntity> entryEventTransferMap = HashMultimap.create();
		this.entryEventTransferService.list(new QueryWrapper<EntryEventTransferEntity>().lambda()
				.eq(EntryEventTransferEntity::getEntry, entry))
				.forEach(o -> entryEventTransferMap.put(o.getEvent(), o));
		// collect
		leagueEventReportEntityList.forEach(o -> {
			int event = o.getEvent();
			Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
					.eq(EventLiveEntity::getEvent, event))
					.stream()
					.collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
			if (!entryEventTransferMap.containsKey(event)) {
				return;
			}
			LeagueEventReportData data = new LeagueEventReportData();
			BeanUtil.copyProperties(o, data);
			Collection<EntryEventTransferEntity> entryEventTransferEntities = entryEventTransferMap.get(event);
			data
					.setTransferInTotalPoints(this.sumElementPoints(elementPointsMap,
							entryEventTransferEntities
									.stream()
									.map(EntryEventTransferEntity::getElementIn)
									.collect(Collectors.toList())
					))
					.setTransferOutTotalPoints(this.sumElementPoints(elementPointsMap,
							entryEventTransferEntities
									.stream()
									.map(EntryEventTransferEntity::getElementOut)
									.collect(Collectors.toList())
					))
					.setTransferInTotalValue(
							entryEventTransferEntities
									.stream()
									.mapToInt(EntryEventTransferEntity::getElementInCost)
									.sum()
					)

					.setTransferOutTotalValue(
							entryEventTransferEntities
									.stream()
									.mapToInt(EntryEventTransferEntity::getElementOutCost)
									.sum()
					);
			data
					.setTransferPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints())
					.setTransferNetPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints() - data.getEventTransfersCost())
					.setTransferValue(data.getTransferInTotalValue() - data.getTransferOutTotalValue())
					.setEntryEventTransferList(this.setEntryTransferList(entryEventTransferEntities, playerMap, positionMap, elementPointsMap));
			if (StringUtils.equals(data.getEventChip(), Chip.WC.getValue()) || StringUtils.equals(data.getEventChip(), Chip.FH.getValue())) {
				data
						.setEventTransfers(entryEventTransferEntities.size())
						.setTransferInTotalValue(0)
						.setTransferOutTotalValue(0)
						.setTransferValue(0);
			}
			list.add(data);
		});
		return new TableData<>(list);
	}

	private LinkedHashMap<String, String> collectSelectedMap(List<Integer> elementList, int teamSize, int limit, Map<Integer, PlayerEntity> playerMap) {
		LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
		Map<Integer, Long> groupingMap = elementList
				.stream()
				.collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
		Map<Integer, Integer> result = groupingMap.entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
				.limit(limit)
				.collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
		result.forEach((k, v) ->
				map.put(playerMap.get(k).getWebName(), NumberUtil.decimalFormat("#.##%", NumberUtil.div(v.intValue(), teamSize))));
		return map;
	}

	private boolean setSearchTotal(long current) {
		return current == 1;
	}

	@Cacheable(value = "qryEventScoutPickList", key = "#event")
	@Override
	public TableData<ScoutData> qryEventScoutPickList(int event) {
		List<ScoutData> list = Lists.newArrayList();
		this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
				.eq(ScoutEntity::getEvent, event))
				.forEach(o ->
						list.add(new ScoutData()
								.setEvent(event)
								.setEntry(o.getEntry())
								.setScoutName(o.getScoutName())
								.setGkpName(this.queryService.qryPlayerWebNameByElement(o.getGkp()))
								.setDefName(this.queryService.qryPlayerWebNameByElement(o.getDef()))
								.setMidName(this.queryService.qryPlayerWebNameByElement(o.getMid()))
								.setFwdName(this.queryService.qryPlayerWebNameByElement(o.getFwd()))
								.setCaptainName(this.queryService.qryPlayerWebNameByElement(o.getCaptain()))
								.setReason(o.getReason())
						));
		return new TableData<>(list);
	}

	@Cacheable(value = "qryEventScoutList", key = "#event")
	@Override
	public TableData<ScoutData> qryEventScoutList(int event) {
		if (event == 0) {
			return this.qrySeasonScoutList();
		}
		List<ScoutData> list = Lists.newArrayList();
		this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
				.eq(ScoutEntity::getEvent, event))
				.forEach(o ->
						list.add(new ScoutData()
								.setEvent(event)
								.setEntry(o.getEntry())
								.setScoutName(o.getScoutName())
								.setGkpName(this.queryService.qryPlayerWebNameByElement(o.getGkp()))
								.setGkpPoints(o.getGkpPoints())
								.setDefName(this.queryService.qryPlayerWebNameByElement(o.getDef()))
								.setDefPoints(o.getDefPoints())
								.setMidName(this.queryService.qryPlayerWebNameByElement(o.getMid()))
								.setMidPoints(o.getMidPoints())
								.setFwdName(this.queryService.qryPlayerWebNameByElement(o.getFwd()))
								.setFwdPoints(o.getFwdPoints())
								.setCaptainName(this.queryService.qryPlayerWebNameByElement(o.getCaptain()))
								.setCaptainPoints(o.getCaptainPoints())
								.setEventPoints(o.getEventPoints())
								.setTotalPoints(o.getTotalPoints())
						));
		return new TableData<>(list);
	}

	private TableData<ScoutData> qrySeasonScoutList() {
		Multimap<Integer, ScoutEntity> scoutMap = HashMultimap.create();
		this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
				.gt(ScoutEntity::getEventPoints, 0))
				.forEach(o -> scoutMap.put(o.getEntry(), o));
		List<ScoutData> list = Lists.newArrayList();
		scoutMap.keySet().forEach(entry -> {
			Collection<ScoutEntity> scoutEntities = scoutMap.get(entry);
			ScoutEntity last = scoutEntities
					.stream()
					.max(Comparator.comparing(ScoutEntity::getEvent))
					.orElse(new ScoutEntity());
			ScoutData scoutData = new ScoutData()
					.setEntry(entry)
					.setScoutName(last.getScoutName())
					.setGkpPoints(
							scoutEntities
									.stream()
									.mapToInt(ScoutEntity::getGkpPoints)
									.sum()
					)
					.setDefPoints(
							scoutEntities
									.stream()
									.mapToInt(ScoutEntity::getDefPoints)
									.sum()
					)
					.setMidPoints(
							scoutEntities
									.stream()
									.mapToInt(ScoutEntity::getMidPoints)
									.sum()
					)
					.setFwdPoints(
							scoutEntities
									.stream()
									.mapToInt(ScoutEntity::getFwdPoints)
									.sum()
					)
					.setCaptainPoints(
							scoutEntities
									.stream()
									.mapToInt(ScoutEntity::getCaptainPoints)
									.sum()
					)
					.setTotalPoints(last.getTotalPoints());
			list.add(scoutData);
		});
		return new TableData<>(list);
	}

}
