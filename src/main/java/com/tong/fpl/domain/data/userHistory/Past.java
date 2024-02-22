package com.tong.fpl.domain.data.userHistory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class Past {

	@JsonProperty("season_name")
	private String seasonName;
	@JsonProperty("total_points")
	private int totalPoints;
	private int rank;

}
