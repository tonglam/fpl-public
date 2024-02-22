package com.tong.fpl.domain.data.entry;

import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/1/21
 */
@Data
public class Cup {

	private List<Match> matches;
	private CupStatus status;

}
