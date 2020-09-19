package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IReportService;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotEmpty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/9/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportServiceImpl implements IReportService {

    private final IQuerySerivce querySerivce;
    private final IStaticSerive staticSerive;
    private final PlayerService playerService;
    private final EventLiveService eventLiveService;
    private final EntryEventResultService entryEventResultService;
    private final EntryCaptainStatService entryCaptainStatService;
    private final LeagueResultStatService leagueResultStatService;
    private final TeamSelectStatService teamSelectStatService;

    @Override
    public void insertEntryCaptainStat(int tournamentId) {
        this.entryCaptainStatService.remove(new QueryWrapper<EntryCaptainStatEntity>().eq("1", 1));
        List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
        List<EntryCaptainStatEntity> entryCaptainStatList = Lists.newArrayList();
        entryList.forEach(entry -> this.initEntryEventCaptainStat(entry, entryCaptainStatList));
        // insert
        this.entryCaptainStatService.saveBatch(entryCaptainStatList);
    }

    private void initEntryEventCaptainStat(int entry, List<EntryCaptainStatEntity> entryCaptainStatList) {
        // entry_info
        EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(entry);
        if (entryInfoEntity == null) {
            return;
        }
        // entry_event_result
        List<EntryEventResultEntity> entryEventResultList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry)
                .gt(EntryEventResultEntity::getEventPoints, 0));
        if (CollectionUtils.isEmpty(entryEventResultList)) {
            return;
        }
        entryEventResultList.forEach(entryEventResult -> {
            List<EntryPickData> captainPickList = this.querySerivce.qryPickListFromPicks(entryEventResult.getEventPicks())
                    .stream()
                    .filter(o -> o.isCaptain() || o.isViceCaptain())
                    .collect(Collectors.toList());
            EntryPickData captainPick = this.getRealCaptainPoints(captainPickList);
            if (captainPick == null) {
                return;
            }
            EntryCaptainStatEntity entryCaptainStatEntity = new EntryCaptainStatEntity();
            entryCaptainStatEntity.setEntry(entry)
                    .setEvent(entryEventResult.getEvent())
                    .setEntryName(entryInfoEntity.getEntryName())
                    .setPlayerName(entryInfoEntity.getPlayerName())
                    .setOverallPoints(entryInfoEntity.getOverallPoints())
                    .setOverallRank(entryInfoEntity.getOverallRank())
                    .setChip(entryEventResult.getEventChip())
                    .setElement(captainPick.getElement())
                    .setWebName(this.querySerivce.getPlayerByElememt(captainPick.getElement()).getWebName())
                    .setPoints(captainPick.getPoints());
            entryCaptainStatEntity.setTotalPoints(Chip.getChipFromValue(entryCaptainStatEntity.getChip()).equals(Chip.TC) ?
                    3 * entryCaptainStatEntity.getPoints() : 2 * entryCaptainStatEntity.getPoints());
            entryCaptainStatList.add(entryCaptainStatEntity);
        });
    }

    private EntryPickData getRealCaptainPoints(List<EntryPickData> captainPickList) {
        if (CollectionUtils.isEmpty(captainPickList)) {
            return null;
        }
        EntryPickData captain = captainPickList.stream().filter(EntryPickData::isCaptain).findFirst().orElse(null);
        EntryPickData viceCaptain = captainPickList.stream().filter(EntryPickData::isViceCaptain).findFirst().orElse(null);
        if (captain == null || viceCaptain == null) {
            return null;
        }
        if (captain.getPoints() == 0 && viceCaptain.getPoints() > 0) {
            return viceCaptain;
        }
        return captain;
    }

    @Override
    public void insertLeagueResultStat(int event, String leagueType, int leagueId, int limit) {
        // get league Entry
        LeagueInfoData leagueInfoData = this.getLeagueDataByTypeAndId(leagueType, leagueId, limit);
        String leagueName = leagueInfoData.getName();
        // prepare
        Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        // init league result stat
        List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
        log.info("entryInfoDataList size:{}", entryInfoDataList.size());
        // async
        List<CompletableFuture<LeagueResultStatEntity>> future = entryInfoDataList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.initEntryResultStat(event, o, elementPointsMap), new ForkJoinPool(4)))
                .collect(Collectors.toList());
        List<LeagueResultStatEntity> leagueResultStatList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        log.info("leagueResultStatList size:{}", leagueResultStatList.size());
        // league info
        leagueResultStatList.forEach(o -> o.setLeagueId(leagueId).setLeagueType(leagueType).setLeagueName(leagueName));
        // save
        this.leagueResultStatService.saveBatch(leagueResultStatList);
        log.info("insert league_result_stat size:{}!", leagueResultStatList.size());
    }

    private LeagueInfoData getLeagueDataByTypeAndId(String leagueType, int leagueId, int limit) {
        LeagueInfoData leagueInfoData = new LeagueInfoData();
        if (LeagueType.valueOf(leagueType).equals(LeagueType.Classic)) {
            leagueInfoData = this.staticSerive.getEntryInfoListFromClassicByLimit(leagueId, limit);
        } else if (LeagueType.valueOf(leagueType).equals(LeagueType.H2h)) {
            leagueInfoData = this.staticSerive.getEntryInfoListFromH2hByLimit(leagueId, limit);
        }
        return leagueInfoData;
    }

    private LeagueResultStatEntity initEntryResultStat(int event, EntryInfoData entryInfoData, Map<Integer, Integer> elementPointsMap) {
        LeagueResultStatEntity leagueResultStatEntity = new LeagueResultStatEntity();
        // entry info
        BeanUtil.copyProperties(entryInfoData, leagueResultStatEntity, CopyOptions.create().ignoreNullValue());
        // entry event result
        int entry = entryInfoData.getEntry();
        Optional<UserPicksRes> result = this.staticSerive.getUserPicks(event, entry);
        result.ifPresent(userPick ->
                leagueResultStatEntity.setEntry(entry)
                        .setEvent(event)
                        .setOverallPoints(userPick.getEntryHistory().getTotalPoints())
                        .setOverallRank(userPick.getEntryHistory().getOverallRank())
                        .setBank(userPick.getEntryHistory().getBank())
                        .setTeamValue(userPick.getEntryHistory().getValue())
                        .setEventPoints(userPick.getEntryHistory().getPoints())
                        .setEventTransfers(userPick.getEntryHistory().getEventTransfers())
                        .setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
                        .setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
                        .setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench())
                        .setEventRank(userPick.getEntryHistory().getRank())
                        .setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip())
                        .setEventPicks(this.setUserPicks(userPick.getPicks(), elementPointsMap)));
        // event captain
        List<EntryPickData> captainPickList = this.querySerivce.qryPickListFromPicks(leagueResultStatEntity.getEventPicks())
                .stream()
                .filter(o -> o.isCaptain() || o.isViceCaptain())
                .collect(Collectors.toList());
        EntryPickData captainPick = this.getRealCaptainPoints(captainPickList);
        if (captainPick != null) {
            leagueResultStatEntity
                    .setEventCaptain(captainPick.getWebName())
                    .setEventCaptainPoints(captainPick.getPoints());
        }
        return leagueResultStatEntity;
    }

    private String setUserPicks(List<Pick> picks, @NotEmpty Map<Integer, Integer> elementPointsMap) {
        List<EntryPickData> pickList = Lists.newArrayList();
        picks.forEach(o -> pickList.add(new EntryPickData()
                .setElement(o.getElement())
                .setPosition(o.getPosition())
                .setMultiplier(o.getMultiplier())
                .setCaptain(o.isCaptain())
                .setViceCaptain(o.isViceCaptain())
                .setPoints(elementPointsMap.getOrDefault(o.getElement(), 0))
        ));
        return JsonUtils.obj2json(pickList);
    }

    @Override
    public void inertTeamSelectStat(int event, String leagueType, int leagueId, int limit) {
        // get league Entry
        LeagueInfoData leagueInfoData = this.getLeagueDataByTypeAndId(leagueType, leagueId, limit);
        // init league result stat
        List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
        // get user picks
        List<CompletableFuture<TeamSelectStatEntity>> future = entryInfoDataList.stream()
                .map(o ->
                        CompletableFuture.supplyAsync(() ->
                                this.initEntryTeamSelectStat(event, o.getEntry(), leagueInfoData.getName()), new ForkJoinPool(50)))
                .collect(Collectors.toList());
        List<TeamSelectStatEntity> teamSelectStatList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // save
        this.teamSelectStatService.saveBatch(teamSelectStatList);
        log.info("insert team_select_stat size:{}!", teamSelectStatList.size());
    }

    private TeamSelectStatEntity initEntryTeamSelectStat(int event, int entry, String leagueName) {
        UserPicksRes userPicksRes = this.querySerivce.getUserPicks(event, entry);
        if (userPicksRes == null) {
            return new TeamSelectStatEntity();
        }
        List<Pick> picks = userPicksRes.getPicks();
        TeamSelectStatEntity teamSelectStatEntity = new TeamSelectStatEntity();
        teamSelectStatEntity.setLeagueName(leagueName)
                .setEvent(event)
                .setEntry(entry)
                .setChip(userPicksRes.getActiveChip() == null ? "n/a" : userPicksRes.getActiveChip())
                .setPosition1(picks.get(0).getElement())
                .setPosition2(picks.get(1).getElement())
                .setPosition3(picks.get(2).getElement())
                .setPosition4(picks.get(3).getElement())
                .setPosition5(picks.get(4).getElement())
                .setPosition6(picks.get(5).getElement())
                .setPosition7(picks.get(6).getElement())
                .setPosition8(picks.get(7).getElement())
                .setPosition9(picks.get(8).getElement())
                .setPosition10(picks.get(9).getElement())
                .setPosition11(picks.get(10).getElement())
                .setPosition12(picks.get(11).getElement())
                .setPosition13(picks.get(12).getElement())
                .setPosition14(picks.get(13).getElement())
                .setPosition15(picks.get(14).getElement());
        teamSelectStatEntity.setCaptain(picks
                .stream()
                .filter(Pick::isCaptain)
                .map(Pick::getElement)
                .findFirst()
                .orElse(0)
        );
        teamSelectStatEntity.setViceCaptain(picks
                .stream()
                .filter(Pick::isViceCaptain)
                .map(Pick::getElement)
                .findFirst()
                .orElse(0)
        );
        return teamSelectStatEntity;
    }

    @Override
    public LeagueStatData getLeagueStatData(String leagueName, int event) {
        LeagueStatData leagueStatData = new LeagueStatData().setName(leagueName).setEvent(event);
        // player info
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, v -> v));
        // team select
        List<TeamSelectStatEntity> teamSelectList = this.teamSelectStatService.list(new QueryWrapper<TeamSelectStatEntity>().lambda()
                .eq(TeamSelectStatEntity::getLeagueName, leagueName)
                .eq(TeamSelectStatEntity::getEvent, event));
        int teamSize = teamSelectList.size();
        if (CollectionUtils.isEmpty(teamSelectList)) {
            return leagueStatData;
        }
        // most transfer in
        LinkedHashMap<String, String> mostTransferInMap = this.getMostTransferInMap(leagueName, event, teamSelectList, teamSize, playerMap);
        leagueStatData.setMostTransferIn(mostTransferInMap);
        // most transfer out
        LinkedHashMap<String, String> mostTransferOutMap = this.getMostTransferOutMap(leagueName, event, teamSelectList, teamSize, playerMap);
        leagueStatData.setMostTransferOut(mostTransferOutMap);
        // captain selected
        LinkedHashMap<String, String> captainSelectedMap = this.getCaptainSelectedMap(teamSelectList, teamSize, playerMap);
        leagueStatData.setCaptainSelectedMap(captainSelectedMap);
        // vice captain selected
        LinkedHashMap<String, String> viceCaptainSelectedMap = this.getViceCaptainSelectedMap(teamSelectList, teamSize, playerMap);
        leagueStatData.setViceCaptainSelectedMap(viceCaptainSelectedMap);
        // top selected player
        LinkedHashMap<String, String> topSelectedPlayerMap = this.getTopSelectedPlayerMap(teamSelectList, teamSize, playerMap);
        leagueStatData.setTopSelectedPlayerMap(topSelectedPlayerMap);
        // top selected team
        LinkedHashMap<Integer, Map<String, String>> topSelectedTeamMap = this.getTopSelectedTeamMap(teamSelectList, teamSize, playerMap);
        leagueStatData.setTopSelectedTeamMap(topSelectedTeamMap);
        return leagueStatData;
    }

    private LinkedHashMap<String, String> getMostTransferInMap(String leagueName, int event, List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        if (event <= 1) {
            return Maps.newLinkedHashMap();
        }
        // current gw
        Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(teamSelectList);
        // previous gw
        Map<Integer, List<Integer>> previousSelectMap = this.collectPreviousEntrySelectedMap(leagueName, event);
        // different
        List<Integer> elementList = Lists.newArrayList();
        currentSelectMap.keySet().forEach(entry -> {
            List<Integer> currentList = currentSelectMap.get(entry);
            List<Integer> previousList = previousSelectMap.getOrDefault(entry, Lists.newArrayList());
            currentList
                    .stream()
                    .filter(o -> !previousList.contains(o))
                    .forEach(elementList::add);
        });
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getMostTransferOutMap(String leagueName, int event, List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        if (event <= 1) {
            return Maps.newLinkedHashMap();
        }
        // current gw
        Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(teamSelectList);
        // previous gw
        Map<Integer, List<Integer>> previousSelectMap = this.collectPreviousEntrySelectedMap(leagueName, event);
        // different
        List<Integer> elementList = Lists.newArrayList();
        previousSelectMap.keySet().forEach(entry -> {
            List<Integer> previousList = previousSelectMap.get(entry);
            List<Integer> currentList = currentSelectMap.getOrDefault(entry, Lists.newArrayList());
            previousList
                    .stream()
                    .filter(o -> !currentList.contains(o))
                    .forEach(elementList::add);
        });
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private Map<Integer, List<Integer>> collectPreviousEntrySelectedMap(String leagueName, int event) {
        List<TeamSelectStatEntity> previousSelectList = this.teamSelectStatService.list(new QueryWrapper<TeamSelectStatEntity>().lambda()
                .eq(TeamSelectStatEntity::getLeagueName, leagueName)
                .eq(TeamSelectStatEntity::getEvent, event - 1));
        return this.collectEntrySelectedMap(previousSelectList);
    }

    private Map<Integer, List<Integer>> collectEntrySelectedMap(List<TeamSelectStatEntity> teamSelectList) {
        Map<Integer, List<Integer>> teamSelectMap = Maps.newHashMap();
        teamSelectList.forEach(o -> {
            List<Integer> elementList = Lists.newArrayList(
                    o.getPosition1(), o.getPosition2(), o.getPosition3(), o.getPosition4(), o.getPosition5(),
                    o.getPosition6(), o.getPosition7(), o.getPosition8(), o.getPosition9(), o.getPosition10(),
                    o.getPosition11(), o.getPosition12(), o.getPosition13(), o.getPosition14(), o.getPosition15()
            );
            teamSelectMap.put(o.getEntry(), elementList);
        });
        return teamSelectMap;
    }

    private LinkedHashMap<String, String> getCaptainSelectedMap(List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        List<Integer> elementList = teamSelectList
                .stream()
                .map(TeamSelectStatEntity::getCaptain)
                .collect(Collectors.toList());
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getViceCaptainSelectedMap(List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        // collect
        List<Integer> elementList = teamSelectList
                .stream()
                .map(TeamSelectStatEntity::getViceCaptain)
                .collect(Collectors.toList());
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getTopSelectedPlayerMap(List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        List<Integer> elementList = Lists.newArrayList();
        teamSelectList.forEach(o -> {
            elementList.add(o.getPosition1());
            elementList.add(o.getPosition2());
            elementList.add(o.getPosition3());
            elementList.add(o.getPosition4());
            elementList.add(o.getPosition5());
            elementList.add(o.getPosition6());
            elementList.add(o.getPosition7());
            elementList.add(o.getPosition8());
            elementList.add(o.getPosition9());
            elementList.add(o.getPosition10());
            elementList.add(o.getPosition11());
            elementList.add(o.getPosition12());
            elementList.add(o.getPosition13());
            elementList.add(o.getPosition14());
            elementList.add(o.getPosition15());
        });
        return this.collectSelectedMap(elementList, teamSize, 20, playerMap);
    }

    private LinkedHashMap<Integer, Map<String, String>> getTopSelectedTeamMap(List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        // element list
        List<PlayerEntity> elementPlayerInfoList = Lists.newArrayList();
        teamSelectList.forEach(o -> {
            elementPlayerInfoList.add(playerMap.get(o.getPosition1()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition2()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition3()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition4()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition5()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition6()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition7()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition8()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition9()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition10()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition11()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition12()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition13()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition14()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition15()));
        });
        // collect
        Map<Integer, Map<Integer, Long>> elementTypeCountMap = elementPlayerInfoList
                .stream()
                .collect(Collectors.groupingBy(PlayerEntity::getElementType, Collectors.groupingBy(PlayerEntity::getElement, Collectors.counting())));
        // sort by element type
        Map<Integer, Integer> playerSelectedMap = Maps.newHashMap(); // key:element -> value: count
        elementTypeCountMap.keySet().forEach(elementType -> {
            Map<Integer, Integer> result = elementTypeCountMap.get(elementType).entrySet()
                    .stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(this.getLimitByElementType(elementType))
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
            result.forEach(playerSelectedMap::put);
        });
        // add key:element_type
        Map<Integer, Map<Integer, Integer>> elementTypeMap = this.collectPlayerSelectedMap(playerSelectedMap, playerMap); // key:element_type -> value: elementCOuntMap
        // sort by selected
        LinkedHashMap<Integer, Integer> elementSelectedSortMap = playerSelectedMap.entrySet() // key:element -> value: count (sort by count)
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        List<PlayerEntity> elementList = Lists.newArrayList();
        elementSelectedSortMap.forEach((k, v) -> elementList.add(playerMap.get(k)));
        // selected line up
        LinkedHashMap<Integer, Map<String, String>> map = Maps.newLinkedHashMap(); // key:element_type -> value:elementCountMap(key:element -> value:percent)
        LinkedHashMap<Integer, Integer> lineupMap = this.getLineupMapByElementList(elementTypeMap, elementList); // key:position -> value:element
        lineupMap.forEach((position, element) -> {
            long count = playerSelectedMap.get(element);
            PlayerEntity playerEntity = playerMap.get(element);
            int elementType = playerEntity.getElementType();
            Map<String, String> valueMap = Maps.newHashMap();
            if (map.containsKey(elementType)) {
                valueMap = map.get(elementType);
            }
            valueMap.put(playerEntity.getWebName(), NumberUtil.decimalFormat("#.##%", NumberUtil.div(count, teamSize)));
            map.put(elementType, valueMap);
        });
        return map;
    }

    private Map<Integer, Map<Integer, Integer>> collectPlayerSelectedMap(Map<Integer, Integer> playerSelectedMap, Map<Integer, PlayerEntity> playerMap) {
        Map<Integer, Map<Integer, Integer>> map = Maps.newHashMap();
        playerSelectedMap.forEach((k, v) -> {
            int elementType = playerMap.get(k).getElementType();
            Map<Integer, Integer> valueMap = Maps.newHashMap();
            if (map.containsKey(elementType)) {
                valueMap = map.get(elementType);
            }
            valueMap.put(k, v);
            map.put(elementType, valueMap);
        });
        return map;
    }

    private LinkedHashMap<Integer, Integer> getLineupMapByElementList(Map<Integer, Map<Integer, Integer>> elementTypeMap, List<PlayerEntity> elementList) {
        // gkp
        List<Integer> gkpList = Lists.newArrayList();
        elementTypeMap.get(1).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> gkpList.add(o.getKey()));
        // def
        List<Integer> defList = Lists.newArrayList();
        elementTypeMap.get(2).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> defList.add(o.getKey()));
        // mid
        List<Integer> midList = Lists.newArrayList();
        elementTypeMap.get(3).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> midList.add(o.getKey()));
        // fwd
        List<Integer> fwdList = Lists.newArrayList();
        elementTypeMap.get(4).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> fwdList.add(o.getKey()));
        // linue up
        Map<String, Integer> formationMap = this.getFormationMap(elementList);
        List<Integer> positionList = Lists.newArrayList();
        // gkp line up
        positionList.add(gkpList.get(0));
        // def line up
        int defStartIndex = 2;
        int defEndIndex = defStartIndex + formationMap.get("def");
        IntStream.range(defStartIndex, defEndIndex).forEach(index -> positionList.add(defList.get(index - defStartIndex)));
        // mid line up
        int midEndIndex = defEndIndex + formationMap.get("mid");
        IntStream.range(defEndIndex, midEndIndex).forEach(index -> positionList.add(midList.get(index - defEndIndex)));
        // fwd line up
        int fwdEndIndex = midEndIndex + formationMap.get("fwd");
        IntStream.range(midEndIndex, fwdEndIndex).forEach(index -> positionList.add(fwdList.get(index - midEndIndex)));
        // return
        LinkedHashMap<Integer, Integer> map = Maps.newLinkedHashMap();
        for (int i = 0; i < positionList.size(); i++) {
            map.put(i + 1, positionList.get(i));
        }
        return map;
    }

    private Map<String, Integer> getFormationMap(List<PlayerEntity> elementList) {
        int def = 0;
        int mid = 0;
        int fwd = 0;
        List<PlayerEntity> standbyList = Lists.newArrayList();
        for (PlayerEntity playerEntity : elementList) {
            int elementType = playerEntity.getElementType();
            switch (elementType) {
                case 2: {
                    if (def < 3) {
                        def++;
                        break;
                    }
                }
                case 4: {
                    if (fwd < 1) {
                        fwd++;
                        break;
                    }
                }
                default:
                    standbyList.add(playerEntity);
            }
        }
        for (PlayerEntity playerEntity : standbyList) {
            if (def + mid + fwd >= 10) {
                break;
            }
            int elementType = playerEntity.getElementType();
            switch (elementType) {
                case 2: {
                    def++;
                    break;
                }
                case 3: {
                    mid++;
                    break;
                }
                case 4: {
                    fwd++;
                }
            }
        }
        Map<String, Integer> map = Maps.newHashMap();
        map.put("def", def);
        map.put("mid", mid);
        map.put("fwd", fwd);
        return map;
    }

    private int getLimitByElementType(int elementType) {
        switch (elementType) {
            case 1:
                return 2;
            case 2:
            case 3:
                return 5;
            case 4:
                return 3;
        }
        return 0;
    }

    private LinkedHashMap<String, String> collectSelectedMap(List<Integer> elementList, int teamSize, int limit, Map<Integer, PlayerEntity> playerMap) {
        LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
        Map<Integer, Long> groupingMap = elementList
                .stream()
                .collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
        Map<Integer, Integer> result = groupingMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        result.forEach((k, v) ->
                map.put(playerMap.get(k).getWebName(), NumberUtil.decimalFormat("#.##%", NumberUtil.div(v.intValue(), teamSize))));
        return map;
    }

}
