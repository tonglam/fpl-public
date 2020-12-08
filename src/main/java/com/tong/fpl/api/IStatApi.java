package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.ScoutPlayerData;

import java.util.List;

/**
 * Create by tong on 2020/9/2
 */
public interface IStatApi {

	/**
	 * @apiNote price
	 */
	TableData<PlayerValueData> qryPriceChangeList();

	/**
	 * @apiNote compare
	 */
	TableData<PlayerInfoData> qryPlayerList(String season);

	/**
	 * @apiNote selected
	 */
	List<String> qryTeamSelectStatList();

	TableData<LeagueStatData> qryTeamSelectStatByName(String leagueName, int event);

	/**
	 * @apiNote scout
	 */
	TableData<ScoutPlayerData> qryScoutPlayerList(int elementType);

}
