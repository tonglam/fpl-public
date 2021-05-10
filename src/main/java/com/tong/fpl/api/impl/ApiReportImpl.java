package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiReport;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiReportImpl implements IApiReport {

	private final IQueryService queryService;

}
