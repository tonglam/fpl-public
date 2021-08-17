package com.tong.fpl.controller.api;

import com.google.common.collect.Lists;
import com.tong.fpl.api.IApiTournament;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.SearchEntryEventResultData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupEventChampionData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentPointsGroupEventResultData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
@RestController
@RequestMapping("/api/tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentApiController {

    private final IApiTournament apiTournament;

    @GetMapping("/qryEntryPointsRaceTournament")
    public List<TournamentInfoData> qryEntryPointsRaceTournament(@RequestParam int entry) {
        if (entry <= 0) {
            return Lists.newArrayList();
        }
        return this.apiTournament.qryEntryPointsRaceTournament(entry);
    }

    @GetMapping("/qryTournamentInfo")
    public TournamentInfoData qryTournamentInfo(@RequestParam int id) {
        if (id <= 0) {
            return new TournamentInfoData();
        }
        return this.apiTournament.qryTournamentInfo(id);
    }

    @GetMapping("/qryTournamentEventResult")
    public List<EntryEventResultData> qryTournamentEventResult(@RequestParam int event, @RequestParam int tournamentId) {
        if (event <= 0 || tournamentId <= 0) {
            return Lists.newArrayList();
        }
        return this.apiTournament.qryTournamentEventResult(event, tournamentId);
    }

    @GetMapping("/qryTournamentEventSearchResult")
    SearchEntryEventResultData qryTournamentEventSearchResult(@RequestParam int event, @RequestParam int tournamentId, @RequestParam int element) {
        if (event <= 0 || tournamentId <= 0 || element <= 0) {
            return new SearchEntryEventResultData();
        }
        return this.apiTournament.qryTournamentEventSearchResult(event, tournamentId, element);
    }

    @GetMapping("/qryTournamentEventSummary")
    List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(@RequestParam int event, @RequestParam int tournamentId) {
        if (event <= 0 || tournamentId <= 0) {
            return Lists.newArrayList();
        }
        return this.apiTournament.qryTournamentEventSummary(event, tournamentId);
    }

    @GetMapping("/qryTournamentEntryEventSummary")
    List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(@RequestParam int tournamentId, @RequestParam int entry) {
        if (tournamentId <= 0 || entry <= 0) {
            return Lists.newArrayList();
        }
        return this.apiTournament.qryTournamentEntryEventSummary(tournamentId, entry);
    }

    @GetMapping("/qryTournamentEventChampion")
    TournamentGroupEventChampionData qryTournamentEventChampion(@RequestParam int tournamentId) {
        return this.apiTournament.qryTournamentEventChampion(tournamentId);
    }

}
