package com.tong.fpl.task;

import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Create by tong on 2020/9/16
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportTask {

    private final IQueryService queryService;
    private final IReportService reportService;

    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
    public void insertLeagueEventSelectStat() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isSelectTime(event)) {
            return;
        }
        this.queryService.qryLeagueMap(event).forEach((leagueIdStr, leagueType) -> {
            int leagueId = Integer.parseInt(leagueIdStr);
            if (leagueId == 99999) {
                return;
            }
            if (this.reportService.eventLeagueEventExists(event, leagueId, leagueType)) {
                return;
            }
            this.reportService.insertLeagueEventSelect(event, leagueId, leagueType, 0);
        });
    }

//    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
//    public void insertLeagueEventSelectTournamentStat() {
//        int event = this.queryService.getCurrentEvent();
//        if (!this.queryService.isSelectTime(event)) {
//            return;
//        }
//        String leagueType = "Tournament";
//        List<Integer> tournamentList = Lists.newArrayList(13, 14);
//        tournamentList.forEach(tournamentId -> {
//            if (this.reportService.eventLeagueEventExists(event, tournamentId, leagueType)) {
//                return;
//            }
//            this.reportService.insertEntryLeagueEventSelectByTournament(event, tournamentId);
//        });
//    }

    @Scheduled(cron = "0 0 9,12 * * *")
    public void updateLeagueEventResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        Map<String, String> leagueMap = this.queryService.qryLeagueMap(event);
//        leagueMap.put("13", "Tournament");
//        leagueMap.put("14", "Tournament");
        leagueMap.forEach((leagueIdStr, leagueType) -> {
            try {
                log.info("league_id:{}, league_type:{}, start update league event result", leagueIdStr, leagueType);
                this.reportService.updateLeagueEventResult(event, Integer.parseInt(leagueIdStr), leagueType);
                log.info("league_id:{}, league_type:{}, finish update league event result", leagueIdStr, leagueType);

            } catch (Exception e) {
                log.error("league_id:{}, league_type:{}, update league event result error:{}", leagueIdStr, leagueType, e.getMessage());
            }
        });
    }

}
