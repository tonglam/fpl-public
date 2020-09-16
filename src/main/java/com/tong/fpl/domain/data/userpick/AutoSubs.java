package com.tong.fpl.domain.data.userpick;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/3/12
 */
@Data
public class AutoSubs {

	private int entry;
	@JsonProperty("element_in")
	private int elementIn;
	@JsonProperty("element_out")
	private int elementOut;
	private int event;

}
