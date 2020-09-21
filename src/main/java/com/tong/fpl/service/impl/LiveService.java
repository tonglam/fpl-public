package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.config.collector.ElementLiveCollector;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.MatchPlayStatus;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.constant.enums.PositionRule;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.service.ILiveService;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.EntryInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create by tong on 2020/7/13
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveService implements ILiveService {

    private final IQuerySerivce querySerivce;
    private final EntryInfoService entryInfoService;

    @Override
    public LiveCalaData calcLivePointsByEntry(int event, int entry) {
        // prepare
        Map<Integer, String> positionMap = this.getPositionMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // calc entry points
        LiveCalaData liveCalaData = this.calcLiveSingleEntryPoints(event, entry, Maps.newHashMap(), positionMap,
                teamLiveFixtureMap, eventLiveMap, liveBonusTable, new ForkJoinPool(4));
        // entry info
        EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(entry);
        if (entryInfoEntity != null) {
            BeanUtil.copyProperties(entryInfoEntity, liveCalaData);
            liveCalaData.setLiveTotalPoints(liveCalaData.getOverallPoints() + liveCalaData.getLiveNetPoints());
        }
        return liveCalaData;
    }

    @Override
    public List<LiveCalaData> calcLivePointsByTournament(int event, int tournamentId) {
        // prepare
        Map<Integer, String> positionMap = this.getPositionMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // get entry list
        List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
        // calc live entry points
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<CompletableFuture<LiveCalaData>> future = entryList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.calcLiveSingleEntryPoints(event, o, Maps.newHashMap(), positionMap,
                        teamLiveFixtureMap, eventLiveMap, liveBonusTable, forkJoinPool), forkJoinPool))
                .collect(Collectors.toList());
        List<LiveCalaData> liveCalaList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // entry info
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list()
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, v -> v));
        liveCalaList.forEach(liveCalaData -> {
            EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(liveCalaData.getEntry(), null);
            if (entryInfoEntity != null) {
                BeanUtil.copyProperties(entryInfoEntity, liveCalaData);
                liveCalaData.setLiveTotalPoints(liveCalaData.getOverallPoints() + liveCalaData.getLiveNetPoints());
            }
        });
        return liveCalaList
                .stream()
                .sorted(Comparator.comparing(LiveCalaData::getLiveTotalPoints).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public LiveCalaData calcLivePointsByElementList(int event, Map<Integer, Integer> elementMap, String chip, int captain, int viceCaptain) {
        // prepare
        Map<Integer, String> positionMap = this.getPositionMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // init user pick from elementMap
        List<Pick> picks = Lists.newArrayList();
        elementMap.forEach((position, element) ->
                picks.add(new Pick()
                        .setElement(element)
                        .setPosition(position)
                        .setCaptain(element == captain)
                        .setViceCaptain(element == viceCaptain)
                )
        );
        // initialize element_live_data, static part
        List<ElementEventResultData> elementEventResultDataList = this.qryEntryLiveStaticData(event, picks, Maps.newHashMap(), positionMap,
                teamLiveFixtureMap, new ForkJoinPool(4));
        // initialize element_live_data, event_live part
        this.initEventLiveData(elementEventResultDataList, eventLiveMap, liveBonusTable);
        // get active picks
        List<ElementEventResultData> pickList = this.getPickList(elementEventResultDataList);
        // calc live points
        int livePoints = this.calcActivePoints(Chip.getChipFromValue(chip), pickList);
        return new LiveCalaData()
                .setEntry(0)
                .setEvent(event)
                .setPickList(pickList)
                .setChip(chip)
                .setLiveTotalPoints(livePoints)
                .setLivePoints(livePoints)
                .setTransferCost(0)
                .setLiveNetPoints(livePoints);
    }

    private Map<Integer, String> getPositionMap() {
        Map<Integer, String> map = Maps.newHashMap();
        this.querySerivce.getPositionMap().forEach((k, v) -> map.put(Integer.valueOf(k), v));
        return map;
    }

    private Map<Integer, EventLiveEntity> getEventLiveByEvent(int event) {
        Map<Integer, EventLiveEntity> map = Maps.newHashMap();
        this.querySerivce.getEventLiveByEvent(event).forEach((k, v) -> map.put(Integer.valueOf(k), v));
        return map;
    }

    private Map<Integer, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap() {
        Map<Integer, Map<String, List<LiveFixtureData>>> map = Maps.newHashMap(); // key:teamId -> value:(key:status -> value:liveFixtureData)
        this.querySerivce.getEventLiveFixtureMap().forEach((k, v) -> map.put(Integer.valueOf(k), v));
        return map;
    }

    private Table<Integer, Integer, Integer> getLiveBonusTable() {
        Table<Integer, Integer, Integer> table = HashBasedTable.create();
        this.querySerivce.getLiveBonusCacheMap().forEach((k, v) -> {
            int element = v.keySet()
                    .stream()
                    .mapToInt(Integer::parseInt)
                    .findFirst()
                    .orElse(0);
            int bonus = v.values()
                    .stream()
                    .findFirst()
                    .orElse(0);
            table.put(Integer.parseInt(k), element, bonus);
        });
        return table;
    }

    private LiveCalaData calcLiveSingleEntryPoints(int event, int entry,
                                                   Map<Integer, PlayerEntity> playerInfoMap, Map<Integer, String> positionMap,
                                                   Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap,
                                                   Map<Integer, EventLiveEntity> eventLiveMap, Table<Integer, Integer, Integer> liveBonusTable,
                                                   ForkJoinPool forkJoinPool) {
        // get user pick
        UserPicksRes userPicksRes = this.querySerivce.getUserPicks(event, entry);
        if (CollectionUtils.isEmpty(userPicksRes.getPicks())) {
            return new LiveCalaData();
        }
        // initialize element_live_data, static part
        List<ElementEventResultData> elementEventResultDataList = this.qryEntryLiveStaticData(event, userPicksRes.getPicks(), playerInfoMap, positionMap, teamLiveFixtureMap, forkJoinPool);
        // initialize element_live_data, event_live part
        this.initEventLiveData(elementEventResultDataList, eventLiveMap, liveBonusTable);
        // get active picks
        List<ElementEventResultData> pickList = this.getPickList(elementEventResultDataList);
        // calc live points
        int livePoints = this.calcActivePoints(Chip.getChipFromValue(userPicksRes.getActiveChip()), pickList);
        return new LiveCalaData()
                .setEntry(entry)
                .setEvent(event)
                .setPickList(pickList
                        .stream()
                        .sorted(Comparator.comparing(ElementEventResultData::isPickAvtive).reversed()
                                .thenComparing(ElementEventResultData::getPlayStatus))
                        .collect(Collectors.toList())
                )
                .setChip(userPicksRes.getActiveChip())
                .setLivePoints(livePoints)
                .setTransferCost(userPicksRes.getEntryHistory().getEventTransfersCost())
                .setLiveNetPoints(livePoints - userPicksRes.getEntryHistory().getEventTransfersCost())
                .setLiveTotalPoints(livePoints - userPicksRes.getEntryHistory().getEventTransfersCost())
                .setToPlay(pickList
                        .stream()
                        .filter(o -> o.isPickAvtive() && !o.isPlayed())
                        .count())
                .setPlayed(pickList
                        .stream()
                        .filter(o -> o.isPickAvtive() && o.isPlayed())
                        .count());
    }

    public List<ElementEventResultData> qryEntryLiveStaticData(int event, List<Pick> picks, Map<Integer, PlayerEntity> playerInfoMap, Map<Integer, String> positionMap,
                                                               Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap, ForkJoinPool forkJoinPool) {
        List<CompletableFuture<ElementEventResultData>> future = picks.stream()
                .map(o ->
                        CompletableFuture.supplyAsync(() ->
                                this.qryElementLiveStaticData(event, o.getElement(), o, playerInfoMap, positionMap, teamLiveFixtureMap), forkJoinPool))
                .collect(Collectors.toList());
        return future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public ElementEventResultData qryElementLiveStaticData(int event, int element, Pick pick, Map<Integer, PlayerEntity> playerInfoMap, Map<Integer, String> positionMap,
                                                           Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap) {
        // from user pick
        ElementEventResultData elementEventResultData = new ElementEventResultData();
        elementEventResultData
                .setEvent(event)
                .setElement(element)
                .setPosition(pick.getPosition())
                .setMultiplier(pick.getMultiplier())
                .setCaptain(pick.isCaptain())
                .setViceCaptain(pick.isViceCaptain());
        // player info
        PlayerEntity playerEntity;
        if (playerInfoMap.containsKey(element)) {
            playerEntity = playerInfoMap.get(element);
        } else {
            playerEntity = this.querySerivce.getPlayerByElememt(element);
        }
        if (playerEntity != null) {
            elementEventResultData
                    .setElementType(playerEntity.getElementType())
                    .setElementTypeName(positionMap.get(playerEntity.getElementType()))
                    .setPrice(playerEntity.getPrice())
                    .setWebName(playerEntity.getWebName());
            // event fixture
            int teamId = playerEntity.getTeamId();
            Map<String, List<LiveFixtureData>> liveFixtureMap = teamLiveFixtureMap.get(teamId);
            if (!CollectionUtils.isEmpty(liveFixtureMap)) {
                this.setMatchInfo(elementEventResultData, liveFixtureMap);
                this.setMatchPlayStatus(elementEventResultData, liveFixtureMap);
            }
        }
        return elementEventResultData;
    }

    private void setMatchInfo(ElementEventResultData elementEventResultData, Map<String, List<LiveFixtureData>> liveFixtureMap) {
        List<LiveFixtureData> list = Lists.newArrayList();
        liveFixtureMap.values().forEach(list::addAll);
        // team short name
        List<String> teamShortNameList = list
                .stream()
                .map(LiveFixtureData::getTeamShortName)
                .collect(Collectors.toList());
        String teamShortName = this.setMultiMatch(teamShortNameList);
        // against short name
        List<String> againstShortNameList = list
                .stream()
                .map(LiveFixtureData::getAgainstShortName)
                .collect(Collectors.toList());
        String againstShortName = this.setMultiMatch(againstShortNameList);
        // was home
        List<String> wasHomeList = list
                .stream()
                .map(o -> o.isWasHome() ? "是" : "否")
                .collect(Collectors.toList());
        String wasHome = this.setMultiMatch(wasHomeList);
        // score
        List<String> scoreList = list
                .stream()
                .map(o -> o.getTeamScore() + "-" + o.getAgainstTeamScore())
                .collect(Collectors.toList());
        String score = this.setMultiMatch(scoreList);
        elementEventResultData
                .setTeamShortName(teamShortName)
                .setAgainstShortName(againstShortName)
                .setWasHome(wasHome)
                .setScore(score);
    }

    private String setMultiMatch(List<String> list) {
        StringBuffer buffer = new StringBuffer();
        list.forEach(str -> buffer.append(str).append(","));
        return buffer.toString().substring(0, buffer.lastIndexOf(","));
    }

    private void setMatchPlayStatus(ElementEventResultData elementEventResultData, Map<String, List<LiveFixtureData>> liveFixtureMap) {
        int playingSize = 0;
        int notStartSize = 0;
        int finishSize = 0;
        if (!CollectionUtils.isEmpty(liveFixtureMap)) {
            playingSize = liveFixtureMap.get(MatchPlayStatus.Playing.name()).size();
            notStartSize = liveFixtureMap.get(MatchPlayStatus.Not_Start.name()).size();
            finishSize = liveFixtureMap.get(MatchPlayStatus.Finished.name()).size();
        }
        if (playingSize > 0) {
            elementEventResultData.setGwStarted(true).setGwFinished(false).setPlayStatus(MatchPlayStatus.Playing.getStatus());
        } else {
            if (notStartSize != 0) {
                if (finishSize == 0) { // n,0,0
                    elementEventResultData.setGwStarted(false).setGwFinished(false).setPlayStatus(MatchPlayStatus.Not_Start.getStatus());
                } else { // n,0,n
                    elementEventResultData.setGwStarted(true).setGwFinished(false).setPlayStatus(MatchPlayStatus.Event_Not_Finished.getStatus());
                }
            } else {
                if (finishSize == 0) { // 0,0,0
                    elementEventResultData.setGwStarted(false).setGwFinished(false).setPlayStatus(MatchPlayStatus.Blank.getStatus());
                } else { // 0,0,n
                    elementEventResultData.setGwStarted(true).setGwFinished(true).setPlayStatus(MatchPlayStatus.Finished.getStatus());
                }
            }
        }
    }

    private void initEventLiveData(List<ElementEventResultData> elementEventResultDataList, Map<Integer, EventLiveEntity> eventLiveMap, Table<Integer, Integer, Integer> liveBonusTable) {
        if (CollectionUtils.isEmpty(eventLiveMap)) {
            return;
        }
        elementEventResultDataList.forEach(elementEventResultData -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(elementEventResultData.getElement());
            if (eventLiveEntity != null) {
                BeanUtil.copyProperties(eventLiveEntity, elementEventResultData, CopyOptions.create().ignoreNullValue());
                elementEventResultData.setPlayed(elementEventResultData.getMinutes() > 0 || elementEventResultData.getYellowCards() > 0 || elementEventResultData.getRedCards() > 0);
                this.setEventLiveBonusData(eventLiveEntity, liveBonusTable);
            }
        });
    }

    private void setEventLiveBonusData(EventLiveEntity eventLiveEntity, Table<Integer, Integer, Integer> liveBonusTable) {
        int teamId = eventLiveEntity.getTeamId();
        int element = eventLiveEntity.getElement();
        if (liveBonusTable.contains(teamId, element)) {
            eventLiveEntity.setBonus(liveBonusTable.get(teamId, element));
        }
    }

    private List<ElementEventResultData> getPickList(List<ElementEventResultData> elementEventResultDataList) {
        // element_type -> active -> start
        Map<Integer, Table<Boolean, Boolean, List<ElementEventResultData>>> map = elementEventResultDataList.stream().collect(new ElementLiveCollector());
        // gkp
        List<ElementEventResultData> gkps = this.createSteam(map.get(Position.GKP.getPosition()).get(true, true),
                map.get(Position.GKP.getPosition()).get(true, false),
                map.get(Position.GKP.getPosition()).get(false, true))
                .flatMap(Collection::stream)
                .limit(PositionRule.MIN_NUM_GKP.getNum())
                .collect(Collectors.toList());
        // active defs
        List<ElementEventResultData> defs = this.createSteam(map.get(Position.DEF.getPosition()).get(true, true))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                .collect(Collectors.toList());
        // def rule, at least 3
        if (defs.size() < PositionRule.MIN_NUM_DEF.getNum()) {
            defs = this.createSteam(defs,
                    map.get(Position.DEF.getPosition()).get(true, false),
                    map.get(Position.DEF.getPosition()).get(false, true))
                    .flatMap(Collection::stream)
                    .limit(PositionRule.MIN_NUM_DEF.getNum())
                    .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                    .collect(Collectors.toList());
        }
        // active fwds
        List<ElementEventResultData> fwds = this.createSteam(map.get(Position.FWD.getPosition()).get(true, true))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                .collect(Collectors.toList());
        // fwd rule, at least 1
        if (fwds.size() < PositionRule.MIN_NUM_FWD.getNum()) {
            fwds = this.createSteam(fwds,
                    map.get(Position.FWD.getPosition()).get(true, false),
                    map.get(Position.FWD.getPosition()).get(false, true))
                    .flatMap(Collection::stream)
                    .limit(PositionRule.MIN_NUM_FWD.getNum())
                    .collect(Collectors.toList());
        }
        // mids
        int maxMidNum = PositionRule.MIN_PLAYERS.getNum() - gkps.size() - defs.size() - fwds.size();
        List<ElementEventResultData> mids = this.createSteam(map.get(Position.MID.getPosition()).get(true, true),
                map.get(Position.MID.getPosition()).get(true, false))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                .limit(maxMidNum)
                .collect(Collectors.toList());
        // active_list
        List<ElementEventResultData> activeList = this.createSteam(gkps, defs, fwds, mids)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<ElementEventResultData> standByList = this.createSteam(map.get(Position.DEF.getPosition()).get(false, true),
                map.get(Position.MID.getPosition()).get(false, true),
                map.get(Position.FWD.getPosition()).get(false, true))
                .flatMap(Collection::stream)
                .filter(o -> !activeList.contains(o))
                .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                .limit(PositionRule.MIN_PLAYERS.getNum() - activeList.size())
                .collect(Collectors.toList());
        List<ElementEventResultData> pickList = this.createSteam(activeList, standByList)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ElementEventResultData::getElementType).thenComparing(ElementEventResultData::getPosition))
                .collect(Collectors.toList());
        pickList.addAll(elementEventResultDataList.stream()
                .filter(o -> !activeList.contains(o))
                .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                .collect(Collectors.toList()));
        return pickList;
    }

    private int calcActivePoints(Chip chip, List<ElementEventResultData> pickList) {
        int activeCaptain = this.getActiveCaptain(pickList);
        if (activeCaptain == 0) {
            return 0;
        }
        switch (chip) {
            case TC:
                pickList.subList(0, 11).forEach(o -> o.setPickAvtive(true));
                return pickList.subList(0, 11).stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 3 * o.getTotalPoints()).sum();
            case BB:
                pickList.forEach(o -> o.setPickAvtive(true));
                return pickList.stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 2 * o.getTotalPoints()).sum();
            // only 3c and bb change the calculate rule
            case NONE:
            case WC:
            case FH:
                pickList.subList(0, 11).forEach(o -> o.setPickAvtive(true));
                return pickList.subList(0, 11).stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 2 * o.getTotalPoints()).sum();
            default:
                return 0;
        }
    }

    private int getActiveCaptain(List<ElementEventResultData> pickList) {
        // captain played
        int activeCaptain = pickList.stream()
                .filter(ElementEventResultData::isCaptain)
                .filter(o -> !o.isGwStarted() || o.isPlayed())
                .map(ElementEventResultData::getElement)
                .findFirst()
                .orElse(0);
        // vice captain played
        if (activeCaptain == 0) {
            activeCaptain = pickList.stream()
                    .filter(ElementEventResultData::isViceCaptain)
                    .filter(o -> !o.isGwStarted() || o.isPlayed())
                    .map(ElementEventResultData::getElement)
                    .findFirst()
                    .orElse(0);
        }
        // none played
        if (activeCaptain == 0) {
            activeCaptain = pickList.stream()
                    .filter(ElementEventResultData::isCaptain)
                    .map(ElementEventResultData::getElement)
                    .findFirst()
                    .orElse(0);
        }
        return activeCaptain;
    }

    @SafeVarargs
    private final <T> Stream<T> createSteam(T... values) {
        Stream.Builder<T> builder = Stream.builder();
        Arrays.asList(values).forEach(builder::add);
        return builder.build();
    }

}
