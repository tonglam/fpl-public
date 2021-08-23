package com.tong.fpl.config.event;

import com.tong.fpl.domain.event.CreateTournamentEventData;
import com.tong.fpl.domain.event.CreateZjTournamentEventData;
import com.tong.fpl.service.IEventDataService;
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
    private final IEventDataService eventDataService;

    @Async("eventExecutor")
    @EventListener({CreateTournamentEventData.class})
    public void onApplicationEvent(CreateTournamentEventData createTournamentEvent) {
        log.info("recieve event:{}, start process", CreateTournamentEventData.class.getSimpleName());
        this.tournamentService.createNewTournamentBackground(createTournamentEvent.getTournamentName(), createTournamentEvent.getInputEntryList());
    }

    @Async("eventExecutor")
    @EventListener({CreateZjTournamentEventData.class})
    public void onApplicationEvent(CreateZjTournamentEventData createZjTournamentEvent) {
        log.info("recieve event:{}, start process", CreateZjTournamentEventData.class.getSimpleName());
        this.tournamentService.createNewZjTournamentBackground(createZjTournamentEvent.getZjTournamentCreateData());
    }

}
