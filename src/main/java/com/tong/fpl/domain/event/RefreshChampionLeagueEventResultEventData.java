package com.tong.fpl.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * event for refresh champion league event result
 * <p>
 * Create by tong on 2021/8/23
 */
@Getter
@Setter
public class RefreshChampionLeagueEventResultEventData extends ApplicationEvent {

    private static final long serialVersionUID = -7707898730370128412L;

    private int event;
    private int tournamentId;

    public RefreshChampionLeagueEventResultEventData(Object source, int event, int tournamentId) {
        super(source);
        this.event = event;
        this.tournamentId = tournamentId;
    }

}
