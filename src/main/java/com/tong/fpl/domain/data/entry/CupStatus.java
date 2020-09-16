package com.tong.fpl.domain.data.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/1/21
 */
@Data
public class CupStatus {

	@JsonProperty("qualification_event")
	private int qualificationEvent;
	@JsonProperty("qualification_numbers")
	private int qualificationNumbers;
	@JsonProperty("qualification_rank")
	private int qualificationRank;
	@JsonProperty("qualification_state")
	private String qualificationState;

}
