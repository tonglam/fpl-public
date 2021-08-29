package com.tong.fpl.task;

import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.EventLiveRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.IEventDataService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.IStaticService;
import com.tong.fpl.service.db.EntryInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/7/21
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DailyTask {

    private final IQueryService queryService;
    private final IStaticService staticService;
    private final IRedisCacheService redisCacheService;
    private final IEventDataService eventDataService;
    private final EntryInfoService entryInfoService;

    @Scheduled(cron = "0 35 6 * * *")
    public void insertEvent() {
        StaticRes staticRes = this.staticService.getBootstrapStatic();
        if (staticRes == null) {
            return;
        }
        this.redisCacheService.insertEvent(staticRes);
    }

    @Scheduled(cron = "0 40 6 * * *")
    public void insertEventFixture() {
        this.redisCacheService.insertEventFixture();
        StaticRes staticRes = this.staticService.getBootstrapStatic();
        if (staticRes == null) {
            return;
        }
        this.redisCacheService.insertEventAfterDeadlineCache(this.queryService.getCurrentEvent(), staticRes);
    }

    @Scheduled(cron = "0 25-35 9 * * *")
    public void refreshPlayerValue() {
        try {
            StaticRes staticRes = this.staticService.getBootstrapStatic();
            if (staticRes == null) {
                return;
            }
            this.redisCacheService.insertPlayer(staticRes);
            this.redisCacheService.insertPlayerStat(staticRes);
            this.redisCacheService.insertPlayerValue(staticRes);
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
            this.eventDataService.updateEntryInfoByList(entryList);
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
        EventLiveRes eventLiveRes = this.staticService.getEventLive(event);
        if (eventLiveRes == null) {
            return;
        }
        this.redisCacheService.insertEventLive(event, eventLiveRes);
        List<EventFixturesRes> eventFixturesResList = this.staticService.getEventFixture(event);
        if (CollectionUtils.isEmpty(eventFixturesResList)) {
            return;
        }
        this.redisCacheService.insertSingleEventFixture(event, eventFixturesResList);
    }

}


