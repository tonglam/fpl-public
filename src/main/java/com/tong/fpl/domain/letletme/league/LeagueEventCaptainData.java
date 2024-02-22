package com.tong.fpl.domain.letletme.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/11/10
 */
@Data
@Accessors(chain = true)
public class LeagueEventCaptainData {

	private int totalPoints;
	private String totalPointsByPercent;
	private int maxPointsEvent;
	private int maxPoints;
	private String maxPointsWebName;
	private int minPointsEvent;
	private int minPoints;
	private String minPointsWebName;
	private int mostSelected;
	private String mostSelectedWebName;
	private int mostSelectedTimes;
	private int blankTimes;
	private int hitTimes;

}
