package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.entry.EntryEventSimulatePickData;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulateTransfersData;
import com.tong.fpl.domain.letletme.scout.ScoutData;

/**
 * Create by tong on 2020/12/9
 */
public interface IGroupService {

    /**
     * 更新每轮球探推荐名单
     */
    String upsertEventScout(ScoutData scoutData);

    /**
     * 更新每轮球探推荐得分结果
     */
    void updateEventScoutResult(int event);

    /**
     * 更新每轮球探模拟阵容
     */
    void upsertEventScoutSimulatePick(EntryEventSimulatePickData entryEventSimulatePickData);

    /**
     * 更新每轮球探模拟转会
     */
    void upsertEventScoutSimulateTransfers(EntryEventSimulateTransfersData entryEventSimulateTransfersData);

    /**
     * 刷新当前比赛周当前球探推荐结果缓存
     */
    void refreshCurrentEventScoutResult(int entry);

}
