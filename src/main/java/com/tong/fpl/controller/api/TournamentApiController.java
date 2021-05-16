package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiTournament;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
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

    @GetMapping("/qryTournamentInfoById")
    public TournamentInfoData qryTournamentInfoById(@RequestParam int id) {
        return this.apiTournament.qryTournamentInfoById(id);
    }

}
