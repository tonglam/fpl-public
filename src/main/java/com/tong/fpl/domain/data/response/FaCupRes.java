package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/3/9
 */
@Data
public class FaCupRes {
    private int id;
    @JsonProperty("entry_ids")
    private List<Integer> entryIds;
    @JsonProperty("entry_names")
    private List<String> entryNames;
    @JsonProperty("player_names")
    private List<String> playerNames;
}
