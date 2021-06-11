package com.tong.fpl.task;

import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.IEventDataService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2020/7/21
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DailyTask {

    private final IRedisCacheService redisCacheService;
    private final IQueryService queryService;
    private final IEventDataService eventDataService;

    @Scheduled(cron = "0 35 6 * * *")
    public void insertEvent() {
        this.redisCacheService.insertEvent();
    }

    @Scheduled(cron = "0 40 6 * * *")
    public void insertEventFixture() {
        this.redisCacheService.insertEventFixture();
    }

    @Scheduled(cron = "0 45 6 * * *")
    public void insertSingleEventPassedDeadlineCache() {
        int nextEvent = this.queryService.getNextEvent();
        this.redisCacheService.insertSingleEventPassedDeadlineCache(nextEvent);
    }

    @Scheduled(cron = "0 25-35 9 * * *")
    public void refreshPlayerValue() {
        try {
            this.redisCacheService.insertPlayer();
            this.redisCacheService.insertPlayerStat();
            this.redisCacheService.insertPlayerValue();
        } catch (Exception e) {
            e.printStackTrace();
            TaskLog.error(e.getMessage());
            this.refreshPlayerValue();
        }
    }

    @Scheduled(cron = "0 50 9 * * *")
    public void updateEntryInfo() {
        try {
            this.eventDataService.updateEntryInfo();
        } catch (Exception e) {
            e.printStackTrace();
            TaskLog.error(e.getMessage());
        }
    }

}


