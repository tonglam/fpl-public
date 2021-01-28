package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.entry.EntryEventSimulatePickData;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulateTransfersData;

/**
 * Create by tong on 2020/6/29
 */
public interface IUpdateEventService {

	void updateEntryInfo();

	void upsertEntryEventResult(int event, int entry);

	void insertEntryEventTransfer(int entry);

	void updateEntryEventTransfersPlayed(int event, int entry);

	void upsertTournamentEntryEventResult(int event, int tournamentId);

	void insertTournamentEntryEventTransfers(int tournamentId);

	void updateTournamentEventTransfersPlayed(int event, int tournamentId);

	void updatePointsRaceGroupResult(int event, int tournamentId);

	void updateBattleRaceGroupResult(int event, int tournamentId);

	void updateKnockoutResult(int event, int tournamentId);

	void updateZjPhaseOneResult(int event, int tournamentId);

	void updateZjPhaseTwoResult(int event, int tournamentId);

	void updateZjPkResult(int event, int tournamentId);

	void updateZjTournamentResult(int tournamentId);

	void updateAllEventResult(int event);

	void upsertEventPick(EntryEventSimulatePickData entryEventSimulatePickData);

	void upsertEventTransfers(EntryEventSimulateTransfersData entryEventSimulateTransfersData);

}
