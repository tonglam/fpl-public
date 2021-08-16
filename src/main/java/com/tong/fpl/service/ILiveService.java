package com.tong.fpl.service;

import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.SearchLiveCalcData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/7/13
 */
public interface ILiveService {

    /**
     * calculate entry live points
     */
    LiveCalcData calcLivePointsByEntry(int event, int entry);

    /**
     * calculate entry live points(do not has entry)
     */
    LiveCalcData calcLivePointsByElementList(int event, Map<Integer, Integer> elementMap, String chip, int captain, int viceCaptain);

    /**
     * calculate entry live points in the tournament
     */
    List<LiveCalcData> calcLivePointsByTournament(int event, int tournamentId);

    /**
     * calculate entry live points in the tournament
     */
    SearchLiveCalcData calcSearchLivePointsByTournament(int event, int tournamentId, int element);

    /**
     * calculate element live points
     */
    int calcElementLivePoints(int event, int element);

    /**
     * calculate element live points
     */
    int calcElementLivePoints(EventLiveEntity eventLiveEntity);

}
