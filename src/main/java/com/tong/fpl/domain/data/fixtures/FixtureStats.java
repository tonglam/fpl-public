package com.tong.fpl.domain.data.fixtures;

import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/7/3
 */
@Data
public class FixtureStats {

	private String identifier;
	private List<FixtureStatsDetail> a;
	private List<FixtureStatsDetail> h;

}
