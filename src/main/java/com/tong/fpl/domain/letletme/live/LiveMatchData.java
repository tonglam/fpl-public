package com.tong.fpl.domain.letletme.live;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/9/26
 */
@Data
@Accessors(chain = true)
public class LiveMatchData {

	private int matchId;
	private int minutes;
	private int homeTeamId;
	private String homeTeamName;
	private String homeTeamShortName;
	private int homeScore;
	private List<ElementEventResultData> homeTeamDataList;
	private int awayTeamId;
	private String awayTeamName;
	private String awayTeamShortName;
	private int awayScore;
	private List<ElementEventResultData> awayTeamDataList;
	private String kickoffTime;

}
