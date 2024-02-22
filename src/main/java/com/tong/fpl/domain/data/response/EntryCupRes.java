package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.entry.CupStatus;
import com.tong.fpl.domain.data.entry.Match;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/1/21
 */
@Data
public class EntryCupRes {

	@JsonProperty(value = "cup_matches")
	private List<Match> cupMatches;
	@JsonProperty(value = "cup_status")
	private CupStatus cupStatus;

}
