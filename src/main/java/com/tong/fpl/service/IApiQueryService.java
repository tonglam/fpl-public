package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.EventScoutData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiQueryService {

    /**
     * @apiNote common
     */
    Map<String, String> qryCurrentEventAndNextUtcDeadline();

    /**
     * @apiNote entry
     */
    EntryInfoData qryEntryInfoData(int entry);

    /**
     * @apiNote league
     * */

    /**
     * @apiNote live
     */
    List<LiveMatchData> qryLiveMatchDataByStatus(String playStatus);

    /**
     * @apiNote team
     */
    Map<String, String> getTeamNameMap();

    default String getTeamNameByTeam(int teamId) {
        return this.getTeamNameMap().getOrDefault(String.valueOf(teamId), "");
    }

    Map<String, String> getTeamShortNameMap();

    default String getShortTeamNameByTeam(int teamId) {
        return this.getTeamShortNameMap().getOrDefault(String.valueOf(teamId), "");
    }

    /**
     * @apiNote player
     */
    PlayerInfoData qryPlayerInfoByElement(int element);

    LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

    PlayerDetailData qryPlayerDetailData(int element);

    Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName);

    Map<String, List<PlayerValueData>> qryPlayerValueByChangeDate(String changeDate);

    /**
     * @apiNote report
     * */

    /**
     * @apiNote scout
     */
    Map<String, String> qryScoutEntry();

    EventScoutData qryEventScoutPickResult(int event, int entry);

    List<EventScoutData> qryEventScoutResult(int event);

    /**
     * @apiNote tournament
     * */
}
