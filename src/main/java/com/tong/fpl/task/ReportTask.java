package com.tong.fpl.task;

import com.google.common.collect.Maps;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;

/**
 * Create by tong on 2020/9/16
 */
@Slf4j
//@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportTask {

    private final IReportService reportService;
    private final IQuerySerivce querySerivce;

    @Scheduled(cron = "0 45 19 * * *")
    public void insertLeagueResultStat() {
        log.info("start insertLeagueResultStat task");
        int event = this.querySerivce.getCurrentEvent();
        Map<Integer, Integer> map = Maps.newHashMap();
        map.put(314, 10000);
        map.put(65, 0);
        map.keySet().forEach(leagueId -> {
            log.info("start insert:{}", leagueId);
            try {
                this.reportService.inertTeamSelectStat(event, "Classic", leagueId, map.get(leagueId));
                log.info("end insert:{}", leagueId);
            } catch (Exception e) {
                log.error("insert:{}, error:{}", leagueId, e.getMessage());
                this.insertLeagueResultStat();
            }
        });
    }

}
