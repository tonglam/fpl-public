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
    private int adminerEntry;
    private String creator;
    private int totalTeam;
    private String pointsGroupStartGw;
    private String pointsGroupEndGw;
    private String battleGroupStartGw;
    private String battleGroupEndGw;
    private String pkStartGw;
    private String pkEndGw;
    private List<ZjTournamentGroupData> groupDataList;

}
