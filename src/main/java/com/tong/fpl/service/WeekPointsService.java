package com.tong.fpl.service;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.GwEntry;
import com.tong.fpl.domain.data.entry.Standings;
import com.tong.fpl.domain.data.leaguesClassic.ClassicResult;
import com.tong.fpl.domain.response.LeagueClassicRes;
import com.tong.fpl.domain.response.UserPicksRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/3/10
 */
@Service
@Slf4j
public class WeekPointsService {

    private final InterfaceService interfaceService;

    public WeekPointsService(InterfaceService interfaceService) {
        this.interfaceService = interfaceService;
    }

    public List<GwEntry> calcWeekPoints(int classicId, int event) {
        List<GwEntry> list = this.getClassicPoints(event, classicId);
        for (GwEntry gwEntry : list
        ) {
            this.setPicksInfo(gwEntry, event);
        }
        return list;
    }

    private void setPicksInfo(GwEntry gwEntry, int event) {
        UserPicksRes userPicksRes = this.interfaceService.getUserPicks(gwEntry.getEntry(), event, Constant.PL_PROFILE);
        if (userPicksRes == null) {
            return;
        }
        gwEntry.setActiveChips(userPicksRes.getActiveChip());
        gwEntry.setEventCost(userPicksRes.getEntryHistory().getEventTransfersCost());
        gwEntry.setNetPoint(gwEntry.getGwPoint() - gwEntry.getEventCost());
        gwEntry.setOverallRank(userPicksRes.getEntryHistory().getOverallRank());
    }

    private List<GwEntry> getClassicPoints(int event, int classicId) {
        List<GwEntry> list = Lists.newArrayList();
        this.setOnePageClassic(list, event, classicId, 1);
        return list;
    }

    private void setOnePageClassic(List<GwEntry> list, int event, int classicId, int page) {
        LeagueClassicRes leagueClassicRes = this.interfaceService.getLeaguesClassic(classicId, Constant.PL_PROFILE, page);
        if (leagueClassicRes == null) {
            return;
        }
        Standings standings = leagueClassicRes.getStandings();
        for (ClassicResult result : standings.getResults()) {
            GwEntry gwEntry = new GwEntry();
            gwEntry.setEvent(event);
            gwEntry.setEntry(result.getEntry());
            gwEntry.setEntryName(result.getEntryName());
            gwEntry.setPlayerName(result.getPlayerName());
            gwEntry.setGwPoint(result.getEventTotal());
            gwEntry.setTotalPoints(result.getTotal());
            list.add(gwEntry);
        }
        if (standings.isHasNext()) {
            page++;
            setOnePageClassic(list, event, classicId, page);
        }
    }

}
