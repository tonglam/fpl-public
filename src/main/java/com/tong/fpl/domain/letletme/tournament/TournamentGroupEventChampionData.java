package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/10/27
 */
@Data
@Accessors(chain = true)
public class TournamentGroupEventChampionData {

	private List<TournamentPointsGroupEventResultData> eventResultList;
	private List<TournamentGroupChampionCountData> championCountList;

}
