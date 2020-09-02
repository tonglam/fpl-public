package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.table.TableData;

/**
 * Create by tong on 2020/9/2
 */
public interface IStatApi {

    TableData<EntryInfoData> qryEntryInfoByTournament(String season, int tournamentId, long page, long limit);

    TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry, long page, long limit);

}
