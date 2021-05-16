package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiTournament;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
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
    public List<TournamentInfoData> qryEntryTournament(int entry) {
        return this.apiQueryService.qryEntryTournament(entry);
    }

    @Override
    public TournamentInfoData qryTournamentInfoById(int id) {
        return this.apiQueryService.qryTournamentInfoById(id);
    }

}
