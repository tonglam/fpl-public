package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.data.fpl.TournamentKnockoutResultData;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.IUpdateGwResultService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculate gw points
 * Create by tong on 2020/3/10
 */
@Validated
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateGwResultServiceImpl implements IUpdateGwResultService {

	private final EventService eventService;
	private final EventLiveService eventLiveService;
	private final EventResultService eventResultService;
	private final EntryInfoService entryInfoService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentGroupService tournamentGroupService;
	private final TournamentGroupResultService tournamentGroupResultService;
	private final TournamentKnockoutService tournamentKnockoutService;
	private final TournamentKnockoutResultService tournamentKnockoutResultService;
	private final InterfaceServiceImpl interfaceService;
	private final IStaticSerive staticSerive;

	@Override
	public void updateEntryInfo(int event) {
		List<EntryInfoEntity> updateEntryInfoList = Lists.newArrayList();
		// get entry info list
		List<EntryInfoEntity> entryInfoList = this.entryInfoService.list()
				.stream()
				.collect(Collectors.collectingAndThen(
						Collectors.toCollection(() -> new TreeSet<>(
								Comparator.comparing(EntryInfoEntity::getEntry)
						)), Lists::newArrayList));
		// update event info
		entryInfoList.forEach(entryInfoEntity -> {
			EntryRes entryRes = this.staticSerive.getEntry(entryInfoEntity.getEntry());
			if (entryRes == null) {
				return;
			}
			entryInfoEntity
					.setOverallPoints(entryRes.getSummaryOverallPoints())
					.setOverallRank(entryRes.getSummaryOverallRank())
					.setBank(entryRes.getLastDeadlineBank())
					.setTeamValue(entryRes.getLastDeadlineValue())
					.setTotalTransfers(entryRes.getLastDeadlineTotalTransfers());
		});
		// update
		this.entryInfoService.updateBatchById(updateEntryInfoList);
	}

	@Override
	public void upsertEventResult(int event, List<Integer> entryList) {
		if (CollectionUtils.isEmpty(entryList)) {
			return;
		}
		this.eventResultService.remove(new QueryWrapper<EventResultEntity>().lambda().eq(EventResultEntity::getEvent, event));
		// update base date
		this.updateEventBaseData(event);
		// update entry info
		List<EntryInfoEntity> entryInfoEntityList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
				.in(EntryInfoEntity::getEntry, entryList));
		this.entryInfoService.updateBatchById(entryInfoEntityList);
		// update event result
		List<EventResultEntity> eventResultList = Lists.newArrayList();
		entryList.forEach(entry -> this.calcEntryEventPoints(event, entry, eventResultList));
		this.eventResultService.saveBatch(eventResultList);
	}

	private void calcEntryEventPoints(int event, int entry, List<EventResultEntity> eventResultList) {
		Optional<UserPicksRes> userPicksRes = this.interfaceService.getUserPicks(entry, event);
		userPicksRes.ifPresent(userPick -> {
			boolean finished = this.eventService.getById(event).getFinished();
			Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
					.eq(EventLiveEntity::getEvent, event))
					.stream()
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
					.setOverallRank(userPick.getEntryHistory().getOverallRank())
					.setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip())
					.setEventPicks(this.setUserPicks(userPick.getPicks(), elementPointsMap))
					.setEventFinished(finished)
			);
			elementPointsMap.clear();
		});
	}

	@Override
	public void updateGroupResult(int event) {
		// get event_result list
		Map<Integer, EventResultEntity> eventResultMap = this.eventResultService.list(new QueryWrapper<EventResultEntity>().lambda()
				.eq(EventResultEntity::getEvent, event))
				.stream()
				.collect(Collectors.toMap(EventResultEntity::getEntry, v -> v));
		if (CollectionUtils.isEmpty(eventResultMap)) {
			log.error("event_result not update, event:{}!", event);
			return;
		}
		// update all tournaments
		List<TournamentInfoEntity> tournamentInfoList = this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
				.ne(TournamentInfoEntity::getGroupMode, GroupMode.No_group.name())
				.ge(TournamentInfoEntity::getGroupStartGw, event)
				.le(TournamentInfoEntity::getGroupEndGw, event)
				.orderByAsc(TournamentInfoEntity::getId));
		tournamentInfoList.forEach(tournamentInfoEntity -> this.updateSingleGroupResult(tournamentInfoEntity.getId(), tournamentInfoEntity.getGroupMode(),
				event, eventResultMap));
		// clear
		eventResultMap.clear();
	}

	private void updateSingleGroupResult(int tournamentId, String groupMode, int event, Map<Integer, EventResultEntity> eventResultMap) {
		switch (GroupMode.valueOf(groupMode)) {
			case Points_race:
				this.updatePointsGroupResult(tournamentId, event, eventResultMap);
			case Battle_race:
				this.updateBattleGroupResult(tournamentId, event, eventResultMap);
			default:
		}
	}

	private void updatePointsGroupResult(int tournamentId, int event, Map<Integer, EventResultEntity> eventResultMap) {
		List<TournamentGroupResultEntity> groupResultList = this.tournamentGroupResultService.list(new QueryWrapper<TournamentGroupResultEntity>().lambda()
				.eq(TournamentGroupResultEntity::getTournamentId, tournamentId)
				.eq(TournamentGroupResultEntity::getEvent, event)
				.orderByAsc(TournamentGroupResultEntity::getGroupId));
	}

	private void updateBattleGroupResult(int tournamentId, int event, Map<Integer, EventResultEntity> eventResultMap) {

	}

	@Override
	public void updateKnockoutResult(int event) {
		// get event_result list
		Map<Integer, EventResultEntity> eventResultMap = this.eventResultService.list(new QueryWrapper<EventResultEntity>().lambda()
				.eq(EventResultEntity::getEvent, event))
				.stream()
				.collect(Collectors.toMap(EventResultEntity::getEntry, v -> v));
		if (CollectionUtils.isEmpty(eventResultMap)) {
			log.error("event_result not update, event:{}!", event);
			return;
		}
		// update all tournaments
		List<TournamentInfoEntity> tournamentInfoList = this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
				.ne(TournamentInfoEntity::getKnockoutMode, KnockoutMode.No_knockout.name())
				.ge(TournamentInfoEntity::getKnockoutStartGw, event)
				.le(TournamentInfoEntity::getKnockoutEndGw, event)
				.orderByAsc(TournamentInfoEntity::getId));
		tournamentInfoList.forEach(tournamentInfoEntity -> this.updateSingleKnockoutResult(tournamentInfoEntity.getId(), event, eventResultMap));
		// clear
		eventResultMap.clear();
	}

	private void updateSingleKnockoutResult(int tournamentId, int event, @NotEmpty Map<Integer, EventResultEntity> eventResultMap) {
		// update tournament_knockout_result
		Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = this.updateKnockoutResult(tournamentId, event, eventResultMap);
		// update tournament_knockout
		Map<Integer, TournamentKnockoutResultData> nextKouckoutMap = this.updateKnockoutInfo(tournamentId, event, knockoutResultDataMap);
		// update next round entry for tournament_knockout and tournament_knockout_result
		this.updateNextKnockout(tournamentId, nextKouckoutMap);
	}

	private Multimap<Integer, TournamentKnockoutResultData> updateKnockoutResult(int tournamentId, int event, Map<Integer, EventResultEntity> eventResultMap) {
		Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = ArrayListMultimap.create();
		List<TournamentKnockoutResultEntity> knockoutResultUpdateList = Lists.newArrayList();
		List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
				.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).eq(TournamentKnockoutResultEntity::getEvent, event)
				.orderByAsc(TournamentKnockoutResultEntity::getMatchId));
		knockoutResultList.forEach(knockoutResult -> {
			int homeEntry = knockoutResult.getHomeEntry();
			int awayEntry = knockoutResult.getAwayEntry();
			EventResultEntity homeEventResult = eventResultMap.get(knockoutResult.getHomeEntry());
			EventResultEntity awayEventResult = eventResultMap.get(knockoutResult.getAwayEntry());
			int matchWinner = this.getMatchWinner(homeEntry, awayEntry, homeEventResult, awayEventResult);
			// add return data
			knockoutResultDataMap.put(knockoutResult.getMatchId(), new TournamentKnockoutResultData()
					.setTournamentId(tournamentId)
					.setEvent(event)
					.setPlayAgainstId(knockoutResult.getPlayAginstId())
					.setMatchId(knockoutResult.getMatchId())
					.setMatchWinner(matchWinner)
					.setWinnerRank(matchWinner == homeEntry ? homeEventResult.getOverallRank() : awayEventResult.getOverallRank())
			);
			// add update list
			knockoutResultUpdateList.add(knockoutResult
					.setHomeEntryNetPoint(homeEntry > 0 ? homeEventResult.getEventNetPoints() : 0)
					.setHomeEntryRank(homeEntry > 0 ? homeEventResult.getEventRank() : 0)
					.setHomeEntryChip(homeEntry > 0 ? homeEventResult.getEventChip() : Chip.NONE.getValue())
					.setAwayEntryNetPoint(awayEntry > 0 ? awayEventResult.getEventNetPoints() : 0)
					.setAwayEntryRank(awayEntry > 0 ? awayEventResult.getEventRank() : 0)
					.setAwayEntryChip(awayEntry > 0 ? awayEventResult.getEventChip() : Chip.NONE.getValue())
					.setMatchWinner(matchWinner));
		});
		// update
		this.tournamentKnockoutResultService.updateBatchById(knockoutResultUpdateList);
		// clear
		knockoutResultUpdateList.clear();
		return knockoutResultDataMap;
	}

	private Map<Integer, TournamentKnockoutResultData> updateKnockoutInfo(int tournamentId, int event,
	                                                                      @NotEmpty Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap) {
		Map<Integer, TournamentKnockoutResultData> nextKouckoutMap = Maps.newHashMap();
		List<TournamentKnockoutEntity> knockoutUpdateList = Lists.newArrayList();
		Map<Integer, TournamentKnockoutEntity> knockoutMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutEntity::getEndGw, event)
				.orderByAsc(TournamentKnockoutEntity::getMatchId))
				.stream()
				.collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, v -> v));
		// update by match id
		knockoutResultDataMap.keySet().forEach(matchId ->
				knockoutResultDataMap.get(matchId).forEach(resultData -> {
					// update tournament_knockout
					if (!knockoutMap.containsKey(matchId)) {
						return;
					}
					TournamentKnockoutEntity knockoutEntity = knockoutMap.get(matchId);
					if (resultData.getEvent() != knockoutEntity.getEndGw()) { // round not finished
						return;
					}
					int roundWinner = this.getRoundWinner(knockoutResultDataMap.get(matchId));
					knockoutUpdateList.add(knockoutEntity.setRoundWinner(roundWinner));
					// set return data for next round
					resultData.setNextMatchId(knockoutEntity.getNextMatchId()).setRoundWinner(roundWinner);
					if (matchId % 2 == 1) {
						resultData.setNextRoundHomeEntry(roundWinner);
					} else {
						resultData.setNextRoundAwayEntry(roundWinner);
					}
					nextKouckoutMap.put(knockoutEntity.getNextMatchId(), resultData);
				})
		);
		// update
		this.tournamentKnockoutService.updateBatchById(knockoutUpdateList);
		// clear
		knockoutUpdateList.clear();
		return nextKouckoutMap;
	}

	private void updateNextKnockout(int tournamentId, @NotEmpty Map<Integer, TournamentKnockoutResultData> nextKouckoutMap) {
		// tournament_knockout
		List<TournamentKnockoutEntity> updateKnockoutList = Lists.newArrayList();
		List<TournamentKnockoutEntity> knockoutEntityList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.in(TournamentKnockoutEntity::getNextMatchId, nextKouckoutMap.keySet()));
		knockoutEntityList.forEach(knockoutEntity -> updateKnockoutList.add(knockoutEntity
				.setHomeEntry(nextKouckoutMap.get(knockoutEntity.getNextMatchId()).getNextRoundHomeEntry())
				.setAwayEntry(nextKouckoutMap.get(knockoutEntity.getNextMatchId()).getNextRoundAwayEntry())));
		// tournament_knockout_result
		List<TournamentKnockoutResultEntity> updateKnockoutResultList = Lists.newArrayList();
		List<TournamentKnockoutResultEntity> knockoutResultEntityList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
				.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
				.in(TournamentKnockoutResultEntity::getMatchId, nextKouckoutMap.keySet()));
		knockoutResultEntityList.forEach(knockoutResultEntity -> knockoutResultEntityList.add(knockoutResultEntity
				.setHomeEntry(nextKouckoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundHomeEntry())
				.setAwayEntry(nextKouckoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundAwayEntry())));
		// save
		this.tournamentKnockoutService.saveOrUpdateBatch(updateKnockoutList);
		this.tournamentKnockoutResultService.saveOrUpdateBatch(updateKnockoutResultList);
		// clear
		updateKnockoutList.clear();
		updateKnockoutResultList.clear();
	}

	private void updateEventBaseData(int event) {
//		this.staticSerive.insertBaseData(event);
//		this.staticSerive.insertEventFixture(event);
		this.staticSerive.insertEventLive(event);
	}

	private String setUserPicks(List<Pick> picks, @NotEmpty Map<Integer, Integer> elementPointsMap) {
		List<Pick> pickList = Lists.newArrayList();
		picks.forEach(o -> {
			Pick pick = new Pick();
			pick.setElement(o.getElement());
			pick.setCaptain(o.isCaptain());
			pick.setViceCaptain(o.isViceCaptain());
			pick.setPoints(elementPointsMap.getOrDefault(o.getElement(), 0));
			pickList.add(pick);
		});
		return JsonUtils.obj2json(pickList);
	}

	private int getMatchWinner(int homeEntry, int awayEntry, EventResultEntity homeEntryResult, EventResultEntity awayEntryResult) {
		// if blank
		if (homeEntry <= 0) {
			return awayEntry;
		} else if (awayEntry <= 0) {
			return homeEntry;
		}
		// compare net points; if equal, compare rank; if equal, random
		int winner = this.compareNetPoint(homeEntry, homeEntryResult.getEventNetPoints(), awayEntry, awayEntryResult.getEventNetPoints());
		if (winner != 0) {
			return winner;
		}
		winner = this.compareRank(homeEntry, homeEntryResult.getOverallRank(), awayEntry, awayEntryResult.getOverallRank());
		if (winner != 0) {
			return winner;
		}
		return this.randomWinner(homeEntry, awayEntry);
	}

	private int compareNetPoint(int homeEntry, int homeNetPoint, int awayEntry, int awayNetPoint) {
		if (homeNetPoint > awayNetPoint) {
			return homeEntry;
		} else if (homeNetPoint < awayNetPoint) {
			return awayEntry;
		}
		return 0;
	}

	private int compareRank(int homeEntry, int homeRank, int awayEntry, int awayRank) {
		if (homeRank > awayRank) {
			return homeEntry;
		} else if (homeRank < awayRank) {
			return awayEntry;
		}
		return 0;
	}

	private int randomWinner(int homeEntry, int awayEntry) {
		if (new Random().nextInt(10) % 2 == 0) {
			return homeEntry;
		} else {
			return awayEntry;
		}
	}

	private int getRoundWinner(Collection<TournamentKnockoutResultData> collention) {
		List<TournamentKnockoutResultData> winners = new ArrayList<>(collention);
		int firstWinner = winners.get(0).getMatchWinner();
		Map<Integer, Integer> secondWinnerMap = collention.stream()
				.filter(tournamentKnockoutResultData -> tournamentKnockoutResultData.getMatchWinner() != firstWinner)
				.limit(1)
				.collect(Collectors.toMap(TournamentKnockoutResultData::getMatchWinner, TournamentKnockoutResultData::getWinnerRank)); // find the other match winner
		int secondWinner = secondWinnerMap.keySet().stream().findFirst().orElse(0);
		if (secondWinner == 0) { // all matches won by the first winner
			return firstWinner;
		}
		// tie, compare rank, then random
		int roundWinner = this.compareRank(firstWinner, winners.get(0).getWinnerRank(), secondWinner, secondWinnerMap.get(secondWinner));
		if (roundWinner != 0) {
			return roundWinner;
		}
		return this.randomWinner(firstWinner, secondWinner);
	}

}
