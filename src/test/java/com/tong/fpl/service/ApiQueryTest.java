package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.utils.JsonUtils;
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
    @Autowired
    private EntryEventResultService entryEventResultService;

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
        List<TournamentInfoData> list = this.apiQueryService.qryEntryPointsRaceTournament(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"35, 集合吧！FPL2021让让群小联赛"})
    void qryTeamSelectByLeagueName(int event, String leagueName) {
        LeagueStatData data = this.apiQueryService.qryTeamSelectByLeagueName(event, leagueName);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"36, 1870"})
    void qryPlayerEventResultByPicks(int event, int entry) {
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity == null) {
            return;
        }
        List<EntryPickData> pickList = JsonUtils.json2Collection(entryEventResultEntity.getEventPicks(), List.class, EntryPickData.class);
        List<ElementEventResultData> list = this.apiQueryService.qryEntryEventPicksResult(event, pickList);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"37, 1870"})
    void qryEntryEventResult(int event, int entry) {
        EntryEventResultData data = this.apiQueryService.qryEntryEventResult(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"37, 1870"})
    void qryEntryEventTransfers(int event, int entry) {
        List<EntryEventTransfersData> list = this.apiQueryService.qryEntryEventTransfers(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"36, 1"})
    void qryTournamentEventResult(int event, int tournamentId) {
        List<EntryEventResultData> list = this.apiQueryService.qryTournamentEventResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"36, 1, 203"})
    void qryTournamentEntryContainElement(int event, int tournamentId, int element) {
        List<Integer> list = this.apiQueryService.qryTournamentEntryContainElement(event, tournamentId, element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"36, 1, 203"})
    void qryTournamentEntryPlayElement(int event, int tournamentId, int element) {
        List<Integer> list = this.apiQueryService.qryTournamentEntryPlayElement(event, tournamentId, element);
        System.out.println(1);
    }

}
