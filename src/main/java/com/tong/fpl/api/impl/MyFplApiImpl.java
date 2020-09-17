package com.tong.fpl.api.impl;

import cn.hutool.core.bean.BeanUtil;
import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
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
    private final ITableQueryService tableQueryService;

    @Override
    public TableData<PlayerInfoData> qryPlayerDataList(long page, long limit) {
        return this.tableQueryService.qryPagePlayerDataList(page, limit);
    }

    @Override
    public EntryInfoData qryEntryInfo(int entry) {
        if (entry <= 0) {
            return new EntryInfoData();
        }
        EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(entry);
        if (entryInfoEntity == null) {
            return new EntryInfoData();
        }
        return BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class);
    }

    @Override
    public TableData<EntryEventResultData> qryEntryResultList(int entry) {
        return this.tableQueryService.qryEntryResultList(entry);
    }

    @Override
    public TableData<EntryPickData> qryEntryEventResult(int event, int entry) {
        return this.tableQueryService.qryEntryEventResult(event, entry);
    }

    @Override
    public TableData<ElementEventResultData> qryElementEventResult(int event, int element) {
        return this.tableQueryService.qryElementEventResult(event, element);
    }

    @Override
    public TableData<TournamentInfoData> qryEntryPointsGroupTournamentList(int entry) {
        return this.tableQueryService.qryEntryPointsGroupTournamentList(entry);
    }

    @Override
    public TableData<TournamentGroupData> qryTournamentResultList(int tournamentId, int event) {
        return this.tableQueryService.qryTournamentResultList(tournamentId, event);
    }

}
