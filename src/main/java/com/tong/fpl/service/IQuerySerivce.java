package com.tong.fpl.service;

import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.web.PlayerValueData;

import java.util.List;

/**
 * Create by tong on 2020/7/31
 */
public interface IQuerySerivce {

	List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

	EntryEventResultEntity qryEntryEvent(int event, int entry);

}
