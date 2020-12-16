package com.tong.fpl.task;

import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.TournamentMode;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Create by tong on 2020/7/21
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MatchDayTask {

	private final IQuerySerivce querySerivce;
	private final IRedisCacheSerive redisCacheSerive;
	private final ITournamentService tournamentService;
	private final IUpdateEventResultService updateEventResultsService;
	private final IScoutService scoutService;

	@Scheduled(cron = "0 0/5 0-7,19-23 * * *")
	public void insertEventLiveCache() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDayTime(event)) {
			return;
		}
		TaskLog.info("start true insertEventLiveCache task");
		this.redisCacheSerive.insertEventLiveCache(event);
		this.redisCacheSerive.insertLiveBonusCache();
		this.redisCacheSerive.insertSingleEventFixtureCache(event);
		this.redisCacheSerive.insertLiveFixtureCache();
	}

	@Scheduled(cron = "0 30 6 * * *")
	public void insertEventLive() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		TaskLog.info("start true insertEventLive task");
		this.redisCacheSerive.insertSingleEventFixture(event);
		this.redisCacheSerive.insertLiveFixtureCache();
		this.redisCacheSerive.insertLiveBonusCache();
		this.redisCacheSerive.insertEventLive(event);
	}

	@Scheduled(cron = "0 35 6/8 * * *")
	public void insertEvent() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		TaskLog.info("start true insertEvent task");
		this.redisCacheSerive.insertEvent();
	}

	@Scheduled(cron = "0 0 7/9 * * *")
	public void updateTournamentResult() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		this.redisCacheSerive.insertEventLive(event);
		this.querySerivce.qryAllTournamentList()
				.stream()
				.map(TournamentInfoEntity::getId)
				.forEach(tournamentId -> this.updateSingleTournamentResult(event, tournamentId));
	}

	private void updateSingleTournamentResult(int event, int tournamentId) {
		try {
			TaskLog.info("start add tournament new entry, event:{}, tournament:{}", event, tournamentId);
			this.tournamentService.addTournamentNewEntry(tournamentId);
			TaskLog.info("start update tournament result, event:{}, tournament:{}", event, tournamentId);
			this.updateEventResultsService.updateTournamentEntryEventResult(event, tournamentId);
			TaskLog.info("end update tournament result, event:{}, tournament:{}", event, tournamentId);
		} catch (Exception e) {
			e.printStackTrace();
			TaskLog.error("update tournament result error:{}, event:{}, tournament:{}", e.getMessage(), event, tournamentId);
		}
	}

	@Scheduled(cron = "0 5 8/9 * * *")
	public void updatePointsRaceGroupResult() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		this.querySerivce.qryAllTournamentList()
				.stream()
				.filter(o -> StringUtils.equals(o.getTournamentMode(), TournamentMode.Normal.name()))
				.filter(o -> StringUtils.equals(o.getGroupMode(), GroupMode.Points_race.name()))
				.filter(o -> o.getGroupStartGw() <= event && o.getGroupEndGw() >= event)
				.map(TournamentInfoEntity::getId)
				.forEach(tournamentId -> this.updateSinglePointsRaceGroupResult(event, tournamentId));
	}

	private void updateSinglePointsRaceGroupResult(int event, int tournamentId) {
		try {
			TaskLog.info("start update points_race group result, event:{}, tournament:{}", event, tournamentId);
			this.updateEventResultsService.updatePointsRaceGroupResult(event, tournamentId);
			TaskLog.info("end update points_race group result, event:{}, tournament:{}", event, tournamentId);
		} catch (Exception e) {
			e.printStackTrace();
			TaskLog.error("update points_race group result error:{}, event:{}, tournament:{}", e.getMessage(), event, tournamentId);
		}
	}

	@Scheduled(cron = "0 10 8/9 * * *")
	public void updateBattleRaceGroupResult() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		this.querySerivce.qryAllTournamentList()
				.stream()
				.filter(o -> StringUtils.equals(o.getTournamentMode(), TournamentMode.Normal.name()))
				.filter(o -> StringUtils.equals(o.getGroupMode(), GroupMode.Battle_race.name()))
				.filter(o -> o.getGroupStartGw() <= event && o.getGroupEndGw() >= event)
				.map(TournamentInfoEntity::getId)
				.forEach(tournamentId -> this.updateSingleBattleRaceGroupResult(event, tournamentId));
	}

	private void updateSingleBattleRaceGroupResult(int event, int tournamentId) {
		try {
			TaskLog.info("start update battle_race group result, event:{}, tournament:{}", event, tournamentId);
			this.updateEventResultsService.updateBattleRaceGroupResult(event, tournamentId);
			TaskLog.info("end update battle_race group result, event:{}, tournament:{}", event, tournamentId);
		} catch (Exception e) {
			e.printStackTrace();
			TaskLog.error("update battle_race group result error:{}, event:{}, tournament:{}", e.getMessage(), event, tournamentId);
		}
	}

	@Scheduled(cron = "0 15 8/9 * * *")
	public void updateKnockoutResult() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		this.querySerivce.qryAllTournamentList()
				.stream()
				.filter(o -> StringUtils.equals(o.getTournamentMode(), TournamentMode.Normal.name()))
				.filter(o -> !StringUtils.equals(o.getKnockoutMode(), KnockoutMode.No_knockout.name()))
				.filter(o -> o.getKnockoutStartGw() <= event && o.getKnockoutEndGw() >= event)
				.map(TournamentInfoEntity::getId)
				.forEach(tournamentId -> this.updateSingleKnockoutResult(event, tournamentId));
	}

	private void updateSingleKnockoutResult(int event, int tournamentId) {
		try {
			TaskLog.info("start update knockout result, event:{}, tournament:{}", event, tournamentId);
			this.updateEventResultsService.updateKnockoutResult(event, tournamentId);
			TaskLog.info("end update knockout result, event:{}, tournament:{}", event, tournamentId);
		} catch (Exception e) {
			e.printStackTrace();
			TaskLog.error("update knockout result error:{}, event:{}, tournament:{}", e.getMessage(), event, tournamentId);
		}
	}

	@Scheduled(cron = "0 20 8/9 * * *")
	public void updateScoutResult() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		this.scoutService.updateEventScoutResult(event);
	}

	@Scheduled(cron = "0 0/5 0-4,18-23 * * *")
	public void insertEventTransfer() {
		int current = this.querySerivce.getCurrentEvent();
		if (!this.isNotSelectTime(current)) {
			return;
		}
		TaskLog.info("start true insertEventTransfer task");
		this.querySerivce.qryAllTournamentList()
				.stream()
				.map(TournamentInfoEntity::getId)
				.distinct()
				.forEach(this.updateEventResultsService::updateTournamentEntryEventTransfer);
	}

	private boolean isNotSelectTime(int event) {
		LocalDateTime localDateTime = LocalDateTime.parse(this.querySerivce.getDeadlineByEvent(event).replace(" ", "T"));
		return LocalDateTime.now().equals(localDateTime);
	}

}
