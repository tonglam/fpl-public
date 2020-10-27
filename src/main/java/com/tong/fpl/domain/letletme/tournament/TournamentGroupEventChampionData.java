package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/10/27
 */
@Data
@Accessors(chain = true)
public class TournamentGroupEventChampionData {

	private List<TournamentPointsGroupEventResultData> eventResultList;
	private Map<Integer, Map<Integer, Integer>> countMap; // key:rank -> value: (key: entry -> value: times)

}
