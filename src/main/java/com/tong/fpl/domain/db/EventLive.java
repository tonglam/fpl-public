package com.tong.fpl.domain.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Create by tong on 2020/3/12
 */
@Data
@Document(collection = "event_live")
public class EventLive {
    private int element;
    @JsonProperty("element_type")
    private int elementType;
    private int event;
    private int minutes;
    @JsonProperty("goals_scored")
    private int goalsScored;
    private int assists;
    @JsonProperty("clean_sheets")
    private int cleanSheets;
    @JsonProperty("goals_conceded")
    private int goalsConceded;
    @JsonProperty("own_goals")
    private int ownGoals;
    @JsonProperty("penalties_saved")
    private int penaltiesSaved;
    @JsonProperty("penalties_missed")
    private int penaltiesMissed;
    @JsonProperty("yellow_cards")
    private int yellowCards;
    @JsonProperty("red_cards")
    private int redCards;
    private int saves;
    private int bonus;
    private int bps;
    @JsonProperty("total_points")
    private int totalPoints;
}
