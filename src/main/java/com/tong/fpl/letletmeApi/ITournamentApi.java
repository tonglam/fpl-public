package com.tong.fpl.letletmeApi;

import com.tong.fpl.domain.letletme.entry.EntryCupData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;

import java.util.List;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentApi {

    /**
     * @apiNote create
     */
    String createNewTournament(TournamentCreateData tournamentCreateData);

    int countTournamentLeagueTeams(String url);

    TableData<EntryInfoData> qryLeagueEntryList(String url);

    boolean checkTournamentName(String name);

    /**
     * @apiNote result
     */
    TableData<TournamentEntryData> qryEntryTournamentList(int entry);

    /**
     * @apiNote fixture
     */
    List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId);

    List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId);

    /**
     * @apiNote pointsResult
     */
    TableData<TournamentGroupEventChampionData> qryPointsGroupChampion(int tournamentId);

    TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

    TableData<EntryCupData> qryPageEntryEventCupResult(int entry, int page, int limit);

    KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId);

    /**
     * @apiNote battleResult
     */
    TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

    /**
     * @apiNote manage
     */
    String updateTournamentInfo(TournamentCreateData tournamentCreateData);

    String deleteTournamentByName(String name);

    /**
     * @apiNote common
     */
    int getCurrentEvent();

    int getNextEvent();

    TableData<TournamentInfoData> qryTournamentList(TournamentQueryParam param);

    TournamentInfoData qryTournamentInfoById(int tournamentId);

    EntryInfoData qryEntryInfo(int entry);

    TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

}
