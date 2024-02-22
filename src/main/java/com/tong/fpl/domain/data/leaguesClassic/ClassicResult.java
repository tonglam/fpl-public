package com.tong.fpl.domain.data.leaguesClassic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class ClassicResult {

    private int id;
    @JsonProperty("event_total")
    private int eventTotal;
    @JsonProperty("player_name")
    private String playerName;
    private int rank;
    @JsonProperty("last_rank")
    private int lastRank;
    @JsonProperty("rank_sort")
    private int rankSort;
    private int total;
    private int entry;
    @JsonProperty("entry_name")
    private String entryName;
    @JsonProperty("player_first_name")
    private String playerFirstName;
    @JsonProperty("player_last_name")
    private String playerLastName;

}
