package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiReport;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
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
@RequestMapping("/api/report")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportApiController {

    private final IApiReport apiReport;

    @GetMapping("/qryPlayerValueByDate")
    public Map<String, List<PlayerValueData>> qryPlayerValueByDate(@RequestParam String date) {
        if (date.contains("-")) {
            date = date.replaceAll("-", "");
        }
        return this.apiReport.qryPlayerValueByDate(date);
    }

    @GetMapping("/qryLeagueInfo")
    public List<String> qryLeagueInfo() {
        return this.apiReport.qryLeagueInfo();
    }

    @GetMapping("/qryTeamSelectByLeagueName")
    public LeagueStatData qryTeamSelectByLeagueName(@RequestParam int event, @RequestParam String leagueName) {
        return this.apiReport.qryTeamSelectByLeagueName(event, leagueName);
    }

}
