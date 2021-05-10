package com.tong.fpl.api.impl;

import com.tong.fpl.api.IEntryApi;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.service.IApiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EntryApiImpl implements IEntryApi {

	private final IApiQueryService apiQueryService;

	@Override
	public EntryInfoData qryEntryInfoData(int entry) {
		return this.apiQueryService.qryEntryInfoData(entry);
	}

}
