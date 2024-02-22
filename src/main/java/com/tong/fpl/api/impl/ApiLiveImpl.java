package com.tong.fpl.api.impl;

import com.google.common.collect.Lists;
import com.tong.fpl.api.IApiLive;
import com.tong.fpl.domain.letletme.live.*;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.ILiveService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
    public List<LiveCalcData> calcLivePointsByEntries(int event, String entries) {
        if (StringUtils.isBlank(entries)) {
            return Lists.newArrayList();
        }
        List<Integer> entryList = Lists.newArrayList();
        try {
            Arrays.stream(StringUtils.split(entries, ",")).forEach(entry -> entryList.add(Integer.parseInt(entry)));
        } catch (Exception e) {
            return Lists.newArrayList();
        }
        return this.liveService.calcLivePointsByEntryList(event, entryList);
    }

    @Override
    public LiveCalcData calcLivePointsByElementList(LiveCalcParamData liveCalcParamData) {
        return this.liveService.calcLivePointsByElementList(liveCalcParamData);
    }

    @Override
    public LiveTournamentCalcData calcLivePointsByTournament(int event, int tournamentId) {
        return this.liveService.calcLivePointsByTournament(event, tournamentId);
    }

    @Override
    public SearchLiveTournamentCalcData calcSearchLivePointsByTournament(LiveCalcSearchParamData liveCalcSearchParamData) {
        return this.liveService.calcSearchLivePointsByTournament(liveCalcSearchParamData);
    }

    @Override
    public List<LiveKnockoutResultData> calcLivePointsByKnockout(int event, int tournamentId) {
        return this.liveService.calcLivePointsByKnockout(event, tournamentId);
    }

    @Override
    public List<LiveCalcData> calcLiveGwPointsByChampionLeague(int event, int tournamentId, String stage) {
        return this.liveService.calcLiveGwPointsByChampionLeague(event, tournamentId, stage);
    }

    @Override
    public List<LiveCalcData> calcLiveTotalPointsByChampionLeague(int event, int tournamentId, String stage) {
        return this.liveService.calcLiveTotalPointsByChampionLeague(event, tournamentId, stage);
    }

    @Override
    public List<LiveMatchData> qryLiveMatchByStatus(String playStatus) {
        return this.apiQueryService.qryLiveMatchByStatus(playStatus);
    }

}
