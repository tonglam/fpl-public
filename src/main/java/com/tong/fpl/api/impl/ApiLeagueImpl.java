package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiLeague;
import com.tong.fpl.service.IApiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiLeagueImpl implements IApiLeague {

	private final IApiQueryService apiQueryService;

}
