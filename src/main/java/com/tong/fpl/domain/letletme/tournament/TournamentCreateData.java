package com.tong.fpl.domain.letletme.tournament;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * Create by tong on 2020/6/23
 */

@Data
@Accessors(chain = true)
public class TournamentCreateData {

    private String url;
    @NotBlank
    private String tournamentName;
    private String creator;
    private int adminerEntry;
    @JsonIgnore
    private String leagueType;
    @JsonIgnore
    private int leagueId;
    private int totalTeam;
    // group params
    @NotBlank
    private String groupMode;
    private int groupStartGw;
    private int groupEndGw;
	private int teamPerGroup;
	private int groupNum;
    private int groupQualifiers;
    private boolean groupFillAverage;
    // knockout params
    @NotBlank
    private String knockoutMode;
    private int knockoutTeam;
    private int knockoutRounds;
    private int knockoutEvents;
    private int knockoutStartGw;
    private int knockoutEndGw;

}
