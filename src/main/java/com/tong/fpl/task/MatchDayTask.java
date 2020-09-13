package com.tong.fpl.task;

import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.db.TournamentInfoService;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Create by tong on 2020/7/21
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MatchDayTask {

    private final IRedisCacheSerive redisCacheSerive;
    private final ITableQueryService tableQueryService;
    private final TournamentInfoService tournamentInfoService;

    @Scheduled(cron = "0 0/5 20-23 * * *")
    public void insertEventLiveCache() {
        log.info("start insertEventLiveCache task, time:{}", LocalDateTime.now());
        int event = this.redisCacheSerive.getCurrentEvent();
        this.redisCacheSerive.insertEventLiveCache(event);
        // league live
//        int tournamentIds = this.tournamentInfoService.count();
        log.info("start insertEventLiveCache tournament, id:{}, time:{}", 2, LocalDateTime.now());
        String key = "qryTournamentLivePoints::" + 2;
        RedisUtils.removeCacheByKey(key);
        this.tableQueryService.qryTournamentLivePoints(2);
    }

    @Scheduled(cron = "0 0/10 20-23 * * *")
    public void updateTournamentCache() {
        // league live
//        int tournamentIds = this.tournamentInfoService.count();
        log.info("start insertEventLiveCache tournament, id:{}, time:{}", 1, LocalDateTime.now());
        String key = "qryTournamentLivePoints::" + 1;
        RedisUtils.removeCacheByKey(key);
        this.tableQueryService.qryTournamentLivePoints(1);
    }

}
