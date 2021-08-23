package com.tong.fpl.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * event for refresh player value
 * Create by tong on 2021/8/23
 */
@Getter
@Setter
public class RefreshPlayerValueEventData extends ApplicationEvent {


    private static final long serialVersionUID = -3952167652126351373L;

    public RefreshPlayerValueEventData(Object source) {
        super(source);
    }

}
