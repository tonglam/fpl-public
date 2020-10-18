package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentApi {

	String createNewTournament(TournamentCreateData tournamentCreateData);

	String createNewZjTournament(ZjTournamentCreateData zjTournamentCreateData);

	TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

	int countTournamentLeagueTeams(String url);

	boolean checkTournamentName(String name);

	String updateTournamentInfo(TournamentCreateData tournamentCreateData);

	String deleteTournamentByName(String name);

	TableData<TournamentEntryData> qryEntryTournamentList(int entry);

	EntryInfoData qryEntryInfoData(int entry);

	TournamentInfoData qryTournamentInfoById(int tournamentId);

	List<TournamentKnockoutData> qryKnockoutListByTournamentId(int tournamentId);

	Map<String, String> qryZjTournamentGroupNameMap(int tournamentId);

	TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

	TournamentGroupData qryDiscloseGroupData(int tournamentId, int entry, int currentGroupId);

	int qryZjTournamentPhaseOneRankByGroupId(int tournamentId, int currentGroupId);

	List<EntryInfoData> qryGroupEntryInfoList(int tournamentId, int groupId);

	TableData<TournamentGroupData> qrySeeableGroupInfoListByGroupId(int tournamentId, int currentGroupId, int groupId);

	KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId);

	List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId);

	TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

	TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

	TableData<TournamentPointsGroupEventResultData> qryZjTournamentGroupResult(int tournamentId, int stage, int groupId, int entry, int page, int limit);

	List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId);

	List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId);

	EntryInfoData qryEntryInfo(int entry);

	TableData<ZjTournamentResultData> qryZjTournamentResultById(int tournamentId);

	List<ZjTournamentCaptainData> qryZjTournamentCaptain(int tournamentId);

	String updateZjTournamentPhaseTwoGroupData(List<TournamentGroupData> groupDataList, int captainEntry);

	String updateZjTournamentPkData(int tournamentId, int entry, int pkEntry, int groupId, int captainEntry);

}
