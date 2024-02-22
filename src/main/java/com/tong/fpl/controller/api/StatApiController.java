package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiStat;
import com.tong.fpl.domain.letletme.element.ElementAgainstInfoData;
import com.tong.fpl.domain.letletme.element.ElementAgainstRecordData;
import com.tong.fpl.domain.letletme.element.ElementEventLiveExplainData;
import com.tong.fpl.domain.letletme.element.ElementSummaryData;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.team.TeamAgainstInfoData;
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

    @GetMapping("/qryPlayerPriceChange")
    public List<PlayerValueData> qryPlayerPriceChange(@RequestParam String date) {
        if (date.contains("-")) {
            date = date.replaceAll("-", "");
        }
        return this.apiStat.qryPlayerPriceChange(date);
    }

    @GetMapping("/qryPlayerValueByElement")
    public List<PlayerValueData> qryPlayerValueByElement(@RequestParam int element) {
        return this.apiStat.qryPlayerValueByElement(element);
    }

    @GetMapping("/refreshPlayerValue")
    public void refreshPlayerValue() {
        this.apiStat.refreshPlayerValue();
    }

    @GetMapping("/qryLeagueSelectByName")
    public LeagueEventSelectData qryLeagueSelectByName(@RequestParam int event, @RequestParam int leagueId, @RequestParam String leagueName) {
        return this.apiStat.qryLeagueSelectByName(event, leagueId, leagueName);
    }

    @GetMapping("/refreshLeagueSelect")
    public void refreshLeagueSelect(@RequestParam int event, @RequestParam String leagueName) {
        this.apiStat.refreshLeagueSelect(event, leagueName);
    }

    @GetMapping("/qrySeasonFixture")
    public List<List<String>> qrySeasonFixture() {
        return this.apiStat.qrySeasonFixture();
    }

    @GetMapping("/qryPlayerSummary")
    public PlayerSummaryData qryPlayerSummary(@RequestParam String season, @RequestParam int code) {
        return this.apiStat.qryPlayerSummary(season, code);
    }

    @GetMapping("/refreshPlayerSummary")
    public void refreshPlayerSummary(@RequestParam String season, @RequestParam int code) {
        this.apiStat.refreshPlayerSummary(season, code);
    }

    @GetMapping("/qryTeamSummary")
    public TeamSummaryData qryTeamSummary(@RequestParam String season, @RequestParam String name) {
        return this.apiStat.qryTeamSummary(season, name);
    }

    @GetMapping("/refreshTeamSummary")
    public void refreshTeamSummary(@RequestParam String season, @RequestParam String name) {
        this.apiStat.refreshTeamSummary(season, name);
    }

    @GetMapping("/qryElementEventExplainResult")
    public ElementEventLiveExplainData qryElementEventExplainResult(@RequestParam int event, @RequestParam int element) {
        return this.apiStat.qryElementEventExplainResult(event, element);
    }

    @GetMapping("/qryTeamAgainstRecordInfo")
    public TeamAgainstInfoData qryTeamAgainstRecordInfo(@RequestParam int teamId, @RequestParam int againstId) {
        return this.apiStat.qryTeamAgainstRecordInfo(teamId, againstId);
    }

    @GetMapping("/qryTeamAgainstRecordResult")
    public List<ElementSummaryData> qryTeamAgainstRecordResult(@RequestParam String season, @RequestParam int event, @RequestParam int teamHId, @RequestParam int teamAId) {
        return this.apiStat.qryTeamAgainstRecordResult(season, event, teamHId, teamAId);
    }

    @GetMapping("/qryTopElementAgainstInfo")
    public List<ElementAgainstInfoData> qryTopElementAgainstInfo(@RequestParam int teamId, @RequestParam int againstId, @RequestParam boolean active) {
        return this.apiStat.qryTopElementAgainstInfo(teamId, againstId, active);
    }

    @GetMapping("/qryElementAgainstRecord")
    public List<ElementAgainstRecordData> qryElementAgainstRecord(@RequestParam int teamId, @RequestParam int againstId, @RequestParam int elementCode) {
        return this.apiStat.qryElementAgainstRecord(teamId, againstId, elementCode);
    }

}
