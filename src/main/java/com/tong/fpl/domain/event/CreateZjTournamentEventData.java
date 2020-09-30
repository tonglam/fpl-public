package com.tong.fpl.domain.event;

import com.tong.fpl.domain.letletme.tournament.ZjTournamentCreateData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * event for create a new zj tournament
 * Create by tong on 2020/9/29
 */
@Getter
@Setter
public class CreateZjTournamentEventData extends ApplicationEvent {

    private ZjTournamentCreateData zjTournamentCreateData;

    public CreateZjTournamentEventData(Object source, ZjTournamentCreateData zjTournamentCreateData) {
        super(source);
        this.zjTournamentCreateData = zjTournamentCreateData;
    }

}
