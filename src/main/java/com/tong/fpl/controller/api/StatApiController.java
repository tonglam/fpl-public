package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiStat;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.team.TeamSummaryData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@RestController
@RequestMapping("/api/stat")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatApiController {

    private final IApiStat apiStat;

    @GetMapping("/qryPlayerValueByDate")
    public Map<String, List<PlayerValueData>> qryPlayerValueByDate(@RequestParam String date) {
        if (date.contains("-")) {
            date = date.replaceAll("-", "");
        }
        return this.apiStat.qryPlayerValueByDate(date);
    }

    @GetMapping("/qryPlayerValueByElement")
    public List<PlayerValueData> qryPlayerValueByElement(@RequestParam int element) {
        return this.apiStat.qryPlayerValueByElement(element);
    }

    @GetMapping("/qryPlayerValueByTeamId")
    public Map<String, List<PlayerValueData>> qryPlayerValueByTeamId(@RequestParam int teamId) {
        return this.apiStat.qryPlayerValueByTeamId(teamId);
    }

    @GetMapping("/refreshPlayerValue")
    public void refreshPlayerValue() {
        this.apiStat.refreshPlayerValue();
    }

    @GetMapping("/qryTeamSelectByLeagueName")
    public LeagueEventSelectData qryTeamSelectByLeagueName(@RequestParam String season, @RequestParam int event, @RequestParam String leagueName) {
        return this.apiStat.qryTeamSelectByLeagueName(season, event, leagueName);
    }

    @GetMapping("/qrySeasonFixture")
    public List<List<String>> qrySeasonFixture() {
        return this.apiStat.qrySeasonFixture();
    }

    @GetMapping("/qryPlayerInfo")
    public PlayerInfoData qryPlayerInfo(@RequestParam String season, @RequestParam int code) {
        return this.apiStat.qryPlayerInfo(season, code);
    }

    @GetMapping("/qryPlayerSummary")
    public PlayerSummaryData qryPlayerSummary(@RequestParam String season, @RequestParam int code) {
        return this.apiStat.qryPlayerSummary(season, code);
    }

    @GetMapping("/qryTeamSummary")
    public TeamSummaryData qryTeamSummary(@RequestParam String season, @RequestParam String name) {
        return this.apiStat.qryTeamSummary(season, name);
    }

}
