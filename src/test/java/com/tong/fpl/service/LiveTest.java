package com.tong.fpl.service;

import com.google.common.collect.Maps;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/7/15
 */
public class LiveTest extends FplApplicationTests {

    @Autowired
    private ILiveService liveCalcService;

    @ParameterizedTest
    @CsvSource({"1, 6496"})
    void calcLivePoints(int event, int entry) {
        long start = System.currentTimeMillis();
        LiveCalcData liveCalaData = this.liveCalcService.calcLivePointsByEntry(event, entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println("points: " + liveCalaData.getLivePoints());
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void calcLivePointsByElementList(int event) {
        Map<Integer, Integer> map = Maps.newHashMap();
        map.put(1, 363);
        map.put(2, 259);
        map.put(3, 457);
        map.put(4, 123);
        map.put(5, 254);
        map.put(6, 4);
        map.put(7, 445);
        map.put(8, 394);
        map.put(9, 117);
        map.put(10, 377);
        map.put(11, 506);
        LiveCalcData liveCalaData = this.liveCalcService.calcLivePointsByElementList(event, map, "n/a", 254, 4);
        System.out.println("points: " + liveCalaData.getLivePoints());
        liveCalaData.getPickList().forEach(o -> System.out.println(o.getWebName() + "-" + o.getTotalPoints()));
    }

    @ParameterizedTest
    @CsvSource({"18, 1"})
    void calcLivePointsByTournament(int event, int tournamentId) {
        long start = System.currentTimeMillis();
        List<LiveCalcData> list = this.liveCalcService.calcLivePointsByTournament(event, tournamentId);
        long end = System.currentTimeMillis();
        System.out.println("escape: " + ((end - start) / 1000) + "s!");
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, 1, 275"})
    void calcSearchLivePointsByTournament(int event, int tournamentId, int element) {
        List<LiveCalcData> list = this.liveCalcService.calcSearchLivePointsByTournament(event, tournamentId, element);
        System.out.println(1);
    }

}
