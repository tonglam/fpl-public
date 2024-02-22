package com.tong.fpl.domain.letletme.team;

import com.tong.fpl.domain.letletme.global.MapData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/10/11
 */
@Data
@Accessors(chain = true)
public class TeamAgainstRecordData {

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
    private List<MapData<Integer>> goalScored;
    private List<MapData<Integer>> assists;
    private List<MapData<Integer>> ownGoals;
    private List<MapData<Integer>> penaltiesSaved;
    private List<MapData<Integer>> penaltiesMissed;
    private List<MapData<Integer>> redCards;

}
