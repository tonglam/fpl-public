package com.tong.fpl.event;

import com.tong.fpl.service.impl.TournamentManagementImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2020/6/24
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentEventLister implements ApplicationListener<CreateTournamentEvent> {

	private final TournamentManagementImpl tournamentManagement;

	@Override
	public void onApplicationEvent(CreateTournamentEvent createTournamentEvent) {
		String cupName = createTournamentEvent.getCupName();
		// save entry_info
		this.tournamentManagement.saveTournamentEntryInfo(cupName);
		// draw groups
		this.tournamentManagement.drawGroups(cupName);
		// draw knockouts
		if (createTournamentEvent.isDrawKnockouts()) {
			try {
				this.tournamentManagement.drawKnockouts(cupName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
