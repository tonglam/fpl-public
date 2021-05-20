package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiTournament;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
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
        return this.apiTournament.qryEntryPointsRaceTournament(entry);
    }

    @GetMapping("/qryTournamentInfo")
    public TournamentInfoData qryTournamentInfo(@RequestParam int id) {
        return this.apiTournament.qryTournamentInfo(id);
    }

    @GetMapping("/qryTournamentEventResult")
    public List<EntryEventResultData> qryTournamentEventResult(@RequestParam int event, @RequestParam int tournamentId) {
        return this.apiTournament.qryTournamentEventResult(event, tournamentId);
    }

    @GetMapping("/qryTournamentEntryContainElement")
    List<Integer> qryTournamentEntryContainElement(int event, int tournamentId, int element) {
        return this.apiTournament.qryTournamentEntryContainElement(event, tournamentId, element);
    }

    @GetMapping("/qryTournamentEntryPlayElement")
    List<Integer> qryTournamentEntryPlayElement(int event, int tournamentId, int element) {
        return this.apiTournament.qryTournamentEntryPlayElement(event, tournamentId, element);
    }

    @GetMapping("/qryTournamentEventSummary")
    List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId) {
        return this.apiTournament.qryTournamentEventSummary(event, tournamentId);
    }

    @GetMapping("/qryTournamentEntryEventSummary")
    List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry) {
        return this.apiTournament.qryTournamentEntryEventSummary(tournamentId, entry);
    }

}
