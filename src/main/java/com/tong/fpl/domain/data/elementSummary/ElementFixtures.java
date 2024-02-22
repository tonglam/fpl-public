package com.tong.fpl.domain.data.elementSummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/7/7
 */
@Data
public class ElementFixtures {

	private int id;
	private int code;
	@JsonProperty("team_h")
	private int teamH;
	@JsonProperty("team_h_score")
	private int teamHScore;
	@JsonProperty("team_a")
	private int teamA;
	@JsonProperty("team_a_score")
	private int teamAScore;
	private int event;
	private boolean finished;
	private int minutes;
	@JsonProperty("provisional_start_time")
	private boolean provisionalStartTime;
	@JsonProperty("kickoff_time")
	private String kickoffTime;
	@JsonProperty("event_name")
	private String eventName;
	private boolean isHome;
	private int difficulty;

}
