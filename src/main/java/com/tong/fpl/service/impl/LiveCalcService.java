package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.letletme.live.ElementLiveData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.service.ILiveCalcService;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.db.EventFixtureService;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Create by tong on 2020/7/13
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveCalcService implements ILiveCalcService {

	private final PlayerService playerService;
	private final EventFixtureService eventFixtureService;
	private final EventLiveService eventLiveService;
	private final IStaticSerive staticService;

	@Override
	public LiveCalaData calcLivePointsByEntry(int event, int entry) {
		LiveCalaData liveCalaData = new LiveCalaData();
		// update event_live
		this.staticService.insertEventLive(event);
		// get user picks
		Optional<UserPicksRes> userPicksResult = this.staticService.getUserPicks(event, entry);
		userPicksResult.ifPresent(userPicksRes -> {
			// initialize element_live_data
			List<ElementLiveData> elementLiveDataList = this.initElemetLiveData(event, userPicksRes.getPicks());
			// get active picks
			List<ElementLiveData> pickList = this.getPickList(elementLiveDataList);
			// calc live points
			int livePoints = this.calcActivePoints(Chip.getChipFromValue(userPicksRes.getActiveChip()), pickList);
			liveCalaData
					.setEntry(entry)
					.setEvent(event)
					.setPickList(pickList)
					.setChip(userPicksRes.getActiveChip())
					.setLivePoints(livePoints)
					.setTransferCost(userPicksRes.getEntryHistory().getEventTransfersCost())
					.setLiveNetPoints(livePoints - userPicksRes.getEntryHistory().getEventTransfersCost());
		});
		return liveCalaData;
	}

	@Override
	public LiveCalaData calcLivePointsByElementList(int event, Map<Integer, Integer> elementMap, int captain, int viceCaptain) {
		LiveCalaData liveCalaData = new LiveCalaData();
		// update event_live
		this.staticService.insertEventLive(event);
		// initialize element_live_data
		List<ElementLiveData> pickList = Lists.newArrayList();
		elementMap.keySet().forEach(position -> {
			int element = elementMap.get(position);
			ElementLiveData elementLiveData = new ElementLiveData();
			elementLiveData.setPosition(position);
			elementLiveData.setMultiplier(this.ifMultiplier(event, element));
			elementLiveData.setCaptain(element == captain);
			elementLiveData.setViceCaptain(element == viceCaptain);
			// from event_live
			EventLiveEntity eventLiveEntity = this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda()
					.eq(EventLiveEntity::getEvent, event).eq(EventLiveEntity::getElement, element));
			if (eventLiveEntity != null) {
				BeanUtil.copyProperties(eventLiveEntity, elementLiveData, CopyOptions.create().ignoreNullValue());
			}
			elementLiveData.setGwStarted(this.isFixtureStarted(event, element));
			elementLiveData.setPlayed(elementLiveData.getMinutes() > 0 || elementLiveData.getYellowCards() > 0 || elementLiveData.getRedCards() > 0);
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

	@Override
	public Map<Integer, Integer> calcEventCaptainStat(int event, int num) {
		// thread safety
		Map<Integer, Integer> entryCaptainMap = Maps.newConcurrentMap();
		List<Integer> entryList = Lists.newCopyOnWriteArrayList();
		IntStream.range(1, (int) Math.round(num * 1.0 / 50)).parallel().forEach(page -> {
			List<EntryInfoEntity> entryInfoEntityList = this.staticService.getLeaguesClassicByPage(314, page);
			entryInfoEntityList.forEach(entryInfoEntity -> entryList.add(entryInfoEntity.getEntry()));
		});
		entryList.parallelStream().forEach(entry -> {
			Optional<UserPicksRes> userPicksRes = this.staticService.getUserPicks(event, entry);
			userPicksRes.ifPresent(userPicks -> userPicks.getPicks().parallelStream()
					.filter(Pick::isCaptain)
					.forEach(o -> entryCaptainMap.put(entry, o.getElement())));
		});
		entryCaptainMap.forEach((key, value) -> log.info("entry:{}, captain:{}", key, value));
		return entryCaptainMap;
	}

	private int ifMultiplier(int event, int element) {
		int teamId = this.playerService.getById(element).getTeamId();
		if (teamId == 0) {
			return 0;
		}
		List<EventFixtureEntity> eventFixtureEntityList = this.eventFixtureService.list(new QueryWrapper<EventFixtureEntity>().lambda()
				.eq(EventFixtureEntity::getEvent, event)
				.and(o -> o.eq(EventFixtureEntity::getTeamH, teamId).or(i -> i.eq(EventFixtureEntity::getTeamA, teamId)))
		);
		if (CollectionUtils.isEmpty(eventFixtureEntityList)) {
			return 0;
		}
		return eventFixtureEntityList.size();
	}

	private List<ElementLiveData> initElemetLiveData(int event, List<Pick> picks) {
		List<ElementLiveData> elementLiveDataList = Lists.newArrayList();
		picks.forEach(pick -> {
			int element = pick.getElement();
			// from user pick
			ElementLiveData elementLiveData = new ElementLiveData();
			elementLiveData.setPosition(pick.getPosition());
			elementLiveData.setMultiplier(pick.getMultiplier());
			elementLiveData.setCaptain(pick.isCaptain());
			elementLiveData.setViceCaptain(pick.isViceCaptain());
			// from event_live
			EventLiveEntity eventLiveEntity = this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda()
					.eq(EventLiveEntity::getEvent, event).eq(EventLiveEntity::getElement, element));
			if (eventLiveEntity != null) {
				BeanUtil.copyProperties(eventLiveEntity, elementLiveData, CopyOptions.create().ignoreNullValue());
			}
			elementLiveData.setGwStarted(this.isFixtureStarted(event, element));
			elementLiveData.setPlayed(elementLiveData.getMinutes() > 0 || elementLiveData.getYellowCards() > 0 || elementLiveData.getRedCards() > 0);
			elementLiveDataList.add(elementLiveData);
		});
		return elementLiveDataList;
	}

	private boolean isFixtureStarted(int event, int element) {
		PlayerEntity player = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getElement, element));
		if (player == null) {
			return false;
		}
		int teamId = player.getTeamId();
		List<EventFixtureEntity> eventFixtureList = this.eventFixtureService.list(new QueryWrapper<EventFixtureEntity>().lambda()
				.eq(EventFixtureEntity::getEvent, event)
				.and(o -> o.eq(EventFixtureEntity::getTeamH, teamId)
						.or(i -> i.eq(EventFixtureEntity::getTeamA, teamId)))
				.orderByAsc(EventFixtureEntity::getKickoffTime));
		if (CollectionUtils.isEmpty(eventFixtureList)) {
			return true;
		}
		return eventFixtureList.get(0).isStarted();
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
		int activeCaptain = this.getactiveCaptain(pickList);
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

	private int getactiveCaptain(List<ElementLiveData> pickList) {
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

}
