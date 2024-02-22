package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.tournament.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

public class TableQueryTest extends FplApplicationTests {

    @Autowired
    private ITableQueryService tableQueryService;

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryLivePoints(int entry) {
        TableData<LiveCalcData> liveCalaDataTableData = this.tableQueryService.qryEntryLivePoints(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, Overall"})
    void qryTeamSelectStatByName(int event, String leagueName) {
        TableData<LeagueStatData> leagueStatData = this.tableQueryService.qryTeamSelectStatByName(event, leagueName);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntryResultList(int entry) {
        TableData<EntryEventResultData> data = this.tableQueryService.qryEntryResultList(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryPointsGroupChampion(int tournamentId) {
        TableData<TournamentGroupEventChampionData> data = this.tableQueryService.qryPointsGroupChampion(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"5, 1, 1466060, 1, 20"})
    void qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        TableData<TournamentBattleGroupEventResultData> data = this.tableQueryService.qryPageBattleGroupResult(tournamentId, groupId, entry, page, limit);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"0"})
    void qryLiveMatchList(int statusId) {
        TableData<LiveMatchTeamData> data = this.tableQueryService.qryLiveTeamDataList(statusId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic"})
    void qryLeagueCaptainReportStat(int leagueId, String leagueType) {
        TableData<LeagueEventReportStatData> data = this.tableQueryService.qryLeagueCaptainReportStat(leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, 13, Tournament"})
    void qryLeagueCaptainEventReportList(int event, int leagueId, String leagueType) {
        TableData<LeagueEventReportData> data = this.tableQueryService.qryLeagueCaptainEventReportList(event, leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic, 1870"})
    void qryEntryCaptainEventReportList(int leagueId, String leagueType, int entry) {
        TableData<LeagueEventReportData> data = this.tableQueryService.qryEntryCaptainEventReportList(leagueId, leagueType, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic"})
    void qryLeagueTransfersReportStat(int leagueId, String leagueType) {
        TableData<LeagueEventReportStatData> data = this.tableQueryService.qryLeagueTransfersReportStat(leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"14, 3571, Classic"})
    void qryLeagueTransfersEventReportList(int event, int leagueId, String leagueType) {
        TableData<LeagueEventReportData> data = this.tableQueryService.qryLeagueTransfersEventReportList(event, leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic, 1870"})
    void qryEntryTransfersEventReportList(int leagueId, String leagueType, int entry) {
        TableData<LeagueEventReportData> data = this.tableQueryService.qryEntryTransfersEventReportList(leagueId, leagueType, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic"})
    void qryLeagueScoringReportStat(int leagueId, String leagueType) {
        long start = System.currentTimeMillis();
        TableData<LeagueEventReportStatData> data = this.tableQueryService.qryLeagueScoringReportStat(leagueId, leagueType);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"19, 3571, Classic"})
    void qryLeagueScoringEventReportList(int event, int leagueId, String leagueType) {
        long start = System.currentTimeMillis();
        TableData<LeagueEventReportData> data = this.tableQueryService.qryLeagueScoringEventReportList(event, leagueId, leagueType);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic, 1144629"})
    void qryEntryScoringEventReportList(int leagueId, String leagueType, int entry) {
        long start = System.currentTimeMillis();
        TableData<LeagueEventReportData> data = this.tableQueryService.qryEntryScoringEventReportList(leagueId, leagueType, entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"8, 1"})
    void qryGroupInfoListByGroupId(int tournamentId, int groupId) {
        TableData<TournamentGroupData> data = this.tableQueryService.qryGroupInfoListByGroupId(tournamentId, groupId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2"})
    void qryPlayerShowListByElementType(int element) {
        long start = System.currentTimeMillis();
        TableData<PlayerShowData> data = this.tableQueryService.qryPlayerShowListByElementType(element);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"21, 4074865, 1870"})
    void qryEntryEventPlayerShowList(int event, int entry, int operator) {
        long start = System.currentTimeMillis();
        TableData<PlayerShowData> data = this.tableQueryService.qryEntryEventPlayerShowList(event, entry, operator);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"18, 4074865"})
    void qryEntryEventPlayerShowListForTransfers(int event, int entry) {
        TableData<PlayerShowData> data = this.tableQueryService.qryEntryEventPlayerShowListForTransfers(event, entry);
        System.out.println(1);
    }

    @Test
    void qryTournamentList() {
        TournamentQueryParam param = new TournamentQueryParam()
                .setEntry(0)
                .setLeagueId(0)
                .setSeason("2021");
        TableData<TournamentInfoData> data = this.tableQueryService.qryTournamentList(param);
        System.out.println(1);
    }

}
