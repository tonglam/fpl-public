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
	private String gkpName;
	private double gkpPrice;
	private int gkpPoints;
	private int def;
	private String defName;
	private double defPrice;
	private int defPoints;
	private int mid;
	private String midName;
	private double midPrice;
	private int midPoints;
	private int fwd;
	private String fwdName;
	private double fwdPrice;
	private int fwdPoints;
	private int captain;
	private String captainName;
	private int captainPoints;
	private String reason;
	private int eventPoints;
	private int totalPoints;

}
