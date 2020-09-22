package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.tong.fpl.constant.enums.LeagueType.Classic;

/**
 * Create by tong on 2020/9/22
 */
@Getter
@AllArgsConstructor
public enum ReportLeague {

	League_4089(Classic.name(), 4089, 0), League_3571(Classic.name(), 3571, 0), League_11316(Classic.name(), 11316, 0),
	League_65(Classic.name(), 65, 0), League_314(Classic.name(), 314, 10000);

	private final String type;
	private final int id;
	private final int limit;

}
