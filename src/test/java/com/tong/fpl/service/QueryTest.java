package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import com.tong.fpl.domain.entity.TournamentKnockoutEntity;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutEventResultData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.service.db.EntryEventResultService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class QueryTest extends FplApplicationTests {

    @Autowired
    private IQueryService queryService;
    @Autowired
    private EntryEventResultService entryEventResultService;

    /**
     * @apiNote time
     */
    @ParameterizedTest
    @CsvSource({"2122"})
    void getPlayerMap(String season) {
        Map<String, PlayerEntity> map = this.queryService.getPlayerMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void getPlayerStatMap(String season) {
        Map<String, PlayerStatEntity> map = this.queryService.getPlayerStatMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 1, 1870"})
    void qryEntryEvent(String season, int event, int entry) {
        EntryEventResultData entryEventResultData = this.queryService.qryEntryEventResult(season, event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"483"})
    void qryPlayerData(int element) {
        PlayerData playerData = this.queryService.qryPlayerData(element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"16, -1, -1"})
    void qryPlayerFixtureList(int teamId, int previous, int next) {
        List<PlayerFixtureData> list = this.queryService.qryPlayerFixtureList(teamId, previous, next);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021"})
    void qryAllPlayers(String season) {
        List<PlayerInfoData> list = this.queryService.qryAllPlayers(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"10"})
    void qryGroupFixtureListById(int tournamentId) {
        List<TournamentGroupFixtureData> list = this.queryService.qryGroupFixtureListById(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"10"})
    void qryKnockoutFixtureListById(int tournamentId) {
        List<TournamentKnockoutFixtureData> list = this.queryService.qryKnockoutFixtureListById(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"15"})
    void qryKnockoutResultByTournament(int tournamentId) {
        List<TournamentKnockoutResultData> list = this.queryService.qryKnockoutResultByTournament(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"69"})
    void getPlayerByElement(int element) {
        long start = System.currentTimeMillis();
        PlayerEntity playerEntity = this.queryService.getPlayerByElement(element);
        long end = System.currentTimeMillis();
        System.out.println("escape: " + (end - start) + "ms!");
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"15"})
    void qryKnockoutBracketResultByTournament(int tournamentId) {
        KnockoutBracketData knockoutBracketData = this.queryService.qryKnockoutBracketResultByTournament(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"8"})
    void getDeadlineByEvent(int event) {
        String deadline = this.queryService.getDeadlineByEvent(event).replace(" ", "T");
        LocalDateTime localDateTime = LocalDateTime.parse(deadline).minusHours(1);
        System.out.println(LocalDateTime.now().equals(localDateTime));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("15")
    void getScoutDeadlineByEvent(int event) {
        String a = this.queryService.getScoutDeadlineByEvent(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"65, Classic"})
    void qryLeagueNameByIdAndType(int leagueId, String leagueType) {
        String a = this.queryService.qryLeagueNameByIdAndType(leagueId, leagueType);
        System.out.println(1);
    }

    @Test
    void qryPickListByPosition() {
        String picks = "[{\"element\":383,\"position\":1,\"multiplier\":1,\"points\":3,\"captain\":false,\"viceCaptain\":false},{\"element\":375,\"position\":2,\"multiplier\":1,\"points\":1,\"captain\":false,\"viceCaptain\":false},{\"element\":259,\"position\":3,\"multiplier\":1,\"points\":1,\"captain\":false,\"viceCaptain\":false},{\"element\":353,\"position\":4,\"multiplier\":1,\"points\":0,\"captain\":false,\"viceCaptain\":false},{\"element\":200,\"position\":5,\"multiplier\":1,\"points\":3,\"captain\":false,\"viceCaptain\":false},{\"element\":254,\"position\":6,\"multiplier\":1,\"points\":20,\"captain\":false,\"viceCaptain\":false},{\"element\":338,\"position\":7,\"multiplier\":1,\"points\":3,\"captain\":false,\"viceCaptain\":false},{\"element\":390,\"position\":8,\"multiplier\":1,\"points\":2,\"captain\":false,\"viceCaptain\":true},{\"element\":500,\"position\":9,\"multiplier\":1,\"points\":2,\"captain\":false,\"viceCaptain\":false},{\"element\":4,\"position\":10,\"multiplier\":2,\"points\":7,\"captain\":true,\"viceCaptain\":false},{\"element\":506,\"position\":11,\"multiplier\":1,\"points\":8,\"captain\":false,\"viceCaptain\":false},{\"element\":35,\"position\":12,\"multiplier\":0,\"points\":0,\"captain\":false,\"viceCaptain\":false},{\"element\":262,\"position\":13,\"multiplier\":0,\"points\":0,\"captain\":false,\"viceCaptain\":false},{\"element\":27,\"position\":14,\"multiplier\":0,\"points\":0,\"captain\":false,\"viceCaptain\":false},{\"element\":50,\"position\":15,\"multiplier\":0,\"points\":0,\"captain\":false,\"viceCaptain\":false}]";
        PlayerPickData playerData = this.queryService.qryPickListByPosition(picks);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"16, 4074865"})
    void qryEntryPickData(int event, int entry) {
        PlayerPickData data = this.queryService.qryEntryPickData(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"35, 65, Classic"})
    void qryLeagueEventEoMap(int event, int leagueId, String leagueType) {
        Map<String, String> map = this.queryService.qryLeagueEventEoMap(event, leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic"})
    void qryLeaguePickDataList(int leagueId, String leagueType) {
        long start = System.currentTimeMillis();
        List<PlayerPickData> list = this.queryService.qryLeaguePickDataList(leagueId, leagueType, Lists.newArrayList());
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"16, 3571, Classic"})
    void qryLeagueEventPickDataList(int event, int leagueId, String leagueType) {
        List<PlayerPickData> list = this.queryService.qryLeagueEventPickDataList(event, leagueId, leagueType);
        System.out.println(1);
    }

    @Test
    void qryPickListFromPicks() {
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, 1870)
                .eq(EntryEventResultEntity::getEvent, 16));
        List<EntryPickData> list = this.queryService.qryPickListFromPicks(entryEventResultEntity.getEventPicks());
        System.out.println(1);
    }

    @Test
    void qryOffiaccountLineupForTransfers() {
        List<PlayerPickData> list = this.queryService.qryOffiaccountLineupForTransfers();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1327348"})
    void qryEntryFreeTransfers(int entry) {
        Map<Integer, Integer> map = this.queryService.qryEntryFreeTransfersMap(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryLiveTeamDataList(int statusId) {
        List<LiveMatchTeamData> list = this.queryService.qryLiveTeamDataList(statusId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"15, 20"})
    void qryKnockoutMapByTournament(int tournamentId, int event) {
        Map<String, TournamentKnockoutEntity> map = this.queryService.qryKnockoutMapByTournament(tournamentId, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"15, 1, 23"})
    void qryKnockoutRoundMapByTournament(int tournament, int round, int event) {
        Map<String, List<TournamentKnockoutEventResultData>> map = this.queryService.qryKnockoutRoundMapByTournament(tournament, round, event);
        System.out.println(1);
    }

}
