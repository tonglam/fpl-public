package com.tong.fpl.api.impl;

import com.google.common.collect.Lists;
import com.tong.fpl.api.IGroupApi;
import com.tong.fpl.constant.enums.FollowAccount;
import com.tong.fpl.domain.letletme.entry.EntryEventLineupData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.DropdownData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IGroupService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.IUpdateEventService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Create by tong on 2021/1/8
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroupApiImpl implements IGroupApi {

	private final IQueryService queryService;
	private final ITableQueryService tableQueryService;
	private final IGroupService scoutService;
	private final IUpdateEventService updateEventService;

	/**
	 * @implNote scout
	 */
	@Override
	public TableData<PlayerShowData> qryScoutPlayerList(int elementType) {
		return this.tableQueryService.qryPlayerShowListByElementType(elementType);
	}

	@Override
	public void upsertEventScout(ScoutData scoutData) {
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
		int currentGw = this.getCurrentEvent();
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

	/**
	 * @implNote transfers
	 */
	@Override
	public TableData<PlayerShowData> qryEntryEventPlayerShowListForTransfers(int event) {
		return this.tableQueryService.qryEntryEventPlayerShowListForTransfers(event, FollowAccount.getFollowAccountEntry("Offiaccount", CommonUtils.getCurrentSeason()));
	}

	@Override
	public TableData<PlayerShowData> qryPlayerShowListByElementForTransfers(List<EntryPickData> pickList) {
		return this.tableQueryService.qryPlayerShowListByElementForTransfers(pickList);
	}

	@Override
	public PlayerPickData qryOffiaccountPickListForTransfers() {
		return this.queryService.qryEntryPickDataForTransfers(this.getCurrentEvent(), FollowAccount.getFollowAccountEntry("Offiaccount", CommonUtils.getCurrentSeason()));
	}

	@Override
	public List<PlayerPickData> qryOffiaccountLineupForTransfers() {
		return this.queryService.qryOffiaccountLineupForTransfers();
	}

	@Override
	public void upsertEventLineup(EntryEventLineupData entryEventLineupData) {
		this.updateEventService.upsertEntryEventLineup(entryEventLineupData);
	}

	/**
	 * @implNote common
	 */
	@Override
	public int getCurrentEvent() {
		return this.queryService.getCurrentEvent();
	}

	@Override
	public int getNextEvent() {
		return this.queryService.getNextEvent();
	}

	@Override
	public TableData<PlayerDetailData> qryPlayerDetailData(int element) {
		return this.tableQueryService.qryPlayerDetailData(element);
	}

}
