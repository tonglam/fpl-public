package com.tong.fpl.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * event for create a new tournament
 * Create by tong on 2020/6/24
 */
@Getter
@Setter
public class CreateTournamentEventData extends ApplicationEvent {

    private static final long serialVersionUID = -9185212999642141619L;

    private int tournamentId;

    public CreateTournamentEventData(Object source, int tournamentId) {
        super(source);
        this.tournamentId = tournamentId;
    }

}
