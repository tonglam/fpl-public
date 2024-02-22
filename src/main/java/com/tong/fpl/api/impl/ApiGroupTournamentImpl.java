package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiGroupTournament;
import com.tong.fpl.domain.letletme.groupTournament.GroupTournamentResultData;
import com.tong.fpl.service.IGroupTournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 5/11/2023
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiGroupTournamentImpl implements IApiGroupTournament {

    private final IGroupTournamentService groupTournamentService;

    @Override
    public void insertEntryEventResult(int groupTournamentId, int event) {
        this.groupTournamentService.insertEntryEventResult(groupTournamentId, event);
    }

    @Override
    public List<GroupTournamentResultData> getEventGroupTournamentResult(int groupTournamentId, int event) {
        return this.groupTournamentService.getEventGroupTournamentResult(groupTournamentId, event);
    }

    @Override
    public void refreshGroupTournamentResult(int groupTournamentId, int event) {
        this.groupTournamentService.refreshGroupTournamentResult(groupTournamentId, event);
    }

}
