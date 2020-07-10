//package com.tong.fpl.service;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.tong.fpl.constant.enums.ChipEnum;
//import com.tong.fpl.constant.enums.PositionEnum;
//import com.tong.fpl.constant.enums.PositionRuleEnum;
//import com.tong.fpl.domain.data.response.UserPicksRes;
//import com.tong.fpl.db.entity.EventLiveEntity;
//import com.tong.fpl.service.db.EventLiveService;
//import com.tong.fpl.service.db.EventResultService;
//import com.tong.fpl.service.impl.InterfaceServiceImpl;
//import com.tong.fpl.service.impl.StaticServiceImpl;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.validation.annotation.Validated;
//
//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.NotEmpty;
//import javax.validation.constraints.NotNull;
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * https://fantasy.premierleague.com/help/rules
// * 1.Your team can play in any formation providing that 1 goalkeeper, at least 3 defenders and at least 1 forward are selected at all times.
// * 2.a.If your captain plays 0 minutes in the Gameweek, the captain will be changed to the vice-captain.
// * b.If both captain and vice-captain play 0 minutes in a Gameweek, then no player's score will be doubled.
// * 3.Playing in a Gameweek means playing at least 1 minute or receiving a yellow / red card.
// * 4.a.If your Goalkeeper doesn't play in the Gameweek, he will be substituted by your replacement Goalkeeper, if he played in the Gameweek.
// * b.If any of your outfield players don't play in the Gameweek, they will be substituted by the highest priority outfield substitute who played in the Gameweek and doesn't break the formation rules.
// * <p>
// * Create by tong on 2020/3/12
// */
//@Slf4j
//@Validated
//@Service
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//public class CalcLivePointsService {
//
//	private final EventResultService eventResultService;
//	private final EventLiveService eventLiveService;
//	private final InterfaceServiceImpl interfaceService;
//	private final StaticServiceImpl staticService;
//
//	public Map<List<EntryLiveEntity>, Integer> calcLivePointsService(@NotNull int entry, @NotNull int event) {
//		Map<List<EntryLiveEntity>, Integer> resultMap = Maps.newHashMap(); // (k,v) -> (team,points)
//		// update collection player and event_live
//		this.staticService.insertPlayers();
//		this.staticService.insertEventLive(event);
//		// insert entry_live
//		Optional<UserPicksRes> userPicksRes = this.interfaceService.getUserPicks(entry, event);
//		userPicksRes.ifPresent(o -> {
//			this.insertEntryLive(entry, event, userPicksRes.get());
//			// initialize entry_live
//			List<EntryLiveEntity> entryLiveList = this.eventResultService.list(new QueryWrapper<EntryLiveEntity>().lambda()
//					.eq(EntryLiveEntity::getEvent, event).eq(EntryLiveEntity::getEntry, entry));
//			// find chip
//			String chip = StringUtils.isNotEmpty(userPicksRes.get().getActiveChip()) ? userPicksRes.get().getActiveChip() : ChipEnum.NONE.getValue();
//			// retrun result
//			resultMap.put(entryLiveList, this.calcActivePoints(chip, entryLiveList));
//		});
//		return resultMap;
//	}
//
//	private void insertEntryLive(int entry, int event, UserPicksRes userPicksRes) {
//		Map<Integer, EntryLiveEntity> map = Maps.newHashMap();
//		List<EntryLiveEntity> captainList = Lists.newArrayList();
//		userPicksRes.getPicks().forEach(pick -> {
//			EventLiveEntity eventLive = this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda()
//					.eq(EventLiveEntity::getElement, pick.getElement()));
//			if (eventLive == null) {
//				return;
//			}
//			EntryLiveEntity entryLive = new EntryLiveEntity();
//			entryLive.setEntry(entry);
//			entryLive.setEvent(event);
//			entryLive.setElement(pick.getElement());
//			entryLive.setElementType(eventLive.getElementType());
//			entryLive.setPosition(pick.getPosition());
//			entryLive.setMinutes(eventLive.getMinutes());
//			entryLive.setIsPlayed(eventLive.getMinutes() > 0 || eventLive.getYellowCards() > 0 || eventLive.getRedCards() > 0);
//			entryLive.setBonus(eventLive.getBonus());
//			entryLive.setPoint(eventLive.getTotalPoints());
//			entryLive.setIsCaptain(pick.isCaptain());
//			entryLive.setIsViceCaptain(pick.isViceCaptain());
//			if (pick.isCaptain() || pick.isViceCaptain()) {
//				captainList.add(entryLive);
//			}
//			map.put(entryLive.getPosition(), entryLive);
//		});
//		this.setEntryLiveCapain(captainList, map);
//		List<EntryLiveEntity> list = Lists.newArrayList();
//		list.addAll(map.values());
//		this.eventResultService.getBaseMapper().truncateTable();
//		this.eventResultService.saveBatch(list);
//		log.info("insert entry_live size is " + list.size() + "!");
//	}
//
//	private void setEntryLiveCapain(List<EntryLiveEntity> captainList, Map<Integer, EntryLiveEntity> map) {
//		EntryLiveEntity captain = captainList.get(0);
//		EntryLiveEntity viceCaptain = captainList.get(1);
//		if (captain.getMinutes() == 0 && viceCaptain.getMinutes() > 0) {
//			captain.setIsCaptain(false);
//			map.put(captain.getPosition(), captain);
//			viceCaptain.setIsCaptain(true);
//			map.put(viceCaptain.getPosition(), viceCaptain);
//		}
//	}
//
//	private int calcActivePoints(@NotBlank String chips, @NotEmpty List<EntryLiveEntity> entryLiveList) {
//		// get active pickups
//		List<EntryLiveEntity> activePicks = this.getActivePicks(entryLiveList);
//		// only 3c and bb change the calculate rule
//		switch (ChipEnum.valueOf(chips)) {
//			case NONE:
//				return activePicks.stream().filter(o -> !o.getIsCaptain()).mapToInt(EntryLiveEntity::getPoint).sum()
//						+ activePicks.stream().filter(EntryLiveEntity::getIsCaptain).mapToInt(o -> 2 * o.getPoint()).sum();
//			case TC:
//				return activePicks.stream().filter(o -> !o.getIsCaptain()).mapToInt(EntryLiveEntity::getPoint).sum()
//						+ activePicks.stream().filter(EntryLiveEntity::getIsCaptain).mapToInt(o -> 3 * o.getPoint()).sum();
//			case BB:
//				return entryLiveList.stream().mapToInt(EntryLiveEntity::getPoint).sum();
//			default:
//				return 0;
//		}
//	}
//
//	private List<EntryLiveEntity> getActivePicks(List<EntryLiveEntity> entryLiveList) {
//		// element_type -> active -> start
//		Map<Integer, Map<Boolean, Map<Boolean, List<EntryLiveEntity>>>> map = entryLiveList.parallelStream()
//				.collect(Collectors.groupingBy(EntryLiveEntity::getElementType,
//						Collectors.partitioningBy(EntryLiveEntity::getIsPlayed,
//								Collectors.partitioningBy(entryLiveEntity -> entryLiveEntity.getPosition() < 12))));
//		// gkp
//		List<EntryLiveEntity> gkps = this.createSteam(map.get(PositionEnum.GKP.getPosition()).get(true).get(true),
//				map.get(PositionEnum.GKP.getPosition()).get(true).get(false), map.get(1).get(false).get(true))
//				.flatMap(Collection::stream)
//				.limit(PositionRuleEnum.MIN_NUM_GKP.getNum())
//				.collect(Collectors.toList());
//		// active defs
//		List<EntryLiveEntity> defs = this.createSteam(map.get(PositionEnum.DEF.getPosition()).get(true).get(true),
//				map.get(PositionEnum.DEF.getPosition()).get(true).get(false))
//				.flatMap(Collection::stream)
//				.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
//				.collect(Collectors.toList());
//		// def rule, at least 3
//		if (defs.size() < PositionRuleEnum.MIN_NUM_DEF.getNum()) {
//			defs = this.createSteam(defs, map.get(PositionEnum.DEF.getPosition()).get(false).get(true))
//					.flatMap(Collection::stream)
//					.limit(PositionRuleEnum.MIN_NUM_DEF.getNum())
//					.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
//					.collect(Collectors.toList());
//		}
//		// active fwds
//		List<EntryLiveEntity> fwds = this.createSteam(map.get(PositionEnum.FWD.getPosition()).get(true).get(true),
//				map.get(PositionEnum.FWD.getPosition()).get(true).get(false))
//				.flatMap(Collection::stream)
//				.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
//				.collect(Collectors.toList());
//		// fwd rule, at least 1
//		if (fwds.size() < PositionRuleEnum.MIN_NUM_FWD.getNum()) {
//			fwds.add(map.get(PositionEnum.FWD.getPosition()).get(false).get(true).get(0));
//		}
//		// mids
//		int maxMidNum = PositionRuleEnum.MIN_PLAYERS.getNum() - gkps.size() - defs.size() - fwds.size();
//		List<EntryLiveEntity> mids = this.createSteam(map.get(PositionEnum.MID.getPosition()).get(true).get(true),
//				map.get(PositionEnum.MID.getPosition()).get(true).get(false))
//				.flatMap(Collection::stream)
//				.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
//				.limit(maxMidNum)
//				.collect(Collectors.toList());
//		// active_list
//		List<EntryLiveEntity> activeList = this.createSteam(gkps, defs, fwds, mids)
//				.flatMap(Collection::stream)
//				.collect(Collectors.toList());
//		List<EntryLiveEntity> standByList = this.createSteam(map.get(PositionEnum.DEF.getPosition()).get(false).get(true),
//				map.get(PositionEnum.MID.getPosition()).get(false).get(true),
//				map.get(PositionEnum.FWD.getPosition()).get(false).get(true))
//				.flatMap(Collection::stream)
//				.filter(o -> !activeList.contains(o))
//				.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
//				.limit(PositionRuleEnum.MIN_PLAYERS.getNum() - activeList.size())
//				.collect(Collectors.toList());
//		return this.createSteam(activeList, standByList)
//				.flatMap(Collection::stream)
//				.sorted(Comparator.comparing(EntryLiveEntity::getElementType).thenComparing(EntryLiveEntity::getPosition))
//				.collect(Collectors.toList());
//	}
//
//	@SafeVarargs
//	private final <T> Stream<T> createSteam(T... values) {
//		Stream.Builder<T> builder = Stream.builder();
//		Arrays.asList(values).forEach(builder::add);
//		return builder.build();
//	}
//
//}
