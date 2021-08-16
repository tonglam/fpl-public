package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiTournament;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentPointsGroupEventResultData;
import com.tong.fpl.service.IApiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiTournamentImpl implements IApiTournament {

    private final IApiQueryService apiQueryService;

    @Override
    public List<TournamentInfoData> qryEntryPointsRaceTournament(int entry) {
        return this.apiQueryService.qryEntryPointsRaceTournament(entry);
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
    public List<EntryEventResultData> qryTournamentEventSearchResult(int event, int tournamentId, int element) {
        return this.apiQueryService.qryTournamentEventSearchResult(event, tournamentId, element);
    }

    @Override
    public List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId) {
        return this.apiQueryService.qryTournamentEventSummary(event, tournamentId);
    }

    @Override
    public List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry) {
        return this.apiQueryService.qryTournamentEntryEventSummary(tournamentId, entry);
    }

}
