package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;

/**
 * Create by tong on 2021/2/26
 */
public interface ICommonApi {

	int getNextEvent();

	String getUtcDeadlineByEvent(int event);

	EntryInfoData qryEntryInfoData(int entry);

}
