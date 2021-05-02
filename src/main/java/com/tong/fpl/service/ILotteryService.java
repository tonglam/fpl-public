package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.lottery.LotteryData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/2
 */
public interface ILotteryService {

    String addLottery(LotteryData data);

    String updateLottery(LotteryData data);

    String deleteLottery(LotteryData data);

    String updatePrize(int lotteryId, Map<String, Integer> prizeMap);

    String addEntry(int lotteryId, int entry);

    String deleteEntry(int lotteryId, int entry);

    List<String> getLotteryEntryList(int lotteryId);

    void draw(int lotteryId) throws Exception;

    Map<String, Integer> getDrawResult(int lotteryId);

}
