package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiLive;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveCalcElementData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.SearchLiveCalcData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.ILiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiLiveImpl implements IApiLive {

    private final IApiQueryService apiQueryService;
    private final ILiveService liveService;

    @Override
    public LiveCalcElementData calcLivePointsByElement(int event, int element) {
        return this.liveService.calcLivePointsByElement(event, element);
    }

    @Override
    public LiveCalcData calcLivePointsByEntry(int event, int entry) {
        return this.liveService.calcLivePointsByEntry(event, entry);
    }

    @Override
    public List<LiveCalcData> calcLivePointsByTournament(int event, int tournamentId) {
        return this.liveService.calcLivePointsByTournament(event, tournamentId);
    }

    @Override
    public SearchLiveCalcData calcSearchLivePointsByTournament(int event, int tournamentId, int element) {
        return this.liveService.calcSearchLivePointsByTournament(event, tournamentId, element);
    }

    @Override
    public List<LiveMatchData> qryLiveMatchByStatus(String playStatus) {
        return this.apiQueryService.qryLiveMatchByStatus(playStatus);
    }

}
