package com.tong.fpl.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * event for refresh tournament event result
 * <p>
 * Create by tong on 2021/8/23
 */
@Getter
@Setter
public class RefreshTournamentEventResultEventData extends ApplicationEvent {

    private static final long serialVersionUID = -3952167652126351373L;

    private int event;
    private int tournamentId;

    public RefreshTournamentEventResultEventData(Object source, int event, int tournamentId) {
        super(source);
        this.event = event;
        this.tournamentId = tournamentId;
    }

}
