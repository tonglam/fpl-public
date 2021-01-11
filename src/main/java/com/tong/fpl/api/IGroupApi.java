package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.DropdownData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.scout.ScoutData;

import java.util.List;

/**
 * Create by tong on 2020/9/2
 */
public interface IGroupApi {

    /**
     * @apiNote scout
     */
    TableData<PlayerShowData> qryScoutPlayerList(int elementType);

    void upsertEventScout(ScoutData scoutData) throws Exception;

    TableData<ScoutData> qryEventScoutPickList(int event);

    TableData<ScoutData> qryEventScoutList(int event);

    List<DropdownData> getScoutEvent();

    String getScoutDeadline(int event);

    /**
     * @apiNote transfers
     */
    ScoutData qryScoutEntryEventData(int event, int entry);

    TableData<PlayerShowData> qryOffiaccountPlayerShowList(int event);

    TableData<PlayerShowData> qryPlayerShowListByElement(List<EntryPickData> pickList);

    PlayerPickData qryOffiaccountPickList();

    /**
     * @apiNote common
     */
    int getCurrentEvent();

    int getNextEvent();

    TableData<PlayerDetailData> qryPlayerDetailData(int element);

}
