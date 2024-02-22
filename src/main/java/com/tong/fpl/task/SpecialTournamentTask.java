package com.tong.fpl.task;

import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.ISpecialTournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2022/2/26
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecialTournamentTask {

    private final IQueryService queryService;
    private final ISpecialTournamentService specialTournamentService;

    @Scheduled(cron = "0 */5 * * * *")
    public void updateLiveCache() {
        log.info("start updateLiveCache task");
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDayTime(event)) {
            return;
        }
        log.info("true start updateLiveCache task");
        this.specialTournamentService.insertEntryEventResult(2, event);
        this.specialTournamentService.insertGroupEventResult(2, event);
        this.specialTournamentService.insertShuffledGroupEventResult(2, event);
        log.info("end updateLiveCache task");
    }

}
