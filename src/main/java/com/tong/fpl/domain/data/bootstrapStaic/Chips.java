package com.tong.fpl.domain.data.bootstrapStaic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/1/20
 */
@Data
public class Chips {

	@JsonProperty("chip_name")
	private String chipName;
	@JsonProperty("num_played")
	private int numPlayed;

}
