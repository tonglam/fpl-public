package com.tong.fpl.service;

import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;

import java.util.List;

/**
 * Create by tong on 2020/7/31
 */
public interface IQuerySerivce {

	List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

	EntryEventData qryEntryEvent(int event, int entry);

}
