package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/15
 */
@Data
@Accessors(chain = true)
public class LiveFixtureData {

	private int teamId;
	private String teamName;
	private int againstId;
	private String againstName;
	private boolean wasHome;
	private boolean started;
	private boolean finished;

}
