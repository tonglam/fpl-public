package com.tong.fpl.task;

import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.IEventDataService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.EntryInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/7/21
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DailyTask {

    private final IQueryService queryService;
    private final IRedisCacheService redisCacheService;
    private final IEventDataService eventDataService;
    private final EntryInfoService entryInfoService;

    @Scheduled(cron = "0 35 6 * * *")
    public void insertEvent() {
        this.redisCacheService.insertEvent();
    }

    @Scheduled(cron = "0 40 6 * * *")
    public void insertEventFixture() {
        this.redisCacheService.insertEventFixture();
        this.redisCacheService.insertEventAfterDeadlineCache(this.redisCacheService.getCurrentEvent());
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
            List<Integer> entryList = this.entryInfoService.list()
                    .stream()
                    .map(EntryInfoEntity::getEntry)
                    .distinct()
                    .collect(Collectors.toList());
            this.eventDataService.updateEntryInfo(entryList);
        } catch (Exception e) {
            e.printStackTrace();
            TaskLog.error(e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 */1 * * *")
    public void insertEventLive() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDayTime(event)) {
            return;
        }
        TaskLog.info("start true insertEventLive task");
        this.redisCacheService.insertEventLive(event);
        this.redisCacheService.insertSingleEventFixture(event);
    }

}


