package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/24
 */
@Data
@Accessors(chain = true)
public class PlayerShowFixtureData {

	private int event;
	private String againstTeamShortName;
	private int difficulty;
	private String wasHome;
	private boolean bgw;
	private boolean dgw;

}
