package com.tong.fpl.api.impl;

import com.tong.fpl.api.ITournamentApi;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.EntryTournamentData;
import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.ITournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/24
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentApiImpl implements ITournamentApi {

    private final ITournamentService tournamentService;
    private final ITableQueryService tableQueryService;

    @Override
    public String createNewTournament(TournamentCreateData tournamentCreateData) {
        return this.tournamentService.createNewTournament(tournamentCreateData);
    }

    @Override
    public TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
        return this.tableQueryService.qryTournamenList(param);
    }

    @Override
    public int countTournamentLeagueTeams(String url) {
        return this.tournamentService.countTournamentLeagueTeams(url);
    }

    @Override
    public boolean checkTournamentName(String name) {
        return this.tournamentService.checkTournamentName(name);
    }

    @Override
    public String updateTournament(TournamentCreateData tournamentCreateData) {
        return this.tournamentService.updateTournament(tournamentCreateData);
    }

    @Override
    public String deleteTournamentByName(String name) {
        return this.tournamentService.deleteTournamentByName(name);
    }

    @Override
    public TableData<EntryTournamentData> qryEntryTournamenList(int entry) {
        return this.tableQueryService.qryEntryTournamenList(entry);
    }

    @Override
    public EntryInfoEntity getEntryInfo(int entry) {
        return this.tournamentService.getEntryInfo(entry);
    }

}
