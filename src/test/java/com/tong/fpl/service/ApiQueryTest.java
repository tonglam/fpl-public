package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public class ApiQueryTest extends FplApplicationTests {

    @Autowired
    private IApiQueryService apiQueryService;

    @ParameterizedTest
    @CsvSource({"1"})
    void qryPlayerInfoListByElementType(int elementType) {
        LinkedHashMap<String, List<PlayerInfoData>> map = this.apiQueryService.qryPlayerInfoByElementType(elementType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"finished"})
    void qryLiveMatchDataByStatus(String playStatus) {
        List<LiveMatchData> list = this.apiQueryService.qryLiveMatchByStatus(playStatus);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"20210514"})
    void qryPlayerValueByChangeDate(String changeDate) {
        Map<String, List<PlayerValueData>> map = this.apiQueryService.qryPlayerValueByDate(changeDate);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryLeagueInfo(int entry) {
        EntryLeagueInfoData data = this.apiQueryService.qryEntryLeagueInfo(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryHistoryInfo(int entry) {
        EntryHistoryInfoData data = this.apiQueryService.qryEntryHistoryInfo(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"35, 1870"})
    void qryEntryEventResult(int event, int entry) {
        EntryEventResultData data = this.apiQueryService.qryEntryEventResult(event, entry);
        System.out.println(1);
    }

    @Test
    void fuzzyQueryEntry() {
        EntryQueryParam param = new EntryQueryParam()
                .setEntryName("")
                .setPlayerName("让让群");
        List<EntryInfoData> list = this.apiQueryService.fuzzyQueryEntry(param);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryTournament(int entry) {
        List<TournamentInfoData> list = this.apiQueryService.qryEntryTournament(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"35, 集合吧！FPL2021让让群小联赛"})
    void qryTeamSelectByLeagueName(int event, String leagueName) {
        LeagueStatData data = this.apiQueryService.qryTeamSelectByLeagueName(event, leagueName);
        System.out.println(1);
    }

}
