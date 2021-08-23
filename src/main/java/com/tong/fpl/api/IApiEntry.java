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
     * 获取简介
     */
    EntryInfoData qryEntryInfo(int entry);

    /**
     * 获取联赛信息
     */
    EntryLeagueInfoData qryEntryLeagueInfo(int entry);

    /**
     * 获取历史信息
     */
    EntryHistoryInfoData qryEntryHistoryInfo(int entry);

    /**
     * 获取周得分
     */
    EntryEventResultData qryEntryEventResult(int event, int entry);

    /**
     * 刷新周得分
     */
    void refreshEntryEventResult(int event, int entry);

    /**
     * 获取周转会
     */
    List<EntryEventTransfersData> qryEntryEventTransfers(int event, int entry);

    /**
     * 刷新周转会
     */
    void refreshEntryEventTransfers(int event, int entry);

}
