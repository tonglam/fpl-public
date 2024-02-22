package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiGroupTournament;
import com.tong.fpl.domain.letletme.groupTournament.GroupTournamentResultData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Create by tong on 4/11/2023
 */
@RestController
@RequestMapping("/api/group_tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroupTournamentController {

    private final IApiGroupTournament apiGroupTournament;

    @RequestMapping("/insertEntryEventResult")
    public void insertShuffledGroupEventLiveResult(@RequestParam int groupTournamentId, @RequestParam int event) {
        this.apiGroupTournament.insertEntryEventResult(groupTournamentId, event);
    }

    @RequestMapping("/getEventGroupTournamentResult")
    public List<GroupTournamentResultData> getEventGroupTournamentResult(@RequestParam int groupTournamentId, @RequestParam int event) {
        return this.apiGroupTournament.getEventGroupTournamentResult(groupTournamentId, event);
    }

    @RequestMapping("/refreshGroupTournamentResult")
    public void refreshGroupTournamentResult(@RequestParam int groupTournamentId, @RequestParam int event) {
        this.apiGroupTournament.refreshGroupTournamentResult(groupTournamentId, event);
    }

}
