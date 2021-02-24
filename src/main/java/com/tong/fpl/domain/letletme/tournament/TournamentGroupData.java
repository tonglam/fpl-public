package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/9/3
 */
@Data
@Accessors(chain = true)
public class TournamentGroupData {

	private int tournamentId;
	private int event;
	private String groupMode;
	private int groupId;
	private String groupName;
	private int groupIndex;
	private int entry;
	private String entryName;
	private String playerName;
	private String region;
	private int startedEvent;
	private int bank;
	private int teamValue;
	private int groupPoints;
	private int groupRank;
	private int play;
	private int win;
	private int draw;
	private int lose;
	private int totalPoints;
	private int totalTransfersCost;
	private int totalNetPoints;
	private boolean qualified;
	private int overallRank;
	private int startGw;
	private int endGw;
	private boolean drawPhaseTwo;
	private List<Integer> discloseList;
	private Map<String, String> tournamentGroupNameMap;
	private boolean pkDraw;
	private int pkEntry;
	private String pkGroupName;
	private String pkEntryName;
	private String pkPlayerName;
	private TournamentPointsGroupEventResultData pointsGroupEventResult;
	private int lastCupEvent;

}
