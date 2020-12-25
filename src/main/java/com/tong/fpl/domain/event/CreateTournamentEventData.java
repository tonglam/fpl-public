package com.tong.fpl.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * event for create a new tournament
 * Create by tong on 2020/6/24
 */
@Getter
@Setter
public class CreateTournamentEventData extends ApplicationEvent {

	private static final long serialVersionUID = -9185212999642141619L;

	private String tournamentName;
	private List<Integer> inputEntryList;

	public CreateTournamentEventData(Object source, String tournamentName, List<Integer> inputEntryList) {
		super(source);
		this.tournamentName = tournamentName;
		this.inputEntryList = inputEntryList;
	}

}
