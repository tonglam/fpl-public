package com.tong.fpl.domain.data.fpl;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Data
public class TournamentCreateData {

	@Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*/standings/[c|h]]$")
	private String url;
	@NotBlank
	private String tournamentName;
	@NotBlank
	private String creator;
	// group configuration
	@NotBlank
	private String groupMode;
	private int groupPlayAgainstNum;
	private String groupStartGw;
	private String groupEndGw;
	private int teamsPerGroup;
	private boolean groupFillAverage;
	private int groupQualifiers;
	// knockout configuration
	@NotBlank
	private String knockoutMode;
	private int knockoutRounds;
	private String knockoutStartGw;
	private String knockoutEndGw;

}
