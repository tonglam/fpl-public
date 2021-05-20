package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiStat;
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

    @GetMapping("/qryAllLeagueName")
    public List<String> qryAllLeagueName() {
        return this.apiStat.qryAllLeagueName();
    }

    @GetMapping("/qryTeamSelectByLeagueName")
    public LeagueStatData qryTeamSelectByLeagueName(@RequestParam int event, @RequestParam String leagueName) {
        return this.apiStat.qryTeamSelectByLeagueName(event, leagueName);
    }

}
