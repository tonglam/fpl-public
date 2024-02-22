package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.letletme.element.*;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.scout.PopularScoutData;
import com.tong.fpl.domain.letletme.team.TeamAgainstInfoData;
import com.tong.fpl.domain.letletme.team.TeamData;
import com.tong.fpl.domain.letletme.team.TeamSummaryData;
import com.tong.fpl.domain.letletme.tournament.*;
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
        long start = System.currentTimeMillis();
        Map<String, String> map = this.apiQueryService.qryCurrentEventAndNextUtcDeadline();
        long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");
    }

    @Test
    void qryEventAverageScore() {
        Map<String, Integer> map = this.apiQueryService.qryEventAverageScore();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void qryTeamList(String season) {
        List<TeamData> list = this.apiQueryService.qryTeamList(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2324"})
    void qryAllLeagueName(String season) {
        List<LeagueInfoData> list = this.apiQueryService.qryAllLeagueName(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryNextFixture(int event) {
        List<PlayerFixtureData> list = this.apiQueryService.qryNextFixture(event);
        System.out.println(1);
    }

    /**
     * @apiNote entry
     */
    @Test
    void fuzzyQueryEntry() {
        EntryQueryParam param = new EntryQueryParam()
                .setEntryName("杀猪会")
                .setPlayerName("");
        List<EntryInfoData> list = this.apiQueryService.fuzzyQueryEntry(param);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void qryEntryInfo(int entry) {
        EntryInfoData data = this.apiQueryService.qryEntryInfo(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"18377"})
    void qryEntryLeagueInfo(int entry) {
        EntryLeagueData data = this.apiQueryService.qryEntryLeagueInfo(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"7802"})
    void qryEntryHistoryInfo(int entry) {
        EntryHistoryData data = this.apiQueryService.qryEntryHistoryInfo(entry);
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
    @CsvSource({"2, 6496"})
    void qryEntryEventTransfers(int event, int entry) {
        List<EntryEventTransfersData> list = this.apiQueryService.qryEntryEventTransfers(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void qryEntryAllTransfers(int entry) {
        List<EntryEventTransfersData> list = this.apiQueryService.qryEntryAllTransfers(entry);
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
    @CsvSource({"3, 2418908"})
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

    @ParameterizedTest
    @CsvSource({"7, 301764"})
    void qryEventScoutLeftTransfers(int event, int entry) {
        int leftTransfers = this.apiQueryService.qryEventScoutLeftTransfers(event, entry);
        System.out.println(1);
    }

    /**
     * @apiNote live
     */
    @ParameterizedTest
    @CsvSource({"not_start"})
    void qryLiveMatchDataByStatus(String playStatus) {
        List<LiveMatchData> list = this.apiQueryService.qryLiveMatchByStatus(playStatus);
        System.out.println(1);
    }

    /**
     * @apiNote player
     */
    @ParameterizedTest
    @CsvSource({"2324, 352"})
    void qryPlayerInfoByElement(String season, int element) {
        PlayerInfoData data = this.apiQueryService.qryPlayerInfoByElement(season, element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2223, 225321"})
    void qryPlayerInfoByCode(String season, int code) {
        PlayerInfoData data = this.apiQueryService.qryPlayerInfoByCode(season, code);
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
        MybatisPlusConfig.season.set("2021");
        PlayerDetailData data = this.apiQueryService.qryPlayerDetailByElement(element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"ARS"})
    void qryTeamFixtureByShortName(String shortName) {
        Map<String, List<PlayerFixtureData>> map = this.apiQueryService.qryTeamFixtureByShortName(shortName);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void qryFilterPlayers(String season) {
        List<PlayerFilterData> list = this.apiQueryService.qryFilterPlayers(season);
        System.out.println(1);
    }

    /**
     * @apiNote stat
     */
    @ParameterizedTest
    @CsvSource({"20230811"})
    void qryPlayerValueByChangeDate(String changeDate) {
        Map<String, List<PlayerValueData>> map = this.apiQueryService.qryPlayerValueByDate(changeDate);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"20240220"})
    void qryPlayerPriceChange(String date) {
        List<PlayerValueData> list = this.apiQueryService.qryPlayerPriceChange(date);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"302"})
    void qryPlayerValueByElement(int element) {
        MybatisPlusConfig.season.set("2021");
        List<PlayerValueData> list = this.apiQueryService.qryPlayerValueByElement(element);
        System.out.println(1);
    }

    @Test
    void qrySeasonFixture() {
        List<List<String>> list = this.apiQueryService.qrySeasonFixture();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 37, 3571, Classic"})
    void qryLeagueEventEoWebNameMap(String season, int event, int leagueId, String leagueType) {
        MybatisPlusConfig.season.set("2021");
        Map<String, String> map = this.apiQueryService.qryLeagueEventEoWebNameMap(season, event, leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2324, 1, 4, TLB让让我逃生赛(每周最低分淘汰)"})
    void qryLeagueSelectByName(String season, int event, int leagueId, String leagueName) {
        LeagueEventSelectData data = this.apiQueryService.qryLeagueSelectByName(season, event, leagueId, leagueName);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1617, 61366"})
    void qryPlayerSummary(String season, int code) {
        PlayerSummaryData data = this.apiQueryService.qryPlayerSummary(season, code);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"61366"})
    void qryPlayerSeasonSummary(int code) {
        List<PlayerSeasonSummaryData> list = this.apiQueryService.qryPlayerSeasonSummary(code);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2223, Arsenal"})
    void qryTeamSummary(String season, String shorName) {
        TeamSummaryData data = this.apiQueryService.qryTeamSummary(season, shorName);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"7, 353"})
    void qryElementEventExplainResult(int event, int element) {
        ElementEventLiveExplainData data = this.apiQueryService.qryElementEventExplainResult(event, element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"18, 11"})
    void qryTeamAgainstRecordInfo(int teamId, int againstId) {
        TeamAgainstInfoData data = this.apiQueryService.qryTeamAgainstRecordInfo(teamId, againstId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1617, 19, 1, 5"})
    void qryTeamAgainstRecordResult(String season, int event, int teamHId, int teamAId) {
        List<ElementSummaryData> list = this.apiQueryService.qryTeamAgainstRecordResult(season, event, teamHId, teamAId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"11, 3, true"})
    void qryTopElementAgainstInfo(int teamId, int againstId, boolean active) {
        List<ElementAgainstInfoData> list = this.apiQueryService.qryTopElementAgainstInfo(teamId, againstId, active);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, 19, 59966"})
    void qryElementAgainstRecord(int teamId, int againstId, int elementCode) {
        List<ElementAgainstRecordData> list = this.apiQueryService.qryElementAgainstRecord(teamId, againstId, elementCode);
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
    @CsvSource({"18377"})
    void qryEntryPointsRaceTournament(int entry) {
        List<TournamentInfoData> list = this.apiQueryService.qryEntryPointsRaceTournament(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2064147"})
    void qryEntryKnockoutTournament(int entry) {
        List<TournamentInfoData> list = this.apiQueryService.qryEntryKnockoutTournament(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"7802, 10"})
    void qryEntryChampionLeagueStage(int entry, int tournamentId) {
        String a = this.apiQueryService.qryEntryChampionLeagueStage(entry, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"17"})
    void qryChampionLeagueStage(int tournamentId) {
        LinkedHashMap<String, List<String>> list = this.apiQueryService.qryChampionLeagueStage(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"17"})
    void qryChampionLeagueGroupQualifications(int tournamentId) {
        List<List<TournamentGroupData>> list = this.apiQueryService.qryChampionLeagueGroupQualifications(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"17, 3"})
    void qryChampionLeagueStageKnockoutRound(int tournamentId, int round) {
        List<List<TournamentKnockoutData>> list = this.apiQueryService.qryChampionLeagueStageKnockoutRound(tournamentId, round);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryTournamentInfo(int id) {
        TournamentInfoData data = this.apiQueryService.qryTournamentInfo(id);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"5, 1"})
    void qryTournamentEventResult(int event, int tournamentId) {
        List<EntryEventResultData> list = this.apiQueryService.qryTournamentEventResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, 1, 30"})
    void qryTournamentEventSearchResult(int event, int tournamentId, int element) {
        SearchEntryEventResultData data = this.apiQueryService.qryTournamentEventSearchResult(event, tournamentId, element);
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

    @ParameterizedTest
    @CsvSource({"1"})
    void qryTournamentEventChampion(int tournamentId) {
        TournamentGroupEventChampionData data = this.apiQueryService.qryTournamentEventChampion(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryDrawKnockoutResults(int tournamentId) {
        List<EntryAgainstInfoData> list = this.apiQueryService.qryDrawKnockoutResults(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryDrawKnockoutEntries(int tournamentId) {
        List<Integer> list = this.apiQueryService.qryDrawKnockoutEntries(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, 991"})
    void qryDrawKnockoutOpponents(int tournamentId, int entry) {
        List<EntryInfoData> list = this.apiQueryService.qryDrawKnockoutOpponents(tournamentId, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryDrawKnockoutNotice(int tournamentId) {
        String notice = this.apiQueryService.qryDrawKnockoutNotice(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryDrawKnockoutPairs(int tournamentId) {
        List<List<EntryInfoData>> list = this.apiQueryService.qryDrawKnockoutPairs(tournamentId);
        System.out.println(1);
    }

    /**
     * @apiNote summary
     */
    @ParameterizedTest
    @CsvSource({"3"})
    void qryEventOverallResult(int event) {
        EventOverallResultData data = this.apiQueryService.qryEventOverallResult(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void qryEventDreamTeam(int event) {
        List<ElementEventData> list = this.apiQueryService.qryEventDreamTeam(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void qryEventEliteElements(int event) {
        List<ElementEventData> list = this.apiQueryService.qryEventEliteElements(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void qryEventOverallTransfers(int event) {
        Map<String, List<ElementEventData>> map = this.apiQueryService.qryEventOverallTransfers(event);
        System.out.println(1);
    }

    @Test
    void qryAllPopularScoutSource() {
        List<String> list = this.apiQueryService.qryAllPopularScoutSource();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("1, fantasy_premier_league")
    void qryEventSourceScoutResult(int event, String source) {
        PopularScoutData data = this.apiQueryService.qryEventSourceScoutResult(event, source);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("1")
    void qryOverallEventScoutResult(int event) {
        List<PopularScoutData> list = this.apiQueryService.qryOverallEventScoutResult(event);
        System.out.println(1);
    }

}
