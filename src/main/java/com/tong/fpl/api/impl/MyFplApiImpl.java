package com.tong.fpl.api.impl;

import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.data.letletme.PlayerData;
import com.tong.fpl.service.IQuerySerivce;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/8/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplApiImpl implements IMyFplApi {

    private final IQuerySerivce querySerivce;

    @Override
    public List<PlayerData> qryPlayerDataList(long current, long size) {
        return this.querySerivce.qryPlayerDataList(current, size);
    }

}
