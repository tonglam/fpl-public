package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TeamEntity;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.TeamService;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/5/10
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiQueryServiceImpl implements IApiQueryService {

	private final IQueryService queryService;
	private final IRedisCacheService redisCacheService;
	private final TeamService teamService;
	private final PlayerService playerService;

	/**
	 * @implNote common
	 */

	@Cacheable(value = "api::qryCurrentEventAndNextUtcDeadline", cacheManager = "apiCacheManager")
	@Override
	public Map<String, String> qryCurrentEventAndNextUtcDeadline() {
		Map<String, String> map = Maps.newHashMap();
		int event = this.queryService.getCurrentEvent();
		map.put("event", String.valueOf(event));
		String utcDeadline = this.queryService.getUtcDeadlineByEvent(event + 1);
		map.put("utcDeadline", utcDeadline);
		return map;
	}

	/**
	 * @implNote entry
	 */

	@Cacheable(value = "api::qryEntryInfoData", key = "#entry", cacheManager = "apiCacheManager")
	@Override
	public EntryInfoData qryEntryInfoData(int entry) {
		EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(CommonUtils.getCurrentSeason(), entry);
		if (entryInfoEntity == null) {
			return new EntryInfoData();
		}
		return BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class);
	}

	/**
	 * @implNote league
	 */

	/**
	 * @implNote live
	 */

	@Cacheable(value = "api::qryLiveFixtureByStatus", key = "#playStatus", cacheManager = "apiCacheManager")
	@Override
	public List<LiveMatchData> qryLiveFixtureByStatus(String playStatus) {
		List<LiveMatchData> list = Lists.newArrayList();
		Map<String, Map<String, List<LiveFixtureData>>> eventLiveFixtureMap = this.redisCacheService.getEventLiveFixtureMap();
		eventLiveFixtureMap.keySet().forEach(teamId ->
				eventLiveFixtureMap.get(teamId).forEach((status, fixtureList) -> {
					if (!StringUtils.equals(status, playStatus)) {
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
	public List<LiveMatchTeamData> qryLiveMatchDataByStatus(String playStatus) {
		List<LiveMatchTeamData> list = Lists.newArrayList();
		return list;
	}

	/**
	 * @implNote player
	 */

	@Cacheable(value = "api::qryPlayerInfoByElementType", key = "#elementType", cacheManager = "apiCacheManager")
	@Override
	public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType) {
		LinkedHashMap<String, List<PlayerInfoData>> map = Maps.newLinkedHashMap();
		Multimap<String, PlayerInfoData> multimap = HashMultimap.create();
		// prepare
		Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
		Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
		// init
		this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
				.eq(PlayerEntity::getElementType, elementType))
				.forEach(o -> {
					PlayerInfoData data = BeanUtil.copyProperties(o, PlayerInfoData.class);
					data.setElementTypeName(Position.getNameFromElementType(data.getElementType()))
							.setTeamName(teamNameMap.getOrDefault(String.valueOf(data.getTeamId()), ""))
							.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(data.getTeamId()), ""))
							.setPrice(data.getPrice() / 10);
					multimap.put(data.getTeamShortName(), data);
				});
		// collect
		List<String> shortNameSortedList = multimap.keySet()
				.stream()
				.sorted(Comparator.comparing(String::toUpperCase))
				.collect(Collectors.toList());
		shortNameSortedList.forEach(team ->
				map.put(team, multimap.get(team)
						.stream()
						.sorted(Comparator.comparing(PlayerInfoData::getPrice).reversed())
						.collect(Collectors.toList())));
		return map;
	}

	@Cacheable(value = "api::qryPlayerDetailData", key = "#element", cacheManager = "apiCacheManager")
	@Override
	public PlayerDetailData qryPlayerDetailData(int element) {
		return this.queryService.qryPlayerDetailData(element);
	}

	@Cacheable(value = "api::qryTeamFixtureByShortName", key = "#shortName", cacheManager = "apiCacheManager")
	@Override
	public Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName) {
		int teamId = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda()
				.eq(TeamEntity::getShortName, shortName))
				.getId();
		return this.queryService.getEventFixtureByTeamId(teamId);
	}

	/**
	 * @implNote report
	 */

	/**
	 * @implNote scout
	 */

	@Cacheable(value = "api::qryScoutEntry", cacheManager = "apiCacheManager")
	@Override
	public Map<String, String> qryScoutEntry() {
		Map<String, String> map = Maps.newHashMap();
		RedisUtils.getHashByKey("scoutEntry").forEach((k, v) -> map.put(k.toString(), v.toString()));
		return map;
	}

	/**
	 * @implNote tournament
	 */
}
