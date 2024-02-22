package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.groupTournament.GroupTournamentResultData;

import java.util.List;

/**
 * Created by tong on 2023/1/4
 */
public interface IGroupTournamentService {

    void insertGroupEntry(List<GroupTournamentResultData> groupInfoList);

    void insertEntryEventResult(int groupTournamentId, int event);

    List<GroupTournamentResultData> getEventGroupTournamentResult(int groupTournamentId, int event);

    void refreshGroupTournamentResult(int groupTournamentId, int event);

}
