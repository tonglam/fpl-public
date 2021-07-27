package com.tong.fpl.controller.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.api.IApiStat;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isEmpty(date)) {
            return Maps.newLinkedHashMap();
        }
        if (date.contains("-")) {
            date = date.replaceAll("-", "");
        }
        return this.apiStat.qryPlayerValueByDate(date);
    }

    @GetMapping("/qryPlayerValueByElement")
    public List<PlayerValueData> qryPlayerValueByElement(@RequestParam int element) {
        if (element <= 0) {
            return Lists.newArrayList();
        }
        return this.apiStat.qryPlayerValueByElement(element);
    }

    @GetMapping("/qryPlayerValueByTeamId")
    public Map<String, List<PlayerValueData>> qryPlayerValueByTeamId(@RequestParam int teamId) {
        if (teamId <= 0) {
            return Maps.newLinkedHashMap();
        }
        return this.apiStat.qryPlayerValueByTeamId(teamId);
    }

    @GetMapping("/qryTeamSelectByLeagueName")
    public LeagueStatData qryTeamSelectByLeagueName(@RequestParam int event, @RequestParam String leagueName) {
        if (event <= 0 || StringUtils.isEmpty(leagueName)) {
            return new LeagueStatData();
        }
        return this.apiStat.qryTeamSelectByLeagueName(event, leagueName);
    }

}
