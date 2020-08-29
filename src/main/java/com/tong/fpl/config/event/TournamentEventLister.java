package com.tong.fpl.config.event;

import com.tong.fpl.domain.event.CreateTournamentEventData;
import com.tong.fpl.service.impl.TournamentManagementServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2020/6/24
 */
@Slf4j
@EnableAsync
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentEventLister {

    private final TournamentManagementServiceImpl tournamentManagement;

    @Async("eventExecutor")
    @EventListener
    public void onApplicationEvent(CreateTournamentEventData createTournamentEvent) {
        this.tournamentManagement.createNewTournamentBackground(createTournamentEvent.getTournamentName());
    }

}
