package com.tong.fpl.domain.letletme.entry;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/8/3
 */
@Data
@Accessors(chain = true)
public class EntryEventResultData {

    private int event;
    private String entryName;
    private String playerName;
    private int entry;
    private int points;
    private int netPoints;
    private int transfersCost;
    private int benchPoints;
    private int transfers;
    private int rank;
    private String chip;
    private Integer playedCaptain;
    private String captainName;
    private Integer captainPoints;
    private double value;
    private double bank;
    private double teamValue;
    private int overallPoints;
    private int overallRank;
    private int eventTournamentRank;
    private int tournamentRank;
    private List<EntryPickData> picks;
    private List<ElementEventResultData> pickList;

}
