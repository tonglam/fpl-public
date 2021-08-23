package com.tong.fpl.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * event for refresh league summary
 * Create by tong on 2021/8/23
 */
@Getter
@Setter
public class RefreshLeagueSummaryEventData extends ApplicationEvent {

    private int event;
    private String leagueName;
    private int entry;

    public RefreshLeagueSummaryEventData(Object source, int event, String leagueName, int entry) {
        super(source);
        this.event = event;
        this.leagueName = leagueName;
        this.entry = entry;
    }

}
