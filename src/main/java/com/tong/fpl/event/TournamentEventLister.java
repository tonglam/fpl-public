package com.tong.fpl.event;

import com.tong.fpl.service.impl.TournamentManagementImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2020/6/24
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentEventLister implements ApplicationListener<CreateTournamentEvent> {

	private final TournamentManagementImpl tournamentManagement;

	@Async
	@Override
	public void onApplicationEvent(CreateTournamentEvent createTournamentEvent) {
		String tournamentName = createTournamentEvent.getTournamentName();
		// save entry_info
		this.tournamentManagement.saveTournamentEntryInfo(tournamentName);
		// draw groups
		this.tournamentManagement.drawGroups(tournamentName);
		// draw knockouts
		try {
			this.tournamentManagement.drawKnockouts(tournamentName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
