package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiEntry;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiEntryImpl implements IApiEntry {

	private final IQueryService queryService;

	@Override
	public EntryInfoData qryEntryInfoData(int entry) {
		return this.queryService.qryEntryInfoData(entry);
	}

}
