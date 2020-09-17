package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.league.LeagueData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IReportService;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/9/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportServiceImpl implements IReportService {

	private final IQuerySerivce querySerivce;
	private final IStaticSerive staticSerive;
	private final EventLiveService eventLiveService;
	private final EntryEventResultService entryEventResultService;
	private final EntryCaptainStatService entryCaptainStatService;
	private final LeagueResultStatService leagueResultStatService;
	private final TeamSelectStatService teamSelectStatService;

	@Override
	public void insertEntryCaptainStat(int tournamentId) {
		this.entryCaptainStatService.remove(new QueryWrapper<EntryCaptainStatEntity>().eq("1", 1));
		List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
		List<EntryCaptainStatEntity> entryCaptainStatList = Lists.newArrayList();
		entryList.forEach(entry -> this.initEntryEventCaptainStat(entry, entryCaptainStatList));
		// insert
		this.entryCaptainStatService.saveBatch(entryCaptainStatList);
	}

	private void initEntryEventCaptainStat(int entry, List<EntryCaptainStatEntity> entryCaptainStatList) {
		// entry_info
		EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(entry);
		if (entryInfoEntity == null) {
			return;
		}
		// entry_event_result
		List<EntryEventResultEntity> entryEventResultList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEntry, entry)
				.gt(EntryEventResultEntity::getEventPoints, 0));
		if (CollectionUtils.isEmpty(entryEventResultList)) {
			return;
		}
		entryEventResultList.forEach(entryEventResult -> {
			List<EntryPickData> captainPickList = this.querySerivce.qryPickListFromPicks(entryEventResult.getEventPicks())
					.stream()
					.filter(o -> o.isCaptain() || o.isViceCaptain())
					.collect(Collectors.toList());
			EntryPickData captainPick = this.getRealCaptainPoints(captainPickList);
			if (captainPick == null) {
				return;
			}
			EntryCaptainStatEntity entryCaptainStatEntity = new EntryCaptainStatEntity();
			entryCaptainStatEntity.setEntry(entry)
					.setEvent(entryEventResult.getEvent())
					.setEntryName(entryInfoEntity.getEntryName())
					.setPlayerName(entryInfoEntity.getPlayerName())
					.setOverallPoints(entryInfoEntity.getOverallPoints())
					.setOverallRank(entryInfoEntity.getOverallRank())
					.setChip(entryEventResult.getEventChip())
					.setElement(captainPick.getElement())
					.setWebName(this.querySerivce.getPlayerByElememt(captainPick.getElement()).getWebName())
					.setPoints(captainPick.getPoints());
			entryCaptainStatEntity.setTotalPoints(Chip.getChipFromValue(entryCaptainStatEntity.getChip()).equals(Chip.TC) ?
					3 * entryCaptainStatEntity.getPoints() : 2 * entryCaptainStatEntity.getPoints());
			entryCaptainStatList.add(entryCaptainStatEntity);
		});
	}

	private EntryPickData getRealCaptainPoints(List<EntryPickData> captainPickList) {
		if (CollectionUtils.isEmpty(captainPickList)) {
			return null;
		}
		EntryPickData captain = captainPickList.stream().filter(EntryPickData::isCaptain).findFirst().orElse(null);
		EntryPickData viceCaptain = captainPickList.stream().filter(EntryPickData::isViceCaptain).findFirst().orElse(null);
		if (captain == null || viceCaptain == null) {
			return null;
		}
		if (captain.getPoints() == 0 && viceCaptain.getPoints() > 0) {
			return viceCaptain;
		}
		return captain;
	}

	@Override
	public void insertLeagueResultStat(int event, String leagueType, int leagueId, int limit) {
		// get league Entry
		LeagueData leagueData = this.getLeagueDataByTypeAndId(leagueType, leagueId, limit);
		String leagueName = leagueData.getName();
		// prepare
		Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event))
				.stream()
				.collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
		// init league result stat
		List<EntryInfoData> entryInfoDataList = leagueData.getEntryInfoList();
		log.info("entryInfoDataList size:{}", entryInfoDataList.size());
		// async
		List<CompletableFuture<LeagueResultStatEntity>> future = entryInfoDataList.stream()
				.map(o -> CompletableFuture.supplyAsync(() -> this.initEntryResultStat(event, o, elementPointsMap), new ForkJoinPool(10)))
				.collect(Collectors.toList());
		List<LeagueResultStatEntity> leagueResultStatList = future
				.stream()
				.map(CompletableFuture::join)
				.collect(Collectors.toList());
		log.info("leagueResultStatList size:{}", leagueResultStatList.size());
		// league info
		leagueResultStatList.forEach(o -> o.setLeagueId(leagueId).setLeagueType(leagueType).setLeagueName(leagueName));
		// save
		this.leagueResultStatService.saveBatch(leagueResultStatList);
		log.info("insert league_result_stat size:{}!", leagueResultStatList.size());
	}

	private LeagueData getLeagueDataByTypeAndId(String leagueType, int leagueId, int limit) {
		LeagueData leagueData = new LeagueData();
		if (LeagueType.valueOf(leagueType).equals(LeagueType.Classic)) {
			leagueData = this.staticSerive.getEntryInfoListFromClassicByLimit(leagueId, limit);
		} else if (LeagueType.valueOf(leagueType).equals(LeagueType.H2h)) {
			leagueData = this.staticSerive.getEntryInfoListFromH2hByLimit(leagueId, limit);
		}
		return leagueData;
	}

	private LeagueResultStatEntity initEntryResultStat(int event, EntryInfoData entryInfoData, Map<Integer, Integer> elementPointsMap) {
		LeagueResultStatEntity leagueResultStatEntity = new LeagueResultStatEntity();
		// entry info
		BeanUtil.copyProperties(entryInfoData, leagueResultStatEntity, CopyOptions.create().ignoreNullValue());
		// entry event result
		int entry = entryInfoData.getEntry();
		Optional<UserPicksRes> result = this.staticSerive.getUserPicks(event, entry);
		result.ifPresent(userPick ->
				leagueResultStatEntity.setEntry(entry)
						.setEvent(event)
						.setOverallPoints(userPick.getEntryHistory().getTotalPoints())
						.setOverallRank(userPick.getEntryHistory().getOverallRank())
						.setBank(userPick.getEntryHistory().getBank())
						.setTeamValue(userPick.getEntryHistory().getValue())
						.setEventPoints(userPick.getEntryHistory().getPoints())
						.setEventTransfers(userPick.getEntryHistory().getEventTransfers())
						.setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
						.setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
						.setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench())
						.setEventRank(userPick.getEntryHistory().getRank())
						.setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip())
						.setEventPicks(this.setUserPicks(userPick.getPicks(), elementPointsMap)));
		// event captain
		List<EntryPickData> captainPickList = this.querySerivce.qryPickListFromPicks(leagueResultStatEntity.getEventPicks())
				.stream()
				.filter(o -> o.isCaptain() || o.isViceCaptain())
				.collect(Collectors.toList());
		EntryPickData captainPick = this.getRealCaptainPoints(captainPickList);
		if (captainPick != null) {
			leagueResultStatEntity
					.setEventCaptain(captainPick.getWebName())
					.setEventCaptainPoints(captainPick.getPoints());
		}
		return leagueResultStatEntity;
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

	@Override
	public void inertTeamSelectStat(int event, String leagueType, int leagueId, int limit) {
		// get league Entry
		LeagueData leagueData = this.getLeagueDataByTypeAndId(leagueType, leagueId, limit);
		// init league result stat
		List<EntryInfoData> entryInfoDataList = leagueData.getEntryInfoList();
		// get user picks
		List<CompletableFuture<TeamSelectStatEntity>> future = entryInfoDataList.stream()
				.map(o ->
						CompletableFuture.supplyAsync(() ->
								this.initEntryTeamSelectStat(event, o.getEntry(), leagueData.getName()), new ForkJoinPool(50)))
				.collect(Collectors.toList());
		List<TeamSelectStatEntity> teamSelectStatList = future
				.stream()
				.map(CompletableFuture::join)
				.collect(Collectors.toList());
		// save
		this.teamSelectStatService.saveBatch(teamSelectStatList);
		log.info("insert team_select_stat size:{}!", teamSelectStatList.size());
	}

	private TeamSelectStatEntity initEntryTeamSelectStat(int event, int entry, String leagueName) {
		UserPicksRes userPicksRes = this.querySerivce.getUserPicks(event, entry);
		if (userPicksRes == null) {
			return new TeamSelectStatEntity();
		}
		List<Pick> picks = userPicksRes.getPicks();
		TeamSelectStatEntity teamSelectStatEntity = new TeamSelectStatEntity();
		teamSelectStatEntity.setLeagueName(leagueName)
				.setEvent(event)
				.setEntry(entry)
				.setChip(userPicksRes.getActiveChip() == null ? "n/a" : userPicksRes.getActiveChip())
				.setPosition1(picks.get(0).getElement())
				.setPosition2(picks.get(1).getElement())
				.setPosition3(picks.get(2).getElement())
				.setPosition4(picks.get(3).getElement())
				.setPosition5(picks.get(4).getElement())
				.setPosition6(picks.get(5).getElement())
				.setPosition7(picks.get(6).getElement())
				.setPosition8(picks.get(7).getElement())
				.setPosition9(picks.get(8).getElement())
				.setPosition10(picks.get(9).getElement())
				.setPosition11(picks.get(10).getElement())
				.setPosition12(picks.get(11).getElement())
				.setPosition13(picks.get(12).getElement())
				.setPosition14(picks.get(13).getElement())
				.setPosition15(picks.get(14).getElement());
		teamSelectStatEntity.setCaptain(picks
				.stream()
				.filter(Pick::isCaptain)
				.map(Pick::getElement)
				.findFirst()
				.orElse(0)
		);
		teamSelectStatEntity.setViceCaptain(picks
				.stream()
				.filter(Pick::isViceCaptain)
				.map(Pick::getElement)
				.findFirst()
				.orElse(0)
		);
		return teamSelectStatEntity;
	}

	@Override
	public Map<String, String> getTopSelectedMap(String leagueName, int event, boolean budge) {
		List<TeamSelectStatEntity> list = this.teamSelectStatService.list(new QueryWrapper<TeamSelectStatEntity>().lambda()
				.eq(TeamSelectStatEntity::getLeagueName, leagueName)
				.eq(TeamSelectStatEntity::getEvent, event));
		if (CollectionUtils.isEmpty(list)) {
			return Maps.newHashMap();
		}
		if (budge) {
			return this.getTopSelectedMapWithBudge(list);
		} else {
			return this.getTopSelectedMapWithoutBudge(list);
		}
	}

	private Map<String, String> getTopSelectedMapWithBudge(List<TeamSelectStatEntity> list) {
		return null;
	}

	private Map<String, String> getTopSelectedMapWithoutBudge(List<TeamSelectStatEntity> list) {
		return null;
	}

}
