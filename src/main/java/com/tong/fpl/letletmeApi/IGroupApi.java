package com.tong.fpl.letletmeApi;

import com.tong.fpl.domain.letletme.entry.EntryEventSimulatePickData;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulateTransfersData;
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

	void upsertEventScout(ScoutData scoutData);

	TableData<ScoutData> qryEventScoutPickList(int event);

	TableData<ScoutData> qryEventScoutList(int event);

	List<DropdownData> getScoutEvent();

	String getScoutDeadline(int event);

	/**
	 * @apiNote pick
	 */
	PlayerPickData qryOffiaccountPickData(int operator);

	List<PlayerPickData> qryOffiaccountPickList();

	TableData<PlayerShowData> qryOffiaccountEventPlayerShowList(int event, int operator);

	TableData<PlayerShowData> qrySortedEntryEventPlayerShowList(List<PlayerShowData> playerShowDataList);

	void upsertEventPick(EntryEventSimulatePickData entryEventSimulatePickData);

	/**
	 * @apiNote transfers
	 */
	ScoutData qryScoutEntryEventData(int event, int entry);

	TableData<PlayerShowData> qryEntryEventPlayerShowListForTransfers(int event);

	TableData<PlayerShowData> qryPlayerShowListByElementForTransfers(List<EntryPickData> pickList);

	PlayerPickData qryOffiaccountPickListForTransfers();

	List<PlayerPickData> qryOffiaccountLineupForTransfers();

	void upsertEventTransfers(EntryEventSimulateTransfersData entryEventLineupData);

	/**
	 * @apiNote common
	 */
	int getCurrentEvent();

	int getNextEvent();

	TableData<PlayerDetailData> qryPlayerDetailData(int element);

}
