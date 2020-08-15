package com.tong.fpl.api.impl;

import com.tong.fpl.api.ITournamentManagementApi;
import com.tong.fpl.domain.data.letletme.QueryParam;
import com.tong.fpl.domain.data.letletme.TournamentCreateData;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.service.ITournamentManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/6/24
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentManagementApiImpl implements ITournamentManagementApi {

    private final ITournamentManagementService tournamentManagementService;

    @Override
    public String createNewTournament(TournamentCreateData tournamentCreateData) {
        return this.tournamentManagementService.createNewTournament(tournamentCreateData);
    }

    @Override
    public List<TournamentInfoEntity> queryTournamentInfo(QueryParam param) {
        return this.tournamentManagementService.queryTournamentInfo(param);
    }

    @Override
    public String deleteTournamentByCupName(String cupName) {
        return this.tournamentManagementService.deleteTournamentByCupName(cupName);
    }

    @Override
    public int countLeagueTeams(String url) {
        return this.tournamentManagementService.countLeagueTeams(url);
    }

    @Override
    public boolean checkTournamentName(String name) {
        return this.tournamentManagementService.checkTournamentName(name);
    }

}
