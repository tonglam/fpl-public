package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/30
 */
@Data
@Accessors(chain = true)
public class TournamentQueryParam {

	private int entry;
	private String name;
	private String creator;
	private int leagueId;
	private String season;
	private String createTime;

}
