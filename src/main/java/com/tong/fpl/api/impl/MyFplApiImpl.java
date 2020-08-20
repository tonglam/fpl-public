package com.tong.fpl.api.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.data.letletme.player.PlayerData;
import com.tong.fpl.service.IQuerySerivce;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplApiImpl implements IMyFplApi {

    private final IQuerySerivce querySerivce;

    @Override
    public Page<PlayerData> qryPlayerDataList(long current, long size) {
        return this.querySerivce.qryPagePlayerDataList(current, size);
    }

}
