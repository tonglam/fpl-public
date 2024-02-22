package com.tong.fpl.config.event;

import com.google.common.collect.Lists;
import com.tong.fpl.domain.event.CreateTournamentEventData;
import com.tong.fpl.domain.event.RefreshLeagueSummaryEventData;
import com.tong.fpl.domain.event.RefreshTournamentEventResultEventData;
import com.tong.fpl.service.IRefreshService;
import com.tong.fpl.service.ITournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Create by tong on 2020/6/24
 */
@Slf4j
@EnableAsync
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FplEventListener {

    private final ITournamentService tournamentService;
    private final IRefreshService refreshService;

    @Async("eventExecutor")
    @EventListener({CreateTournamentEventData.class})
    public void onApplicationEvent(CreateTournamentEventData data) {
        log.info("receive event:{}, start process", CreateTournamentEventData.class.getSimpleName());
        this.tournamentService.createNewTournamentBackground(data.getTournamentId(), Lists.newArrayList());
    }

    @Async("eventExecutor")
    @EventListener({RefreshTournamentEventResultEventData.class})
    public void onApplicationEvent(RefreshTournamentEventResultEventData data) {
        log.info("receive event:{}, start process", RefreshTournamentEventResultEventData.class.getSimpleName());
        this.refreshService.refreshTournamentEventResult(data.getEvent(), data.getTournamentId());
    }

    @Async("eventExecutor")
    @EventListener({RefreshLeagueSummaryEventData.class})
    public void onApplicationEvent(RefreshLeagueSummaryEventData data) {
        log.info("receive event:{}, start process", RefreshLeagueSummaryEventData.class.getSimpleName());
        this.refreshService.refreshLeagueSummary(data.getEvent(), data.getLeagueName(), data.getEntry());
    }

}
