package com.tong.fpl.service;

import com.tong.fpl.domain.special.GroupInfoData;
import com.tong.fpl.domain.special.GroupRankResultData;
import com.tong.fpl.domain.special.GroupResultData;
import com.tong.fpl.domain.special.ShuffledGroupResultData;

import java.util.List;
import java.util.Map;

/**
 * Created by tong on 2022/02/25
 */
public interface ISpecialTournamentService {

    List<Integer> getTournamentEntryList(int tournamentId);

    void insertGroupInfo(int tournamentId, Map<String, String> groupInfoMap);

    void insertGroupEntry(int tournamentId, List<GroupInfoData> groupInfoList);

    void insertEntryEventResult(int tournamentId, int event);

    void insertShuffledGroupEventResult(int tournamentId, int event);

    void insertGroupEventResult(int tournamentId, int event);

    Map<Integer, List<ShuffledGroupResultData>> getShuffledGroupResult(int tournamentId, int event);

    List<GroupResultData> getEventGroupResult(int tournamentId, int event);

    List<GroupRankResultData> getGroupRankResult(int tournamentId, int event);

    void refreshShuffledGroupResult(int tournamentId, int event);

    void refreshEventGroupResult(int tournamentId, int event);

}
