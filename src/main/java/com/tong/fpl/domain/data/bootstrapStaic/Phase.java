package com.tong.fpl.domain.data.bootstrapStaic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/1/20
 */
@Data
public class Phase {

	private int id;
	private String name;
	@JsonProperty("start_event")
	private int startEvent;
	@JsonProperty("stop_event")
	private int stopEvent;

}
