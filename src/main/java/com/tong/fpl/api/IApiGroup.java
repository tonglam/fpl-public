package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiGroup {

    /**
     * 获取球探名单
     */
    Map<String, String> qryScoutEntry();

    /**
     * 根据球探获取比赛周推荐结果
     */
    EventScoutData qryEventEntryScoutResult(int event, int entry);

    /**
     * 获取比赛周所有推荐结果
     */
    List<EventScoutData> qryEventScoutResult(int event);

    /**
     * 刷新当前比赛周当前球探推荐结果缓存
     */
    void refreshCurrentEventScoutResult(int entry);

    /**
     * 提交球探推荐
     */
    ResponseEntity<Map<String, Object>> upsertEventScout(ScoutData scoutData);

    /**
     * 更新指定比赛周的球探推荐结果
     */
    void updateEventScoutResult(int event);

}
