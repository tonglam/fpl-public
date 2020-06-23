package com.tong.fpl.data;

import lombok.Data;

/**
 * Create by tong on 2020/6/23
 */
@Data
public class CupCreateData {

	private String url;
	private String cupName;
	private String creator;
	private String startGw;
	private String endGw;
	private int teamsPerGroup;
	private int qualifiers;
	private boolean fillAverage;

}
