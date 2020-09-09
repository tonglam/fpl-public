package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;

import java.util.List;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentApi {

	String createNewTournament(TournamentCreateData tournamentCreateData);

	TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

	int countTournamentLeagueTeams(String url);

	boolean checkTournamentName(String name);

	String updateTournament(TournamentCreateData tournamentCreateData);

	String deleteTournamentByName(String name);

	TableData<EntryTournamentData> qryEntryTournamentList(int entry);

	EntryInfoData qryEntryInfoData(int entry);

	TournamentInfoData qryTournamentInfoById(int tournamentId);

	List<TournamentKnockoutData> qryKnockoutListByTournamentId(int tournamentId);

	TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

	List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId);

	TableData<EntryEventResultData> qryEntryEventResult(int startGw, int endGw, int entry);

}
