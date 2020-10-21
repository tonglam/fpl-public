package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/10/21
 */
@Data
@Accessors(chain = true)
@TableName(value = "zj_tournament_result")
public class ZjTournamentResultEntity {

	private int tournamentId;
	private int groupId;
	private String groupName;
	private int phaseOneTotalPoints;
	private int phaseOneGroupPoints;
	private int phaseOneTotalGroupPoints;
	private int phaseTwoTotalPoints;
	private int phaseTwoGroupPoints;
	private int phaseTwoTotalGroupPoints;
	private int pkTotalPoints;
	private int pkGroupPoints;
	private int pkTotalGroupPoints;
	private int tournamentTotalPoints;
	private int tournamentTotalGroupPoints;
	private int tournamentRank;

}
