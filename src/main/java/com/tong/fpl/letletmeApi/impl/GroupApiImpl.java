package com.tong.fpl.letletmeApi.impl;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.enums.FollowAccount;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulatePickData;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulateTransfersData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.DropdownData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.letletmeApi.IGroupApi;
import com.tong.fpl.service.IGroupService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.ITableQueryService;
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
    private final IGroupService groupService;

    /**
     * @implNote scout
     */
    @Override
    public TableData<PlayerShowData> qryScoutPlayerList(int elementType) {
        return this.tableQueryService.qryPlayerShowListByElementType(elementType);
    }

    @Override
    public void upsertEventScout(ScoutData scoutData) {
        this.groupService.upsertEventScout(scoutData);
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
     * @implNote pick
     */
    @Override
    public PlayerPickData qryOffiaccountPickData(int operator) {
        return this.queryService.qryEntryEventPickData(this.getNextEvent(), FollowAccount.getFollowAccountEntry("Offiaccount"), operator);
    }

    @Override
    public List<PlayerPickData> qryOffiaccountPickList() {
        return this.queryService.qryOffiaccountPickList();
    }

    @Override
    public TableData<PlayerShowData> qryOffiaccountEventPlayerShowList(int event, int operator) {
        return this.tableQueryService.qryEntryEventPlayerShowList(event, FollowAccount.getFollowAccountEntry("Offiaccount"), operator);
    }

    @Override
    public TableData<PlayerShowData> qrySortedEntryEventPlayerShowList(List<PlayerShowData> playerShowDataList) {
        return this.tableQueryService.qrySortedEntryEventPlayerShowList(playerShowDataList);
    }

    @Override
    public void upsertEventPick(EntryEventSimulatePickData entryEventSimulatePickData) {
        this.groupService.upsertEventScoutSimulatePick(entryEventSimulatePickData);
    }

    /**
     * @implNote transfers
     */
    @Override
    public TableData<PlayerShowData> qryEntryEventPlayerShowListForTransfers(int event) {
        return this.tableQueryService.qryEntryEventPlayerShowListForTransfers(event, FollowAccount.getFollowAccountEntry("Offiaccount"));
    }

    @Override
    public TableData<PlayerShowData> qryPlayerShowListByElementForTransfers(List<EntryPickData> pickList) {
        return this.tableQueryService.qryPlayerShowListByElementForTransfers(pickList);
    }

    @Override
    public PlayerPickData qryOffiaccountPickListForTransfers() {
        return this.queryService.qryEntryPickDataForTransfers(this.getCurrentEvent(), FollowAccount.getFollowAccountEntry("Offiaccount"));
    }

    @Override
    public List<PlayerPickData> qryOffiaccountLineupForTransfers() {
        return this.queryService.qryOffiaccountLineupForTransfers();
    }

    @Override
    public void upsertEventTransfers(EntryEventSimulateTransfersData entryEventSimulateTransfersData) {
        this.groupService.upsertEventScoutSimulateTransfers(entryEventSimulateTransfersData);
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
