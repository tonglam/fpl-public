package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiSpecialTournament;
import com.tong.fpl.domain.special.GroupRankResultData;
import com.tong.fpl.domain.special.GroupResultData;
import com.tong.fpl.domain.special.ShuffledGroupResultData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2022/2/26
 */
@RestController
@RequestMapping("/api/special_tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecialTournamentController {

    private final IApiSpecialTournament apiSpecialTournament;

    @RequestMapping("/getTournamentEntryList")
    public List<Integer> getTournamentEntryList(@RequestParam int tournamentId) {
        return this.apiSpecialTournament.getTournamentEntryList(tournamentId);
    }

    @RequestMapping("/insertShuffledGroupEventResult")
    public void insertShuffledGroupEventLiveResult(@RequestParam int tournamentId, @RequestParam int event) {
        this.apiSpecialTournament.insertShuffledGroupEventResult(tournamentId, event);
    }

    @RequestMapping("/insertGroupEventResult")
    public void insertGroupEventLiveResult(@RequestParam int tournamentId, @RequestParam int event) {
        this.apiSpecialTournament.insertGroupEventResult(tournamentId, event);
    }

    @RequestMapping("/getShuffledGroupResult")
    public Map<Integer, List<ShuffledGroupResultData>> getShuffledGroupResult(@RequestParam int tournamentId, @RequestParam int event) {
        return this.apiSpecialTournament.getShuffledGroupResult(tournamentId, event);
    }

    @RequestMapping("/getEventGroupResult")
    public List<GroupResultData> getEventGroupResult(@RequestParam int tournamentId, @RequestParam int event) {
        return this.apiSpecialTournament.getEventGroupResult(tournamentId, event);
    }

    @RequestMapping("/getGroupRankResult")
    public List<GroupRankResultData> getGroupRankResult(@RequestParam int tournamentId, @RequestParam int event) {
        return this.apiSpecialTournament.getGroupRankResult(tournamentId, event);
    }

    @RequestMapping("/refreshShuffledGroupResult")
    public void refreshShuffledGroupResult(@RequestParam int tournamentId, @RequestParam int event) {
        this.apiSpecialTournament.refreshShuffledGroupResult(tournamentId, event);
    }

    @RequestMapping("/refreshEventGroupResult")
    public void refreshEventGroupResult(@RequestParam int tournamentId, @RequestParam int event) {
        this.apiSpecialTournament.refreshEventGroupResult(tournamentId, event);
    }

}
