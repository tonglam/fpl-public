package com.tong.fpl.domain.data.letletme.team;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/27
 */
@Data
@Accessors(chain = true)
public class TeamFixtureData {

	private int teamId;
	private int event;
	private int againstTeamId;
	private String againstTeamName;
	private int difficulty;
	private String kickoffTime;
	private boolean started;
	private boolean finished;
	private boolean wasHome;
	private String score;

}
