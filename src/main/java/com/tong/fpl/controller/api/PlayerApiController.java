package com.tong.fpl.controller.api;

import com.google.common.collect.Maps;
import com.tong.fpl.api.IApiPlayer;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerApiController {

    private final IApiPlayer apiPlayer;

    @GetMapping("/qryPlayerInfoByElementType")
    public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(@RequestParam int elementType) {
        if (elementType < 0 || elementType > 4) {
            return Maps.newLinkedHashMap();
        }
        return this.apiPlayer.qryPlayerInfoByElementType(elementType);
    }

    @GetMapping("/qryPlayerDetailByElement")
    public PlayerDetailData qryPlayerDetailByElement(@RequestParam int element) {
        if (element <= 0) {
            return new PlayerDetailData();
        }
        return this.apiPlayer.qryPlayerDetailByElement(element);
    }

    @GetMapping("/qryTeamFixtureByShortName")
    public Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(@RequestParam String shortName) {
        if (StringUtils.isEmpty(shortName)) {
            return Maps.newHashMap();
        }
        return this.apiPlayer.qryTeamFixtureByShortName(shortName);
    }

    @GetMapping("/qryPlayerInfo")
    public PlayerInfoData qryPlayerInfo(@RequestParam String season, @RequestParam int element) {
        if (element <= 0) {
            return new PlayerInfoData();
        }
        return this.apiPlayer.qryPlayerInfo(season, element);
    }

    @GetMapping("/qryPlayerSummary")
    public PlayerSummaryData qryPlayerSummary(@RequestParam String season, @RequestParam int element) {
        if (element <= 0) {
            return new PlayerSummaryData();
        }
        return this.apiPlayer.qryPlayerSummary(season, element);
    }

}
