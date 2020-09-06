package com.tong.fpl.api.impl;

import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.service.ITableQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/9/2
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatApiImpl implements IStatApi {

    private final ITableQueryService tableQueryService;

    @Override
    public TableData<EntryInfoData> qryEntryInfoByTournament(String season, int tournamentId, long page, long limit) {
        return this.tableQueryService.qryPageEntryInfoByTournament(season, tournamentId, page, limit);
    }

    @Override
    public TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry, long page, long limit) {
        return this.tableQueryService.qryEntryCaptainList(season, entry, page, limit);
    }

    @Override
    public TableData<PlayerInfoData> qryPagePlayerList(String season) {
        return this.tableQueryService.qryPagePlayerList(season);
    }

}
