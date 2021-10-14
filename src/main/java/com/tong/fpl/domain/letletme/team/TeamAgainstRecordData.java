package com.tong.fpl.domain.letletme.team;

import com.tong.fpl.domain.entity.EventLiveEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

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
    private Map<String, Integer> goalScored;
    private Map<String, Integer> assists;
    private Map<String, Integer> ownGoals;
    private Map<String, Integer> penaltiesSaved;
    private Map<String, Integer> penaltiesMissed;
    private Map<String, Integer> redCards;
    private List<EventLiveEntity> eventLiveEntityList; // only use in this application

}
