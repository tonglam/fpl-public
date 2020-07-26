package com.tong.fpl.api.impl;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.domain.web.PlayerValueData;
import com.tong.fpl.service.ILiveCalcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/7/20
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpApiImpl implements IHttpApi {

    private final ILiveCalcService liveCalcService;

    @Override
    public List<PlayerValueData> qryDayChangePlayerValue(String changeDate) {
        return this.liveCalcService.qryDayChangePlayerValue(changeDate);
    }

}
