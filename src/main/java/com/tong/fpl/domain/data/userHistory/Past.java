package com.tong.fpl.domain.data.userHistory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class Past {
    @JsonProperty("seasonName")
    private String season_name;
    @JsonProperty("totalPoints")
    private int total_points;
    private int rank;
}
