package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;

/**
 * Create by tong on 2021/5/10
 */
public interface IEntryApi {

	/**
	 * 获取entry信息
	 */
	EntryInfoData qryEntryInfoData(int entry);

}
