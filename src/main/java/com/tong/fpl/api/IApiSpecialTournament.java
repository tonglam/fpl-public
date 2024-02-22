package com.tong.fpl.api;

import com.tong.fpl.domain.special.GroupRankResultData;
import com.tong.fpl.domain.special.GroupResultData;
import com.tong.fpl.domain.special.ShuffledGroupResultData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2022/2/26
 */
public interface IApiSpecialTournament {

    List<Integer> getTournamentEntryList(int tournamentId);

    void insertGroupEventResult(int tournamentId, int event);

    Map<Integer, List<ShuffledGroupResultData>> getShuffledGroupResult(int tournamentId, int event);

    void insertShuffledGroupEventResult(int tournamentId, int event);

    List<GroupResultData> getEventGroupResult(int tournamentId, int event);

    List<GroupRankResultData> getGroupRankResult(int tournamentId, int event);

    void refreshShuffledGroupResult(int tournamentId, int event);

    void refreshEventGroupResult(int tournamentId, int event);

}
