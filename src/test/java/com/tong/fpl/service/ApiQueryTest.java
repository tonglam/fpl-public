package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentPointsGroupEventResultData;
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

    /**
     * @apiNote common
     */
    @Test
    void qryCurrentEventAndNextUtcDeadline() {
        Map<String, String> map = this.apiQueryService.qryCurrentEventAndNextUtcDeadline();
        System.out.println(1);
    }

    /**
     * @apiNote entry
     */
    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryInfo(int entry) {
        EntryInfoData data = this.apiQueryService.qryEntryInfo(entry);
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
    @CsvSource({"37, 1870"})
    void qryEntryEventResult(int event, int entry) {
        EntryEventResultData data = this.apiQueryService.qryEntryEventResult(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"36, 1870"})
    void qryEntryEventPicksResult(int event, int entry) {
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity == null) {
            return;
        }
        List<EntryPickData> pickList = JsonUtils.json2Collection(entryEventResultEntity.getEventPicks(), List.class, EntryPickData.class);
        List<ElementEventResultData> list = this.apiQueryService.qryEntryEventPicksResult(event, Chip.NONE.getValue(), pickList);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"37, 1870"})
    void qryEntryEventTransfers(int event, int entry) {
        List<EntryEventTransfersData> list = this.apiQueryService.qryEntryEventTransfers(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryEventSummary(int entry) {
        List<EntryEventResultData> list = this.apiQueryService.qryEntryEventSummary(entry);
        System.out.println(1);
    }

    /**
     * @apiNote live
     */
    @ParameterizedTest
    @CsvSource({"next_event"})
    void qryLiveMatchDataByStatus(String playStatus) {
        List<LiveMatchData> list = this.apiQueryService.qryLiveMatchByStatus(playStatus);
        System.out.println(1);
    }

    /**
     * @apiNote player
     */
    @ParameterizedTest
    @CsvSource({"37, 302"})
    void qryPlayerInfoByElement(int event, int element) {
        PlayerInfoData data = this.apiQueryService.qryPlayerInfoByElement(event, element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryPlayerInfoListByElementType(int elementType) {
        LinkedHashMap<String, List<PlayerInfoData>> map = this.apiQueryService.qryPlayerInfoByElementType(elementType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"302"})
    void qryPlayerDetailByElement(int element) {
        PlayerDetailData data = this.apiQueryService.qryPlayerDetailByElement(element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"ARS"})
    void qryTeamFixtureByShortName(String shortName) {
        Map<String, List<PlayerFixtureData>> map = this.apiQueryService.qryTeamFixtureByShortName(shortName);
        System.out.println(1);
    }

    /**
     * @apiNote stat
     */
    @ParameterizedTest
    @CsvSource({"20210514"})
    void qryPlayerValueByChangeDate(String changeDate) {
        Map<String, List<PlayerValueData>> map = this.apiQueryService.qryPlayerValueByDate(changeDate);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"302"})
    void qryPlayerValueByElement(int element) {
        List<PlayerValueData> list = this.apiQueryService.qryPlayerValueByElement(element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"13"})
    void qryPlayerValueByTeamId(int teamId) {
        Map<String, List<PlayerValueData>> map = this.apiQueryService.qryPlayerValueByTeamId(teamId);
        System.out.println(1);
    }

    @Test
    void qryAllLeagueName() {
        List<String> list = this.apiQueryService.qryAllLeagueName();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"37, 3571, Classic"})
    void qryLeagueEventEoWebNameMap(int event, int leagueId, String leagueType) {
        Map<String, String> map = this.apiQueryService.qryLeagueEventEoWebNameMap(event, leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"35, 集合吧！FPL2021让让群小联赛"})
    void qryTeamSelectByLeagueName(int event, String leagueName) {
        LeagueStatData data = this.apiQueryService.qryTeamSelectByLeagueName(event, leagueName);
        System.out.println(1);
    }

    /**
     * @apiNote scout
     */
    @Test
    void qryScoutEntry() {
        Map<String, String> map = this.apiQueryService.qryScoutEntry();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"35, 1870"})
    void qryEventScoutPickResult(int event, int entry) {
        EventScoutData data = this.apiQueryService.qryEventScoutPickResult(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"0"})
    void qryEventScoutResult(int event) {
        List<EventScoutData> list = this.apiQueryService.qryEventScoutResult(event);
        System.out.println(1);
    }

    /**
     * @apiNote tournament
     */
    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryTournamentEntry(int entry) {
        List<Integer> list = this.apiQueryService.qryEntryTournamentEntry(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryPointsRaceTournament(int entry) {
        List<TournamentInfoData> list = this.apiQueryService.qryEntryPointsRaceTournament(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryTournamentInfo(int id) {
        TournamentInfoData data = this.apiQueryService.qryTournamentInfo(id);
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

    @ParameterizedTest
    @CsvSource({"1, 1"})
    void qryTournamentEventSummary(int event, int tournamentId) {
        List<TournamentPointsGroupEventResultData> list = this.apiQueryService.qryTournamentEventSummary(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, 1870"})
    void qryTournamentEntryEventSummary(int tournamentId, int entry) {
        List<TournamentPointsGroupEventResultData> list = this.apiQueryService.qryTournamentEntryEventSummary(tournamentId, entry);
        System.out.println(1);
    }

}
