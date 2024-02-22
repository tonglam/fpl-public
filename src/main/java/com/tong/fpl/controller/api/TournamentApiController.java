package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiTournament;
import com.tong.fpl.domain.letletme.entry.EntryAgainstInfoData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.SearchEntryEventResultData;
import com.tong.fpl.domain.letletme.tournament.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
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

    @GetMapping("/qryEntryKnockoutTournament")
    public List<TournamentInfoData> qryEntryKnockoutTournament(@RequestParam int entry) {
        return this.apiTournament.qryEntryKnockoutTournament(entry);
    }

    @GetMapping("/qryEntryPointsRaceChampionLeague")
    public List<TournamentInfoData> qryEntryPointsRaceChampionLeague(@RequestParam int entry) {
        return this.apiTournament.qryEntryPointsRaceChampionLeague(entry);
    }

    @GetMapping("/qryEntryChampionLeagueStage")
    public String qryEntryChampionLeagueStage(@RequestParam int entry, @RequestParam int tournamentId) {
        return this.apiTournament.qryEntryChampionLeagueStage(entry, tournamentId);
    }

    @GetMapping("/qryChampionLeagueStage")
    public LinkedHashMap<String, List<String>> qryChampionLeagueStage(@RequestParam int tournamentId) {
        return this.apiTournament.qryChampionLeagueStage(tournamentId);
    }

    @GetMapping("/qryChampionLeagueStageGroup")
    public LinkedHashMap<String, List<String>> qryChampionLeagueStageGroup(@RequestParam int tournamentId) {
        return this.apiTournament.qryChampionLeagueStageGroup(tournamentId);
    }

    @GetMapping("/qryChampionLeagueGroupQualifications")
    public List<List<TournamentGroupData>> qryChampionLeagueGroupQualifications(@RequestParam int tournamentId) {
        return this.apiTournament.qryChampionLeagueGroupQualifications(tournamentId);
    }

    @GetMapping("/qryChampionLeagueStageKnockoutRound")
    public List<List<TournamentKnockoutData>> qryChampionLeagueStageKnockoutRound(@RequestParam int tournamentId, @RequestParam int round) {
        return this.apiTournament.qryChampionLeagueStageKnockoutRound(tournamentId, round);
    }

    @GetMapping("/qryTournamentInfo")
    public TournamentInfoData qryTournamentInfo(@RequestParam int id) {
        return this.apiTournament.qryTournamentInfo(id);
    }

    @GetMapping("/qryTournamentEventResult")
    public List<EntryEventResultData> qryTournamentEventResult(@RequestParam int event, @RequestParam int tournamentId) {
        return this.apiTournament.qryTournamentEventResult(event, tournamentId);
    }

    @GetMapping("/refreshTournamentEventResult")
    public void refreshTournamentEventResult(@RequestParam int event, @RequestParam int tournamentId) {
        this.apiTournament.refreshTournamentEventResult(event, tournamentId);
    }

    @GetMapping("/qryTournamentEventSearchResult")
    SearchEntryEventResultData qryTournamentEventSearchResult(@RequestParam int event, @RequestParam int tournamentId, @RequestParam int element) {
        return this.apiTournament.qryTournamentEventSearchResult(event, tournamentId, element);
    }

    @GetMapping("/qryChampionLeagueEventResult")
    public List<EntryEventResultData> qryChampionLeagueEventResult(@RequestParam int event, @RequestParam int tournamentId) {
        return this.apiTournament.qryChampionLeagueEventResult(event, tournamentId);
    }

    @GetMapping("/refreshChampionLeagueEventResult")
    public void refreshChampionLeagueEventResult(@RequestParam int event, @RequestParam int tournamentId) {
        this.apiTournament.refreshChampionLeagueEventResult(event, tournamentId);
    }

    @GetMapping("/qryTournamentEventSummary")
    List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(@RequestParam int event, @RequestParam int tournamentId) {
        return this.apiTournament.qryTournamentEventSummary(event, tournamentId);
    }

    @GetMapping("/qryTournamentEntryEventSummary")
    List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(@RequestParam int tournamentId, @RequestParam int entry) {
        return this.apiTournament.qryTournamentEntryEventSummary(tournamentId, entry);
    }

    @GetMapping("/qryTournamentEventChampion")
    TournamentGroupEventChampionData qryTournamentEventChampion(@RequestParam int tournamentId) {
        return this.apiTournament.qryTournamentEventChampion(tournamentId);
    }

    @GetMapping("/qryDrawKnockoutEntries")
    List<Integer> qryDrawKnockoutEntries(@RequestParam int tournamentId) {
        return this.apiTournament.qryDrawKnockoutEntries(tournamentId);
    }

    @GetMapping("/qryDrawKnockoutResults")
    List<EntryAgainstInfoData> qryDrawKnockoutResults(@RequestParam int tournamentId) {
        return this.apiTournament.qryDrawKnockoutResults(tournamentId);
    }

    @GetMapping("/qryDrawKnockoutOpponents")
    List<EntryInfoData> qryDrawKnockoutOpponents(@RequestParam int tournamentId, @RequestParam int entry) {
        return this.apiTournament.qryDrawKnockoutOpponents(tournamentId, entry);
    }

    @GetMapping("/qryDrawKnockoutNotice")
    String qryDrawKnockoutNotice(@RequestParam int tournamentId) {
        return this.apiTournament.qryDrawKnockoutNotice(tournamentId);
    }

    @GetMapping("/drawKnockoutSinglePair")
    String drawKnockoutSinglePair(@RequestParam int tournamentId, @RequestParam String groupName, @RequestParam int entry, @RequestParam int position) {
        return this.apiTournament.drawKnockoutSinglePair(tournamentId, groupName, entry, position);
    }

    @GetMapping("/qryDrawKnockoutPairs")
    List<List<EntryInfoData>> qryDrawKnockoutPairs(@RequestParam int tournamentId) {
        return this.apiTournament.qryDrawKnockoutPairs(tournamentId);
    }

}
