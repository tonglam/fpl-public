package com.tong.fpl.api;

import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;

import java.util.List;

/**
 * Create by tong on 2020/7/20
 */
public interface IHttpApi {

    List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

    EntryEventData qryEntryEvent(int event, int entry);

}
