package com.tong.fpl.domain.letletme.scout;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/7
 */
@Data
@Accessors(chain = true)
public class ScoutData {

	private int id;
	private int event;
	private int entry;
	private String scoutName;
	private int gkp;
	private int gkpPoints;
	private int def;
	private int defPoints;
	private int mid;
	private int midPoints;
	private int fwd;
	private int fwdPoints;
	private int captain;
	private int captainPoints;
	private String reason;
	private int eventPoints;
	private int totalPoints;

}
