package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.service.ICacheSerive;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CacheServiceImpl implements ICacheSerive {

	private final RedisTemplate<String, Object> redisTemplate;
	private final IInterfaceService interfaceService;
	private final TeamService teamNameService;
	private final EventService eventService;
	private final EventFixtureService eventFixtureService;
	private final PlayerService playerService;
	private final PlayerValueService playerValueService;

	@Override
	public void insertTeam() {
		Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
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
			log.info("insert team name size is " + teamList.size() + "!");
			// set cache
			Map<String, Object> cacheMap = Maps.newHashMap();
			teamList.forEach(teamNameEntity ->
					cacheMap.put(createKey(teamNameEntity, "id", teamNameEntity.getId()), teamNameEntity));
			this.setCache(cacheMap, -1);
		});
	}

	@Override
	public void insertHisTeam(String season) {
		MybatisPlusConfig.season.set(season);
		List<TeamEntity> teamList = this.teamNameService.list();
		MybatisPlusConfig.season.remove();
		Map<String, Object> cacheMap = Maps.newHashMap();
		teamList.forEach(teamEntity ->
				cacheMap.put(createKey(season, teamEntity, "id", teamEntity.getId()), teamEntity));
		this.setCache(cacheMap, -1);
	}

	@Override
	public void insertEvent() {
		Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
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
			log.info("insert event size:{}", eventList.size());
			// set cache
			Map<String, Object> cacheMap = Maps.newHashMap();
			eventList.forEach(eventEntity ->
					cacheMap.put(createKey(eventEntity, "id", eventEntity.getId()), eventEntity));
			this.setCache(cacheMap, -1);
		});
	}

	@Override
	public void insertHisEvent(String season) {
		MybatisPlusConfig.season.set(season);
		List<EventEntity> eventList = this.eventService.list();
		MybatisPlusConfig.season.remove();
		Map<String, Object> cacheMap = Maps.newHashMap();
		eventList.forEach(eventEntity ->
				cacheMap.put(createKey(season, eventEntity, "id", eventEntity.getId()), eventEntity));
		this.setCache(cacheMap, -1);
	}

	@Override
	public void insertPlayer() {
		Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
		result.ifPresent(staticRes -> {
			// insert table
			this.playerService.remove(new QueryWrapper<PlayerEntity>().eq("1", 1));
			List<PlayerEntity> playerList = Lists.newArrayList();
			staticRes.getElements().forEach(bootstrapPlayer -> {
				PlayerEntity playerEntity = new PlayerEntity();
				playerEntity.setPrice(this.getPlayerCurrentPrice(bootstrapPlayer.getId()));
				BeanUtil.copyProperties(bootstrapPlayer, playerEntity, CopyOptions.create().ignoreNullValue());
				playerEntity.setElement(bootstrapPlayer.getId());
				playerEntity.setTeamId(bootstrapPlayer.getTeam());
				playerList.add(playerEntity);
			});
			this.playerService.saveBatch(playerList);
			log.info("insert player size is " + playerList.size() + "!");
			// set cache
			Map<String, Object> cacheMap = Maps.newHashMap();
			playerList.forEach(playerEntity ->
					cacheMap.put(createKey(playerEntity, "element", playerEntity.getElement()), playerEntity));
			this.setCache(cacheMap, 86400); // expire in 1 day
		});
	}

	@Override
	public void insertHisPlayer(String season) {
		MybatisPlusConfig.season.set(season);
		List<PlayerEntity> playerList = this.playerService.list();
		MybatisPlusConfig.season.remove();
		Map<String, Object> cacheMap = Maps.newHashMap();
		playerList.forEach(playerEntity ->
				cacheMap.put(createKey(season, playerEntity, "element", playerEntity.getElement()), playerEntity));
		this.setCache(cacheMap, -1);
	}

	private int getPlayerCurrentPrice(int element) {
		List<PlayerValueEntity> playerValueEntityList = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>()
				.lambda()
				.eq(PlayerValueEntity::getElement, element)
				.orderByDesc(PlayerValueEntity::getUpdateTime));
		if (CollectionUtils.isEmpty(playerValueEntityList)) {
			return 0;
		}
		return playerValueEntityList.get(0).getValue();
	}

	@Override
	public void insertEventFixture() {
		int currentEvent = CommonUtils.getCurrentEvent();
		IntStream.range(currentEvent, 39).forEach(this::insertEventFixtureByEvent);
	}

	private void insertEventFixtureByEvent(int event) {
		log.info("start insert gw{} fixtures!", event);
		this.eventFixtureService.remove(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event));
		List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
		Optional<List<EventFixturesRes>> eventFixtureResList = this.interfaceService.getEventFixture(event);
		eventFixtureResList.ifPresent(list -> {
			list.forEach(o -> {
				EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
				BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
				eventFixtureEntity.setKickoffTime(CommonUtils.getZoneDate(o.getKickoffTime()));
				eventFixtureList.add(eventFixtureEntity);
				// set cache
				String key = this.createKey(eventFixtureEntity, "id", eventFixtureEntity.getId());
				this.redisTemplate.opsForValue().set(key, eventFixtureEntity);
				this.redisTemplate.persist(key);
			});
			this.eventFixtureService.saveBatch(eventFixtureList);
			log.info("insert event_fixture size is " + eventFixtureList.size() + "!");
			// set cache
//			Map<String, Object> cacheMap = Maps.newHashMap();
//			eventFixtureList.forEach(teamNameEntity -> cacheMap.put(createKey(teamNameEntity, "teamId", teamNameEntity.getTeamId()), teamNameEntity));
//			this.SetPersistCache(cacheMap);
		});
	}

	@Override
	public void insertPlayerValue() {

	}

	private <T> String createKey(T entity, String keyName, Object key) {
		return createKey(CommonUtils.getCurrentSeason(), entity, keyName, key);
	}

	private <T> String createKey(String season, T entity, String keyName, Object key) {
		return StringUtils.joinWith("::", entity.getClass().getSimpleName(), keyName, season, key);
	}

	private void setCache(Map<String, Object> cacheMap, long expireSeconds) {
		this.redisTemplate.opsForValue().get("");
		this.redisTemplate.executePipelined(new RedisCallback<Object>() {
			@Nullable
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.openPipeline();
				RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
				RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
				cacheMap.forEach((k, v) -> {
					byte[] serializedKey = keySerializer.serialize(k);
					byte[] serializedValue = valueSerializer.serialize(v);
					if (serializedKey == null || serializedValue == null) {
						return;
					}
					connection.set(serializedKey, serializedValue);
					if (expireSeconds == -1) {
						connection.persist(k.getBytes());
					} else {
						connection.expire(k.getBytes(), expireSeconds);
					}
				});
				connection.close();
				return null;
			}
		});
		log.info("insert cache:{}", cacheMap.size());
	}

}
