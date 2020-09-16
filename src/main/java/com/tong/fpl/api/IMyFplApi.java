package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

    TableData<PlayerInfoData> qryPlayerDataList(long page, long limit);

    EntryInfoData qryEntryInfo(int entry);

    TableData<EntryEventResultData> qryEntryResultList(int entry);

    TableData<EntryPickData> qryEntryEventResult(int event, int entry);

    TableData<ElementEventResultData> qryElementEventResult(int event, int element);

}
