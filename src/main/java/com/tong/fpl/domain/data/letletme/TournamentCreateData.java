package com.tong.fpl.domain.data.letletme;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Data
@Accessors(chain = true)
public class TournamentCreateData {

	@Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*/standings/[c|h]]$")
	private String url;
	@NotBlank
	private String tournamentName;
	@NotBlank
	private String creator;
	// group params
	@NotBlank
	private String groupMode;
	private String groupStartGw;
	private String groupEndGw;
	private int teamsPerGroup;
	private boolean groupFillAverage;
	private int groupQualifiers;
	// knockout params
	@NotBlank
	private String knockoutMode;
	//	private int knockoutRounds;
	@NotBlank
	private String knockoutStartGw;

}
