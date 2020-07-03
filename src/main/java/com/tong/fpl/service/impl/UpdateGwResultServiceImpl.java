package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.ChipEnum;
import com.tong.fpl.constant.KnockoutModeEnum;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.IUpdateGwResultService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculate gw points
 * Create by tong on 2020/3/10
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateGwResultServiceImpl implements IUpdateGwResultService {

	private final EventService eventService;
	private final EventLiveService eventLiveService;
	private final EventResultService eventResultService;
	private final EntryInfoService entryInfoService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentKnockoutService tournamentKnockoutService;
	private final TournamentKnockoutResultService tournamentKnockoutResultService;
	private final InterfaceServiceImpl interfaceService;
	private final IStaticSerive staticSerive;

	public void upsertEventResult(int event, List<Integer> entryList) {
		// update base date
		this.updateEventBaseData(event);
		// update event result
		List<EventResultEntity> eventResultList = Lists.newArrayList();
		entryList.forEach(entry -> this.calcEntryEventPoints(event, entry, eventResultList));
		this.eventResultService.saveOrUpdateBatch(eventResultList);
	}

	private void updateEventBaseData(int event) {
		this.staticSerive.insertBaseData(event);
		this.staticSerive.insertEventLive(event);
	}

	private void calcEntryEventPoints(int event, int entry, List<EventResultEntity> eventResultList) {
		Optional<UserPicksRes> userPicksRes = this.interfaceService.getUserPicks(entry, event);
		userPicksRes.ifPresent(userPick -> {
			boolean finished = this.eventService.getById(event).getFinished();
			Map<Integer, Integer> elementPointsMap = this.eventLiveService.list().stream()
					.collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
			eventResultList.add(new EventResultEntity()
					.setEntry(entry)
					.setEvent(event)
					.setEventPoints(userPick.getEntryHistory().getPoints())
					.setEventTransfers(userPick.getEntryHistory().getEventTransfers())
					.setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
					.setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
					.setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench())
					.setEventRank(userPick.getEntryHistory().getRank())
					.setTotalPoints(userPick.getEntryHistory().getTotalPoints())
					.setOverallRank(userPick.getEntryHistory().getOverallRank())
					.setActiveChip(userPick.getActiveChip())
					.setPicks(this.setUserPicks(userPick.getPicks(), elementPointsMap))
					.setEventFinished(finished)
			);
			elementPointsMap.clear();
		});
	}

	private String setUserPicks(List<Pick> picks, Map<Integer, Integer> elementPointsMap) {
		List<Pick> pickList = Lists.newArrayList();
		picks.forEach(o -> {
			Pick pick = new Pick();
			pick.setElement(o.getElement());
			pick.setPosition(o.getPosition());
			pick.setMultiplier(o.getMultiplier());
			pick.setCaptain(o.isCaptain());
			pick.setViceCaptain(o.isViceCaptain());
			pick.setPoints(elementPointsMap.getOrDefault(o.getElement(), 0));
			pickList.add(pick);
		});
		return JsonUtils.obj2json(pickList);
	}

	@Override
	public void updateGroupResult(int event) {
//		TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
//				.eq(TournamentInfoEntity::getName, tournamentName));
//		if (tournamentInfoEntity == null) {
//			throw new Exception("tournament not exists!");
//		}
//		if (event < tournamentInfoEntity.getKnockoutStartGw() || event > tournamentInfoEntity.getGroupEndGw()) {
//			throw new Exception("event not in tournament range!");
//		}
//		List<Integer> entryList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getTournamentId, tournamentInfoEntity.getId()))
//				.stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
//		if (CollectionUtils.isEmpty(entryList)) {
//			throw new Exception("no entry exists!");
//		}
//		// update

	}

	@Override
	public void updateKnockoutResult(int event) {
		// get all tournament
		List<TournamentInfoEntity> tournamentInfoList = this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
				.ne(TournamentInfoEntity::getKnockoutMode, KnockoutModeEnum.No_knockout.name())
				.ge(TournamentInfoEntity::getKnockoutStartGw, event)
				.le(TournamentInfoEntity::getKnockoutEndGw, event)
				.orderByAsc(TournamentInfoEntity::getId));
		// update
		tournamentInfoList.forEach(tournamentInfoEntity -> this.updateTournamentKnockoutResult(tournamentInfoEntity.getId(), event));
	}

	private void updateTournamentKnockoutResult(int tournamentId, int event) {
		// get entry list
		List<Integer> entryList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getTournamentId, tournamentId))
				.stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(entryList)) {
			log.error("tournament:{} no entry!", tournamentId);
			return;
		}
		// get event_result list
		Map<Integer, EventResultEntity> eventResultMap = this.eventResultService.list(new QueryWrapper<EventResultEntity>().lambda()
				.eq(EventResultEntity::getEvent, event).in(EventResultEntity::getEntry, entryList))
				.stream()
				.collect(Collectors.toMap(EventResultEntity::getEntry, v -> v));
		if (CollectionUtils.isEmpty(eventResultMap)) {
			log.error("tournament:{} no event_result exists!", tournamentId);
			return;
		}
		entryList.forEach(entry -> this.updateEntryKnockoutResult(tournamentId, event, eventResultMap));


