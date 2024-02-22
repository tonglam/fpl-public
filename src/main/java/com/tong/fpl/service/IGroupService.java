package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.entry.EntryEventSimulatePickData;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulateTransfersData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Create by tong on 2020/12/9
 */
public interface IGroupService {

    /**
     * 更新每轮球探推荐名单
     */
    ResponseEntity<Map<String, Object>> upsertEventScout(ScoutData scoutData);

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

}
