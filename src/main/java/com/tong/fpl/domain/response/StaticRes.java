package com.tong.fpl.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.bootstrapStaic.*;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/1/20
 */
@Data
public class StaticRes {
    private List<Events> events;
    @JsonProperty("game_settings")
    private GameSettings gameSettings;
    private List<Phases> phases;
    private List<Teams> teams;
    @JsonProperty("total_players")
    private int totalPlayers;
    @JsonProperty("players")
    private List<Player> players;
    @JsonProperty("element_stats")
    private List<ElementStats> elementStats;
    @JsonProperty("element_types")
    private List<ElementTypes> elementTypes;
}
