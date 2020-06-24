package com.tong.fpl.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * event for create a new tournament
 * Create by tong on 2020/6/24
 */
@Getter
@Setter
public class CreateTournamentEvent extends ApplicationEvent {

	private static final long serialVersionUID = -9185212999642141619L;
	private String cupName;
	private boolean drawKnockouts;

	public CreateTournamentEvent(Object source, String cupName, boolean drawKnockouts) {
		super(source);
		this.cupName = cupName;
		this.drawKnockouts = drawKnockouts;
	}

}
