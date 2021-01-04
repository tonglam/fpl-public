package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.LiveFixtureCollector;
import com.tong.fpl.config.collector.PlayerValueCollector;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.MatchPlayStatus;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.constant.enums.ValueChangeType;
import com.tong.fpl.domain.data.eventLive.ElementStat;
import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.EventLiveRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisCacheServiceImpl implements IRedisCacheService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final IInterfaceService interfaceService;
	private final TeamService teamNameService;
	private final EventService eventService;
	private final EventFixtureService eventFixtureService;
	private final EventLiveService eventLiveService;
	private final PlayerService playerService;
	private final PlayerStatService playerStatService;
	private final PlayerValueService playerValueService;

	@Override
	public void insertTeam() {
		Optional<StaticRes> result = this.interfaceService.getBootstrapStatic();
		result.ifPresent(staticRes -> {
			// insert table
			this.teamNameService.remove(new QueryWrapper<TeamEntity>().eq("1", 1));
			List<TeamEntity> teamList = Lists.newArrayList();
			staticRes.getTeams().forEach(bootstrapTeam -> {
				TeamEntity teamEntity = new TeamEntity();
				teamEntity.setId(bootstrapTeam.getId());
				teamEntity.setName(bootstrapTeam.getName());
				teamEntity.setShortName(bootstrapTeam.getShortName());
				teamList.add(teamEntity);
			});
			this.teamNameService.saveBatch(teamList);
			log.info("insert team size:{}!", teamList.size());
			// set cache
			Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
			String season = CommonUtils.getCurrentSeason();
			// set team_name cache
			this.setTeamNameCache(cacheMap, teamList, season);
			// set team_short_name cache
			this.setTeamShortNameCache(cacheMap, teamList, season);
			RedisUtils.pipelineHashCache(cacheMap, -1, null);
		});
	}

	@Override
	public void insertHisTeam(String season) {
		MybatisPlusConfig.season.set(season);
		List<TeamEntity> teamList = this.teamNameService.list();
		MybatisPlusConfig.season.remove();
		// set cache
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		// set team_name cache
		this.setTeamNameCache(cacheMap, teamList, season);
		// set team_short_name cache
		this.setTeamShortNameCache(cacheMap, teamList, season);
		RedisUtils.pipelineHashCache(cacheMap, -1, null);
	}

	private void setTeamNameCache(Map<String, Map<String, Object>> cacheMap, List<TeamEntity> teamList, String season) {
		Map<String, Object> valueMap = Maps.newHashMap();
		String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "name");
		RedisUtils.removeCacheByKey(key);
		teamList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getName()));
		cacheMap.put(key, valueMap);
	}

	private void setTeamShortNameCache(Map<String, Map<String, Object>> cacheMap, List<TeamEntity> teamList, String season) {
		Map<String, Object> valueMap = Maps.newHashMap();
		String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "shortName");
		RedisUtils.removeCacheByKey(key);
		teamList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getShortName()));
		cacheMap.put(key, valueMap);
	}

	@Override
	public void insertEvent() {
		Optional<StaticRes> result = this.interfaceService.getBootstrapStatic();
		result.ifPresent(staticRes -> {
			// insert table
			this.eventService.remove(new QueryWrapper<EventEntity>().eq("1", 1));
			List<EventEntity> eventList = Lists.newArrayList();
			staticRes.getEvents().forEach(bootstrapEvent -> {
				EventEntity eventEntity = new EventEntity();
				BeanUtil.copyProperties(bootstrapEvent, eventEntity, CopyOptions.create().ignoreNullValue());
				eventEntity.setId(bootstrapEvent.getId())
						.setDeadlineTime(CommonUtils.getZoneDate(bootstrapEvent.getDeadlineTime()));
				eventList.add(eventEntity);
			});
			this.eventService.saveBatch(eventList);
			log.info("insert event size:{}!", eventList.size());
			// set cache
			Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
			Map<String, Object> valueMap = Maps.newHashMap();
			String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
			RedisUtils.removeCacheByKey(key);
			eventList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getDeadlineTime()));
			cacheMap.put(key, valueMap);
			RedisUtils.pipelineHashCache(cacheMap, -1, null);
		});
	}

	@Override
	public void insertHisEvent(String season) {
		MybatisPlusConfig.season.set(season);
		List<EventEntity> eventList = this.eventService.list();
		MybatisPlusConfig.season.remove();
		// set cache
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		Map<String, Object> valueMap = Maps.newHashMap();
		String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
		RedisUtils.removeCacheByKey(key);
		eventList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getDeadlineTime()));
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineHashCache(cacheMap, -1, null);
	}

	@Override
	public void insertEventFixture() {
		List<EventFixtureEntity> fixtureList = Lists.newArrayList();
		Map<String, Set<Object>> cacheMap = Maps.newHashMap();
		// set cache by event
		IntStream.rangeClosed(1, 38).forEach(event -> fixtureList.addAll(this.insertEventFixtureByEvent(cacheMap, event)));
		// set cache by team
		RedisUtils.pipelineSetCache(cacheMap, -1, null);
		this.insertEventFixtureCacheByTeam(CommonUtils.getCurrentSeason(), fixtureList);
	}

	private List<EventFixtureEntity> insertEventFixtureByEvent(Map<String, Set<Object>> cacheMap, int event) {
		log.info("start insert event{} fixtures!", event);
		this.eventFixtureService.remove(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event));
		List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
		Optional<List<EventFixturesRes>> result = this.interfaceService.getEventFixture(event);
		result.ifPresent(eventFixturesRes -> {
			eventFixturesRes.forEach(o -> {
				EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
				BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
				eventFixtureEntity.setKickoffTime(CommonUtils.getZoneDate(o.getKickoffTime()));
				eventFixtureList.add(eventFixtureEntity);
			});
			this.eventFixtureService.saveBatch(eventFixtureList);
			log.info("insert event:{}, event_fixture size:{}!", event, eventFixtureList.size());
			// set cache by event
			this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
		});
		return eventFixtureList;
	}

	private void setEventFixtureCacheBySingleEvent(Map<String, Set<Object>> cacheMap, String season, int event, Collection<EventFixtureEntity> eventFixtureList) {
		String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "event", event);
		Set<Object> valueSet = Sets.newHashSet();
		RedisUtils.removeCacheByKey(key);
		valueSet.addAll(eventFixtureList);
		cacheMap.put(key, valueSet);
	}

	private void insertEventFixtureCacheByTeam(String season, Collection<EventFixtureEntity> fixtureList) {
		IntStream.rangeClosed(1, 20).forEach(teamId -> this.insertEventFixtureCacheBySingleTeam(season, fixtureList, teamId));
	}

	private void insertEventFixtureCacheBySingleTeam(String season, Collection<EventFixtureEntity> fixtureList, int teamId) {
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
		RedisUtils.removeCacheByKey(key);
		Map<String, Object> valueMap = this.setEventFixtureValueBySingleTeam(fixtureList, teamId);
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineHashCache(cacheMap, -1, null);
	}

	private Map<String, Object> setEventFixtureValueBySingleTeam(Collection<EventFixtureEntity> fixtureList, int teamId) {
		Multimap<String, Object> eventFixtureMap = HashMultimap.create();
		// home game
		fixtureList.stream()
				.filter(o -> o.getTeamH() == teamId)
				.forEach(o -> eventFixtureMap.put(String.valueOf(o.getEvent()), new PlayerFixtureData()
						.setTeamId(teamId)
						.setEvent(o.getEvent())
						.setAgainstTeamId(o.getTeamA())
						.setDifficulty(o.getTeamHDifficulty())
						.setKickoffTime(o.getKickoffTime())
						.setStarted(o.getStarted())
						.setFinished(o.getFinished())
						.setWasHome(true)
						.setScore(o.getTeamHScore() + "-" + o.getTeamAScore())
				));
		// away game
		fixtureList.stream()
				.filter(o -> o.getTeamA() == teamId)
				.forEach(o -> eventFixtureMap.put(String.valueOf(o.getEvent()), new PlayerFixtureData()
						.setTeamId(teamId)
						.setEvent(o.getEvent())
						.setAgainstTeamId(o.getTeamH())
						.setDifficulty(o.getTeamADifficulty())
						.setKickoffTime(o.getKickoffTime())
						.setStarted(o.getStarted())
						.setFinished(o.getFinished())
						.setWasHome(false)
						.setScore(o.getTeamAScore() + "-" + o.getTeamHScore())
				));
		Map<String, Object> valueMap = Maps.newHashMap();
		eventFixtureMap.keySet().forEach(event -> {
			List<PlayerFixtureData> list = Lists.newArrayList();
			eventFixtureMap.get(event).forEach(o -> list.add((PlayerFixtureData) o));
			valueMap.put(event, list);
		});
		return valueMap;
	}

	@Override
	public void insertHisEventFixture(String season) {
		MybatisPlusConfig.season.set(season);
		Multimap<Integer, EventFixtureEntity> eventFixtureMap = HashMultimap.create();
		this.eventFixtureService.list().forEach(o -> eventFixtureMap.put(o.getEvent(), o));
		MybatisPlusConfig.season.remove();
		// set cache by event
		Map<String, Set<Object>> cacheMap = Maps.newHashMap();
		eventFixtureMap.keySet().forEach(event ->
				this.setEventFixtureCacheBySingleEvent(cacheMap, season, event, eventFixtureMap.get(event)));
		// set cache by team
		this.insertEventFixtureCacheByTeam(season, eventFixtureMap.values());
		RedisUtils.pipelineSetCache(cacheMap, -1, null);
	}

	@Override
	public void insertSingleEventFixture(int event) {
		this.eventFixtureService.remove(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event));
		List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
		Optional<List<EventFixturesRes>> result = this.interfaceService.getEventFixture(event);
		result.ifPresent(eventFixturesRes -> {
			eventFixturesRes.forEach(o -> {
				EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
				BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
				eventFixtureEntity.setKickoffTime(CommonUtils.getZoneDate(o.getKickoffTime()));
				eventFixtureList.add(eventFixtureEntity);
			});
			this.eventFixtureService.saveBatch(eventFixtureList);
			log.info("insert event:{}, event_fixture size:{}!", event, eventFixtureList.size());
			// set cache by event
			Map<String, Set<Object>> cacheMap = Maps.newHashMap();
			this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
			RedisUtils.pipelineSetCache(cacheMap, -1, null);
			// set cache by team
			IntStream.rangeClosed(1, 20).forEach(teamId ->
					this.insertEventFixtureCacheBySingleTeamAndEvent(CommonUtils.getCurrentSeason(), eventFixtureList, teamId, event));
		});
	}

	@Override
	public void insertSingleEventFixtureCache(int event) {
		List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
		Optional<List<EventFixturesRes>> result = this.interfaceService.getEventFixture(event);
		result.ifPresent(eventFixturesRes -> {
			eventFixturesRes.forEach(o -> {
				EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
				BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
				eventFixtureEntity.setKickoffTime(CommonUtils.getZoneDate(o.getKickoffTime()));
				eventFixtureList.add(eventFixtureEntity);
			});
			// set cache by event
			Map<String, Set<Object>> cacheMap = Maps.newHashMap();
			this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
			RedisUtils.pipelineSetCache(cacheMap, -1, null);
			// set cache by team
			IntStream.rangeClosed(1, 20).forEach(teamId ->
					this.insertEventFixtureCacheBySingleTeamAndEvent(CommonUtils.getCurrentSeason(), eventFixtureList, teamId, event));
		});
	}

	@Override
	public void insertLiveFixtureCache() {
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		int event = this.getCurrentEvent();
		Table<Integer, MatchPlayStatus, List<LiveFixtureData>> table = this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event)
				.stream()
				.collect(new LiveFixtureCollector(this.getTeamNameMap(), this.getTeamShortNameMap()));
		Map<String, Object> valueMap = Maps.newHashMap();
		table.rowKeySet().forEach(teamId -> {
			Map<MatchPlayStatus, List<LiveFixtureData>> map = Maps.newHashMap();
			// playing
			List<LiveFixtureData> playingList = Lists.newArrayList();
			if (table.contains(teamId, MatchPlayStatus.Playing)) {
				playingList = table.get(teamId, MatchPlayStatus.Playing);
			}
			map.put(MatchPlayStatus.Playing, playingList);
			// not start
			List<LiveFixtureData> notStartList = Lists.newArrayList();
			if (table.contains(teamId, MatchPlayStatus.Not_Start)) {
				notStartList = table.get(teamId, MatchPlayStatus.Not_Start);
			}
			map.put(MatchPlayStatus.Not_Start, notStartList);
			// finished
			List<LiveFixtureData> finishedList = Lists.newArrayList();
			if (table.contains(teamId, MatchPlayStatus.Finished)) {
				finishedList = table.get(teamId, MatchPlayStatus.Finished);
			}
			map.put(MatchPlayStatus.Finished, finishedList);
			valueMap.put(String.valueOf(teamId), map);
		});
		// set cache
		String key = StringUtils.joinWith("::", LiveFixtureData.class.getSimpleName());
		RedisUtils.removeCacheByKey(key);
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineHashCache(cacheMap, -1, null);
	}

	private void insertEventFixtureCacheBySingleTeamAndEvent(String season, Collection<EventFixtureEntity> fixtureList, int teamId, int event) {
		String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
		String HashKey = String.valueOf(event);
		this.redisTemplate.opsForHash().delete(key, HashKey);
		Map<String, Object> valueMap = this.setEventFixtureValueBySingleTeam(fixtureList, teamId);
		this.redisTemplate.opsForHash().put(key, HashKey, valueMap.get(HashKey));
	}

	@Override
	public void insertPlayer() {
		Optional<StaticRes> result = this.interfaceService.getBootstrapStatic();
		result.ifPresent(staticRes -> {
			// prepare
			Multimap<Integer, PlayerValueEntity> playerValueMap = HashMultimap.create();
			this.playerValueService.list().forEach(o -> playerValueMap.put(o.getElement(), o));
			// insert table
			this.playerService.remove(new QueryWrapper<PlayerEntity>().eq("1", 1));
			List<PlayerEntity> playerList = Lists.newArrayList();
			staticRes.getElements().forEach(o ->
					playerList.add(new PlayerEntity()
							.setElement(o.getId())
							.setCode(o.getCode())
							.setPrice(this.getPlayerCurrentPrice(playerValueMap.get(o.getId())))
							.setStartPrice(this.getPlayerStartPrice(playerValueMap.get(o.getId())))
							.setElementType(o.getElementType())
							.setFirstName(o.getFirstName())
							.setSecondName(o.getSecondName())
							.setWebName(o.getWebName())
							.setTeamId(o.getTeam())
					));
			this.playerService.saveOrUpdateBatch(playerList);
			log.info("insert player size:{}!", playerList.size());
			// set cache
			Map<String, Map<Object, Double>> cacheMap = Maps.newHashMap();
			String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
			Map<Object, Double> valueMap = Maps.newConcurrentMap();
			RedisUtils.removeCacheByKey(key);
			playerList.forEach(o -> valueMap.put(o, (double) o.getElement()));
			cacheMap.put(key, valueMap);
			RedisUtils.pipelineSortedSetCache(cacheMap, -1, null);
		});
	}

	private int getPlayerCurrentPrice(Collection<PlayerValueEntity> playerValues) {
		if (CollectionUtils.isEmpty(playerValues)) {
			return 0;
		}
		return playerValues
				.stream()
				.max(Comparator.comparing(PlayerValueEntity::getUpdateTime))
				.orElse(new PlayerValueEntity())
				.getValue();
	}

	private int getPlayerStartPrice(Collection<PlayerValueEntity> playerValues) {
		if (CollectionUtils.isEmpty(playerValues)) {
			return 0;
		}
		return playerValues
				.stream()
				.filter(o -> StringUtils.equals(o.getChangeType(), ValueChangeType.Start.name()))
				.map(PlayerValueEntity::getValue)
				.findFirst()
				.orElse(0);
	}

	@Override
	public void insertHisPlayer(String season) {
		MybatisPlusConfig.season.set(season);
		List<PlayerEntity> playerList = this.playerService.list();
		MybatisPlusConfig.season.remove();
		// set cache
		Map<String, Map<Object, Double>> cacheMap = Maps.newHashMap();
		String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
		Map<Object, Double> valueMap = Maps.newConcurrentMap();
		RedisUtils.removeCacheByKey(key);
		playerList.forEach(o -> valueMap.put(o, (double) o.getElement()));
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineSortedSetCache(cacheMap, -1, null);
	}

	@Override
	public void insertPlayerStat() {
		int event = this.getCurrentEvent();
		Map<Integer, Integer> insertTeamMap = this.getInsertTeamList(event);
		Optional<StaticRes> result = this.interfaceService.getBootstrapStatic();
		result.ifPresent(staticRes -> {
			List<PlayerStatEntity> playerStatList = Lists.newArrayList();
			staticRes.getElements().forEach(o -> {
				if (!insertTeamMap.containsKey(o.getTeam())) {
					return;
				}
				// insert table
				PlayerStatEntity playerStatEntity = new PlayerStatEntity();
				playerStatEntity.setEvent(event)
						.setElement(o.getId())
						.setMatchPlayed(insertTeamMap.get(o.getTeam()));
				BeanUtil.copyProperties(o, playerStatEntity, CopyOptions.create().ignoreNullValue());
				playerStatList.add(playerStatEntity);
			});
			this.playerStatService.saveBatch(playerStatList);
			log.info("insert player_stat size:{}", playerStatList.size());
			// set cache
			Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
			String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
			Map<String, Object> valueMap = Maps.newConcurrentMap();
			RedisUtils.removeCacheByKey(key);
			playerStatList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
			cacheMap.put(key, valueMap);
			RedisUtils.pipelineHashCache(cacheMap, -1, null);
		});
	}

	private Map<Integer, Integer> getInsertTeamList(int event) {
		Map<Integer, Integer> insertTeamMap = Maps.newHashMap();
		IntStream.rangeClosed(1, 20).forEach(teamId -> {
			// match_played
			int matchPlayed = this.eventFixtureService.count(new QueryWrapper<EventFixtureEntity>().lambda()
					.eq(EventFixtureEntity::getFinished, 1)
					.and(a -> a.eq(EventFixtureEntity::getTeamH, teamId)
							.or(i -> i.eq(EventFixtureEntity::getTeamA, teamId)))
			);
			this.playerStatService.remove(new QueryWrapper<PlayerStatEntity>().lambda()
					.eq(PlayerStatEntity::getMatchPlayed, matchPlayed)
					.eq(PlayerStatEntity::getEvent, event)
			);
			insertTeamMap.put(teamId, matchPlayed);
		});
		return insertTeamMap;
	}

	@Override
	public void insertHisPlayerStat(String season) {
		MybatisPlusConfig.season.set(season);
		List<PlayerStatEntity> playerStatList = this.playerStatService.list();
		MybatisPlusConfig.season.remove();
		// set cache
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), season);
		Map<String, Object> valueMap = Maps.newConcurrentMap();
		RedisUtils.removeCacheByKey(key);
		playerStatList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineHashCache(cacheMap, -1, null);
	}

	@Override
	public void insertPlayerValue() {
		Optional<StaticRes> result = this.interfaceService.getBootstrapStatic();
		result.ifPresent(staticRes -> {
			// insert table
			String changeDate = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
			this.playerValueService.remove(new QueryWrapper<PlayerValueEntity>().lambda()
					.eq(PlayerValueEntity::getChangeDate, changeDate));
			Map<Integer, PlayerValueEntity> lastValueMap = this.playerValueService.list()
					.stream()
					.collect(new PlayerValueCollector());
			List<PlayerValueEntity> playerValueList = Lists.newArrayList();
			staticRes.getElements()
					.stream()
					.filter(o -> !lastValueMap.containsKey(o.getId()) || o.getNowCost() != lastValueMap.get(o.getId()).getValue())
					.forEach(bootstrapPlayer -> {
						int element = bootstrapPlayer.getId();
						PlayerValueEntity lastEntity = lastValueMap.getOrDefault(element, null);
						int lastValue = lastEntity != null ? lastEntity.getValue() : 0;
						playerValueList.add(new PlayerValueEntity()
								.setElement(element)
								.setElementType(bootstrapPlayer.getElementType())
								.setEvent(this.getCurrentEvent())
								.setValue(bootstrapPlayer.getNowCost())
								.setChangeDate(changeDate)
								.setChangeType(this.getChangeType(bootstrapPlayer.getNowCost(), lastValue))
								.setLastValue(lastValue)
						);
					});
			this.playerValueService.saveBatch(playerValueList);
			log.info("insert player value size:{}", playerValueList.size());
			// set cache
			Map<String, Set<Object>> cacheMap = Maps.newHashMap();
			String key = StringUtils.joinWith("::", PlayerValueEntity.class.getSimpleName(), changeDate);
			Set<Object> valueSet = Sets.newHashSet();
			valueSet.addAll(playerValueList);
			cacheMap.put(key, valueSet);
			RedisUtils.pipelineSetCache(cacheMap, 1, TimeUnit.DAYS);
			// update price in table player
			this.updatePriceOfPlayer(playerValueList);
		});
	}

	@Override
	public void insertEventLive(int event) {
		this.eventLiveService.remove(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event));
		Map<Integer, PlayerEntity> playerMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
		List<EventLiveEntity> eventLiveList = Lists.newArrayList();
		Optional<EventLiveRes> result = this.interfaceService.getEventLive(event);
		result.ifPresent(eventLiveRes ->
				eventLiveRes.getElements().forEach(o -> {
					int element = o.getId();
					ElementStat elementStat = o.getStats();
					EventLiveEntity eventLive = new EventLiveEntity();
					BeanUtil.copyProperties(elementStat, eventLive, CopyOptions.create().ignoreNullValue());
					eventLive
							.setElement(element)
							.setElementType(playerMap.containsKey(element) ?
									playerMap.get(element).getElementType() : 0)
							.setTeamId(playerMap.containsKey(element) ? playerMap.get(element).getTeamId() : 0)
							.setEvent(event);
					eventLiveList.add(eventLive);
				}));
		this.eventLiveService.saveBatch(eventLiveList);
		log.info("insert event_live size is " + eventLiveList.size() + "!");
		// set cache
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		Map<String, Object> valueMap = Maps.newHashMap();
		String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
		RedisUtils.removeCacheByKey(key);
		eventLiveList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineHashCache(cacheMap, 7, TimeUnit.DAYS);
	}

	@Override
	public void insertEventLiveCache(int event) {
		Map<Integer, PlayerEntity> playerMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
		List<EventLiveEntity> eventLiveList = Lists.newArrayList();
		Optional<EventLiveRes> result = this.interfaceService.getEventLive(event);
		result.ifPresent(eventLiveRes ->
				eventLiveRes.getElements().forEach(o -> {
					int element = o.getId();
					ElementStat elementStat = o.getStats();
					EventLiveEntity eventLive = new EventLiveEntity();
					BeanUtil.copyProperties(elementStat, eventLive, CopyOptions.create().ignoreNullValue());
					eventLive.setElement(element)
							.setElementType(playerMap.containsKey(element) ? playerMap.get(element).getElementType() : 0)
							.setTeamId(playerMap.containsKey(element) ? playerMap.get(element).getTeamId() : 0)
							.setEvent(event);
					eventLiveList.add(eventLive);
				}));
		log.info("event_live_cache size is " + eventLiveList.size() + "!");
		// set cache
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		Map<String, Object> valueMap = Maps.newHashMap();
		String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
		RedisUtils.removeCacheByKey(key);
		eventLiveList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineHashCache(cacheMap, -1, null);
	}

	@Override
	public void insertLiveBonusCache() {
		// get playing fixtures
		String liveStatus = MatchPlayStatus.Playing.name();
		Map<Integer, Integer> livePlayingMap = Maps.newHashMap(); // key:team -> value:against_team
		this.getEventLiveFixtureMap().forEach((teamIdStr, map) -> {
			int teamId = Integer.parseInt(teamIdStr);
			map.keySet().forEach(status -> {
				List<LiveFixtureData> liveFixtureList = map.get(liveStatus);
				if (!StringUtils.equals(status, liveStatus) || CollectionUtils.isEmpty(liveFixtureList)) {
					return;
				}
				if (livePlayingMap.containsKey(teamId)) {
					return;
				}
				livePlayingMap.put(teamId, liveFixtureList.get(0).getAgainstId());
				livePlayingMap.put(liveFixtureList.get(0).getAgainstId(), teamId);
			});
		});
		// get event_live
		List<Integer> bonusInTeamList = Lists.newArrayList();
		Map<Integer, List<EventLiveEntity>> teamEventLiveMap = Maps.newHashMap();
		this.getEventLiveByEvent(this.getCurrentEvent()).values()
				.forEach(o -> {

					if (o.getMinutes() <= 0) {
						return;
					}
					int teamId = o.getTeamId();
					if (!livePlayingMap.containsKey(teamId)) {
						return;
					}
					int againstId = livePlayingMap.get(teamId);
					// check bonus in
					if (o.getBonus() > 0) {
						if (teamEventLiveMap.containsKey(teamId) && !bonusInTeamList.contains(teamId)) {
							bonusInTeamList.add(teamId);
						}
						if (teamEventLiveMap.containsKey(againstId) && !bonusInTeamList.contains(againstId)) {
							bonusInTeamList.add(againstId);
						}
						return;
					}
					// home team
					List<EventLiveEntity> homeList = Lists.newArrayList();
					if (teamEventLiveMap.containsKey(teamId)) {
						homeList = teamEventLiveMap.get(teamId);
					}
					homeList.add(o);
					teamEventLiveMap.put(teamId, homeList);
					// away team
					List<EventLiveEntity> awayList = Lists.newArrayList();
					if (teamEventLiveMap.containsKey(againstId)) {
						awayList = teamEventLiveMap.get(againstId);
					}
					awayList.add(o);
					teamEventLiveMap.put(againstId, awayList);
				});
		//  set cache
		String key = StringUtils.joinWith("::", "LiveBonusData");
		RedisUtils.removeCacheByKey(key);
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		Map<String, Object> valueMap = Maps.newHashMap();
		teamEventLiveMap.keySet().forEach(teamId -> {
			if (bonusInTeamList.contains(teamId)) {
				return;
			}
			// sort teamEventLiveMap by bps
			List<EventLiveEntity> sortList = teamEventLiveMap.get(teamId)
					.stream()
					.sorted(Comparator.comparing(EventLiveEntity::getBps).reversed())
					.collect(Collectors.toList());
			// set bonus points
			Map<Integer, Integer> bonusMap = this.setBonusPoints(teamId, sortList);
			if (!CollectionUtils.isEmpty(bonusMap)) {
				valueMap.put(String.valueOf(teamId), bonusMap);
			}
		});
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineHashCache(cacheMap, -1, null);
	}

	private Map<Integer, Integer> setBonusPoints(int teamId, List<EventLiveEntity> sortList) {
		int count = 0;
		Map<Integer, Integer> bonusMap = Maps.newConcurrentMap();
		// 最高分
		EventLiveEntity first = sortList.get(0);
		int highestBps = first.getBps();
		this.setBonusMap(teamId, first, 3, bonusMap);
		count += 1;
		// bps同分
		List<EventLiveEntity> firstList = sortList
				.stream()
				.filter(o -> !o.getElement().equals(first.getElement()))
				.filter(o -> o.getBps() == highestBps)
				.collect(Collectors.toList());
		for (EventLiveEntity eventLiveEntity :
				firstList) {
			count += 1;
			this.setBonusMap(teamId, eventLiveEntity, 3, bonusMap);
		}
		if (count >= 3) {
			return bonusMap;
		}
		// 次高分
		if (count < 2) {
			EventLiveEntity second = sortList.get(count);
			int runnerUpBps = second.getBps();
			this.setBonusMap(teamId, second, 2, bonusMap);
			count += 1;
			// bps同分
			List<EventLiveEntity> secondList = sortList
					.stream()
					.filter(o -> !o.getElement().equals(second.getElement()))
					.filter(o -> o.getBps() == runnerUpBps)
					.collect(Collectors.toList());
			for (EventLiveEntity eventLiveEntity :
					secondList) {
				count += 1;
				this.setBonusMap(teamId, eventLiveEntity, 2, bonusMap);
			}
			if (count >= 3) {
				return bonusMap;
			}
		}
		// 第三高分
		EventLiveEntity third = sortList.get(count);
		int secondRunnerUpBps = third.getBps();
		this.setBonusMap(teamId, third, 1, bonusMap);
		count += 1;
		// bps同分
		List<EventLiveEntity> thirdList = sortList
				.stream()
				.filter(o -> !o.getElement().equals(third.getElement()))
				.filter(o -> o.getBps() == secondRunnerUpBps)
				.collect(Collectors.toList());
		for (EventLiveEntity eventLiveEntity :
				thirdList) {
			count += 1;
			this.setBonusMap(teamId, eventLiveEntity, 1, bonusMap);
		}
		return bonusMap;
	}

	private void setBonusMap(int teamId, EventLiveEntity eventLiveEntity, int bonus, Map<Integer, Integer> bonusMap) {
		if (teamId != eventLiveEntity.getTeamId()) {
			return;
		}
		bonusMap.put(eventLiveEntity.getElement(), bonus);
	}

	@Override
	public void insertPosition() {
		String key = StringUtils.joinWith("::", Position.class.getSimpleName());
		Map<String, Object> valueMap = Arrays.stream(Position.values())
				.collect(Collectors.toMap(k -> String.valueOf(k.getElementType()), Position::name));
		RedisUtils.removeCacheByKey(key);
		Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
		cacheMap.put(key, valueMap);
		RedisUtils.pipelineHashCache(cacheMap, -1, null);
	}

	@Override
	public void insertDiscloseCache(int tournamentId, int captainGroupId, int entry) {
		String key = StringUtils.joinWith("::", "DiscloseList", tournamentId, captainGroupId);
		RedisUtils.removeCacheByKey(key);
		Map<String, Object> cacheMap = Maps.newHashMap();
		List<Integer> list = this.getDiscloseList(tournamentId, captainGroupId);
		if (!list.contains(entry)) {
			list.add(entry);
		}
		cacheMap.put(key, JsonUtils.obj2json(list));
		RedisUtils.pipelineValueCache(cacheMap, -1, null);
	}

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
	public int getNextEvent() {
		int event = 1;
		for (int i = 1; i < 39; i++) {
			String deadline = this.getDeadlineByEvent(i);
			if (LocalDateTime.now().isAfter(LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
				event = i;
			} else {
				break;
			}
		}
		return event + 1;
	}

	private String getChangeType(int nowCost, int lastCost) {
		if (lastCost == 0) {
			return ValueChangeType.Start.name();
		}
		return nowCost > lastCost ? ValueChangeType.Rise.name() : ValueChangeType.Faller.name();
	}

	private void updatePriceOfPlayer(List<PlayerValueEntity> playerValueList) {
		List<PlayerEntity> updatePlayerList = Lists.newArrayList();
		playerValueList.forEach(o -> {
			// update table
			PlayerEntity playerEntity = this.playerService.getById(o.getElement());
			playerEntity.setPrice(o.getValue());
			if (playerEntity.getStartPrice() == 0) {
				// start price
				PlayerValueEntity playerValueEntity = this.playerValueService.getOne(new QueryWrapper<PlayerValueEntity>().lambda()
						.eq(PlayerValueEntity::getElement, o.getElement())
						.eq(PlayerValueEntity::getChangeType, ValueChangeType.Start.name()));
				if (playerValueEntity != null) {
					playerEntity.setStartPrice(playerValueEntity.getValue());
				}
			}
			updatePlayerList.add(playerEntity);
			// set cache
			String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
			this.redisTemplate.opsForZSet().removeRangeByScore(key, o.getElement(), o.getElement());
			this.redisTemplate.opsForZSet().add(key, playerEntity, o.getElement());
		});
		this.playerService.updateBatchById(updatePlayerList);
	}

	@Override
	public Map<String, String> getTeamNameMap(String season) {
		Map<String, String> map = Maps.newHashMap();
		String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "name");
		this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (String) v));
		return map;
	}

	@Override
	public Map<String, String> getTeamShortNameMap(String season) {
		Map<String, String> map = Maps.newHashMap();
		String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "shortName");
		this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (String) v));
		return map;
	}

	@Override
	public String getDeadlineByEvent(String season, int event) {
		String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
		return (String) this.redisTemplate.opsForHash().get(key, String.valueOf(event));
	}

	@Override
	public List<EventFixtureEntity> getEventFixtureByEvent(String season, int event) {
		List<EventFixtureEntity> list = Lists.newArrayList();
		String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "event", event);
		Set<Object> set = this.redisTemplate.opsForSet().members(key);
		if (CollectionUtils.isEmpty(set)) {
			return list;
		}
		set.forEach(o -> list.add((EventFixtureEntity) o));
		return list.stream().sorted(Comparator.comparing(EventFixtureEntity::getId)).collect(Collectors.toList());
	}

	@Override
	public Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId) {
		Map<String, List<PlayerFixtureData>> map = Maps.newHashMap();
		String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
		this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), v == null ? Lists.newArrayList() : (List<PlayerFixtureData>) v));
		return map;
	}

	@Override
	public Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap() {
		Map<String, Map<String, List<LiveFixtureData>>> map = Maps.newHashMap();
		String key = StringUtils.joinWith("::", LiveFixtureData.class.getSimpleName());
		this.redisTemplate.opsForHash().keys(key).forEach(teamId ->
				map.put(teamId.toString(), (Map<String, List<LiveFixtureData>>) this.redisTemplate.opsForHash().get(key, teamId)));
		return map;
	}

	@Override
	public PlayerEntity getPlayerByElement(String season, int element) {
		String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
		Set<Object> set = this.redisTemplate.opsForZSet().rangeByScore(key, element, element);
		if (CollectionUtils.isEmpty(set)) {
			return null;
		}
		return set.stream().map(PlayerEntity.class::cast).findFirst().orElse(null);
	}

	@Override
	public PlayerStatEntity getPlayerStatByElement(String season, int element) {
		String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), season);
		return (PlayerStatEntity) this.redisTemplate.opsForHash().get(key, String.valueOf(element));
	}

	@Override
	public List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay) {
		List<PlayerValueEntity> list = Lists.newArrayList();
		String key = StringUtils.joinWith("::", PlayerValueEntity.class.getSimpleName(), changeDay);
		Set<Object> set = this.redisTemplate.opsForSet().members(key);
		if (CollectionUtils.isEmpty(set)) {
			return list;
		}
		set.forEach(o -> list.add((PlayerValueEntity) o));
		return list;
	}

	@Override
	public Map<String, EventLiveEntity> getEventLiveByEvent(int event) {
		Map<String, EventLiveEntity> map = Maps.newHashMap();
		String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
		this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (EventLiveEntity) v));
		return map;
	}

	@Override
	public Map<String, String> getPositionMap() {
		Map<String, String> map = Maps.newHashMap();
		String key = StringUtils.joinWith("::", Position.class.getSimpleName());
		this.redisTemplate.opsForHash().entries(key).forEach((k, v) ->
				map.put(String.valueOf(k), String.valueOf(v)));
		return map;
	}

	@Override
	public Map<String, Map<String, Integer>> getLiveBonusCacheMap() {
		Map<String, Map<String, Integer>> map = Maps.newHashMap();
		String key = StringUtils.joinWith("::", "LiveBonusData");
		this.redisTemplate.opsForHash().entries(key).forEach((k, v) ->
				map.put(String.valueOf(k), (Map<String, Integer>) v));
		return map;
	}

	@Override
	public List<Integer> getDiscloseList(int tournamentId, int captainGroupId) {
		String key = StringUtils.joinWith("::", "DiscloseList", tournamentId, captainGroupId);
		if (Objects.equals(this.redisTemplate.hasKey(key), false)) {
			return Lists.newArrayList();
		}
		return JsonUtils.json2Collection((String) this.redisTemplate.opsForValue().get(key), List.class, Integer.class);
	}

}
