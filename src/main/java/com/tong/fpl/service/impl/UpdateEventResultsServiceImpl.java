package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutNextRoundData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.IUpdateEventResultsService;
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
public class UpdateEventResultsServiceImpl implements IUpdateEventResultsService {

	private final EventService eventService;
	private final EventLiveService eventLiveService;
	private final EntryEventResultService entryEventResultService;
	private final EntryInfoService entryInfoService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentGroupService tournamentGroupService;
	private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
	private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
	private final TournamentKnockoutService tournamentKnockoutService;
	private final TournamentKnockoutResultService tournamentKnockoutResultService;
	private final IStaticSerive staticSerive;
	private final IQuerySerivce querySerivce;

	@Override
	public void updateEntryInfo() {
		List<EntryInfoEntity> updateEntryInfoList = Lists.newArrayList();
		// get entry info list
		List<EntryInfoEntity> entryInfoList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
				.orderByAsc(EntryInfoEntity::getOverallRank).last("limit 1000"))
				.stream()
				.collect(Collectors.collectingAndThen(
						Collectors.toCollection(() -> new TreeSet<>(
								Comparator.comparing(EntryInfoEntity::getEntry)
						)), Lists::newArrayList));
		entryInfoList.parallelStream().forEach(entryInfoEntity -> {
			Optional<EntryRes> entryRes = this.staticSerive.getEntry(entryInfoEntity.getEntry());
			entryRes.ifPresent(entry -> updateEntryInfoList.add(entryInfoEntity
					.setEntryName(entry.getName())
					.setPlayerName(entry.getPlayerFirstName() + " " + entry.getPlayerLastName())
					.setOverallPoints(entry.getSummaryOverallPoints())
					.setOverallRank(entry.getSummaryOverallRank())
					.setBank(entry.getLastDeadlineBank())
					.setTeamValue(entry.getLastDeadlineValue())
					.setTotalTransfers(entry.getLastDeadlineTotalTransfers())
			));
		});
		// update
		this.entryInfoService.updateBatchById(updateEntryInfoList);
	}

	@Override
	public void updateTournamentEntryEventResult(int event, int tournamentId) {
		// get entry_list
		List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
		// remove first
		this.entryEventResultService.remove(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEvent, event)
				.in(EntryEventResultEntity::getEntry, entryList));
		// update entry_event_result
		List<EntryEventResultEntity> eventResultList = Lists.newArrayList();
		entryList.parallelStream().forEach(entry -> this.calcEntryEventPoints(event, entry, eventResultList));
		this.entryEventResultService.saveBatch(eventResultList);
	}

	private void calcEntryEventPoints(int event, int entry, List<EntryEventResultEntity> eventResultList) {
		if (entry <= 0) {
			return;
		}
		UserPicksRes userPick = this.querySerivce.getUserPicks(event, entry);
		boolean finished = this.eventService.getById(event).isFinished();
		Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event))
				.stream()
				.collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
		eventResultList.add(new EntryEventResultEntity()
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
	}

	@Override
	public void updatePointsRaceGroupResult(int event) {
		// update all points race groups
		this.tournamentInfoService.getAllPointsRaceGroupByEvent(event)
				.forEach(tournamentInfoEntity -> this.updatePointsRaceGroupResultByTournament(event, tournamentInfoEntity.getId()));
	}

	@Override
	public void updatePointsRaceGroupResultByTournament(int event, int tournamentId) {
		// get entry_list by tournament
		List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
		// get event_result list
		Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
		if (CollectionUtils.isEmpty(eventResultMap)) {
			log.error("event_result not updated, event:{}, tournament:{}!", event, tournamentId);
			return;
		}
		// update tournament_group
		List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.in(TournamentGroupEntity::getEntry, entryList));
		if (CollectionUtils.isEmpty(tournamentGroupEntityList)) {
			return;
		}
		// tournament_group
		List<TournamentGroupEntity> updateGroupList = Lists.newArrayList();
		// set point and rank
		tournamentGroupEntityList.forEach(tournamentGroupEntity -> tournamentGroupEntity
				.setOverallPoints(this.entryEventResultService.sumEventNetPoint(event, tournamentGroupEntity.getEntry()))
				.setOverallRank(eventResultMap.containsKey(tournamentGroupEntity.getEntry()) ?
						eventResultMap.get(tournamentGroupEntity.getEntry()).getOverallRank() : 0));
		// key:overall_rank, value:group_rank
		Map<String, Integer> groupRankMap = this.sortPointsRaceGroupRank(tournamentGroupEntityList);
		tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
			tournamentGroupEntity.setGroupPoints(tournamentGroupEntity.getOverallPoints())
					.setGroupRank(groupRankMap.getOrDefault(tournamentGroupEntity.getOverallPoints() + "-" + tournamentGroupEntity.getOverallRank(),
							0))
					.setPlay(event - tournamentGroupEntity.getStartGw() + 1);
			updateGroupList.add(tournamentGroupEntity);
		});
		// tournament_group_points_result
		List<TournamentPointsGroupResultEntity> updateGroupPointsResultList = Lists.newArrayList();
		tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
			int entry = tournamentGroupEntity.getEntry();
			TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = new TournamentPointsGroupResultEntity();
			tournamentPointsGroupResultEntity.setTournamentId(tournamentGroupEntity.getTournamentId());
			tournamentPointsGroupResultEntity.setGroupId(tournamentGroupEntity.getGroupId());
			tournamentPointsGroupResultEntity.setEvent(event);
			tournamentPointsGroupResultEntity.setEntry(entry);
			tournamentPointsGroupResultEntity.setEventGroupRank(tournamentGroupEntity.getGroupRank());
			if (eventResultMap.containsKey(entry)) {
				tournamentPointsGroupResultEntity.setEventPoints(eventResultMap.get(entry).getEventPoints());
				tournamentPointsGroupResultEntity.setEventCost(eventResultMap.get(entry).getEventTransfersCost());
				tournamentPointsGroupResultEntity.setEventNetPoints(tournamentPointsGroupResultEntity.getEventPoints() - tournamentPointsGroupResultEntity.getEventCost());
				tournamentPointsGroupResultEntity.setEventRank(eventResultMap.get(entry).getEventRank());
			} else {
				tournamentPointsGroupResultEntity.setEventPoints(0);
				tournamentPointsGroupResultEntity.setEventCost(0);
				tournamentPointsGroupResultEntity.setEventNetPoints(0);
				tournamentPointsGroupResultEntity.setEventRank(0);
			}
			updateGroupPointsResultList.add(tournamentPointsGroupResultEntity);
		});
		// update
		this.tournamentGroupService.updateBatchById(updateGroupList);
		this.tournamentPointsGroupResultService.saveOrUpdateBatch(updateGroupPointsResultList);
	}

	@Override
	public void updateBattleRaceGroupResult(int event) {
		// update all battle race groups
		this.tournamentInfoService.getAllBattleRaceGroupByEvent(event)
				.forEach(tournamentInfoEntity -> this.updateBattleRaceGroupResultByTournament(event, tournamentInfoEntity.getId()));
	}

	@Override
	public void updateBattleRaceGroupResultByTournament(int event, int tournamentId) {
		// get entry_list by tournament
		List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
		// get event_result list
		Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
		if (CollectionUtils.isEmpty(eventResultMap)) {
			log.error("event_result not update, event:{}, tournament:{}!", event, tournamentId);
			return;
		}
		// update tournament_group_battle_result
		Table<Integer, Integer, Integer> battleResultTable = this.updateGroupBattleResult(event, tournamentId, eventResultMap);
		// update tournament_group by group
		this.updateTournamentGroupByGroupId(tournamentId, battleResultTable, eventResultMap);
	}

	private Table<Integer, Integer, Integer> updateGroupBattleResult(int event, int tournamentId, Map<Integer, EntryEventResultEntity> eventResultMap) {
		List<TournamentBattleGroupResultEntity> updateGroupBattleResultList = Lists.newArrayList();
		Table<Integer, Integer, Integer> battleResultTable = HashBasedTable.create(); // groupId-> entry-> matchPoints
		this.tournamentBattleGroupResultService.list(new QueryWrapper<TournamentBattleGroupResultEntity>()
				.lambda()
				.eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId).eq(TournamentBattleGroupResultEntity::getEvent, event))
				.forEach(groupBattleResult -> {
					int homeEntry = groupBattleResult.getHomeEntry();
					int awayEntry = groupBattleResult.getAwayEntry();
					EntryEventResultEntity homeEventResult = eventResultMap.getOrDefault(homeEntry, null);
					EntryEventResultEntity awayEventResult = eventResultMap.getOrDefault(awayEntry, null);
					int homeEntryMatchPoints = this.getGroupBattleHomeEntryResult(homeEventResult, awayEventResult);
					int awayEntryMatchPoints = this.getGroupBattleHomeEntryResult(awayEventResult, homeEventResult);
					if (homeEntry != 0) {
						battleResultTable.put(groupBattleResult.getGroupId(), homeEntry, homeEntryMatchPoints);
					}
					if (awayEntry != 0) {
						battleResultTable.put(groupBattleResult.getGroupId(), awayEntry, awayEntryMatchPoints);
					}
					updateGroupBattleResultList.add(groupBattleResult
							.setHomeEntryNetPoints(homeEventResult != null ? homeEventResult.getEventNetPoints() : 0)
							.setHomeEntryRank(homeEventResult != null ? homeEventResult.getEventRank() : 0)
							.setHomeEntryMatchPoints(homeEntryMatchPoints)
							.setAwayEntryNetPoints(awayEventResult != null ? awayEventResult.getEventNetPoints() : 0)
							.setAwayEntryRank(awayEventResult != null ? awayEventResult.getEventRank() : 0)
							.setAwayEntryMatchPoints(awayEntryMatchPoints)
					);
				});
		// update
		this.tournamentBattleGroupResultService.updateBatchById(updateGroupBattleResultList);
		// return
		return battleResultTable;
	}

	private void updateTournamentGroupByGroupId(int tournamentId, Table<Integer, Integer, Integer> battleResultTable, Map<Integer, EntryEventResultEntity> eventResultMap) {
		List<TournamentGroupEntity> updateGroupList = Lists.newArrayList();
		// group qualifiers num
		int qualifiers = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getId, tournamentId)
				.eq(TournamentInfoEntity::getState, 1))
				.getGroupQualifiers();
		battleResultTable.rowKeySet().forEach(groupId -> {
			Map<Integer, Integer> matchResultMap = battleResultTable.row(groupId); // entry->matchPoints
			List<TournamentGroupEntity> groupList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
					.eq(TournamentGroupEntity::getTournamentId, tournamentId)
					.eq(TournamentGroupEntity::getGroupId, groupId)
					.orderByAsc(TournamentGroupEntity::getGroupIndex));
			groupList.forEach(o -> {
				EntryEventResultEntity eventResult = eventResultMap.getOrDefault(o.getEntry(), null);
				int matchPoints = matchResultMap.getOrDefault(o.getEntry(), 0);
				o.setGroupPoints(o.getGroupPoints() + matchPoints).setPlay(o.getPlay() + 1);
				if (eventResult != null) {
					o.setOverallPoints(o.getOverallPoints() + eventResult.getEventPoints()).setOverallRank(eventResult.getOverallRank());
				}
				if (matchPoints == 3) {
					o.setWin(o.getWin() + 1);
				} else if (matchPoints == 0) {
					o.setLose(o.getLose() + 1);
				} else {
					o.setDraw(o.getDraw() + 1);
				}
			});
			// sort group list by group points
			Map<String, Integer> groupRankMap = this.sortBattleGroupRank(groupList); // entry-> groupRank
			groupList.forEach(o -> {
				o.setGroupRank(groupRankMap.getOrDefault(o.getGroupPoints() + "-" + o.getOverallRank(), 0));
				o.setQualified(o.getGroupRank() <= qualifiers);
				updateGroupList.add(o);
			});
		});
		// update
		this.tournamentGroupService.updateBatchById(updateGroupList);
	}

	private Map<String, Integer> sortPointsRaceGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
		Map<String, Integer> groupRankMap = Maps.newHashMap(); // entry-> groupRank
		Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
		tournamentGroupEntityList.stream()
				.filter(o -> o.getOverallPoints() != 0)
				.sorted(Comparator.comparing(TournamentGroupEntity::getOverallPoints).reversed()
						.thenComparing(TournamentGroupEntity::getOverallRank))
				.forEachOrdered(o -> this.setGroupRankMapValue(o.getOverallPoints() + "-" + o.getOverallRank(), groupRankCountMap));
		tournamentGroupEntityList.stream()
				.filter(o -> o.getOverallPoints() == 0)
				.sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry))
				.forEachOrdered(o -> this.setGroupRankMapValue(o.getOverallPoints() + "-" + o.getOverallRank(), groupRankCountMap));
		int index = 1;
		for (String key :
				groupRankCountMap.keySet()) {
			groupRankMap.put(key, index);
			index += groupRankCountMap.get(key);
		}
		return groupRankMap;
	}

	private Map<String, Integer> sortBattleGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
		Map<String, Integer> groupRankMap = Maps.newLinkedHashMap();
		Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
		tournamentGroupEntityList.stream()
				.filter(o -> o.getOverallPoints() != 0)
				.sorted(Comparator.comparing(TournamentGroupEntity::getGroupPoints).reversed()
						.thenComparing(TournamentGroupEntity::getOverallRank))
				.forEachOrdered(o -> this.setGroupRankMapValue(o.getGroupPoints() + "-" + o.getOverallRank(), groupRankCountMap));
		tournamentGroupEntityList.stream()
				.filter(o -> o.getOverallPoints() == 0)
				.sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry))
				.forEachOrdered(o -> this.setGroupRankMapValue(o.getGroupPoints() + "-" + o.getOverallRank(), groupRankCountMap));
		int index = 1;
		for (String key :
				groupRankCountMap.keySet()) {
			groupRankMap.put(key, index);
			index += groupRankCountMap.get(key);
		}
		return groupRankMap;
	}

	private void setGroupRankMapValue(String key, Map<String, Integer> groupRankCountMap) {
		if (groupRankCountMap.containsKey(key)) {
			groupRankCountMap.put(key, groupRankCountMap.get(key) + 1);
		} else {
			groupRankCountMap.put(key, 1);
		}
	}

	@Override
	public void updateKnockoutResult(int event) {
		// update all knockouts
		this.tournamentInfoService.getAllKnockoutTournamentsByEvent(event)
				.forEach(tournamentInfoEntity -> this.updateKnockoutResultByTournament(event, tournamentInfoEntity.getId()));
	}

	@Override
	public void updateKnockoutResultByTournament(int event, int tournamentId) {
		// get entry_list by tournament
		List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
		// get event_result list
		Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
		if (CollectionUtils.isEmpty(eventResultMap)) {
			log.error("event_result not update, event:{}, tournament:{}!", event, tournamentId);
			return;
		}
		// update tournament_knockout_result
		Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = this.updateKnockoutResult(tournamentId, event, eventResultMap);
		if (CollectionUtils.isEmpty(knockoutResultDataMap.values())) {
			return;
		}
		// update tournament_knockout
		Map<Integer, TournamentKnockoutNextRoundData> nextKouckoutMap = this.updateKnockoutInfo(tournamentId, event, knockoutResultDataMap);
		if (CollectionUtils.isEmpty(nextKouckoutMap)) {
			return;
		}
		// update next round entry for tournament_knockout and tournament_knockout_result
		this.updateNextKnockout(tournamentId, nextKouckoutMap);
	}

	private Map<Integer, EntryEventResultEntity> getEntryEventResultByEvent(int event, List<Integer> entryList) {
		return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEvent, event)
				.in(EntryEventResultEntity::getEntry, entryList))
				.stream()
				.collect(Collectors.toMap(EntryEventResultEntity::getEntry, v -> v));
	}

	private Multimap<Integer, TournamentKnockoutResultData> updateKnockoutResult(int tournamentId, int event, Map<Integer, EntryEventResultEntity> eventResultMap) {
		// matchId->resultData
		Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = ArrayListMultimap.create();
		List<TournamentKnockoutResultEntity> knockoutResultUpdateList = Lists.newArrayList();
		// get knockout_result
		List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
				.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutResultEntity::getEvent, event)
				.orderByAsc(TournamentKnockoutResultEntity::getMatchId));
		// get match winner and return
		knockoutResultList.forEach(knockoutResult -> {
			int homeEntry = knockoutResult.getHomeEntry();
			int awayEntry = knockoutResult.getAwayEntry();
			EntryEventResultEntity homeEventResult = eventResultMap.get(homeEntry);
			EntryEventResultEntity awayEventResult = eventResultMap.get(awayEntry);
			int matchWinner = this.getMatchWinner(homeEntry, awayEntry, homeEventResult, awayEventResult);
			// add return data
			knockoutResultDataMap.put(knockoutResult.getMatchId(), new TournamentKnockoutResultData()
					.setEvent(event)
					.setPlayAgainstId(knockoutResult.getPlayAginstId())
					.setMatchId(knockoutResult.getMatchId())
					.setMatchWinner(matchWinner)
					.setWinnerRank(matchWinner == homeEntry ? homeEventResult.getOverallRank() : awayEventResult.getOverallRank())
			);
			// add update list
			knockoutResultUpdateList.add(knockoutResult
					.setHomeEntryNetPoints(homeEntry > 0 ? homeEventResult.getEventNetPoints() : 0)
					.setHomeEntryRank(homeEntry > 0 ? homeEventResult.getEventRank() : 0)
					.setAwayEntryNetPoints(awayEntry > 0 ? awayEventResult.getEventNetPoints() : 0)
					.setAwayEntryRank(awayEntry > 0 ? awayEventResult.getEventRank() : 0)
					.setMatchWinner(matchWinner));
		});
		// update
		this.tournamentKnockoutResultService.updateBatchById(knockoutResultUpdateList);
		return knockoutResultDataMap;
	}

	private Map<Integer, TournamentKnockoutNextRoundData> updateKnockoutInfo(int tournamentId, int event,
	                                                                         @NotEmpty Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap) {
		// nextMatchId->resultData
		Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap = Maps.newHashMap();
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
					knockoutEntity.setRoundWinner(this.getRoundWinner(knockoutResultDataMap.get(matchId)));
					knockoutUpdateList.add(knockoutEntity);
					// set next round data
					this.setNextRoundData(nextKnockoutMap, knockoutEntity);
				})
		);
		// update
		this.tournamentKnockoutService.updateBatchById(knockoutUpdateList);
		return nextKnockoutMap;
	}

	private void updateNextKnockout(int tournamentId, @NotEmpty Map<Integer, TournamentKnockoutNextRoundData> nextKouckoutMap) {
		// get round
		int nextRound = nextKouckoutMap.values()
				.stream()
				.map(TournamentKnockoutNextRoundData::getNextRound)
				.findFirst()
				.orElse(0);
		if (nextRound == 0) {
			return;
		}
		// tournament_knockout
		List<TournamentKnockoutEntity> updateKnockoutList = Lists.newArrayList();
		List<TournamentKnockoutEntity> knockoutEntityList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.eq(TournamentKnockoutEntity::getRound, nextRound));
		knockoutEntityList.forEach(knockoutEntity -> updateKnockoutList.add(knockoutEntity
				.setHomeEntry(nextKouckoutMap.get(knockoutEntity.getMatchId()).getNextRoundHomeEntry())
				.setAwayEntry(nextKouckoutMap.get(knockoutEntity.getMatchId()).getNextRoundAwayEntry())));
		// tournament_knockout_result
		List<TournamentKnockoutResultEntity> updateKnockoutResultList = Lists.newArrayList();
		List<TournamentKnockoutResultEntity> knockoutResultEntityList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
				.eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
				.in(TournamentKnockoutResultEntity::getMatchId, nextKouckoutMap.keySet()));
		knockoutResultEntityList.forEach(knockoutResultEntity -> updateKnockoutResultList.add(knockoutResultEntity
				.setHomeEntry(nextKouckoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundHomeEntry())
				.setAwayEntry(nextKouckoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundAwayEntry())));
		// save
		this.tournamentKnockoutService.saveOrUpdateBatch(updateKnockoutList);
		this.tournamentKnockoutResultService.saveOrUpdateBatch(updateKnockoutResultList);
	}

	private String setUserPicks(List<Pick> picks, @NotEmpty Map<Integer, Integer> elementPointsMap) {
		List<EntryPickData> pickList = Lists.newArrayList();
		picks.forEach(o -> pickList.add(new EntryPickData()
				.setElement(o.getElement())
				.setPosition(o.getPosition())
				.setMultiplier(o.getMultiplier())
				.setCaptain(o.isCaptain())
				.setViceCaptain(o.isViceCaptain())
				.setPoints(elementPointsMap.getOrDefault(o.getElement(), 0))
		));
		return JsonUtils.obj2json(pickList);
	}

	private int getMatchWinner(int homeEntry, int awayEntry, EntryEventResultEntity homeEntryResult, EntryEventResultEntity awayEntryResult) {
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

	private int getRoundWinner(@NotEmpty Collection<TournamentKnockoutResultData> collention) {
		if (collention.size() == 1) {
			return collention.stream().map(TournamentKnockoutResultData::getMatchWinner).findFirst().orElse(0);
		}
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

	private void setNextRoundData(Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap, TournamentKnockoutEntity knockoutEntity) {
		TournamentKnockoutNextRoundData nextRoundData = nextKnockoutMap.getOrDefault(knockoutEntity.getNextMatchId(), new TournamentKnockoutNextRoundData());
		nextRoundData.setNextMatchId(knockoutEntity.getNextMatchId());
		nextRoundData.setNextRound(knockoutEntity.getRound() + 1);
		if (knockoutEntity.getMatchId() % 2 == 1) {
			nextRoundData.setNextRoundHomeEntry(knockoutEntity.getRoundWinner());
		} else {
			nextRoundData.setNextRoundAwayEntry(knockoutEntity.getRoundWinner());
		}
		nextKnockoutMap.put(nextRoundData.getNextMatchId(), nextRoundData);
	}

	private int getGroupBattleHomeEntryResult(EntryEventResultEntity firstEventResult, EntryEventResultEntity secondEventResult) {
		if (firstEventResult == null) {
			return 0;
		} else if (secondEventResult == null) {
			return 3;
		}
		if (firstEventResult.getEventNetPoints() > secondEventResult.getEventNetPoints()) {
			return 3;
		} else if (firstEventResult.getEventNetPoints() < secondEventResult.getEventNetPoints()) {
			return 0;
		}
		return 1;
	}

}
