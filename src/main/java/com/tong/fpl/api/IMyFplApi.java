package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

    /**
     * @apiNote entry
     */
    EntryInfoData qryEntryInfo(int entry);

    TableData<EntryEventResultData> qryEntryResultList(int entry);

    TableData<EntryPickData> qryEntryEventResult(int event, int entry);

    /**
     * @apiNote pick
     */
    TableData<PlayerInfoData> qryPlayerDataList(long page, long limit);

    /**
     * @apiNote league
     */
    TableData<TournamentInfoData> qryEntryPointsGroupTournamentList(int entry);

    TableData<TournamentGroupData> qryTournamentResultList(int tournamentId, int event);

}
