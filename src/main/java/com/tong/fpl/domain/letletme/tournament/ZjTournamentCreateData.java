package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Create by tong on 2020/9/29
 */
@Validated
@Data
@Accessors(chain = true)
public class ZjTournamentCreateData {

    @NotBlank
    private String tournamentName;
    private int groupNum;
    private int teamPerGroup;
    private int totalTeam;
    private int adminerEntry;
    private String creator;
    private int pointsRound;
    private int pointsStartGw;
    private int pointsEndGw;
    private int battleRound;
    private int battleStartGw;
    private int battleEndGw;
    private int pkRound;
    private int pkStartGw;
    private int pkEndGw;
    private List<ZjTournamentGroupData> groupDataList;

}
