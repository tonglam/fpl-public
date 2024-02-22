package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiTournament;
import com.tong.fpl.domain.event.RefreshChampionLeagueEventResultEventData;
import com.tong.fpl.domain.event.RefreshTournamentEventResultEventData;
import com.tong.fpl.domain.letletme.entry.EntryAgainstInfoData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.SearchEntryEventResultData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.ITournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiTournamentImpl implements IApiTournament {

    private final ApplicationContext context;
    private final IApiQueryService apiQueryService;
    private final ITournamentService tournamentService;

    @Override
    public List<TournamentInfoData> qryEntryPointsRaceTournament(int entry) {
        return this.apiQueryService.qryEntryPointsRaceTournament(entry);
    }

    @Override
    public List<TournamentInfoData> qryEntryKnockoutTournament(int entry) {
        return this.apiQueryService.qryEntryKnockoutTournament(entry);
    }

    @Override
    public List<TournamentInfoData> qryEntryPointsRaceChampionLeague(int entry) {
        return this.apiQueryService.qryEntryChampionLeague(entry);
    }

    @Override
    public String qryEntryChampionLeagueStage(int entry, int tournamentId) {
        return this.apiQueryService.qryEntryChampionLeagueStage(entry, tournamentId);
    }

    @Override
    public LinkedHashMap<String, List<String>> qryChampionLeagueStage(int tournamentId) {
        return this.apiQueryService.qryChampionLeagueStage(tournamentId);
    }

    @Override
    public LinkedHashMap<String, List<String>> qryChampionLeagueStageGroup(int tournamentId) {
        return this.apiQueryService.qryChampionLeagueStageGroup(tournamentId);
    }

    @Override
    public List<List<TournamentGroupData>> qryChampionLeagueGroupQualifications(int tournamentId) {
        return this.apiQueryService.qryChampionLeagueGroupQualifications(tournamentId);
    }

    @Override
    public List<List<TournamentKnockoutData>> qryChampionLeagueStageKnockoutRound(int tournamentId, int round) {
        return this.apiQueryService.qryChampionLeagueStageKnockoutRound(tournamentId, round);
    }

    @Override
    public TournamentInfoData qryTournamentInfo(int id) {
        return this.apiQueryService.qryTournamentInfo(id);
    }

    @Override
    public List<EntryEventResultData> qryTournamentEventResult(int event, int tournamentId) {
        return this.apiQueryService.qryTournamentEventResult(event, tournamentId);
    }

    @Override
    public void refreshTournamentEventResult(int event, int tournamentId) {
        this.context.publishEvent(new RefreshTournamentEventResultEventData(this, event, tournamentId));
    }

    @Override
    public SearchEntryEventResultData qryTournamentEventSearchResult(int event, int tournamentId, int element) {
        return this.apiQueryService.qryTournamentEventSearchResult(event, tournamentId, element);
    }

    @Override
    public List<EntryEventResultData> qryChampionLeagueEventResult(int event, int tournamentId) {
        return this.apiQueryService.qryChampionLeagueEventResult(event, tournamentId);
    }

    @Override
    public void refreshChampionLeagueEventResult(int event, int tournamentId) {
        this.context.publishEvent(new RefreshChampionLeagueEventResultEventData(this, event, tournamentId));
    }

    @Override
    public List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId) {
        return this.apiQueryService.qryTournamentEventSummary(event, tournamentId);
    }

    @Override
    public List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry) {
        return this.apiQueryService.qryTournamentEntryEventSummary(tournamentId, entry);
    }

    @Override
    public TournamentGroupEventChampionData qryTournamentEventChampion(int tournamentId) {
        return this.apiQueryService.qryTournamentEventChampion(tournamentId);
    }

    @Override
    public List<Integer> qryDrawKnockoutEntries(int tournamentId) {
        return this.apiQueryService.qryDrawKnockoutEntries(tournamentId);
    }

    @Override
    public List<EntryAgainstInfoData> qryDrawKnockoutResults(int tournamentId) {
        return this.apiQueryService.qryDrawKnockoutResults(tournamentId);
    }

    @Override
    public List<EntryInfoData> qryDrawKnockoutOpponents(int tournamentId, int entry) {
        return this.apiQueryService.qryDrawKnockoutOpponents(tournamentId, entry);
    }

    @Override
    public String qryDrawKnockoutNotice(int tournamentId) {
        return this.apiQueryService.qryDrawKnockoutNotice(tournamentId);
    }

    @Override
    public String drawKnockoutSinglePair(int tournamentId, String groupName, int entry, int position) {
        return this.tournamentService.drawKnockoutSinglePair(tournamentId, groupName, entry, position);
    }

    @Override
    public List<List<EntryInfoData>> qryDrawKnockoutPairs(int tournamentId) {
        return this.apiQueryService.qryDrawKnockoutPairs(tournamentId);
    }
}
