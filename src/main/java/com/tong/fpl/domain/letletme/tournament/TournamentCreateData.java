package com.tong.fpl.domain.letletme.tournament;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * Create by tong on 2020/6/23
 */
@Validated
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
    private String groupStartGw;
    private String groupEndGw;
    private int teamsPerGroup;
    private int groupQualifiers;
    private boolean groupFillAverage;
    // knockout params
    @NotBlank
    private String knockoutMode;
    private String knockoutStartGw;
    private String knockoutEndGw;

}
