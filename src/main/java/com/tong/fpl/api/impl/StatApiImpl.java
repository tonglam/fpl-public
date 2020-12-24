package com.tong.fpl.api.impl;

import com.google.common.collect.Lists;
import com.tong.fpl.api.IStatApi;
import com.tong.fpl.constant.enums.FollowAccount;
import com.tong.fpl.domain.letletme.global.DropdownData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IScoutService;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/9/2
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatApiImpl implements IStatApi {

	private final IQueryService queryService;
	private final ITableQueryService tableQueryService;
	private final IScoutService scoutService;

	/**
	 * @implNote price
	 */
	@Override
	public TableData<PlayerValueData> qryPriceChangeList() {
		return this.tableQueryService.qryPriceChangeList();
	}

	/**
	 * @implNote compare
	 */
	@Override
	public TableData<PlayerInfoData> qryPlayerList(String season) {
		return this.tableQueryService.qryPlayerList(season);
	}

	/**
	 * @implNote selected
	 */
	@Override
	public List<String> qryTeamSelectStatList() {
		return this.queryService.qryTeamSelectStatList();
	}

	@Override
	public TableData<LeagueStatData> qryTeamSelectStatByName(int event, String leagueName) {
		return this.tableQueryService.qryTeamSelectStatByName(event, leagueName);
	}

	/**
	 * @implNote scout
	 */
	@Override
	public TableData<PlayerShowData> qryScoutPlayerList(int elementType) {
		return this.tableQueryService.qryPlayerShowListByElementType(elementType);
	}

	@Override
	public void upsertEventScout(ScoutData scoutData) throws Exception {
		this.scoutService.upsertEventScout(scoutData);
	}

	@Override
	public TableData<ScoutData> qryEventScoutPickList(int event) {
		return this.tableQueryService.qryEventScoutPickList(event);
	}

	@Override
	public ScoutData qryScoutEntryEventData(int event, int entry) {
		return this.queryService.qryScoutEntryEventData(event, entry);
	}

	@Override
	public TableData<ScoutData> qryEventScoutList(int event) {
		return this.tableQueryService.qryEventScoutList(event);
	}

	@Override
	public List<DropdownData> getScoutEvent() {
		List<DropdownData> list = Lists.newArrayList();
		list.add(
				new DropdownData()
						.setTxt("赛季")
						.setEvent("0")
		);
		int currentGw = this.queryService.getCurrentEvent();
		IntStream.rangeClosed(4, currentGw).forEachOrdered(event -> {
			String gw = String.valueOf(event);
			list.add(
					new DropdownData()
							.setTxt("GW" + gw)
							.setEvent(gw)
			);
		});
		return list;
	}

	@Override
	public String getScoutDeadline(int event) {
		return this.queryService.getScoutDeadlineByEvent(event);
	}

	@Override
	public TableData<PlayerShowData> qryOffiaccountPlayerShowList(int event) {
		return this.tableQueryService.qryEntryEventPlayerShowList(event, FollowAccount.getFollowAccountEntry("Offiaccount", CommonUtils.getCurrentSeason()));
	}

	@Override
	public PlayerPickData qryOffiaccountPickList() {
		return this.queryService.qryEntryPickData(FollowAccount.getFollowAccountEntry("Offiaccount", CommonUtils.getCurrentSeason()));
	}

}
