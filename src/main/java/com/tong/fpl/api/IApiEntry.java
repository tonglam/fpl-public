package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.*;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiEntry {

    /**
     * 模糊查询team_id
     */
    List<EntryInfoData> fuzzyQueryEntry(EntryQueryParam param);

    /**
     * 获取entry信息
     */
    EntryInfoData qryEntryInfo(int entry);

    /**
     * 获取entry的联赛信息
     */
    EntryLeagueInfoData qryEntryLeagueInfo(int entry);

    /**
     * 获取entry的历史信息
     */
    EntryHistoryInfoData qryEntryHistoryInfo(int entry);

    /**
     * 根据event获取entry的成绩
     */
    EntryEventResultData qryEntryEventResult(int event, int entry);
}
