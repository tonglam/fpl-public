package com.tong.fpl.service;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.data.GwEntry;
import com.tong.fpl.data.entry.Standings;
import com.tong.fpl.data.response.LeagueClassicRes;
import com.tong.fpl.data.response.UserPicksRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/3/10
 */
@Slf4j
@Service
public class WeekPointsService {

    private final InterfaceService interfaceService;

    public WeekPointsService(InterfaceService interfaceService) {
        this.interfaceService = interfaceService;
    }

    public List<GwEntry> calcWeekPoints(int classicId, int event) {
        List<GwEntry> classcList = this.getClassicPoints(event, classicId);
        return classcList.stream()
                .peek(o -> this.setPicksInfo(o, event))
                .collect(Collectors.toList());
    }

    private void setPicksInfo(GwEntry gwEntry, int event) {
        Optional<UserPicksRes> userPicksRes = this.interfaceService.getUserPicks(gwEntry.getEntry(), event, Constant.PL_PROFILE);
        userPicksRes.ifPresent(o -> {
            gwEntry.setActiveChips(userPicksRes.get().getActiveChip());
            gwEntry.setEventCost(userPicksRes.get().getEntryHistory().getEventTransfersCost());
            gwEntry.setNetPoint(gwEntry.getGwPoint() - gwEntry.getEventCost());
            gwEntry.setOverallRank(userPicksRes.get().getEntryHistory().getOverallRank());
        });
    }

    private List<GwEntry> getClassicPoints(int event, int classicId) {
        List<GwEntry> list = Lists.newArrayList();
        this.setOnePageClassic(list, event, classicId, 1);
        return list;
    }

    private void setOnePageClassic(List<GwEntry> list, int event, int classicId, int page) {
        Optional<LeagueClassicRes> leagueClassicRes = this.interfaceService.getLeaguesClassic(classicId, Constant.PL_PROFILE, page);
        if (!leagueClassicRes.isPresent()) {
            return;
        }
        Standings standings = leagueClassicRes.get().getStandings();
        standings.getResults().forEach(result -> {
            GwEntry gwEntry = new GwEntry();
            gwEntry.setEvent(event);
            gwEntry.setEntry(result.getEntry());
            gwEntry.setEntryName(result.getEntryName());
            gwEntry.setPlayerName(result.getPlayerName());
            gwEntry.setGwPoint(result.getEventTotal());
            gwEntry.setTotalPoints(result.getTotal());
            list.add(gwEntry);
        });
        if (standings.isHasNext()) {
            page++;
            setOnePageClassic(list, event, classicId, page);
        }
    }

}
