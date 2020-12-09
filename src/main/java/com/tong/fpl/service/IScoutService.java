package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.scout.ScoutData;

/**
 * Create by tong on 2020/12/9
 */
public interface IScoutService {

    void upsertEventScout(ScoutData scoutData) throws Exception;

    void updateEventScoutResult(int event);

}
