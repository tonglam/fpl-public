package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiSpecialTournament;
import com.tong.fpl.domain.special.GroupRankResultData;
import com.tong.fpl.domain.special.GroupResultData;
import com.tong.fpl.domain.special.ShuffledGroupResultData;
import com.tong.fpl.service.ISpecialTournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2022/2/26
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiSpecialTournamentImpl implements IApiSpecialTournament {

    private final ISpecialTournamentService specialTournamentService;

    @Override
    public List<Integer> getTournamentEntryList(int tournamentId) {
        return this.specialTournamentService.getTournamentEntryList(tournamentId);
    }

    @Override
    public void insertShuffledGroupEventResult(int tournamentId, int event) {
        this.specialTournamentService.insertShuffledGroupEventResult(tournamentId, event);
    }

    @Override
    public void insertGroupEventResult(int tournamentId, int event) {
        this.specialTournamentService.insertGroupEventResult(tournamentId, event);
    }

    @Override
    public Map<Integer, List<ShuffledGroupResultData>> getShuffledGroupResult(int tournamentId, int event) {
        return this.specialTournamentService.getShuffledGroupResult(tournamentId, event);
    }

    @Override
    public List<GroupResultData> getEventGroupResult(int tournamentId, int event) {
        return this.specialTournamentService.getEventGroupResult(tournamentId, event);
    }

    @Override
    public List<GroupRankResultData> getGroupRankResult(int tournamentId, int event) {
        return this.specialTournamentService.getGroupRankResult(tournamentId, event);
    }

    @Override
    public void refreshShuffledGroupResult(int tournamentId, int event) {
        this.specialTournamentService.refreshShuffledGroupResult(tournamentId, event);
    }

    @Override
    public void refreshEventGroupResult(int tournamentId, int event) {
        this.specialTournamentService.refreshEventGroupResult(tournamentId, event);
    }

}
