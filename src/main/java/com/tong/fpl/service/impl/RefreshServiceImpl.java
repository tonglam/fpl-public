package com.tong.fpl.service.impl;

import com.tong.fpl.domain.entity.LeagueEventReportEntity;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRefreshService;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/3/10
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RefreshServiceImpl implements IRefreshService {

    private final IQueryService queryService;
    private final IDataService dataService;

    @Override
    public void refreshEventLive(int event) {
        if (event < 1 || event > 38) {
            return;
        }
        this.dataService.updateEventLive(event);
        log.info("event:{}, refresh event_live success", event);
    }

    @Override
    public void refreshEventLiveCache(int event) {
        if (event < 1 || event > 38) {
            return;
        }
        if (!this.queryService.isMatchDayTime(event)) {
            return;
        }
        this.dataService.updateEventLiveCache(event);
        log.info("event:{}, refresh event_live cache success", event);
    }

    @Override
    public void refreshPlayerValue() {
        int event = this.queryService.getCurrentEvent();
        if (event < 0 || event > 38) {
            return;
        }
        this.dataService.updatePlayerValue();
        log.info("event:{}, refresh player_value success", event);
        // clear cache
        RedisUtils.removeCacheByKey("api::qryPlayerValue");
    }

    @Override
    public void refreshPlayerStat() {
        int event = this.queryService.getCurrentEvent();
        if (event < 0 || event > 38) {
            return;
        }
        this.dataService.updatePlayerStat();
        log.info("event:{}, refresh player_stat success", event);
    }

    @Override
    public void refreshEventOverall(int event) {
        if (event < 1 || event > 38) {
            return;
        }
        this.dataService.upsertEventOverallResult();
        log.info("event:{}, refresh event_overall success", event);
    }

    @Override
    public void refreshEntryInfo(int entry) {
        this.dataService.upsertEntryInfo(entry);
        log.info("entry:{}, refresh entry_info success", entry);
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntryInfo", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntryLeagueInfo", entry));
    }

    @Override
    public void refreshEntryEventTransfers(int event, int entry) {
        this.dataService.updateEntryEventTransfers(event, entry);
        log.info("event:{}, entry:{}, refresh entry_event_transfers success", event, entry);
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntryEventTransfers", event, entry));
    }

    @Override
    public void refreshEntryEventResult(int event, int entry) {
        this.dataService.upsertEntryEventResult(event, entry);
        log.info("event:{}, entry:{}, refresh entry_event_result success", event, entry);
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntryEventResult", event, entry));
    }

    @Override
    public void refreshCurrentEventScoutResult(int entry) {
        int nextEvent = this.queryService.getNextEvent();
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventScoutPickResult", nextEvent, entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventScoutResult", nextEvent));
    }

    @Override
    public void refreshTournamentEventResult(int event, int tournamentId) {
        // tournament_result
        this.dataService.upsertTournamentEventResult(event, tournamentId);
        log.info("event:{}, tournament:{}, refresh tournament entry event result success", event, tournamentId);
        // tournament_points_race_group_result
        this.dataService.updatePointsRaceGroupResult(event, tournamentId);
        log.info("event:{}, tournament:{}, refresh tournament points race group result success", event, tournamentId);
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryTournamentEventResult", event, tournamentId));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryTournamentEventChampion", tournamentId));
    }

    @Override
    public void refreshEntrySummary(int event, int entry) {
        // entry_event_result
        this.refreshEntryEventResult(event, entry);
        // entry_event_transfers
        this.refreshEntryEventTransfers(event, entry);
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonInfo", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonSummary", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonCaptain", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonTransfers", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonScore", entry));
    }

    @Override
    public void refreshLeagueSummary(int event, String leagueName, int entry) {
        LeagueEventReportEntity leagueEventReportEntity = this.queryService.qryLeagueInfoByName(leagueName);
        if (leagueEventReportEntity == null) {
            return;
        }
        this.dataService.updateLeagueEventResult(event, leagueEventReportEntity.getLeagueId());
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSeasonInfo", leagueName));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSeasonSummary", leagueName, entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSeasonCaptain", leagueName, entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSeasonScore", leagueName, entry));
    }

    @Override
    public void refreshLeagueSelect(int event, String leagueName) {
        LeagueEventReportEntity leagueEventReportEntity = this.queryService.qryLeagueInfoByName(leagueName);
        if (leagueEventReportEntity == null) {
            return;
        }
        int leagueId = leagueEventReportEntity.getLeagueId();
        String leagueType = leagueEventReportEntity.getLeagueType();
        this.dataService.updateLeagueEventResult(event, leagueId);
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSelectByName", CommonUtils.getCurrentSeason(), event, leagueName));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueEventEoWebNameMap", CommonUtils.getCurrentSeason(), event, leagueId, leagueType));
    }

    @Override
    public void refreshPlayerSummary(String season, int code) {
        if (StringUtils.equals(season, CommonUtils.getCurrentSeason())) {
            this.dataService.updateEventLive(this.queryService.getCurrentEvent());
        }
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryPlayerInfo", season, code));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryPlayerSummary", season, code));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "qryPlayerDetailData", season));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "qryPlayerFixtureList", season));
    }

    @Override
    public void refreshTeamSummary(String season, String name) {
        if (StringUtils.equals(season, CommonUtils.getCurrentSeason())) {
            this.dataService.updatePlayerValue();
            this.dataService.updatePlayerStat();
        }
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryTeamSummary", season, name));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "qryPlayerFixtureList", season));
    }

    @Override
    public void refreshEventOverallSummary(int event) {
        // refresh
        this.refreshEventOverall(event);
        this.refreshEventLive(event);
        this.refreshPlayerStat();
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventOverallResult", event));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventDreamTeam", event));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventEliteElements", event));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventOverallTransfers", event));
    }

    @Override
    public void refreshEventSourceScoutResult(int event) {
        // refresh
        this.refreshEventLive(event);
        this.dataService.updateEventSourceScoutResult(event);
        // clear cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::api::qryEventSourceScoutResult", event));
    }

}
