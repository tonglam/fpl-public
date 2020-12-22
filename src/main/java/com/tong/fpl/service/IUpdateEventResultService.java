package com.tong.fpl.service;

/**
 * Create by tong on 2020/6/29
 */
public interface IUpdateEventResultService {

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

}
