package com.tong.fpl.data.fpl;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Data
public class TournamentCreateData {

	@Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*")
	private String url;
	@NotBlank
	private String cupName;
	@NotBlank
	private String creator;
	@NotBlank
	private String startGw;
	@NotBlank
	private String endGw;
	@NotNull
	private int teamsPerGroup;
	@NotNull
	private int qualifiers;
	@NotNull
	private boolean fillAverage;
	@NotNull
	private boolean drawKnockoutsNow;
	@NotNull
	private int knockoutRounds;
	@NotNull
	private boolean homeAwayMode;

}
