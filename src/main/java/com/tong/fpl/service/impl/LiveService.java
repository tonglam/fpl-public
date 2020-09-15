package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.config.collector.ElementLiveCollector;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.constant.enums.PositionRule;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.letletme.live.ElementLiveData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.service.ILiveService;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.EntryInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create by tong on 2020/7/13
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveService implements ILiveService {

	private final IQuerySerivce querySerivce;
	private final EntryInfoService entryInfoService;

	@Override
	public LiveCalaData calcLivePointsByEntry(int event, int entry) {
		LiveCalaData liveCalaData = new LiveCalaData();
		// get user picks
		Optional<UserPicksRes> result = this.querySerivce.getUserPicks(event, entry);
		if (result.isPresent()) {
			liveCalaData = this.calcLiveSingleEntryPoints(event, entry, result.get(), Maps.newHashMap());
		}
		// entry info
		EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(entry);
		if (entryInfoEntity != null) {
			liveCalaData
					.setEntryName(entryInfoEntity.getEntryName())
					.setPlayerName(entryInfoEntity.getPlayerName())
					.setRegion(entryInfoEntity.getRegion());
		}
		return liveCalaData;
	}

	private LiveCalaData calcLiveSingleEntryPoints(int event, int entry, UserPicksRes userPicksRes, Map<Integer, PlayerEntity> playerInfoMap) {
		// initialize element_live_data, static part
		List<ElementLiveData> elementLiveDataList = this.initElemetLiveStaticData(event, entry, userPicksRes.getPicks(), playerInfoMap);
		// initialize element_live_data, event_live part
		this.initEventLiveData(event, elementLiveDataList);
		// get active picks
		List<ElementLiveData> pickList = this.getPickList(elementLiveDataList);
		// calc live points
		int livePoints = this.calcActivePoints(Chip.getChipFromValue(userPicksRes.getActiveChip()), pickList);
		return new LiveCalaData()
				.setEntry(entry)
				.setEvent(event)
				.setPickList(pickList)
				.setChip(userPicksRes.getActiveChip())
				.setLivePoints(livePoints)
				.setTransferCost(userPicksRes.getEntryHistory().getEventTransfersCost())
				.setLiveNetPoints(livePoints - userPicksRes.getEntryHistory().getEventTransfersCost());
	}

	@Cacheable(value = "initElemetLiveStaticData", key = "#event+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result == null")
	public List<ElementLiveData> initElemetLiveStaticData(int event, int entry, List<Pick> picks, Map<Integer, PlayerEntity> playerInfoMap) {
		List<ElementLiveData> elementLiveDataList = Lists.newArrayList();
		Map<String, String> positionMap = this.querySerivce.getPositionMap();
		picks.forEach(pick -> {
			int element = pick.getElement();
			// from user pick
			ElementLiveData elementLiveData = new ElementLiveData();
			elementLiveData
					.setEvent(event)
					.setElement(element)
					.setPosition(pick.getPosition())
					.setMultiplier(pick.getMultiplier())
					.setCaptain(pick.isCaptain())
					.setViceCaptain(pick.isViceCaptain());
			// player info
			PlayerEntity playerEntity;
			if (playerInfoMap.containsKey(element)) {
				playerEntity = playerInfoMap.get(element);
			} else {
				playerEntity = this.querySerivce.getPlayerByElememt(element);
			}
			if (playerEntity != null) {
				elementLiveData
						.setElementType(playerEntity.getElementType())
						.setElementTypeName(positionMap.get(String.valueOf(playerEntity.getElementType())))
						.setWebName(playerEntity.getWebName());
				// event fixture
				int teamId = playerEntity.getTeamId();
				elementLiveData.setTeamId(teamId);
				this.setMatchPlayStatus(elementLiveData, event, teamId);
			}
			elementLiveDataList.add(elementLiveData);
		});
		return elementLiveDataList;
	}

	private void initEventLiveData(int event, List<ElementLiveData> elementLiveDataList) {
		Map<String, EventLiveEntity> eventLiveMap = this.querySerivce.getEventLiveByEvent(event);
		if (CollectionUtils.isEmpty(eventLiveMap)) {
			return;
		}
		elementLiveDataList.forEach(elementLiveData -> {
			EventLiveEntity eventLiveEntity = eventLiveMap.get(String.valueOf(elementLiveData.getElement()));
			if (eventLiveEntity != null) {
				BeanUtil.copyProperties(eventLiveEntity, elementLiveData, CopyOptions.create().ignoreNullValue());
				elementLiveData.setPlayed(elementLiveData.getMinutes() > 0 || elementLiveData.getYellowCards() > 0 || elementLiveData.getRedCards() > 0);
			}
		});
	}

	private void setMatchPlayStatus(ElementLiveData elementLiveData, int event, int teamId) {
		Map<Integer, LiveFixtureData> liveFixtureMap = this.querySerivce.getEventLiveFixtureMap();

	}

	private List<ElementLiveData> getPickList(List<ElementLiveData> elementLiveDataList) {
		// element_type -> active -> start
		Map<Integer, Table<Boolean, Boolean, List<ElementLiveData>>> map = elementLiveDataList.stream().collect(new ElementLiveCollector());
		// gkp
		List<ElementLiveData> gkps = this.createSteam(map.get(Position.GKP.getPosition()).get(true, true),
				map.get(Position.GKP.getPosition()).get(true, false),
				map.get(Position.GKP.getPosition()).get(false, true))
				.flatMap(Collection::stream)
				.limit(PositionRule.MIN_NUM_GKP.getNum())
				.collect(Collectors.toList());
		// active defs
		List<ElementLiveData> defs = this.createSteam(map.get(Position.DEF.getPosition()).get(true, true))
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(ElementLiveData::getPosition))
				.collect(Collectors.toList());
		// def rule, at least 3
		if (defs.size() < PositionRule.MIN_NUM_DEF.getNum()) {
			defs = this.createSteam(defs,
					map.get(Position.DEF.getPosition()).get(true, false),
					map.get(Position.DEF.getPosition()).get(false, true))
					.flatMap(Collection::stream)
					.limit(PositionRule.MIN_NUM_DEF.getNum())
					.sorted(Comparator.comparing(ElementLiveData::getPosition))
					.collect(Collectors.toList());
		}
		// active fwds
		List<ElementLiveData> fwds = this.createSteam(map.get(Position.FWD.getPosition()).get(true, true))
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(ElementLiveData::getPosition))
				.collect(Collectors.toList());
		// fwd rule, at least 1
		if (fwds.size() < PositionRule.MIN_NUM_FWD.getNum()) {
			fwds = this.createSteam(fwds,
					map.get(Position.FWD.getPosition()).get(true, false),
					map.get(Position.FWD.getPosition()).get(false, true))
					.flatMap(Collection::stream)
					.limit(PositionRule.MIN_NUM_FWD.getNum())
					.collect(Collectors.toList());
		}
		// mids
		int maxMidNum = PositionRule.MIN_PLAYERS.getNum() - gkps.size() - defs.size() - fwds.size();
		List<ElementLiveData> mids = this.createSteam(map.get(Position.MID.getPosition()).get(true, true),
				map.get(Position.MID.getPosition()).get(true, false))
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(ElementLiveData::getPosition))
				.limit(maxMidNum)
				.collect(Collectors.toList());
		// active_list
		List<ElementLiveData> activeList = this.createSteam(gkps, defs, fwds, mids)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		List<ElementLiveData> standByList = this.createSteam(map.get(Position.DEF.getPosition()).get(false, true),
				map.get(Position.MID.getPosition()).get(false, true),
				map.get(Position.FWD.getPosition()).get(false, true))
				.flatMap(Collection::stream)
				.filter(o -> !activeList.contains(o))
				.sorted(Comparator.comparing(ElementLiveData::getPosition))
				.limit(PositionRule.MIN_PLAYERS.getNum() - activeList.size())
				.collect(Collectors.toList());
		List<ElementLiveData> pickList = this.createSteam(activeList, standByList)
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(ElementLiveData::getElementType).thenComparing(ElementLiveData::getPosition))
				.collect(Collectors.toList());
		pickList.addAll(elementLiveDataList.stream()
				.filter(o -> !activeList.contains(o))
				.sorted(Comparator.comparing(ElementLiveData::getPosition))
				.collect(Collectors.toList()));
		return pickList;
	}

	private int calcActivePoints(Chip chip, List<ElementLiveData> pickList) {
		int activeCaptain = this.getActiveCaptain(pickList);
		if (activeCaptain == 0) {
			return 0;
		}
		switch (chip) {
			case TC:
				pickList.subList(0, 11).forEach(o -> o.setPickAvtive(true));
				return pickList.subList(0, 11).stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementLiveData::getTotalPoints).sum()
						+ pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 3 * o.getTotalPoints()).sum();
			case BB:
				pickList.forEach(o -> o.setPickAvtive(true));
				return pickList.stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementLiveData::getTotalPoints).sum()
						+ pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 2 * o.getTotalPoints()).sum();
			// only 3c and bb change the calculate rule
			case NONE:
			case WC:
			case FH:
				pickList.subList(0, 11).forEach(o -> o.setPickAvtive(true));
				return pickList.subList(0, 11).stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementLiveData::getTotalPoints).sum()
						+ pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 2 * o.getTotalPoints()).sum();
			default:
				return 0;
		}
	}

	private int getActiveCaptain(List<ElementLiveData> pickList) {
		// captain played
		int activeCaptain = pickList.stream()
				.filter(ElementLiveData::isCaptain)
				.filter(o -> !o.isGwStarted() || o.isPlayed())
				.map(ElementLiveData::getElement)
				.findFirst()
				.orElse(0);
		// vice captain played
		if (activeCaptain == 0) {
			activeCaptain = pickList.stream()
					.filter(ElementLiveData::isViceCaptain)
					.filter(o -> !o.isGwStarted() || o.isPlayed())
					.map(ElementLiveData::getElement)
					.findFirst()
					.orElse(0);
		}
		// none played
		if (activeCaptain == 0) {
			activeCaptain = pickList.stream()
					.filter(ElementLiveData::isCaptain)
					.map(ElementLiveData::getElement)
					.findFirst()
					.orElse(0);
		}
		return activeCaptain;
	}

	@SafeVarargs
	private final <T> Stream<T> createSteam(T... values) {
		Stream.Builder<T> builder = Stream.builder();
		Arrays.asList(values).forEach(builder::add);
		return builder.build();
	}

	@Override
	public LiveCalaData calcLivePointsByElementList(int event, Map<Integer, Integer> elementMap, int captain, int viceCaptain) {
		LiveCalaData liveCalaData = new LiveCalaData();
		// initialize element_live_data
		List<ElementLiveData> pickList = Lists.newArrayList();
		Map<String, EventLiveEntity> eventLiveMap = this.querySerivce.getEventLiveByEvent(event);
		Map<String, String> positionMap = this.querySerivce.getPositionMap();
		elementMap.keySet().forEach(position -> {
			int element = elementMap.get(position);
			ElementLiveData elementLiveData = new ElementLiveData();
			elementLiveData.setPosition(position);
			elementLiveData.setMultiplier(this.ifMultiplier(event, element));
			elementLiveData.setCaptain(element == captain);
			elementLiveData.setViceCaptain(element == viceCaptain);
			// player info
			PlayerEntity playerEntity = this.querySerivce.getPlayerByElememt(element);
			if (playerEntity != null) {
				elementLiveData
						.setElementTypeName(positionMap.get(String.valueOf(playerEntity.getElementType())))
						.setWebName(playerEntity.getWebName());
				// event fixture
				int teamId = playerEntity.getTeamId();
				List<PlayerFixtureData> playerFixtureDataList = this.querySerivce.getEventFixtureByTeamIdAndEvent(teamId, event);
				if (!CollectionUtils.isEmpty(playerFixtureDataList)) {
					PlayerFixtureData PlayerFixtureData = playerFixtureDataList
							.stream()
							.filter(o -> o.getEvent() == event)
							.findFirst()
							.orElse(new PlayerFixtureData());
					elementLiveData.setGwStarted(PlayerFixtureData.isStarted());
					elementLiveData.setGwFinished(PlayerFixtureData.isFinished());
					elementLiveData.setPlayed(elementLiveData.getMinutes() > 0 || elementLiveData.getYellowCards() > 0 || elementLiveData.getRedCards() > 0);
				}
			}
			// from event_live
			EventLiveEntity eventLiveEntity = eventLiveMap.get(String.valueOf(element));
			if (eventLiveEntity != null) {
				BeanUtil.copyProperties(eventLiveEntity, elementLiveData, CopyOptions.create().ignoreNullValue());
			}
			pickList.add(elementLiveData);
		});
		// calc live points
		int livePoints = this.calcActivePoints(Chip.NONE, pickList);
		liveCalaData
				.setEntry(0)
				.setEvent(event)
				.setPickList(pickList)
				.setChip(Chip.NONE.getValue())
				.setLivePoints(livePoints)
				.setTransferCost(0)
				.setLiveNetPoints(livePoints);
		return liveCalaData;
	}

	private int ifMultiplier(int event, int element) {
		PlayerEntity playerEntity = this.querySerivce.getPlayerByElememt(element);
		if (playerEntity == null) {
			return 0;
		}
		int teamId = playerEntity.getTeamId();
		List<PlayerFixtureData> playerFixtureDataList = this.querySerivce.getEventFixtureByTeamIdAndEvent(teamId, event);
		if (CollectionUtils.isEmpty(playerFixtureDataList)) {
			return 0;
		}
		return (int) playerFixtureDataList
				.stream()
				.filter(o -> o.getEvent() == event)
				.count();
	}

	@Cacheable(value = "qryCalcLivePointsByTournament", key = "#event+'::'+#tournamentId", unless = "#result == null")
	@Override
	public List<LiveCalaData> calcLivePointsByTournament(int event, int tournamentId) {
		List<LiveCalaData> liveCalaList = Lists.newArrayList();
		// get entry list
		List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
		entryList.forEach(entry -> {
			// get user picks list
			Optional<UserPicksRes> result = this.querySerivce.getUserPicks(event, entry);
			// calc live points
			result.ifPresent(userPicksRes -> {
				LiveCalaData liveCalaData = this.calcLiveSingleEntryPoints(event, entry, userPicksRes, Maps.newHashMap());
				if (liveCalaData != null) {
					liveCalaList.add(liveCalaData);
				}
			});
		});
		// entry info
		List<LiveCalaData> list = liveCalaList
				.stream()
				.sorted(Comparator.comparing(LiveCalaData::getLivePoints).reversed())
				.collect(Collectors.toList());
		Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list()
				.stream()
				.collect(Collectors.toMap(EntryInfoEntity::getEntry, v -> v));
		list.forEach(liveCalaData -> {
			EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(liveCalaData.getEntry(), null);
			if (entryInfoEntity != null) {
				liveCalaData
						.setEntryName(entryInfoEntity.getEntryName())
						.setPlayerName(entryInfoEntity.getPlayerName())
						.setRegion(entryInfoEntity.getRegion());
			}
		});
		return list;
	}

}
