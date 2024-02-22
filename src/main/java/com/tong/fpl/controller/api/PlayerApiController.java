package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiPlayer;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFilterData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/qryPlayerInfoByElement")
    public PlayerInfoData qryPlayerInfoByElement(@RequestParam String season, @RequestParam int element) {
        return this.apiPlayer.qryPlayerInfoByElement(season, element);
    }

    @GetMapping("/qryPlayerInfoByCode")
    public PlayerInfoData qryPlayerInfoByCode(@RequestParam String season, @RequestParam int code) {
        return this.apiPlayer.qryPlayerInfoByCode(season, code);
    }

    @GetMapping("/qryPlayerInfoByElementType")
    public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(@RequestParam int elementType) {
        return this.apiPlayer.qryPlayerInfoByElementType(elementType);
    }

    @GetMapping("/qryPlayerDetailByElement")
    public PlayerDetailData qryPlayerDetailByElement(@RequestParam int element) {
        return this.apiPlayer.qryPlayerDetailByElement(element);
    }

    @GetMapping("/qryTeamFixtureByShortName")
    public Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(@RequestParam String shortName) {
        return this.apiPlayer.qryTeamFixtureByShortName(shortName);
    }

    @GetMapping("/qryFilterPlayers")
    public List<PlayerFilterData> qryFilterPlayers(@RequestParam String season) {
        return this.apiPlayer.qryFilterPlayers(season);
    }

    @GetMapping("/refreshPlayerStat")
    public void refreshPlayerStat() {
        this.apiPlayer.refreshPlayerStat();
    }

}
