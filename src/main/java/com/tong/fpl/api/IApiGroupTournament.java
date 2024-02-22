package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.groupTournament.GroupTournamentResultData;

import java.util.List;

/**
 * Create by tong on 5/11/2023
 */
public interface IApiGroupTournament {

    void insertEntryEventResult(int groupTournamentId, int event);

    List<GroupTournamentResultData> getEventGroupTournamentResult(int groupTournamentId, int event);

    void refreshGroupTournamentResult(int groupTournamentId, int event);

}
