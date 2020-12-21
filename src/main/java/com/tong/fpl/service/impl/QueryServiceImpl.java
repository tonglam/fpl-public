package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.BattleGroupResultCollector;
import com.tong.fpl.config.collector.KnockoutResultCollector;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.*;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.TransferRes;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.IStaticService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/31
 */
@Slf4j
@Valid
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueryServiceImpl implements IQueryService {

	private final IRedisCacheService redisCacheService;
	private final IStaticService staticService;
	private final PlayerService playerService;
	private final EntryInfoService entryInfoService;
	private final EventLiveService eventLiveService;
	private final EntryEventResultService entryEventResultService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentEntryService tournamentEntryService;
	private final TournamentGroupService tournamentGroupService;
	private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
	private final TournamentKnockoutService tournamentKnockoutService;
	private final TournamentKnockoutResultService tournamentKnockoutResultService;
	private final ZjTournamentCaptainService zjTournamentCaptainService;
	private final LeagueEventReportService leagueEventReportService;
	private final ScoutService scoutService;

	/**
	 * @implNote player
	 */
	@Override
	public int qryPlayerElementByCode(String season, int code) {
		MybatisPlusConfig.season.set(season);
		PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getCode, code));
		MybatisPlusConfig.season.remove();
		return playerEntity == null ? 0 : playerEntity.getElement();
	}

	@Override
	public int qryPlayerElementByWebName(String season, String webName) throws Exception {
		MybatisPlusConfig.season.set(season);
		List<PlayerEntity> playerList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getWebName, webName));
		MybatisPlusConfig.season.remove();
		if (CollectionUtils.isEmpty(playerList)) {
			return 0;
		} else if (playerList.size() > 1) {
			throw new Exception("webName不止一个球员，请用element或code查询!");
		}
		return playerList.get(0) == null ? 0 : playerList.get(0).getElement();
	}

	@Override
	public String qryPlayerWebNameByElement(String season, int element) {
		MybatisPlusConfig.season.set(season);
		PlayerEntity playerEntity = this.playerService.getById(element);
		MybatisPlusConfig.season.remove();
		return playerEntity == null ? "" : playerEntity.getWebName();
	}

	@Cacheable(value = "qryPlayerData", key = "#element", cacheManager = "apiCacheManager", unless = "#result == null")
	@Override
	public PlayerData qryPlayerData(int element) {
		PlayerEntity playerEntity = this.getPlayerByElement(element);
		if (playerEntity == null) {
			return null;
		}
		PlayerData playerData = new PlayerData();
		// info
		playerData.setInfoData(this.initPlayerInfo(CommonUtils.getCurrentSeason(), playerEntity));
		// fixture, next 5 gw
		playerData.setFixtureDataList(this.qryPlayerFixture(playerEntity.getTeamId()));
		// current season data
		playerData.setCurrentSeason(this.qrySeasonData(CommonUtils.getCurrentSeason(), playerEntity.getCode()));
		// history season data（use code as unique index）
		playerData.setHistorySeasonList(this.qryHistorySeasonData(playerEntity.getCode()));
		return playerData;
	}

	@Cacheable(value = "initPlayerInfo", key = "#playerEntity.element", condition = "#playerEntity.element gt 0", unless = "#result == null")
	@Override
	public PlayerInfoData initPlayerInfo(String season, PlayerEntity playerEntity) {
		Map<String, String> teamNameMap = this.getTeamNameMap(season);
		Map<String, String> teamShortNameMap = this.getTeamShortNameMap(season);
		Map<String, String> positionMap = this.getPositionMap();
		return new PlayerInfoData()
				.setElement(playerEntity.getElement())
				.setCode(playerEntity.getCode())
				.setWebName(playerEntity.getWebName())
				.setElementType(playerEntity.getElementType())
				.setElementTypeName(positionMap.get(String.valueOf(playerEntity.getElementType())))
				.setTeamId(playerEntity.getTeamId())
				.setTeamName(teamNameMap.get(String.valueOf(playerEntity.getTeamId())))
				.setTeamShortName(teamShortNameMap.get(String.valueOf(playerEntity.getTeamId())))
				.setPrice(NumberUtil.div(playerEntity.getPrice().intValue(), 10, 2));
	}

	@Cacheable(value = "setPlayerFixture", key = "#teamId", unless = "#result == null")
	@Override
	public List<PlayerFixtureData> qryPlayerFixture(int teamId) {
		List<PlayerFixtureData> playerFixtureList = Lists.newArrayList();
		int currentEvent = this.getCurrentEvent();
		Map<String, String> teamNameMap = this.getTeamNameMap();
		Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
		Map<String, List<PlayerFixtureData>> teamFixtureMap = this.getEventFixtureByTeamId(teamId);
		int lastEvent = currentEvent + 3;
		if (lastEvent > 38) {
			lastEvent = 38;
		}
		List<PlayerFixtureData> teamFixtureList = Lists.newArrayList();
		IntStream.rangeClosed(currentEvent - 1, lastEvent).forEach(event -> {
			if (teamFixtureMap.containsKey(String.valueOf(event))) {
				if (CollectionUtils.isEmpty(teamFixtureMap.get(String.valueOf(event)))) {
					teamFixtureList.add(new PlayerFixtureData()
							.setAgainstTeamId(0)
							.setAgainstTeamName("BLANK")
							.setAgainstTeamShortName("BLANK")
					);
				} else {
					teamFixtureList.addAll(teamFixtureMap.get(String.valueOf(event)));
				}
			}
		});
		teamFixtureList.forEach(o -> {
					o.setAgainstTeamName(teamNameMap.get(String.valueOf(o.getAgainstTeamId())));
					o.setAgainstTeamShortName(teamShortNameMap.get(String.valueOf(o.getAgainstTeamId())));
					playerFixtureList.add(o);
				}
		);
		return playerFixtureList;
	}

	@Cacheable(value = "setSeasonData", key = "#season+'::'+#code", unless = "#result == null")
	@Override
	public PlayerDetailData qrySeasonData(String season, int code) {
		int element = this.qryPlayerElementByCode(season, code);
		PlayerDetailData playerDetailData = new PlayerDetailData().setSeason(season);
		PlayerStatEntity playerStatEntity = this.getPlayerStatByElement(season, element);
		if (playerStatEntity == null) {
			return playerDetailData;
		}
		BeanUtil.copyProperties(playerStatEntity, playerDetailData, CopyOptions.create().ignoreNullValue());
		return playerDetailData;
	}

	@Cacheable(value = "setHistorySeasonData", key = "#code", unless = "#result == null")
	@Override
	public List<PlayerDetailData> qryHistorySeasonData(int code) {
		List<PlayerDetailData> historySeasonList = Lists.newArrayList();
		Arrays.stream(HistorySeason.values()).forEach(o ->
				historySeasonList.add(this.qrySeasonData(o.getSeason(), code)));
		return historySeasonList;
	}

	@Cacheable(value = "qryAllPlayers", key = "#season", cacheManager = "apiCacheManager", unless = "#result == null")
	@Override
	public List<PlayerInfoData> qryAllPlayers(String season) {
		List<PlayerInfoData> list = Lists.newArrayList();
		this.playerService.list().forEach(o -> list.add(this.initPlayerInfo(season, o)));
		return list;
	}

	@Cacheable(value = "getPlayerByElement", key = "#season+'::'+#element", unless = "#result == null")
	@Override
	public PlayerEntity getPlayerByElement(String season, int element) {
		return this.redisCacheService.getPlayerByElememt(season, element);
	}

	@Cacheable(value = "getPlayerStatByElement", key = "#season+'::'+#element", unless = "#result == null")
	@Override
	public PlayerStatEntity getPlayerStatByElement(String season, int element) {
		return this.redisCacheService.getPlayerStatByElement(season, element);
	}

	@Cacheable(value = "getPlayerValueByChangeDay", key = "#changeDay", unless = "#result == null")
	@Override
	public List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay) {
		return this.redisCacheService.getPlayerValueByChangeDay(changeDay);
	}

	@Cacheable(value = "qryPlayerShowData", key = "#element")
	@Override
	public PlayerShowData qryPlayerShowData(int element, Map<String, String> positionMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap,
	                                        Map<Integer, PlayerEntity> playerMap, Multimap<Integer, EventLiveEntity> eventLiveMap) {
		PlayerShowData data = new PlayerShowData().setElement(element);
		// prepare
		if (CollectionUtils.isEmpty(positionMap)) {
			positionMap = this.getPositionMap();
		}
		if (CollectionUtils.isEmpty(teamNameMap)) {
			teamNameMap = this.getTeamNameMap();
		}
		if (CollectionUtils.isEmpty(teamShortNameMap)) {
			teamShortNameMap = this.getTeamShortNameMap();
		}
		if (eventLiveMap.size() == 0) {
			this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
					.eq(EventLiveEntity::getElement, element))
					.forEach(o -> eventLiveMap.put(o.getElement(), o));
		}
		// player
		PlayerEntity playerEntity = playerMap.getOrDefault(element, null);
		if (playerEntity == null) {
			playerEntity = this.getPlayerByElement(element);
			if (playerEntity == null) {
				return data;
			}
		}
		int teamId = playerEntity.getTeamId();
		data
				.setElementType(playerEntity.getElementType())
				.setElementTypeName(positionMap.getOrDefault(String.valueOf(playerEntity.getElementType()), ""))
				.setWebName(playerEntity.getWebName())
				.setTeamId(teamId)
				.setTeamName(teamNameMap.getOrDefault(String.valueOf(teamId), ""))
				.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), ""))
				.setPrice(playerEntity.getPrice() / 10.0);
		// fixture
		List<PlayerFixtureData> fixtureDataList = this.qryPlayerFixture(teamId);
		if (fixtureDataList.size() > 3) {
			fixtureDataList = fixtureDataList
					.stream()
					.skip(2)
					.collect(Collectors.toList());
			PlayerFixtureData fixture1 = fixtureDataList.get(0);
			data
					.setFixtureEvent1(fixture1.getEvent())
					.setAgainstTeam1ShortName(fixture1.getAgainstTeamShortName())
					.setDifficulty1(fixture1.getDifficulty())
					.setWasHome1(fixture1.isWasHome());
			if (fixtureDataList.size() > 1) {
				PlayerFixtureData fixture2 = fixtureDataList.get(1);
				data
						.setFixtureEvent2(fixture2.getEvent())
						.setAgainstTeam2ShortName(fixture2.getAgainstTeamShortName())
						.setDifficulty2(fixture2.getDifficulty())
						.setWasHome2(fixture2.isWasHome());
			}
			if (fixtureDataList.size() > 2) {
				PlayerFixtureData fixture3 = fixtureDataList.get(2);
				data
						.setFixtureEvent3(fixture3.getEvent())
						.setAgainstTeam3ShortName(fixture3.getAgainstTeamShortName())
						.setDifficulty3(fixture3.getDifficulty())
						.setWasHome3(fixture3.isWasHome());
			}
		}
		// event_live
		if (eventLiveMap.containsKey(element)) {
			data.setTotalPoints(eventLiveMap.get(element)
					.stream()
					.mapToInt(EventLiveEntity::getTotalPoints)
					.sum()
			);
		}
		// player_stat
		PlayerStatEntity playerStatEntity = this.getPlayerStatByElement(element);
		if (playerStatEntity != null) {
			data
					.setSelectedByPercent(playerStatEntity.getSelectedByPercent())
					.setPointsPerGame(playerStatEntity.getPointsPerGame());
		}
		return data;
	}

	/**
	 * @implNote entry
	 */
	@Cacheable(value = "qryEntryInfo", key = "#season+'::'+#entry", unless = "#result == null")
	@Override
	public EntryInfoEntity qryEntryInfo(String season, int entry) {
		if (entry <= 0) {
			return new EntryInfoEntity();
		}
		MybatisPlusConfig.season.set(season);
		EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
		MybatisPlusConfig.season.remove();
		if (entryInfoEntity != null) {
			return entryInfoEntity;
		}
		if (!StringUtils.equals(CommonUtils.getCurrentSeason(), season)) {
			return new EntryInfoEntity();
		}
		EntryRes entryRes = this.getEntry(entry);
		if (entryRes == null) {
			return new EntryInfoEntity();
		}
		return new EntryInfoEntity()
				.setEntry(entryRes.getId())
				.setEntryName(entryRes.getName())
				.setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName())
				.setRegion(entryRes.getPlayerRegionName())
				.setStartedEvent(entryRes.getStartedEvent())
				.setOverallPoints(entryRes.getSummaryOverallPoints())
				.setOverallRank(entryRes.getSummaryOverallRank())
				.setBank(entryRes.getLastDeadlineBank())
				.setTeamValue(entryRes.getLastDeadlineValue())
				.setTotalTransfers(entryRes.getLastDeadlineTotalTransfers());
	}

	@Cacheable(value = "getEntry", key = "#entry", unless = "#result == null")
	@Override
	public EntryRes getEntry(int entry) {
		return this.staticService.getEntry(entry).orElse(null);
	}

	@Cacheable(value = "getUserPicks", key = "#event+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result == null")
	@Override
	public UserPicksRes getUserPicks(int event, int entry) {
		return this.staticService.getUserPicks(event, entry).orElse(null);
	}

	@Cacheable(value = "getUserHistory", key = "#entry", cacheManager = "apiCacheManager", unless = "#result == null")
	@Override
	public UserHistoryRes getUserHistory(int entry) {
		return this.staticService.getUserHistory(entry).orElse(null);
	}

	@Override
	public List<Integer> qryEntryTournamentList(int entry) {
		return this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
				.eq(TournamentEntryEntity::getEntry, entry))
				.stream()
				.map(TournamentEntryEntity::getTournamentId)
				.collect(Collectors.toList());
	}

	@Cacheable(value = "getTransfer", key = "#entry", cacheManager = "apiCacheManager", unless = "#result == null")
	@Override
	public List<TransferRes> getTransfer(int entry) {
		return this.staticService.getTransfer(entry).orElse(null);
	}

	/**
	 * @implNote event
	 */
	@Override
	public int getCurrentEvent() {
		int event = 1;
		for (int i = 1; i < 39; i++) {
			String deadline = this.getDeadlineByEvent(i);
			if (LocalDateTime.now().isAfter(LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
				event = i;
			} else {
				break;
			}
		}
		return event;
	}

	@Override
	public int getLastEvent() {
		return this.getCurrentEvent() - 1;
	}

	@Override
	public int getNextEvent() {
		return this.getCurrentEvent() + 1;
	}

	@Cacheable(value = "getDeadlineByEvent", key = "#season+'::'+#event", unless = "#result == null")
	@Override
	public String getDeadlineByEvent(String season, int event) {
		return this.redisCacheService.getDeadlineByEvent(season, event);
	}

	@Cacheable(value = "getMatchDayByEvent", key = "#event", unless = "#result == null")
	@Override
	public List<LocalDate> getMatchDayByEvent(int event) {
		List<LocalDate> matchDayList = Lists.newArrayList();
		this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event).forEach(eventFixtureEntity -> {
					String matchDay = StringUtils.substringBefore(eventFixtureEntity.getKickoffTime(), " ");
					LocalDate date = LocalDate.parse(matchDay);
					if (!matchDayList.contains(date)) {
						matchDayList.add(date);
					}
				}
		);
		return matchDayList
				.stream()
				.sorted(LocalDate::compareTo)
				.collect(Collectors.toList());
	}

	@Cacheable(value = "getMatchDayTimeByEvent", key = "#event", unless = "#result == null")
	@Override
	public List<LocalDateTime> getMatchDayTimeByEvent(int event) {
		List<LocalDateTime> matchDayTimeList = Lists.newArrayList();
		this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event).forEach(eventFixtureEntity -> {
					String kickoffTime = eventFixtureEntity.getKickoffTime().replace(" ", "T");
					LocalDateTime dateTime = LocalDateTime.parse(kickoffTime);
					if (!matchDayTimeList.contains(dateTime)) {
						matchDayTimeList.add(dateTime);
					}
				}
		);
		return matchDayTimeList
				.stream()
				.sorted(LocalDateTime::compareTo)
				.collect(Collectors.toList());
	}

	@Override
	public boolean isMatchDay(int event) {
		List<LocalDate> matchDayList = this.getMatchDayByEvent(event);
		return matchDayList.contains(LocalDate.now());
	}

	@Override
	public boolean isMatchDayTime(int event) {
		List<LocalDateTime> matchDayTimeList = this.getMatchDayTimeByEvent(event);
		LocalDateTime start = matchDayTimeList.stream().min(LocalDateTime::compareTo).orElse(null);
		LocalDateTime last = matchDayTimeList.stream().max(LocalDateTime::compareTo).orElse(null);
		if (start == null) {
			return false;
		}
		return LocalDateTime.now().isAfter(start) && LocalDateTime.now().minusHours(2).isBefore(last);
	}

	@Override
	public boolean isLastMatchDay(int event) {
		LocalDate eventLastDay = this.getMatchDayByEvent(event)
				.stream()
				.max(LocalDate::compareTo)
				.orElse(null);
		if (eventLastDay == null) {
			return false;
		}
		return eventLastDay.isEqual(LocalDate.now());
	}

	/**
	 * @implNote team
	 */
	@Cacheable(value = "getTeamNameMap", key = "#season", unless = "#result == null")
	@Override
	public Map<String, String> getTeamNameMap(String season) {
		return this.redisCacheService.getTeamNameMap(season);
	}

	@Cacheable(value = "getTeamShortNameMap", key = "#season", unless = "#result == null")
	@Override
	public Map<String, String> getTeamShortNameMap(String season) {
		return this.redisCacheService.getTeamShortNameMap(season);
	}

	@Cacheable(value = "getPositionMap")
	@Override
	public Map<String, String> getPositionMap() {
		return this.redisCacheService.getPositionMap();
	}

	/**
	 * @apiNote fixture
	 */
	@Cacheable(value = "getEventFixtureByEvent", key = "#season+'::'+#event", unless = "#result == null")
	@Override
	public List<EventFixtureEntity> getEventFixtureByEvent(String season, int event) {
		return this.redisCacheService.getEventFixtureByEvent(season, event);
	}

	@Cacheable(value = "getEventFixtureByTeamId", key = "#season+'::'+#teamId", unless = "#result == null")
	@Override
	public Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId) {
		return this.redisCacheService.getEventFixtureByTeamId(season, teamId);
	}

	@Cacheable(value = "qryGroupFixtureListById", key = "#tournamentId", unless = "#result.size() eq 0")
	@Override
	public List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId) {
		List<TournamentGroupFixtureData> list = Lists.newArrayList();
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return list;
		}
		if (!StringUtils.equals(tournamentInfoEntity.getGroupMode(), GroupMode.Battle_race.name())) {
			return list;
		}
		int currentGw = this.getCurrentEvent();
		List<TournamentBattleGroupResultEntity> battleGroupResultEntityList = this.tournamentBattleGroupResultService.list(new QueryWrapper<TournamentBattleGroupResultEntity>().lambda()
				.eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId)
				.orderByAsc(TournamentBattleGroupResultEntity::getEvent)
				.orderByAsc(TournamentBattleGroupResultEntity::getGroupId));
		// entryInfo map
		List<Integer> entryList = battleGroupResultEntityList
				.stream()
				.map(TournamentBattleGroupResultEntity::getHomeEntry)
				.collect(Collectors.toList());
		entryList.addAll(battleGroupResultEntityList
				.stream()
				.map(TournamentBattleGroupResultEntity::getAwayEntry)
				.collect(Collectors.toList()));
		if (CollectionUtils.isEmpty(entryList)) {
			return list;
		}
		Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
				.in(EntryInfoEntity::getEntry, entryList))
				.stream()
				.collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
		// return list
		list = battleGroupResultEntityList
				.stream()
				.collect(new BattleGroupResultCollector());
		list.forEach(o -> {
			o.setTournamentId(tournamentId);
			o.getGroupEventFixtureList()
					.forEach(i -> i.getEventEntryFixtureList()
							.forEach(tournamentGroupFixtureData ->
									tournamentGroupFixtureData.setShowMessage(this.setBattleFixtureMsg(currentGw, o.getEvent(), entryInfoMap,
											tournamentGroupFixtureData.getHomeEntry(), tournamentGroupFixtureData.getAwayEntry(),
											tournamentGroupFixtureData.getHomeEntryNetPoints(), tournamentGroupFixtureData.getAwayEntryNetPoints()))
							));
		});
		return list;
	}

	@Cacheable(value = "qryKnockoutFixtureListById", key = "#tournamentId", unless = "#result == null")
	@Override
	public List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId) {
		List<TournamentKnockoutFixtureData> list = Lists.newArrayList();
		int currentGw = this.getCurrentEvent();
		List<TournamentKnockoutResultEntity> knockoutResultEntityList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
				.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
				.orderByAsc(TournamentKnockoutResultEntity::getEvent)
				.orderByAsc(TournamentKnockoutResultEntity::getMatchId));
		if (CollectionUtils.isEmpty(knockoutResultEntityList)) {
			return list;
		}
		// entryInfo map
		List<Integer> entryList = knockoutResultEntityList
				.stream()
				.map(TournamentKnockoutResultEntity::getHomeEntry)
				.collect(Collectors.toList());
		entryList.addAll(knockoutResultEntityList
				.stream()
				.map(TournamentKnockoutResultEntity::getAwayEntry)
				.collect(Collectors.toList()));
		if (CollectionUtils.isEmpty(entryList)) {
			return list;
		}
		Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
				.in(EntryInfoEntity::getEntry, entryList))
				.stream()
				.collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
		// return list
		List<TournamentKnockoutFixtureData> knockoutFixtureList = knockoutResultEntityList
				.stream()
				.collect(new KnockoutResultCollector());
		knockoutFixtureList
				.stream()
				.filter(o -> o.getEvent() <= currentGw)
				.forEach(o -> {
					o.setTournamentId(tournamentId);
					o.getKnockoutEventFixtureList()
							.forEach(i -> i.setShowMessage(this.setBattleFixtureMsg(currentGw, o.getEvent(), entryInfoMap,
									i.getHomeEntry(), i.getAwayEntry(), i.getHomeEntryPoints(), i.getAwayEntryPoints()))
							);
					list.add(o);
				});
		return list;
	}

	private String setBattleFixtureMsg(int currentGw, int event, Map<Integer, EntryInfoEntity> entryInfoMap,
	                                   int homeEntry, int awayEntry, int homeEntryNetPoints, int awayEntryNetPoints) {
		// home
		String homeMsg = this.setBattleEntryMsg(entryInfoMap, homeEntry);
		// away
		String awayMsg = this.setBattleEntryMsg(entryInfoMap, awayEntry);
		// points
		String pointsMsg = this.setBattlePointsMsg(currentGw, event, homeEntryNetPoints, awayEntryNetPoints);
		return homeMsg + " " + pointsMsg + " " + awayMsg;
	}

	private String setBattleEntryMsg(Map<Integer, EntryInfoEntity> entryInfoMap, int entry) {
		String entryName = "";
		String playerName = "";
		if (entry == 0) {
			entryName = "平均分";
			playerName = "平均分";
		} else if (entry < 0) {
			entryName = "轮空";
			playerName = "轮空";
		} else {
			EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, null);
			if (entryInfoEntity != null) {
				entryName = entryInfoEntity.getEntryName();
				playerName = entryInfoEntity.getPlayerName();
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append(" ");
		if (StringUtils.isNotEmpty(entryName)) {
			builder.append(entryName);
		} else {
			builder.append("TBD");
		}
		if (StringUtils.isNotEmpty(playerName)) {
			builder.append(" (").append(playerName).append(") ");
		}
		return builder.toString();
	}

	private String setBattlePointsMsg(int currentGw, int event, int homeEntryNetPoints, int awayEntryNetPoints) {
		if (event > currentGw) {
			return " vs ";
		} else {
			return homeEntryNetPoints + " - " + awayEntryNetPoints;
		}
	}

	/**
	 * @implNote event_live
	 */
	@Cacheable(value = "qryEventLiveAll", key = "#season+'::'+#element", cacheManager = "apiCacheManager", unless = "#result == null")
	@Override
	public List<EventLiveEntity> qryEventLiveAll(String season, int element) {
		MybatisPlusConfig.season.set(season);
		List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getElement, element));
		MybatisPlusConfig.season.remove();
		return list;
	}

	@Override
	public List<EventLiveEntity> qryEventLive(String season, int event, int element) {
		MybatisPlusConfig.season.set(season);
		List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event)
				.eq(EventLiveEntity::getElement, element));
		MybatisPlusConfig.season.remove();
		return list;
	}

	@Cacheable(value = "qryPickListFromPicks", key = "#season+'::'+#picks", unless = "#result == null")
	@Override
	public List<EntryPickData> qryPickListFromPicks(String season, @NotNull String picks) {
		List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
		if (CollectionUtils.isEmpty(pickList)) {
			return Lists.newArrayList();
		}
		Map<String, String> positionMap = this.getPositionMap();
		pickList.forEach(pick -> {
			PlayerEntity playerEntity = this.getPlayerByElement(season, pick.getElement());
			if (playerEntity != null) {
				pick.setElementTypeName(positionMap.get(String.valueOf(playerEntity.getElementType())))
						.setWebName(playerEntity.getWebName());
			}
		});
		return pickList;
	}

	@Cacheable(value = "qryPickListByPosition", key = "#season+'::'+#picks", unless = "#result == null")
	@Override
	public PlayerPickData qryPickListByPosition(String season, String picks) {
		List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
		if (CollectionUtils.isEmpty(pickList)) {
			return new PlayerPickData();
		}
		Map<String, String> teamShortNameMap = this.getTeamShortNameMap(season);
		// collect
		List<EntryPickData> gkpList = Lists.newArrayList();
		List<EntryPickData> defList = Lists.newArrayList();
		List<EntryPickData> midList = Lists.newArrayList();
		List<EntryPickData> fwdList = Lists.newArrayList();
		List<EntryPickData> subList = Lists.newArrayList();
		pickList.forEach(pick -> {
			PlayerEntity playerEntity = this.getPlayerByElement(season, pick.getElement());
			if (playerEntity != null) {
				pick
						.setWebName(playerEntity.getWebName())
						.setElementType(playerEntity.getElementType())
						.setTeamId(playerEntity.getTeamId());
			}
			pick.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(pick.getTeamId()), ""));
			// add into list
			if (pick.getPosition() <= 11) {
				switch (pick.getElementType()) {
					case 1: {
						gkpList.add(pick);
						break;
					}
					case 2: {
						defList.add(pick);
						break;
					}
					case 3: {
						midList.add(pick);
						break;
					}
					case 4: {
						fwdList.add(pick);
						break;
					}
				}
			} else {
				subList.add(pick);
			}
		});
		return new PlayerPickData()
				.setGkps(gkpList
						.stream()
						.sorted(Comparator.comparing(EntryPickData::getPosition))
						.collect(Collectors.toList()))
				.setDefs(defList
						.stream()
						.sorted(Comparator.comparing(EntryPickData::getPosition))
						.collect(Collectors.toList()))
				.setMids(midList
						.stream()
						.sorted(Comparator.comparing(EntryPickData::getPosition))
						.collect(Collectors.toList()))
				.setFwds(fwdList
						.stream()
						.sorted(Comparator.comparing(EntryPickData::getPosition))
						.collect(Collectors.toList()))
				.setSubs(subList
						.stream()
						.sorted(Comparator.comparing(EntryPickData::getPosition))
						.collect(Collectors.toList()));
	}

	/**
	 * @implNote event_result
	 */
	@Cacheable(value = "qryEntryResult", key = "#season+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result == null")
	@Override
	public List<EntryEventResultData> qryEntryResult(String season, int entry) {
		List<EntryEventResultData> list = Lists.newArrayList();
		if (StringUtils.equals(season, "1920")) {
			IntStream.rangeClosed(1, 47).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
		} else {
			IntStream.rangeClosed(1, 38).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
		}
		return list;
	}

	@Cacheable(value = "qryEntryEventResult", key = "#season+'::'+#event+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result == null")
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
				.setPicks(this.qryPickListFromPicks(entryEventResultEntity.getEventPicks()));
		return entryEventResultData;
	}

	@Override
	public PlayerPickData qryEntryPickData(int entry) {
		EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEntry, entry)
				.eq(EntryEventResultEntity::getEvent, this.getCurrentEvent()));
		if (entryEventResultEntity == null) {
			return new PlayerPickData();
		}
		return this.qryPickListByPosition(entryEventResultEntity.getEventPicks());
	}

	/**
	 * @apiNote tournament
	 */
	@Override
	public List<TournamentInfoEntity> qryAllTournamentList() {
		return this.tournamentInfoService.list();
	}

	@Cacheable(cacheNames = "tournamentData", key = "#tournamentId")
	@Override
	public TournamentInfoData qryTournamentDataById(int tournamentId) {
		TournamentInfoData tournamentInfoData = new TournamentInfoData();
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return tournamentInfoData;
		}
		BeanUtil.copyProperties(tournamentInfoEntity, tournamentInfoData);
		tournamentInfoData
				.setGroupModeName(GroupMode.valueOf(tournamentInfoData.getGroupMode()).getModeName())
				.setKnockoutModeName(KnockoutMode.valueOf(tournamentInfoData.getKnockoutMode()).getModeName());
		return tournamentInfoData;
	}

	@Cacheable(cacheNames = "tournamentInfoEntity", key = "#tournamentId")
	@Override
	public TournamentInfoEntity qryTournamentInfoById(int tournamentId) {
		return this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getId, tournamentId)
				.eq(TournamentInfoEntity::getState, 1));
	}

	@Cacheable(value = "qryEntryListByTournament", key = "#tournamentId", unless = "#result.size() gt 0")
	@Override
	public List<Integer> qryEntryListByTournament(int tournamentId) {
		return this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
				.eq(TournamentEntryEntity::getTournamentId, tournamentId))
				.stream()
				.map(TournamentEntryEntity::getEntry)
				.collect(Collectors.toList());
	}

	@Cacheable(value = "qryGroupEntryInfoList", key = "#tournamentId+'::'+#groupId")
	@Override
	public List<EntryInfoData> qryGroupEntryInfoList(int tournamentId, int groupId) {
		List<EntryInfoData> list = Lists.newArrayList();
		List<Integer> entryList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.eq(TournamentGroupEntity::getGroupId, groupId))
				.stream()
				.map(TournamentGroupEntity::getEntry)
				.collect(Collectors.toList());
		entryList.forEach(entry -> {
			EntryInfoEntity entryInfoEntity = this.qryEntryInfo(entry);
			if (entryInfoEntity != null) {
				list.add(BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class));
			}
		});
		return list;
	}

	@Override
	public List<TournamentKnockoutEntity> qryKnockoutListByTournamentId(int tournamentId) {
		return this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId));
	}

	@Cacheable(value = "qryKnockoutBracketResultByTournament", key = "#tournamentId", unless = "#result == null")
	@Override
	public KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId) {
		// round -> tournament_knockout_result_data list
		LinkedHashMap<Integer, List<TournamentKnockoutResultData>> knockoutResultRoundDataMap = Maps.newLinkedHashMap();
		this.qryKnockoutResultByTournament(tournamentId).forEach(o -> {
			int round = o.getRound();
			List<TournamentKnockoutResultData> list = Lists.newArrayList();
			if (knockoutResultRoundDataMap.containsKey(round)) {
				list = knockoutResultRoundDataMap.get(round);
			}
			list.add(o);
			knockoutResultRoundDataMap.put(round, list);
		});
		if (CollectionUtils.isEmpty(knockoutResultRoundDataMap)) {
			return new KnockoutBracketData();
		}
		// return
		List<List<TournamentKnockoutResultData>> results = Lists.newArrayList();
		knockoutResultRoundDataMap.keySet().forEach(round -> results.add(knockoutResultRoundDataMap.get(round)));
		return new KnockoutBracketData()
				.setTeams(knockoutResultRoundDataMap.get(1))
				.setResults(results);
	}

	@Cacheable(value = "qryKnockoutResultByTournament", key = "#tournamentId", unless = "#result == null")
	@Override
	public List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId) {
		List<TournamentKnockoutResultData> knockoutResultDataList = Lists.newArrayList();
		// groupName
		Map<String, String> groupNameMap = this.qryZjTournamentGroupNameMap(tournamentId);
		// entry_group
		Map<String, Integer> entryGroupMap = this.qryZjTournamentGroupEntryGroupIdMap(tournamentId);
		// knockout
		Map<Integer, TournamentKnockoutEntity> knockoutMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId))
				.stream()
				.collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, v -> v));
		if (CollectionUtils.isEmpty(knockoutMap)) {
			return knockoutResultDataList;
		}
		// knockout_result, every match_id return a knockoutResultData
		knockoutMap.keySet().forEach(matchId -> {
			List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
					.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
					.eq(TournamentKnockoutResultEntity::getMatchId, matchId));
			// knockoutResultData
			TournamentKnockoutResultData knockoutResultData = new TournamentKnockoutResultData();
			TournamentKnockoutResultEntity o = knockoutResultList.get(0);
			int homeEntry = o.getHomeEntry();
			int homeGroupId = entryGroupMap.getOrDefault(String.valueOf(homeEntry), 0);
			String homeGroupName = groupNameMap.getOrDefault(String.valueOf(homeGroupId), "");
			int awayEntry = o.getAwayEntry();
			int awayGroupId = entryGroupMap.getOrDefault(String.valueOf(awayEntry), 0);
			String awayGroupName = groupNameMap.getOrDefault(String.valueOf(awayGroupId), "");
			knockoutResultData
					.setTournamentId(tournamentId)
					.setRound(knockoutMap.get(o.getMatchId()).getRound())
					.setEvent(o.getEvent())
					.setPlayAgainstId(o.getPlayAginstId())
					.setMatchId(o.getMatchId())
					.setHomeEntry(o.getHomeEntry())
					.setAwayEntry(o.getAwayEntry())
					.setHomeEntryGroupId(homeGroupId)
					.setAwayEntryGroupId(awayGroupId)
					.setHomeEntryGroupName(homeGroupName)
					.setAwayEntryGroupName(awayGroupName)
					.setHomeEntryName(this.getKnockoutResultEntryName(o.getHomeEntry()))
					.setAwayEntryName(this.getKnockoutResultEntryName(o.getAwayEntry()))
					.setHomeEntryNetPoint(this.calcKnockoutResultDataNetPoint(knockoutResultList, "home"))
					.setAwayEntryNetPoint(this.calcKnockoutResultDataNetPoint(knockoutResultList, "away"))
					.setHomeEntryRank(o.getHomeEntryRank())
					.setAwayEntryRank(o.getAwayEntryRank())
					.setMatchWinner(o.getMatchWinner());
			// match information
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

	private int calcKnockoutResultDataNetPoint(List<TournamentKnockoutResultEntity> knockoutResultList, String
			type) {
		if (StringUtils.equals(type, "home")) {
			return knockoutResultList.stream().mapToInt(TournamentKnockoutResultEntity::getHomeEntryNetPoints).sum();
		} else if (StringUtils.equals(type, "away")) {
			return knockoutResultList.stream().mapToInt(TournamentKnockoutResultEntity::getAwayEntryNetPoints).sum();
		}
		return 0;
	}

	private String setRoundMatchInformation
			(List<TournamentKnockoutResultEntity> knockoutResultList, Map<Integer, String> entryNameMap) {
		StringBuilder builder = new StringBuilder();
		knockoutResultList.forEach(o ->
				builder
						.append(entryNameMap.get(o.getHomeEntry()))
						.append("（").append(o.getHomeEntryNetPoints()).append("）")
						.append("- ")
						.append("（").append(o.getAwayEntryNetPoints()).append("）")
						.append(entryNameMap.get(o.getAwayEntry()))
		);
		return builder.toString();
	}

	@Cacheable(value = "qryZjTournamentCaptain", key = "#tournamentId")
	@Override
	public List<ZjTournamentCaptainData> qryZjTournamentCaptain(int tournamentId) {
		List<ZjTournamentCaptainData> list = Lists.newArrayList();
		this.zjTournamentCaptainService.list(new QueryWrapper<ZjTournamentCaptainEntity>().lambda()
				.eq(ZjTournamentCaptainEntity::getTournamentId, tournamentId))
				.forEach(o -> {
					ZjTournamentCaptainData zjTournamentCaptainData = new ZjTournamentCaptainData();
					BeanUtil.copyProperties(o, zjTournamentCaptainData, CopyOptions.create().ignoreNullValue());
					zjTournamentCaptainData
							.setPhaseTwoDeadline(zjTournamentCaptainData.getPhaseTwoDeadline())
							.setPkDeadline(zjTournamentCaptainData.getPkDeadline());
					list.add(zjTournamentCaptainData);
				});
		return list;
	}

	@Cacheable(value = "qryZjTournamentGroupNameMap", key = "#tournamentId")
	@Override
	public Map<String, String> qryZjTournamentGroupNameMap(int tournamentId) {
		Map<String, String> groupNameMap = Maps.newHashMap();
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return groupNameMap;
		}
		List<Integer> groupList = Lists.newArrayList();
		IntStream.rangeClosed(1, tournamentInfoEntity.getGroupNum()).forEach(groupList::add);
		if (CollectionUtils.isEmpty(groupList)) {
			return groupNameMap;
		}
		this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.in(TournamentGroupEntity::getGroupId, groupList))
				.forEach(o -> groupNameMap.put(String.valueOf(o.getGroupId()), o.getGroupName()));
		return groupNameMap;
	}

	@Override
	public List<TournamentKnockoutResultData> qryZjTournamentPkResultByTournament(int tournamentId) {
		List<TournamentKnockoutResultData> knockoutResultDataList = this.qryKnockoutResultByTournament(tournamentId);
		knockoutResultDataList.forEach(o -> o.setMatchInfo(this.setZjPkMatchInfo(o)));
		return knockoutResultDataList;
	}


	private String setZjPkMatchInfo(TournamentKnockoutResultData tournamentKnockoutResultData) {
		return "【" + tournamentKnockoutResultData.getHomeEntryGroupName() + "】"
				+ tournamentKnockoutResultData.getMatchInfo()
				+ "【" + tournamentKnockoutResultData.getAwayEntryGroupName() + "】";
	}

	@Cacheable(value = "qryZjTournamentPhaseOneRankMap", key = "#tournamentId", unless = "#result == null ")
	@Override
	public Map<String, Integer> qryZjTournamentPhaseOneRankMap(int tournamentId) {
		Map<String, Integer> map = Maps.newHashMap();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return map;
		}
		// group points
		List<Integer> groupList = Lists.newArrayList();
		IntStream.rangeClosed(1, tournamentInfoEntity.getGroupNum()).forEach(groupList::add);
		if (CollectionUtils.isEmpty(groupList)) {
			return map;
		}
		Map<Integer, Integer> groupPointsMap = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.in(TournamentGroupEntity::getGroupId, groupList))
				.stream()
				.collect(Collectors.toMap(TournamentGroupEntity::getGroupId, TournamentGroupEntity::getTotalPoints, Integer::sum));
		// points group
		Multimap<Integer, Integer> pointsGroupMap = HashMultimap.create(); // key:points -> value: groupId
		groupPointsMap.forEach((k, v) -> pointsGroupMap.put(v, k));
		// group points rank
		List<Integer> groupPointsRankList = Lists.newArrayList();
		pointsGroupMap.keySet()
				.stream()
				.sorted(Comparator.reverseOrder())
				.forEachOrdered(points -> {
					int times = pointsGroupMap.get(points).size();
					for (int i = 0; i < times; i++) {
						groupPointsRankList.add(points);
					}
				});
		// group rank
		int rank = 1;
		int levelCount = 0;
		for (int i = 0; i < groupPointsRankList.size(); i++) {
			int totalPoints = groupPointsRankList.get(i);
			Collection<Integer> groupIds = pointsGroupMap.get(totalPoints);
			if (i > 0) {
				if (totalPoints != groupPointsRankList.get(i - 1)) {
					rank = rank + 1 + levelCount;
					levelCount = 0;
				} else {
					levelCount++;
				}
			}
			for (int groupId :
					groupIds) {
				map.put(String.valueOf(groupId), rank);
			}
		}
		return map;
	}

	@Cacheable(value = "qryZjTournamentPhaseTwoGroupPointsMap", key = "#tournamentId", unless = "#result == null")
	@Override
	public Map<String, Integer> qryZjTournamentPhaseTwoGroupPointsMap(int tournamentId) {
		Map<String, Integer> map = Maps.newHashMap();
		Map<String, Map<TournamentGroupEntity, Integer>> entryRankMap = this.qryZjTournamentPhaseTwoEntryRankMap(tournamentId);
		if (CollectionUtils.isEmpty(entryRankMap)) {
			return map;
		}
		entryRankMap.keySet().forEach(groupId -> {
			int groupPoints = entryRankMap.get(groupId).values()
					.stream()
					.mapToInt(this::setZjTournamentPhaseTwoGroupPoints)
					.sum();
			map.put(groupId, groupPoints);
		});
		return map;
	}

	private Map<String, Map<TournamentGroupEntity, Integer>> qryZjTournamentPhaseTwoEntryRankMap(int tournamentId) {
		Map<String, Map<TournamentGroupEntity, Integer>> map = Maps.newHashMap();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return map;
		}
		int groupNum = tournamentInfoEntity.getGroupNum();
		int teamPerGroup = tournamentInfoEntity.getTeamPerGroup();
		// group entry name
		Map<String, Integer> groupEntryGroupMap = this.qryZjTournamentGroupEntryGroupIdMap(tournamentId);
		// group list
		List<Integer> groupList = Lists.newArrayList();
		IntStream.rangeClosed(groupNum + 1, groupNum + teamPerGroup).forEach(groupList::add);
		if (CollectionUtils.isEmpty(groupList)) {
			return map;
		}
		// tournament_group
		List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.in(TournamentGroupEntity::getGroupId, groupList));
		// sort by group
		Multimap<Integer, TournamentGroupEntity> groupEntityMap = HashMultimap.create();
		tournamentGroupEntityList.forEach(o -> groupEntityMap.put(o.getGroupId(), o));
		// sort every group
		groupEntityMap.keySet().forEach(groupId -> {
			List<TournamentGroupEntity> groupEntityList = groupEntityMap.get(groupId)
					.stream()
					.sorted(Comparator.comparing(TournamentGroupEntity::getTotalNetPoints).reversed()
							.thenComparing(TournamentGroupEntity::getTotalTransfersCost))
					.collect(Collectors.toList());
			int rank = 1;
			int levelCount = 0;
			for (int i = 0; i < groupEntityList.size(); i++) {
				TournamentGroupEntity tournamentGroupEntity = groupEntityList.get(i);
				int totalNetPoints = tournamentGroupEntity.getTotalNetPoints();
				int totalTransfersCost = tournamentGroupEntity.getTotalTransfersCost();
				if (i > 0) {
					if (totalNetPoints != groupEntityList.get(i - 1).getTotalNetPoints()) {
						rank = rank + 1 + levelCount;
						levelCount = 0;
					} else if (totalTransfersCost != groupEntityList.get(i - 1).getTotalTransfersCost()) {
						rank = rank + 1 + levelCount;
						levelCount = 0;
					} else {
						levelCount++;
					}
				}
				String entryGroupId = String.valueOf(groupEntryGroupMap.get(String.valueOf(tournamentGroupEntity.getEntry())));
				Map<TournamentGroupEntity, Integer> groupEntryRankMap = Maps.newHashMap();
				if (map.containsKey(entryGroupId)) {
					groupEntryRankMap = map.get(entryGroupId);
				}
				groupEntryRankMap.put(tournamentGroupEntity, rank);
				map.put(entryGroupId, groupEntryRankMap);
			}
		});
		return map;
	}

	@Cacheable(value = "qryZjTournamentPkGroupPointsMap", key = "#tournamentId", unless = "#result == null")
	@Override
	public Map<String, Integer> qryZjTournamentPkGroupPointsMap(int tournamentId) {
		Map<String, Integer> map = Maps.newHashMap();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return map;
		}
		// group entry name
		Map<String, Integer> groupEntryGroupMap = this.qryZjTournamentGroupEntryGroupIdMap(tournamentId);
		// tournament_knockout
		List<TournamentKnockoutEntity> knockoutEntityList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutEntity::getRound, 1)
				.ge(TournamentKnockoutEntity::getHomeEntry, 0)
				.gt(TournamentKnockoutEntity::getAwayEntry, 0));
		if (CollectionUtils.isEmpty(knockoutEntityList)) {
			return map;
		}
		// entry_pk_result
		Map<Integer, Integer> entryPkResultMap = Maps.newHashMap();
		knockoutEntityList.forEach(o -> {
			int homePoints = o.getRoundWinner().intValue() == o.getHomeEntry() ? 1 : 0;
			entryPkResultMap.put(o.getHomeEntry(), homePoints);
			int awayPoints = o.getRoundWinner().intValue() == o.getAwayEntry() ? 1 : 0;
			entryPkResultMap.put(o.getAwayEntry(), awayPoints);
		});
		// group_pk_result
		entryPkResultMap.forEach((entry, points) -> {
			int groupId = groupEntryGroupMap.getOrDefault(String.valueOf(entry), 0);
			int groupPoints = map.getOrDefault(String.valueOf(groupId), 0) + points;
			map.put(String.valueOf(groupId), groupPoints);
		});
		return map;
	}

	@Cacheable(value = "qryZjTournamentPhaseTwoRankMap", key = "#tournamentId", unless = "#result == null")
	@Override
	public Map<String, Integer> qryZjTournamentPhaseTwoRankMap(int tournamentId) {
		Map<String, Integer> map = Maps.newHashMap();
		Map<String, Map<TournamentGroupEntity, Integer>> entryRankMap = this.qryZjTournamentPhaseTwoEntryRankMap(tournamentId);
		List<TournamentGroupEntity> list = Lists.newArrayList();
		entryRankMap.keySet().forEach(groupId ->
				list.add(new TournamentGroupEntity()
						.setGroupId(Integer.parseInt(groupId))
						.setGroupPoints(entryRankMap.get(groupId).values()
								.stream()
								.mapToInt(this::setZjTournamentPhaseTwoGroupPoints)
								.sum())
						.setTotalNetPoints(entryRankMap.get(groupId).keySet()
								.stream()
								.mapToInt(TournamentGroupEntity::getTotalNetPoints)
								.sum())
				));
		List<TournamentGroupEntity> groupEntityList = list
				.stream()
				.sorted(Comparator.comparing(TournamentGroupEntity::getGroupPoints)
						.thenComparing(TournamentGroupEntity::getTotalNetPoints)
						.reversed())
				.collect(Collectors.toList());
		int rank = 1;
		int levelCount = 0;
		for (int i = 0; i < groupEntityList.size(); i++) {
			TournamentGroupEntity tournamentGroupEntity = groupEntityList.get(i);
			int groupPoints = tournamentGroupEntity.getGroupPoints();
			int totalNetPoints = tournamentGroupEntity.getTotalNetPoints();
			if (i > 0) {
				if (groupPoints != groupEntityList.get(i - 1).getGroupPoints()) {
					rank = rank + 1 + levelCount;
					levelCount = 0;
				} else if (totalNetPoints != groupEntityList.get(i - 1).getTotalNetPoints()) {
					rank = rank + 1 + levelCount;
					levelCount = 0;
				} else {
					levelCount++;
				}
			}
			map.put(String.valueOf(tournamentGroupEntity.getGroupId()), rank);
		}
		return map;
	}

	private int setZjTournamentPhaseTwoGroupPoints(int rank) {
		switch (rank) {
			case 1:
				return 5;
			case 2:
				return 3;
			case 3:
				return 2;
			case 4:
				return 1;
			default:
				return 0;
		}
	}

	@Cacheable(value = "qryZjTournamentPkRankMap", key = "#tournamentId", unless = "#result == null")
	@Override
	public Map<String, Integer> qryZjTournamentPkRankMap(int tournamentId) {
		Map<String, Integer> map = Maps.newHashMap();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return map;
		}
		// group entry name
		Map<String, Integer> groupEntryGroupMap = this.qryZjTournamentGroupEntryGroupIdMap(tournamentId);
		// tournament_knockout
		List<TournamentKnockoutEntity> tournamentKnockoutEntityList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutEntity::getRound, 1)
				.ge(TournamentKnockoutEntity::getHomeEntry, 0)
				.gt(TournamentKnockoutEntity::getAwayEntry, 0));
		if (CollectionUtils.isEmpty(tournamentKnockoutEntityList)) {
			return map;
		}
		// tournament_knockout_result
		List<TournamentKnockoutResultEntity> tournamentKnockoutResultEntityList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
				.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
				.ge(TournamentKnockoutResultEntity::getHomeEntry, 0)
				.gt(TournamentKnockoutResultEntity::getAwayEntry, 0));
		if (CollectionUtils.isEmpty(tournamentKnockoutResultEntityList)) {
			return map;
		}
		Map<Integer, Integer> knockoutResultMap = Maps.newHashMap();
		tournamentKnockoutResultEntityList.forEach(o -> knockoutResultMap.put(o.getHomeEntry(), o.getHomeEntryNetPoints()));
		tournamentKnockoutResultEntityList.forEach(o -> knockoutResultMap.put(o.getAwayEntry(), o.getAwayEntryNetPoints()));
		// entry_pk_result
		Map<Integer, ZjTournamentResultData> entryPkResultMap = Maps.newHashMap();
		tournamentKnockoutEntityList.forEach(o -> {
			int homeEntry = o.getHomeEntry();
			int homePoints = o.getRoundWinner() == homeEntry ? 1 : 0;
			entryPkResultMap.put(homeEntry, new ZjTournamentResultData()
					.setPkTotalPoints(knockoutResultMap.getOrDefault(homeEntry, 0))
					.setPkGroupPoints(homePoints)
			);
			int awayEntry = o.getAwayEntry();
			int awayPoints = o.getRoundWinner() == awayEntry ? 1 : 0;
			entryPkResultMap.put(awayEntry, new ZjTournamentResultData()
					.setPkTotalPoints(knockoutResultMap.getOrDefault(awayEntry, 0))
					.setPkGroupPoints(awayPoints)
			);
		});
		// group_pk_result
		Map<Integer, ZjTournamentResultData> groupPkResultMap = Maps.newHashMap();
		entryPkResultMap.forEach((entry, points) -> {
			int groupId = groupEntryGroupMap.getOrDefault(String.valueOf(entry), 0);
			ZjTournamentResultData zjTournamentResultData = groupPkResultMap.getOrDefault(groupId, new ZjTournamentResultData());
			zjTournamentResultData
					.setGroupId(groupEntryGroupMap.getOrDefault(String.valueOf(entry), 0))
					.setPkTotalPoints(zjTournamentResultData.getPkTotalPoints() + entryPkResultMap.get(entry).getPkTotalPoints())
					.setPkGroupPoints(zjTournamentResultData.getPkGroupPoints() + entryPkResultMap.get(entry).getPkGroupPoints());
			groupPkResultMap.put(groupId, zjTournamentResultData);
		});
		// sort group rank
		List<ZjTournamentResultData> groupRankList = groupPkResultMap.values()
				.stream()
				.sorted(Comparator.comparing(ZjTournamentResultData::getPkGroupPoints)
						.thenComparing(ZjTournamentResultData::getPkTotalPoints)
						.reversed())
				.collect(Collectors.toList());
		for (int i = 1; i < groupRankList.size() + 1; i++) {
			map.put(String.valueOf(groupRankList.get(i - 1).getGroupId()), i);
		}
		return map;
	}

	@Cacheable(value = "qryZjTournamentRankMap", key = "#list.get(0).tournamentId", unless = "#result == null")
	@Override
	public Map<String, Integer> qryZjTournamentRankMap(List<ZjTournamentResultData> list) {
		Map<String, Integer> map = Maps.newHashMap();
		List<ZjTournamentResultData> zjTournamentResultDataList = list
				.stream()
				.sorted(Comparator.comparing(ZjTournamentResultData::getTournamentTotalGroupPoints)
						.thenComparing(ZjTournamentResultData::getTournamentTotalPoints)
						.reversed())
				.collect(Collectors.toList());
		int rank = 1;
		int levelCount = 0;
		for (int i = 0; i < zjTournamentResultDataList.size(); i++) {
			ZjTournamentResultData zjTournamentResultData = zjTournamentResultDataList.get(i);
			int totalGroupPoints = zjTournamentResultData.getTournamentTotalGroupPoints();
			int totalPoints = zjTournamentResultData.getTournamentTotalPoints();
			if (i > 0) {
				if (totalGroupPoints != zjTournamentResultDataList.get(i - 1).getTournamentTotalGroupPoints()) {
					rank = rank + 1 + levelCount;
					levelCount = 0;
				} else if (totalPoints != zjTournamentResultDataList.get(i - 1).getTournamentTotalPoints()) {
					rank = rank + 1 + levelCount;
					levelCount = 0;
				} else {
					levelCount++;
				}
			}
			map.put(String.valueOf(zjTournamentResultData.getGroupId()), rank);
		}
		return map;
	}

	@Cacheable(value = "qryZjTournamentGroupEntryGroupIdMap", key = "#tournamentId")
	@Override
	public Map<String, Integer> qryZjTournamentGroupEntryGroupIdMap(int tournamentId) {
		List<Integer> groupList = Lists.newArrayList();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return Maps.newHashMap();
		}
		IntStream.rangeClosed(1, tournamentInfoEntity.getGroupNum()).forEach(groupList::add);
		if (CollectionUtils.isEmpty(groupList)) {
			return Maps.newHashMap();
		}
		return this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.in(TournamentGroupEntity::getGroupId, groupList))
				.stream()
				.collect(Collectors.toMap(o -> String.valueOf(o.getEntry()), TournamentGroupEntity::getGroupId));
	}

	@Cacheable(value = "qryZjTournamentGroupEntryGroupNameMap", key = "#tournamentId")
	@Override
	public Map<String, String> qryZjTournamentGroupEntryGroupNameMap(int tournamentId) {
		List<Integer> groupList = Lists.newArrayList();
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return Maps.newHashMap();
		}
		IntStream.rangeClosed(1, tournamentInfoEntity.getGroupNum()).forEach(groupList::add);
		if (CollectionUtils.isEmpty(groupList)) {
			return Maps.newHashMap();
		}
		return this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.in(TournamentGroupEntity::getGroupId, groupList))
				.stream()
				.collect(Collectors.toMap(o -> String.valueOf(o.getEntry()), TournamentGroupEntity::getGroupName));
	}

	@Override
	public TournamentGroupData qryDiscloseGroupData(int tournamentId, int entry, int currentGroupId) {
		// check num
		List<Integer> discloseList = this.redisCacheService.getDiscloseList(tournamentId, currentGroupId);
		int discloseNum = discloseList.size();
		if (discloseNum > 0 && discloseList.get(0) != entry) {
			return new TournamentGroupData().setDiscloseList(discloseList);
		}
		// tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
		if (tournamentInfoEntity == null) {
			return new TournamentGroupData().setDiscloseList(discloseList);
		}
		// tournament_group
		TournamentGroupEntity tournamentGroupEntity = this.tournamentGroupService.getOne(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.eq(TournamentGroupEntity::getEntry, entry)
				.gt(TournamentGroupEntity::getGroupId, tournamentInfoEntity.getGroupNum()));
		if (tournamentGroupEntity == null) {
			return new TournamentGroupData()
					.setTournamentId(tournamentId)
					.setEntry(-1)
					.setDrawPhaseTwo(false)
					.setDiscloseList(discloseList);
		}
		TournamentGroupData tournamentGroupData = new TournamentGroupData();
		BeanUtil.copyProperties(tournamentGroupEntity, tournamentGroupData, CopyOptions.create().ignoreNullValue());
		// entry_info
		EntryInfoEntity entryInfoEntity = this.qryEntryInfo(entry);
		if (entryInfoEntity != null) {
			tournamentGroupData
					.setEntryName(entryInfoEntity.getEntryName())
					.setPlayerName(entryInfoEntity.getPlayerName());
		}
		// disclose list
		this.redisCacheService.insertDiscloseCache(tournamentId, currentGroupId, entry);
		if (!discloseList.contains(entry)) {
			discloseList.add(entry);
		}
		tournamentGroupData.setDiscloseList(discloseList);
		return tournamentGroupData;
	}

	@Override
	public List<TournamentKnockoutEventFixtureData> qryZjPkPickListById(int tournamentId) {
		List<TournamentKnockoutEventFixtureData> list = Lists.newArrayList();
		this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutEntity::getRound, 1)
				.gt(TournamentKnockoutEntity::getHomeEntry, 0)
				.gt(TournamentKnockoutEntity::getAwayEntry, 0)
				.orderByAsc(TournamentKnockoutEntity::getMatchId))
				.forEach(o -> {
					int homeEntry = o.getHomeEntry();
					int awayEntry = o.getAwayEntry();
					TournamentKnockoutEventFixtureData data = new TournamentKnockoutEventFixtureData()
							.setHomeEntry(homeEntry)
							.setAwayEntry(awayEntry)
							.setMatchId(o.getMatchId());
					// entry_info
					Map<Integer, EntryInfoEntity> entryInfoMap = Maps.newHashMap();
					entryInfoMap.put(homeEntry, this.qryEntryInfo(homeEntry));
					entryInfoMap.put(awayEntry, this.qryEntryInfo(awayEntry));
					String showMessage = this.setBattleFixtureMsg(1, 2, entryInfoMap, homeEntry, awayEntry, 0, 0);
					data.setShowMessage(showMessage);
					list.add(data);
				});
		return list;
	}

	/**
	 * @apiNote report
	 */
	@Cacheable(value = "qryLeagueNameByIdAndType", key = "#leagueId+'::'+#leagueType")
	@Override
	public String qryLeagueNameByIdAndType(int leagueId, String leagueType) {
		List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueId, leagueId)
				.eq(LeagueEventReportEntity::getLeagueType, leagueType));
		if (!CollectionUtils.isEmpty(leagueEventReportEntityList)) {
			return leagueEventReportEntityList
					.stream()
					.map(LeagueEventReportEntity::getLeagueName)
					.findFirst()
					.orElse("");
		}
		LeagueInfoData leagueInfoData = new LeagueInfoData();
		if (StringUtils.equals(LeagueType.Classic.name(), leagueType)) {
			leagueInfoData = this.staticService.getEntryInfoListFromClassicByLimit(leagueId, 1);
		} else if (StringUtils.equals(LeagueType.H2h.name(), leagueType)) {
			leagueInfoData = this.staticService.getEntryInfoListFromH2hByLimit(leagueId, 1);
		}
		return leagueInfoData.getName();
	}

	@Override
	public List<String> qryTeamSelectStatList() {
		return this.leagueEventReportService.getBaseMapper().qryLeagueNameList();
	}

	/**
	 * @implNote live, cannot be cached
	 */
	@Override
	public Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap() {
		return this.redisCacheService.getEventLiveFixtureMap();
	}

	@Override
	public Map<String, EventLiveEntity> getEventLiveByEvent(int event) {
		return this.redisCacheService.getEventLiveByEvent(event);
	}

	@Override
	public Map<String, Map<String, Integer>> getLiveBonusCacheMap() {
		return this.redisCacheService.getLiveBonusCacheMap();
	}

	@Override
	public List<LiveMatchData> qryLiveMatchList(int statusId) {
		List<LiveMatchData> list = Lists.newArrayList();
		String queryStatus = Arrays.stream(MatchPlayStatus.values())
				.filter(o -> o.getStatus() == statusId)
				.map(Enum::name)
				.findFirst()
				.orElse("");
		if (StringUtils.isEmpty(queryStatus)) {
			return list;
		}
		Map<String, Map<String, List<LiveFixtureData>>> eventLiveFixtureMap = this.redisCacheService.getEventLiveFixtureMap();
		eventLiveFixtureMap.keySet().forEach(teamId ->
				eventLiveFixtureMap.get(teamId).forEach((status, fixtureList) -> {
					if (!StringUtils.equals(status, queryStatus)) {
						return;
					}
					fixtureList.forEach(o -> {
						if (!o.isWasHome()) {
							return;
						}
						list.add(new LiveMatchData()
								.setHomeTeamId(o.getTeamId())
								.setHomeTeamName(o.getTeamName())
								.setHomeTeamShortName(o.getTeamShortName())
								.setHomeScore(o.getTeamScore())
								.setAwayTeamId(o.getAgainstId())
								.setAwayTeamName(o.getAgainstName())
								.setAwayTeamShortName(o.getAgainstShortName())
								.setAwayScore(o.getAgainstTeamScore())
								.setKickoffTime(o.getKickoffTime())
						);
					});
				}));
		return list
				.stream()
				.sorted(Comparator.comparing(LiveMatchData::getKickoffTime).reversed())
				.collect(Collectors.toList());
	}

	@Override
	public ScoutData qryScoutEntryEventData(int event, int entry) {
		Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
		ScoutEntity scoutEntity = this.scoutService.getOne(new QueryWrapper<ScoutEntity>().lambda()
				.eq(ScoutEntity::getEvent, event)
				.eq(ScoutEntity::getEntry, entry));
		if (scoutEntity == null) {
			return new ScoutData();
		}
		return new ScoutData()
				.setEvent(event)
				.setEntry(entry)
				.setScoutName(scoutEntity.getScoutName())
				.setGkp(scoutEntity.getGkp())
				.setGkpName(this.qryPlayerWebNameByElement(scoutEntity.getGkp()))
				.setGkpPrice(this.getElememtPrice(scoutEntity.getGkp()))
				.setDef(scoutEntity.getDef())
				.setDefName(this.qryPlayerWebNameByElement(scoutEntity.getDef()))
				.setDefPrice(this.getElememtPrice(scoutEntity.getDef()))
				.setMid(scoutEntity.getMid())
				.setMidName(this.qryPlayerWebNameByElement(scoutEntity.getMid()))
				.setMidPrice(this.getElememtPrice(scoutEntity.getMid()))
				.setFwd(scoutEntity.getFwd())
				.setFwdName(this.qryPlayerWebNameByElement(scoutEntity.getFwd()))
				.setFwdPrice(this.getElememtPrice(scoutEntity.getFwd()))
				.setCaptain(scoutEntity.getCaptain())
				.setCaptainName(this.qryPlayerWebNameByElement(scoutEntity.getCaptain()))
				.setReason(scoutEntity.getReason());
	}

	private double getElememtPrice(int element) {
		PlayerEntity playerEntity = this.getPlayerByElement(element);
		if (playerEntity != null) {
			return playerEntity.getPrice() / 10.0;
		}
		return 0;
	}

}
