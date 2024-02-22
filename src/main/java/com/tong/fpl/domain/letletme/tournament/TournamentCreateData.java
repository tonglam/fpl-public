package com.tong.fpl.domain.letletme.tournament;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/6/23
 */

@Data
@Accessors(chain = true)
public class TournamentCreateData {

	private String url;
	private String tournamentName;
	private String creator;
	private int adminerEntry;
	private String leagueType;
	@JsonIgnore
	private int leagueId;
	private int totalTeam;
	// input entry list
	private List<Integer> inputEntryList = Lists.newArrayList();
	// group params
	private String groupMode;
	private int groupStartGw;
	private int groupEndGw;
	private int teamPerGroup;
	private int groupNum;
	private int groupQualifiers;
	private boolean groupFillAverage;
	private Map<String, List<Integer>> groupDrawMap;
	// knockout params
	private String knockoutMode;
	private int knockoutTeam;
	private int knockoutRounds;
	private int knockoutEvents;
	private int knockoutStartGw;
	private int knockoutEndGw;

}
