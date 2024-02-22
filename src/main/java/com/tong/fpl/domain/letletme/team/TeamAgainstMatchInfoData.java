package com.tong.fpl.domain.letletme.team;

import com.tong.fpl.domain.letletme.element.ElementSummaryData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/10/14
 */
@Data
@Accessors(chain = true)
public class TeamAgainstMatchInfoData {

    private String season;
    private int event;
    private int teamHId;
    private int teamHCode;
    private String teamHName;
    private String teamHShortName;
    private int teamHScore;
    private int teamAId;
    private int teamACode;
    private String teamAName;
    private String teamAShortName;
    private int teamAScore;
    private String kickoffDate;
    private List<ElementSummaryData> elementSummaryList;

}
