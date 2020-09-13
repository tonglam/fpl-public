package com.tong.fpl.task;

import com.tong.fpl.service.IRedisCacheSerive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Create by tong on 2020/7/21
 */
@Slf4j
//@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MatchDayTask {

    private final IRedisCacheSerive redisCacheSerive;

    @Scheduled(cron = "0 5 19-3 * * *")
    public void insertEventLiveCache() {
        int event = this.redisCacheSerive.getCurrentEvent();
        this.redisCacheSerive.insertEventLiveCache(event);
    }

}
