package com.tong.fpl.api.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.api.ITournamentApi;
import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.service.IPageQueryService;
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
    private final IPageQueryService pageQueryService;

    @Override
    public String createNewTournament(TournamentCreateData tournamentCreateData) {
        return this.tournamentService.createNewTournament(tournamentCreateData);
    }

    @Override
    public Page<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
        return this.pageQueryService.qryTournamenList(param);
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

}