//		Multimap<Integer, Integer> nextRoundMap = ArrayListMultimap.create();
//		Map<Integer, EventResultEntity> eventResultMap = this.saveEventPoints(event, entryList)
//				.stream().collect(Collectors.toMap(EventResultEntity::getEntry, v -> v));
//		// 1. update this round result
//		List<TournamentKnockoutEntity> knockoutList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
//				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId).eq(TournamentKnockoutEntity::getEvent, event)
//				.orderByAsc(TournamentKnockoutEntity::getMatchId));
//		knockoutList.forEach(tournamentKnockoutEntity -> {
//			int homeEntry = tournamentKnockoutEntity.getHomeEntry();
//			int awayEntry = tournamentKnockoutEntity.getAwayEntry();
//			if (!eventResultMap.containsKey(homeEntry) || !eventResultMap.containsKey(awayEntry)) {
//				return;
//			}
//			EventResultEntity homeEntryResult = eventResultMap.get(homeEntry);
//			EventResultEntity awayEntryResult = eventResultMap.get(awayEntry);
//			int roundWinner = this.getRoundWinner(homeEntryResult, awayEntryResult);
//			this.tournamentKnockoutService.updateById(tournamentKnockoutEntity
//					.setRoundWinner(roundWinner)
//			);
//			// 2. update next round
//			int nextMatchId = tournamentKnockoutEntity.getNextMatchId();
//			TournamentKnockoutEntity nextRoundKnockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
//					.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
//					.eq(TournamentKnockoutEntity::getEvent, event + 1)
//					.eq(TournamentKnockoutEntity::getMatchId, nextMatchId));
//			if (nextRoundKnockoutEntity == null) {
//				return;
//			}
//			if (CollectionUtils.isEmpty(nextRoundMap.get(nextMatchId))) {
//				this.tournamentKnockoutService.updateById(nextRoundKnockoutEntity.setHomeEntry(roundWinner));
//			} else {
//				this.tournamentKnockoutService.updateById(nextRoundKnockoutEntity.setAwayEntry(roundWinner));
//			}
//			nextRoundMap.put(nextMatchId, roundWinner);
//		});
	}

	private void updateEntryKnockoutResult(int tournamentId, int event, Map<Integer, EventResultEntity> eventResultMap) {
		List<TournamentKnockoutResultEntity> knockoutResultUpdateList = Lists.newArrayList();
		List<TournamentKnockoutEntity> knockoutUpdateList = Lists.newArrayList();
		Multimap<Integer, Integer> matchWinnerMap = ArrayListMultimap.create();
		// get raw knockout result by tournament and event
		List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
				.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).eq(TournamentKnockoutResultEntity::getEvent, event));
		knockoutResultList.forEach(knockoutResult -> {
			// update tournament_knockout_result
			int matchId = knockoutResult.getMatchId();
			int homeEntry = knockoutResult.getHomeEntry();
			int awayEntry = knockoutResult.getAwayEntry();
			EventResultEntity homeEventResult = eventResultMap.get(knockoutResult.getHomeEntry());
			EventResultEntity awayEventResult = eventResultMap.get(knockoutResult.getAwayEntry());
			int matchWinner = this.getMatchWinner(homeEntry, awayEntry, homeEventResult, awayEventResult);
			matchWinnerMap.put(matchId, matchWinner);
			knockoutResultUpdateList.add(knockoutResult
					.setHomeEntryNetPoint(homeEntry > 0 ? homeEventResult.getEventNetPoints() : 0)
					.setHomeEntryRank(homeEntry > 0 ? homeEventResult.getEventRank() : 0)
					.setHomeEntryChip(homeEntry > 0 ? homeEventResult.getActiveChip() : ChipEnum.NONE.getValue())
					.setAwayEntryNetPoint(awayEntry > 0 ? awayEventResult.getEventNetPoints() : 0)
					.setAwayEntryRank(awayEntry > 0 ? awayEventResult.getEventRank() : 0)
					.setAwayEntryChip(awayEntry > 0 ? awayEventResult.getActiveChip() : ChipEnum.NONE.getValue())
					.setMatchWinner(matchWinner));
			// update tournament_knockout
			TournamentKnockoutEntity knockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
					.eq(TournamentKnockoutEntity::getTournamentId, tournamentId).eq(TournamentKnockoutEntity::getMatchId, matchId));
			int endGw = knockoutEntity.getEndGw();
			if (event != endGw) {
				return;
			}
			knockoutUpdateList.add(knockoutEntity.setRound(this.getRoundWinner(matchId, knockoutResult, matchWinnerMap)));
		});
		// update
		this.tournamentKnockoutResultService.updateBatchById(knockoutResultUpdateList);
		this.tournamentKnockoutService.updateBatchById(knockoutUpdateList);
	}

	private int getMatchWinner(int homeEntry, int awayEntry, EventResultEntity homeEntryResult, EventResultEntity awayEntryResult) {
		// if blank
		if (homeEntry <= 0) {
			return awayEntry;
		} else if (awayEntry <= 0) {
			return homeEntry;
		}
		// compare net points; if equal, compare rank; if equal, random
		if (homeEntryResult.getEventNetPoints() > awayEntryResult.getEventNetPoints()) {
			return homeEntryResult.getEntry();
		} else if (homeEntryResult.getEventNetPoints() < awayEntryResult.getEventNetPoints()) {
			return awayEntryResult.getEntry();
		} else {
			if (homeEntryResult.getOverallRank() > awayEntryResult.getOverallRank()) {
				return homeEntryResult.getEntry();
			} else if (homeEntryResult.getOverallRank() < awayEntryResult.getOverallRank()) {
				return awayEntryResult.getEntry();
			} else {
				if (new Random().nextInt(10) % 2 == 0) {
					return homeEntryResult.getEntry();
				} else {
					return awayEntryResult.getEntry();
				}
			}
		}
	}

	private int getRoundWinner(int matchId, TournamentKnockoutResultEntity knockoutResultEntity, Multimap<Integer, Integer> matchWinnerMap) {
		if (matchWinnerMap.get(matchId).size() == 1) {
			return matchWinnerMap.get(matchId).stream().findFirst().orElse(0);
		} else if (matchWinnerMap.get(matchId).size() == 2) { // two rounds, if 1-1 , compare rank
			List<Integer> winnerList = new ArrayList<>(matchWinnerMap.get(matchId));
			if (winnerList.get(0).equals(winnerList.get(1))) {
				return winnerList.get(0);
			} else {
				if (knockoutResultEntity.getHomeEntryRank() > knockoutResultEntity.getAwayEntryRank()) {
					return knockoutResultEntity.getHomeEntry();
				} else if (knockoutResultEntity.getHomeEntryRank() < knockoutResultEntity.getAwayEntryRank()) {
					return knockoutResultEntity.getAwayEntry();
				} else {
					if (new Random().nextInt(10) % 2 == 0) {
						return knockoutResultEntity.getHomeEntry();
					} else {
						return knockoutResultEntity.getAwayEntry();
					}
				}
			}
		}
		return 0;
	}

}
