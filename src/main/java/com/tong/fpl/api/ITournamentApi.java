package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.global.StepsData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentApi {

	/**
	 * @apiNote create
	 */
	String createNewTournament(TournamentCreateData tournamentCreateData);

	String createNewZjTournament(ZjTournamentCreateData zjTournamentCreateData);

	int countTournamentLeagueTeams(String url);

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

	KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId);

	/**
	 * @apiNote battleResult
	 */
	TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

	/**
	 * @apiNote zjResult
	 */
	List<TournamentKnockoutResultData> qryZjTournamentPkResultByTournament(int tournamentId);

	TableData<TournamentPointsGroupEventResultData> qryZjTournamentGroupResult(int tournamentId, int stage, int groupId, int entry, int page, int limit);

	TableData<ZjTournamentResultData> qryZjTournamentResultById(int tournamentId);

	List<ZjTournamentCaptainData> qryZjTournamentCaptain(int tournamentId);

	/**
	 * @apiNote manage
	 */
	String updateTournamentInfo(TournamentCreateData tournamentCreateData);

	String deleteTournamentByName(String name);

	/**
	 * @apiNote manageZjTournament
	 */
	int qryZjTournamentPhaseOneRankByGroupId(int tournamentId, int currentGroupId);

	Map<String, String> qryZjTournamentGroupNameMap(int tournamentId);

	List<EntryInfoData> qryGroupEntryInfoList(int tournamentId, int groupId);

	TournamentGroupData qryDiscloseGroupData(int tournamentId, int entry, int currentGroupId);

	TableData<TournamentGroupData> qrySeeableGroupInfoListByGroupId(int tournamentId, int currentGroupId, int groupId);

	List<TournamentKnockoutEventFixtureData> qryZjPkPickListById(int tournamentId);

	StepsData qryZjTournamentPkPickSteps(int tournamentId);

	TableData<TournamentGroupData> qryZjTournamentPkPickableList(int tournamentId, int currentGroupId);

	String updateZjTournamentPhaseTwoGroupData(List<TournamentGroupData> groupDataList, int captainEntry);

	String updateZjTournamentPkData(int tournamentId, int entry, int pkEntry, int captainEntry);

	/**
	 * @apiNote common
	 */
	TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

	TournamentInfoData qryTournamentInfoById(int tournamentId);

	EntryInfoData qryEntryInfo(int entry);

	TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

}
