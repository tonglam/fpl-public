package com.tong.fpl.domain.data.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/20
 */
@Data
@Accessors(chain = true)
public class PlayerFixtureData {

	private int event;
	private String againstTeam;
	private String AgainstTeamShortName;
	private String kickoffTime;
	private int difficulty;
	private boolean wasHome;
	private boolean started;
	private boolean finished;

}
