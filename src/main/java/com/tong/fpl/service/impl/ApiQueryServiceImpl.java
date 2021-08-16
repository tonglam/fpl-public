package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.*;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.team.TeamData;
import com.tong.fpl.domain.letletme.team.TeamDetailData;
import com.tong.fpl.domain.letletme.team.TeamSummaryData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentPointsGroupEventResultData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.IStaticService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2021/5/10
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiQueryServiceImpl implements IApiQueryService {

    private final IStaticService staticService;
    private final IRedisCacheService redisCacheService;
    private final IQueryService queryService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final PlayerValueService playerValueService;
    private final EventFixtureService eventFixtureService;
    private final EventLiveSummaryService eventLiveSummaryService;
    private final EventLiveService eventLiveService;
    private final EntryInfoService entryInfoService;
    private final EntryEventTransfersService entryEventTransfersService;
    private final EntryEventResultService entryEventResultService;
    private final ScoutService scoutService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final LeagueEventReportService leagueEventReportService;

    /**
     * @implNote common
     */
    @Cacheable(
            value = "api::qryCurrentEventAndNextUtcDeadline",
            cacheManager = "apiCacheManager",
            unless = "#result.size() != 2"
    )
    @Override
    public Map<String, String> qryCurrentEventAndNextUtcDeadline() {
        Map<String, String> map = Maps.newHashMap();
        int event = this.queryService.getCurrentEvent();
        map.put("event", String.valueOf(event));
        String utcDeadline = this.queryService.getUtcDeadlineByEvent(event + 1);
        map.put("utcDeadline", utcDeadline);
        return map;
    }

    @Cacheable(
            value = "api::qryEventAverageScore",
            cacheManager = "apiCacheManager",
            unless = "#result.size() == 0"
    )
    @Override
    public Map<String, Integer> qryEventAverageScore() {
        Map<String, Integer> map = Maps.newHashMap();
        int current = this.queryService.getCurrentEvent();
        IntStream.rangeClosed(1, current).forEach(event -> {
            String key = StringUtils.joinWith("::", "AverageScore", CommonUtils.getCurrentSeason(), event);
            int score = (int) RedisUtils.getValueByKey(key).orElse(0);
            map.put(String.valueOf(event), score);
        });
        return map;
    }

    @Cacheable(
            value = "api::qryNextFixture",
            cacheManager = "apiCacheManager",
            unless = "#result.size() == 0"
    )
    @Override
    public List<PlayerFixtureData> qryNextFixture() {
        int nextEvent = this.queryService.getNextEvent();
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        return this.queryService.getEventFixtureByEvent(nextEvent)
                .stream()
                .map(o ->
                        new PlayerFixtureData()
                                .setEvent(nextEvent)
                                .setTeamId(o.getTeamH())
                                .setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
                                .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
                                .setAgainstTeamId(o.getTeamA())
                                .setAgainstTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
                                .setAgainstTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
                                .setDifficulty(0)
                                .setKickoffTime(o.getKickoffTime())
                                .setStarted(false)
                                .setFinished(false)
                                .setWasHome(false)
                                .setTeamScore(0)
                                .setAgianstTeamScore(0)
                                .setScore("")
                                .setResult("")
                                .setBgw(false)
                                .setDgw(false)
                )
                .sorted(Comparator.comparing(PlayerFixtureData::getKickoffTime))
                .collect(Collectors.toList());
    }

    /**
     * @implNote entry
     */
    @Cacheable(
            value = "api::qryEntryInfo",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntryInfoData qryEntryInfo(int entry) {
        EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(CommonUtils.getCurrentSeason(), entry);
        if (entryInfoEntity == null) {
            return new EntryInfoData();
        }
        return new EntryInfoData()
                .setEntry(entryInfoEntity.getEntry())
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName())
                .setRegion(entryInfoEntity.getRegion())
                .setStartedEvent(entryInfoEntity.getStartedEvent())
                .setOverallPoints(entryInfoEntity.getOverallPoints())
                .setOverallRank(entryInfoEntity.getOverallRank())
                .setTotalTransfers(entryInfoEntity.getTotalTransfers())
                .setValue(entryInfoEntity.getTeamValue() / 10.0)
                .setBank(entryInfoEntity.getBank() / 10.0)
                .setTeamValue((entryInfoEntity.getTeamValue() - entryInfoEntity.getBank()) / 10.0);
    }

    // do not cache
    @Override
    public List<EntryInfoData> fuzzyQueryEntry(EntryQueryParam param) {
        List<EntryInfoData> list = Lists.newArrayList();
        LambdaQueryWrapper<EntryInfoEntity> queryWrapper = new QueryWrapper<EntryInfoEntity>().lambda();
        if (StringUtils.isNotBlank(param.getEntryName())) {
            queryWrapper.like(EntryInfoEntity::getEntryName, param.getEntryName());
            list.addAll(this.fuzzyQueryLeagueReport(queryWrapper));
        }
        if (StringUtils.isNotBlank(param.getPlayerName())) {
            queryWrapper.like(EntryInfoEntity::getPlayerName, param.getPlayerName());
            list.addAll(this.fuzzyQueryLeagueReport(queryWrapper));
        }
        return list
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private List<EntryInfoData> fuzzyQueryLeagueReport(LambdaQueryWrapper<EntryInfoEntity> queryWrapper) {
        return this.entryInfoService.list(queryWrapper)
                .stream()
                .map(o ->
                        new EntryInfoData()
                                .setEntry(o.getEntry())
                                .setEntryName(o.getEntryName())
                                .setPlayerName(o.getPlayerName())
                )
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "api::qryEntryLeagueInfo",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntryLeagueInfoData qryEntryLeagueInfo(int entry) {
        EntryLeagueInfoData data = new EntryLeagueInfoData();
        this.staticService.getEntry(entry).ifPresent(o ->
                data
                        .setEntry(entry)
                        .setClassic(o.getLeagues().getClassic())
                        .setH2h(o.getLeagues().getH2h())
                        .setCup(o.getLeagues().getCup())
        );
        return data;
    }

    @Cacheable(
            value = "api::qryEntryHistoryInfo",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntryHistoryInfoData qryEntryHistoryInfo(int entry) {
        EntryHistoryInfoData data = new EntryHistoryInfoData();
        this.staticService.getUserHistory(entry).ifPresent(o ->
                data
                        .setEntry(entry)
                        .setPast(o.getPast())
                        .setChips(o.getChips())
        );
        return data;
    }

    @Cacheable(
            value = "api::qryEntryEventResult",
            key = "#event+'::'+#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntryEventResultData qryEntryEventResult(int event, int entry) {
        EntryEventResultData data = new EntryEventResultData();
        // from db
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity != null) {
            data
                    .setEvent(event)
                    .setEntry(entry)
                    .setTransfers(entryEventResultEntity.getEventTransfers())
                    .setPoints(entryEventResultEntity.getEventPoints())
                    .setTransfersCost(entryEventResultEntity.getEventTransfersCost())
                    .setNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost())
                    .setBenchPoints(entryEventResultEntity.getEventBenchPoints())
                    .setRank(entryEventResultEntity.getEventRank())
                    .setChip(StringUtils.isBlank(entryEventResultEntity.getEventChip()) ? Chip.NONE.getValue() : entryEventResultEntity.getEventChip())
                    .setValue(entryEventResultEntity.getTeamValue() / 10.0)
                    .setBank(entryEventResultEntity.getBank() / 10.0)
                    .setTeamValue((entryEventResultEntity.getTeamValue() - entryEventResultEntity.getBank()) / 10.0)
                    .setOverallPoints(entryEventResultEntity.getOverallPoints())
                    .setOverallRank(entryEventResultEntity.getOverallRank());
            data.setPickList(this.getPickListFromDB(event, data.getChip(), entryEventResultEntity.getEventPicks()));
            data.setCaptainName(this.getPlayedCaptainName(data.getPickList()));
        }
        // from fpl server
        UserPicksRes userPick = this.queryService.getUserPicks(event, entry);
        if (userPick == null) {
            return data;
        }
        // entry_event_result
        data
                .setEntry(entry)
                .setEvent(event)
                .setTransfers(userPick.getEntryHistory().getEventTransfers())
                .setPoints(userPick.getEntryHistory().getPoints())
                .setTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
                .setNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
                .setBenchPoints(userPick.getEntryHistory().getPointsOnBench())
                .setRank(userPick.getEntryHistory().getRank())
                .setChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip())
                .setValue(userPick.getEntryHistory().getValue() / 10.0)
                .setBank(userPick.getEntryHistory().getBank() / 10.0)
                .setTeamValue((userPick.getEntryHistory().getValue() - userPick.getEntryHistory().getBank()) / 10.0)
                .setOverallPoints(userPick.getEntryHistory().getTotalPoints())
                .setOverallRank(userPick.getEntryHistory().getOverallRank());
        data.setPickList(this.getPickListFromServer(event, data.getChip(), userPick.getPicks()));
        data.setCaptainName(this.getPlayedCaptainName(data.getPickList()));
        return data;
    }

    private List<ElementEventResultData> getPickListFromDB(int event, String chip, String picks) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return Lists.newArrayList();
        }
        return this.qryEntryEventPicksResult(event, chip, pickList);
    }

    private List<ElementEventResultData> getPickListFromServer(int event, String chip, List<Pick> picks) {
        List<EntryPickData> pickList = Lists.newArrayList();
        picks.forEach(o ->
                pickList.add(
                        new EntryPickData()
                                .setElement(o.getElement())
                                .setPosition(o.getPosition())
                                .setMultiplier(o.getMultiplier())
                                .setCaptain(o.isCaptain())
                                .setViceCaptain(o.isViceCaptain())
                ));
        return this.qryEntryEventPicksResult(event, chip, pickList);
    }

    private String getPlayedCaptainName(List<ElementEventResultData> picks) {
        ElementEventResultData captain = picks
                .stream()
                .filter(ElementEventResultData::isCaptain)
                .findFirst()
                .orElse(null);
        ElementEventResultData viceCaptain = picks
                .stream()
                .filter(ElementEventResultData::isViceCaptain)
                .findFirst()
                .orElse(null);
        if (captain == null || viceCaptain == null) {
            return "";
        }
        if (captain.getMinutes() == 0 && viceCaptain.getMinutes() > 0) {
            return viceCaptain.getWebName();
        }
        return captain.getWebName();
    }

    @Cacheable(
            value = "api::qryEntryEventPicksResult",
            key = "#event+'::'+#pickList.get(0).event+'::'+#pickList.get(0).entry",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<ElementEventResultData> qryEntryEventPicksResult(int event, String chip, List<EntryPickData> pickList) {
        List<ElementEventResultData> list = Lists.newArrayList();
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        // collect
        pickList.forEach(pick -> {
            int element = pick.getElement();
            if (!playerMap.containsKey(element)) {
                return;
            }
            PlayerEntity playerEntity = playerMap.get(element);
            ElementEventResultData data = BeanUtil.copyProperties(pick, ElementEventResultData.class);
            data
                    .setEvent(event)
                    .setElement(element)
                    .setElementType(playerEntity.getElementType())
                    .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()))
                    .setWebName(playerEntity.getWebName())
                    .setTeamId(playerEntity.getTeamId())
                    .setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                    .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                    .setPrice(playerEntity.getPrice() / 10.0);
            EventLiveEntity eventLiveEntity = eventLiveMap.get(element);
            if (eventLiveEntity != null) {
                data
                        .setMinutes(eventLiveEntity.getMinutes())
                        .setGoalsScored(eventLiveEntity.getGoalsScored())
                        .setAssists(eventLiveEntity.getAssists())
                        .setCleanSheets(eventLiveEntity.getCleanSheets())
                        .setGoalsConceded(eventLiveEntity.getGoalsConceded())
                        .setOwnGoals(eventLiveEntity.getOwnGoals())
                        .setPenaltiesSaved(eventLiveEntity.getPenaltiesSaved())
                        .setPenaltiesMissed(eventLiveEntity.getPenaltiesSaved())
                        .setYellowCards(eventLiveEntity.getYellowCards())
                        .setRedCards(eventLiveEntity.getRedCards())
                        .setSaves(eventLiveEntity.getSaves())
                        .setBonus(eventLiveEntity.getBonus())
                        .setBps(eventLiveEntity.getBps())
                        .setTotalPoints(eventLiveEntity.getTotalPoints())
                        .setPickActive(true);
            }
            if (!StringUtils.equalsIgnoreCase(Chip.BB.getValue(), chip) && data.getPosition() > 11) {
                data.setPickActive(false);
            }
            list.add(data);
        });
        return list;
    }

    @Cacheable(
            value = "api::qryEntryEventTransfers",
            key = "#event+'::'+#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<EntryEventTransfersData> qryEntryEventTransfers(int event, int entry) {
        List<EntryEventTransfersData> list = Lists.newArrayList();
        //prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<Integer, Integer> pointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        //collect
        this.getEntryEventTransfers(event, entry).forEach(o -> {
            int elementIn = o.getElementIn();
            int elementOut = o.getElementOut();
            EntryEventTransfersData data = new EntryEventTransfersData()
                    .setEvent(o.getEvent())
                    .setEntry(o.getEntry())
                    .setElementIn(elementIn)
                    .setElementInCost(o.getElementInCost() / 10.0)
                    .setElementInPlayed(o.getElementInPlayed())
                    .setElementOut(elementOut)
                    .setElementOutCost(o.getElementOutCost() / 10.0)
                    .setTime(o.getTime());
            PlayerEntity playerInEntity = playerMap.get(elementIn);
            if (playerInEntity != null) {
                data
                        .setElementInWebName(playerInEntity.getWebName())
                        .setElementInType(playerInEntity.getElementType())
                        .setElementInTypeName(Position.getNameFromElementType(playerInEntity.getElementType()))
                        .setElementInTeamId(playerInEntity.getTeamId())
                        .setElementInTeamName(teamNameMap.getOrDefault(String.valueOf(playerInEntity.getTeamId()), ""))
                        .setElementInTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerInEntity.getTeamId()), ""))
                        .setElementInPoints(pointsMap.getOrDefault(elementIn, 0));
            }
            PlayerEntity playerOutEntity = playerMap.get(o.getElementOut());
            if (playerInEntity != null) {
                data
                        .setElementOutWebName(playerOutEntity.getWebName())
                        .setElementOutType(playerOutEntity.getElementType())
                        .setElementOutTypeName(Position.getNameFromElementType(playerOutEntity.getElementType()))
                        .setElementOutTeamId(playerOutEntity.getTeamId())
                        .setElementOutTeamName(teamNameMap.getOrDefault(String.valueOf(playerOutEntity.getTeamId()), ""))
                        .setElementOutTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerOutEntity.getTeamId()), ""))
                        .setElementOutPoints(pointsMap.getOrDefault(elementOut, 0));
            }
            list.add(data);
        });
        return list
                .stream()
                .sorted(Comparator.comparing(EntryEventTransfersData::getTime))
                .collect(Collectors.toList());
    }

    private List<EntryEventTransfersEntity> getEntryEventTransfers(int event, int entry) {
        List<EntryEventTransfersEntity> list;
        list = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEvent, event)
                .eq(EntryEventTransfersEntity::getEntry, entry));
        if (!CollectionUtils.isEmpty(list)) {
            return list;
        }
        // collect
        this.staticService.getTransfer(entry).ifPresent(res ->
                res.forEach(o -> {
                    if (o.getEvent() != event) {
                        return;
                    }
                    list.add(
                            new EntryEventTransfersEntity()
                                    .setEvent(o.getEvent())
                                    .setEntry(o.getEntry())
                                    .setElementIn(o.getElementIn())
                                    .setElementInCost(o.getElementInCost())
                                    .setElementInPlayed(false)
                                    .setElementOut(o.getElementOut())
                                    .setElementOutCost(o.getElementOutCost())
                                    .setTime(o.getTime())
                    );
                })
        );
        return list;
    }

    @Cacheable(
            value = "api::qryEntryEventSummary",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<EntryEventResultData> qryEntryEventSummary(int entry) {
        int event = this.queryService.getCurrentEvent();
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry)
                .le(EntryEventResultEntity::getEvent, event)
                .orderByAsc(EntryEventResultEntity::getEvent));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return Lists.newArrayList();
        }
        return entryEventResultEntityList
                .stream()
                .map(o ->
                        new EntryEventResultData()
                                .setEvent(o.getEvent())
                                .setEntry(entry)
                                .setPoints(o.getEventPoints())
                                .setNetPoints(o.getEventNetPoints())
                                .setTransfersCost(o.getEventTransfersCost())
                                .setTransfers(o.getEventTransfers())
                                .setRank(o.getEventRank())
                                .setChip(o.getEventChip())
                                .setValue(o.getTeamValue() / 10.0)
                                .setBank(o.getBank() / 10.0)
                                .setTeamValue((o.getTeamValue() - o.getBank()) / 10.0)
                                .setOverallPoints(o.getOverallPoints())
                                .setOverallRank(o.getOverallRank())
                )
                .collect(Collectors.toList());
    }

    /**
     * @implNote live (do not cache)
     */
    @Override
    public List<LiveMatchData> qryLiveMatchByStatus(String playStatus) {
        // prepare
        int event = this.queryService.getCurrentEvent();
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        // next event
        if (StringUtils.equalsIgnoreCase(MatchPlayStatus.Next_Event.name(), playStatus)) {
            return this.qryNextEventMatch(event, teamNameMap, teamShortNameMap);
        }
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Collection<EventLiveEntity> eventLiveList = this.queryService.getEventLiveByEvent(event).values();
        // collect
        List<LiveMatchData> list = Lists.newArrayList();
        Map<String, Map<String, List<LiveFixtureData>>> eventLiveFixtureMap = this.redisCacheService.getEventLiveFixtureMap();
        eventLiveFixtureMap.keySet().forEach(teamId ->
                eventLiveFixtureMap.get(teamId).forEach((status, fixtureList) -> {
                    if (!StringUtils.equalsIgnoreCase(status, playStatus)) {
                        return;
                    }
                    fixtureList.forEach(o -> {
                        if (!o.isWasHome()) {
                            return;
                        }
                        LiveMatchData liveMatchData = new LiveMatchData()
                                .setMatchId(list.size() + 1)
                                .setHomeTeamId(o.getTeamId())
                                .setHomeTeamName(o.getTeamName())
                                .setHomeTeamShortName(o.getTeamShortName())
                                .setHomeScore(o.getTeamScore())
                                .setHomeTeamDataList(this.qryLiveTeamData(o.getTeamId(), eventLiveList, playerMap, teamShortNameMap))
                                .setAwayTeamId(o.getAgainstId())
                                .setAwayTeamName(o.getAgainstName())
                                .setAwayTeamShortName(o.getAgainstShortName())
                                .setAwayScore(o.getAgainstTeamScore())
                                .setAwayTeamDataList(this.qryLiveTeamData(o.getAgainstId(), eventLiveList, playerMap, teamShortNameMap))
                                .setKickoffTime(o.getKickoffTime());
                        liveMatchData.setMinutes(
                                liveMatchData
                                        .getHomeTeamDataList()
                                        .stream()
                                        .max(Comparator.comparing(ElementEventResultData::getMinutes))
                                        .map(ElementEventResultData::getMinutes)
                                        .orElse(0)
                        );
                        list.add(liveMatchData);
                    });
                }));
        return list.stream()
                .sorted(Comparator.comparing(LiveMatchData::getKickoffTime).reversed())
                .collect(Collectors.toList());
    }

    private List<LiveMatchData> qryNextEventMatch(int event, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        List<LiveMatchData> list = Lists.newArrayList();
        if (event > 38) {
            return list;
        }
        this.eventFixtureService.list(new QueryWrapper<EventFixtureEntity>().lambda()
                        .eq(EventFixtureEntity::getEvent, event + 1)
                        .orderByAsc(EventFixtureEntity::getKickoffTime))
                .forEach(o ->
                        list.add(new LiveMatchData()
                                .setMatchId(list.size() + 1)
                                .setMinutes(0)
                                .setHomeTeamId(o.getTeamH())
                                .setHomeTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
                                .setHomeTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
                                .setHomeScore(0)
                                .setHomeTeamDataList(Lists.newArrayList())
                                .setAwayTeamId(o.getTeamA())
                                .setAwayTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
                                .setAwayTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
                                .setAwayScore(0)
                                .setAwayTeamDataList(Lists.newArrayList())
                                .setKickoffTime(o.getKickoffTime())
                        )
                );
        return list;
    }

    private List<ElementEventResultData> qryLiveTeamData(int teamId, Collection<EventLiveEntity> eventLiveList, Map<Integer, PlayerEntity> playerMap, Map<String, String> teamShortNameMap) {
        List<ElementEventResultData> list = Lists.newArrayList();
        // team data
        Map<Integer, Integer> liveBonusMap = this.getLiveBonusMap(teamId);
        eventLiveList.forEach(o -> {
            if (o.getTeamId() != teamId || o.getMinutes() <= 0) {
                return;
            }
            ElementEventResultData elementEventResultData = new ElementEventResultData()
                    .setEvent(o.getEvent())
                    .setElement(o.getElement())
                    .setWebName(playerMap.containsKey(o.getElement()) ? playerMap.get(o.getElement()).getWebName() : "")
                    .setElementType(o.getElementType())
                    .setElementTypeName(Position.getNameFromElementType(o.getElementType()))
                    .setTeamId(playerMap.containsKey(o.getElement()) ? playerMap.get(o.getElement()).getTeamId() : 0)
                    .setMinutes(o.getMinutes())
                    .setGoalsScored(o.getGoalsScored())
                    .setAssists(o.getAssists())
                    .setGoalsConceded(o.getGoalsConceded())
                    .setOwnGoals(o.getOwnGoals())
                    .setPenaltiesSaved(o.getPenaltiesSaved())
                    .setPenaltiesMissed(o.getPenaltiesMissed())
                    .setYellowCards(o.getYellowCards())
                    .setRedCards(o.getRedCards())
                    .setSaves(o.getSaves())
                    .setBps(o.getBps())
                    .setTotalPoints(o.getTotalPoints());
            if (o.getBonus() > 0) {
                elementEventResultData
                        .setBonus(o.getBonus())
                        .setTotalPoints(elementEventResultData.getTotalPoints());
            } else {
                elementEventResultData
                        .setBonus(liveBonusMap.getOrDefault(o.getElement(), 0))
                        .setTotalPoints(elementEventResultData.getTotalPoints() + elementEventResultData.getBonus());
            }
            elementEventResultData.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(elementEventResultData.getTeamId()), ""));
            list.add(elementEventResultData);
        });
        return list
                .stream()
                .sorted(Comparator.comparing(ElementEventResultData::getTotalPoints).reversed())
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> getLiveBonusMap(int teamId) {
        Map<Integer, Integer> map = Maps.newHashMap();
        this.queryService.getLiveBonusCacheMap()
                .forEach((team, list) -> {
                    if (!StringUtils.equals(team, String.valueOf(teamId))) {
                        return;
                    }
                    list.forEach((element, bonus) -> map.put(Integer.valueOf(element), bonus));
                });
        return map;
    }

    /**
     * @implNote player
     */
    @Cacheable(
            value = "api::qryPlayerInfoByElementType",
            key = "#elementType",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType) {
        LinkedHashMap<String, List<PlayerInfoData>> map = Maps.newLinkedHashMap();
        Multimap<String, PlayerInfoData> multimap = HashMultimap.create();
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<Integer, Integer> totalPointsMap = this.eventLiveSummaryService.list()
                .stream()
                .collect(Collectors.toMap(EventLiveSummaryEntity::getElement, EventLiveSummaryEntity::getTotalPoints));
        // init
        this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
                        .eq(PlayerEntity::getElementType, elementType))
                .forEach(o -> {
                    PlayerInfoData data = BeanUtil.copyProperties(o, PlayerInfoData.class);
                    data
                            .setElementTypeName(Position.getNameFromElementType(o.getElementType()))
                            .setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                            .setTeamShortName(
                                    teamShortNameMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                            .setPrice(o.getPrice() / 10.0)
                            .setStartPrice(o.getStartPrice() / 10.0)
                            .setPoints(totalPointsMap.getOrDefault(o.getElement(), 0));
                    multimap.put(data.getTeamShortName(), data);
                });
        // collect
        List<String> shortNameSortedList = multimap.keySet()
                .stream()
                .sorted(Comparator.comparing(String::toUpperCase))
                .collect(Collectors.toList());
        shortNameSortedList.forEach(team ->
                map.put(team, multimap.get(team).stream()
                        .sorted(Comparator.comparing(PlayerInfoData::getPrice).reversed())
                        .collect(Collectors.toList())));
        return map;
    }

    @Cacheable(
            value = "api::qryPlayerDetailByElement",
            key = "#element",
            cacheManager = "apiCacheManager",
            unless = "#result.element eq 0"
    )
    @Override
    public PlayerDetailData qryPlayerDetailByElement(int element) {
        return this.queryService.qryPlayerDetailData(element);
    }

    @Cacheable(
            value = "api::qryTeamFixtureByShortName",
            key = "#shortName",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName) {
        int teamId = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda()
                .eq(TeamEntity::getShortName, shortName)).getId();
        return this.queryService.getEventFixtureByTeamId(teamId);
    }

    /**
     * @apiNote team
     */
    @Cacheable(
            value = "api::qryTeamList",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<TeamData> qryTeamList(String season) {
        MybatisPlusConfig.season.set(season);
        List<TeamData> list = this.teamService.list()
                .stream()
                .map(o -> BeanUtil.copyProperties(o, TeamData.class))
                .collect(Collectors.toList());
        MybatisPlusConfig.season.remove();
        return list;
    }

    /**
     * @implNote stat
     */
    @Cacheable(
            value = "api::qryPlayerValueByDate",
            key = "#date",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public Map<String, List<PlayerValueData>> qryPlayerValueByDate(String date) {
        Map<String, List<PlayerValueData>> map = Maps.newHashMap();
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        List<PlayerValueEntity> playerValueList = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                .eq(PlayerValueEntity::getChangeDate, date));
        if (CollectionUtils.isEmpty(playerValueList)) {
            return map;
        }
        List<Integer> elementList = playerValueList
                .stream()
                .map(PlayerValueEntity::getElement)
                .collect(Collectors.toList());
        Map<Integer, PlayerEntity> playerMap = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
                        .in(PlayerEntity::getElement, elementList))
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        // collect
        List<PlayerValueData> playerValueDataList = playerValueList
                .stream()
                .map(o -> this.initPlayerValueData(o, teamNameMap, teamShortNameMap, playerMap))
                .collect(Collectors.toList());
        map.put(
                ValueChangeType.Start.name(),
                playerValueDataList
                        .stream()
                        .filter(o -> StringUtils.equals(ValueChangeType.Start.name(), o.getChangeType()))
                        .collect(Collectors.toList())
        );
        map.put(
                ValueChangeType.Rise.name(),
                playerValueDataList
                        .stream()
                        .filter(o -> StringUtils.equals(ValueChangeType.Rise.name(), o.getChangeType()))
                        .collect(Collectors.toList())
        );
        map.put(
                ValueChangeType.Faller.name(),
                playerValueDataList
                        .stream()
                        .filter(o -> StringUtils.equals(ValueChangeType.Faller.name(), o.getChangeType()))
                        .collect(Collectors.toList())
        );
        return map;
    }

    @Cacheable(
            value = "api::qryPlayerValueByElement",
            key = "#element",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<PlayerValueData> qryPlayerValueByElement(int element) {
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        List<PlayerValueEntity> playerValueList = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                .eq(PlayerValueEntity::getElement, element)
                .orderByAsc(PlayerValueEntity::getChangeDate));
        if (CollectionUtils.isEmpty(playerValueList)) {
            return Lists.newArrayList();
        }
        List<Integer> elementList = playerValueList
                .stream()
                .map(PlayerValueEntity::getElement)
                .collect(Collectors.toList());
        Map<Integer, PlayerEntity> playerMap = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
                        .in(PlayerEntity::getElement, elementList))
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        // collect
        return playerValueList
                .stream()
                .map(o -> this.initPlayerValueData(o, teamNameMap, teamShortNameMap, playerMap))
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "api::qryPlayerValueByTeamId",
            key = "#teamId",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public Map<String, List<PlayerValueData>> qryPlayerValueByTeamId(int teamId) {
        Map<String, List<PlayerValueData>> map = Maps.newHashMap();
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        // element list
        List<Integer> teamElementList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
                        .eq(PlayerEntity::getTeamId, teamId))
                .stream()
                .map(PlayerEntity::getElement)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(teamElementList)) {
            return map;
        }
        List<PlayerValueEntity> playerValueList = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                .in(PlayerValueEntity::getElement, teamElementList)
                .orderByAsc(PlayerValueEntity::getElement)
                .orderByAsc(PlayerValueEntity::getChangeDate));
        if (CollectionUtils.isEmpty(playerValueList)) {
            return map;
        }
        List<Integer> elementList = playerValueList
                .stream()
                .map(PlayerValueEntity::getElement)
                .collect(Collectors.toList());
        Map<Integer, PlayerEntity> playerMap = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
                        .in(PlayerEntity::getElement, elementList))
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        // collect
        playerValueList
                .stream()
                .map(o -> this.initPlayerValueData(o, teamNameMap, teamShortNameMap, playerMap))
                .forEach(o -> {
                    String elementTypeName = o.getElementTypeName();
                    if (!map.containsKey(elementTypeName)) {
                        map.put(elementTypeName, Lists.newArrayList(o));
                    } else {
                        List<PlayerValueData> valueList = map.get(elementTypeName);
                        valueList.add(o);
                        map.put(elementTypeName, valueList);
                    }
                });
        return map;
    }

    private PlayerValueData initPlayerValueData(PlayerValueEntity playerValueEntity, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<Integer, PlayerEntity> playerMap) {
        int element = playerValueEntity.getElement();
        PlayerValueData data = BeanUtil.copyProperties(playerValueEntity, PlayerValueData.class);
        PlayerEntity playerEntity = playerMap.get(element);
        if (playerEntity != null) {
            data
                    .setWebName(playerEntity.getWebName())
                    .setTeamId(playerEntity.getTeamId())
                    .setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                    .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                    .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()))
                    .setValue(playerValueEntity.getValue() / 10.0)
                    .setChangeDate(StringUtils.joinWith("-", data.getChangeDate().substring(0, 4), data.getChangeDate().substring(4, 6), data.getChangeDate().substring(6, 8)))
                    .setLastValue(playerValueEntity.getLastValue() / 10.0);
        }
        return data;
    }

    @Cacheable(
            value = "api::qrySeasonFixture",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<List<String>> qrySeasonFixture() {
        List<List<String>> list = Lists.newArrayList();
        // prepare
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        // fixture
        this.queryService.getTeamShortNameMap().keySet()
                .stream()
                .mapToInt(Integer::parseInt)
                .sorted()
                .forEach(teamId -> {
                    List<String> elementList = Lists.newArrayList(teamShortNameMap.get(String.valueOf(teamId)));
                    List<PlayerFixtureData> fixtureList = Lists.newArrayList();
                    this.queryService.getEventFixtureByTeamId(teamId).values().forEach(fixtureList::addAll);
                    fixtureList
                            .stream()
                            .sorted(Comparator.comparing(PlayerFixtureData::getEvent)
                                    .thenComparing(PlayerFixtureData::getKickoffTime))
                            .forEach(o -> elementList.add(o.getAgainstTeamShortName()));
                    list.add(elementList);
                });
        return list;
    }

    @Cacheable(
            value = "api::qryAllLeagueName",
            key = "#season",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<String> qryAllLeagueName(String season) {
        int event = 38;
        String currentSeason = CommonUtils.getCurrentSeason();
        if (StringUtils.equals(currentSeason, season)) {
            event = this.queryService.getCurrentEvent();
        }
        MybatisPlusConfig.season.set(season);
        List<String> list = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                        .eq(LeagueEventReportEntity::getEvent, event))
                .stream()
                .map(LeagueEventReportEntity::getLeagueName)
                .distinct()
                .collect(Collectors.toList());
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Cacheable(
            value = "api::qryLeagueEventEoWebNameMap",
            key = "#season+'::'+#event+'::'+#leagueId+'::'+#leagueType",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public Map<String, String> qryLeagueEventEoWebNameMap(String season, int event, int leagueId, String leagueType) {
        Map<String, String> map = Maps.newHashMap();
        // prepare
        Map<Integer, String> webNameMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        // collect
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService
                .list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                        .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                        .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                        .eq(LeagueEventReportEntity::getEvent, event));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return map;
        }
        int size = leagueEventReportEntityList.size();
        List<Integer> elementList = Lists.newArrayList();
        leagueEventReportEntityList.forEach(o -> {
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
            if (StringUtils.equals(Chip.TC.getValue(), o.getEventChip())) {
                elementList.add(o.getCaptain());
                elementList.add(o.getCaptain());
            } else {
                elementList.add(o.getCaptain());
            }
        });
        Map<Integer, Long> countMap = elementList
                .stream()
                .collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
        countMap.forEach((element, count) -> map.put(webNameMap.getOrDefault(element, ""),
                NumberUtil.formatPercent(NumberUtil.div(count.doubleValue(), size), 1)));
        return map;
    }

    @Cacheable(
            value = "api::qryTeamSelectByLeagueName",
            key = "#season+'::'+#event+'::'+#leagueName",
            cacheManager = "apiCacheManager",
            unless = "#result.captainSelect.size() eq 0"
    )
    @Override
    public LeagueEventSelectData qryTeamSelectByLeagueName(String season, int event, String leagueName) {
        MybatisPlusConfig.season.set(season);
        LeagueEventSelectData data = new LeagueEventSelectData()
                .setEvent(event)
                .setName(leagueName);
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap(season);
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap(season);
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        // team_select
        List<LeagueEventReportEntity> leagueEventReportList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getEvent, event)
                .eq(LeagueEventReportEntity::getLeagueName, leagueName));
        int teamSize = leagueEventReportList.size();
        if (CollectionUtils.isEmpty(leagueEventReportList)) {
            return data;
        }
        // league_eo_map
        Map<String, String> leagueEventEoMap = this.qryLeagueEventEoWebNameMap(season, event, leagueEventReportList.get(0).getLeagueId(), leagueEventReportList.get(0).getLeagueType());
        // most transfer in
        List<PlayerSelectData> mostTransferIn = this.getMostTransferIn(leagueName, event, leagueEventReportList, teamSize, playerMap, teamNameMap, teamShortNameMap);
        data.setMostTransferIn(mostTransferIn);
        // most transfer out
        List<PlayerSelectData> mostTransferOut = this.getMostTransferOut(leagueName, event, leagueEventReportList, teamSize, playerMap, teamNameMap, teamShortNameMap);
        data.setMostTransferOut(mostTransferOut);
        // captain select
        List<PlayerSelectData> captainSelect = this.getCaptainSelect(leagueEventReportList, teamSize, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
        data.setCaptainSelect(captainSelect);
        // vice captain select
        List<PlayerSelectData> viceCaptainSelect = this.getViceCaptainSelect(leagueEventReportList, teamSize, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
        data.setViceCaptainSelect(viceCaptainSelect);
        // most select player
        List<PlayerSelectData> mostSelectPlayer = this.getMostSelectPlayer(leagueEventReportList, teamSize, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
        data.setMostSelectPlayer(mostSelectPlayer);
        // most select team
        Map<Integer, List<PlayerSelectData>> mostSelectTeam = this.getMostSelectTeam(leagueEventReportList, teamSize, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
        data
                .setMostSelectTeamGkp(mostSelectTeam.get(1))
                .setMostSelectTeamDef(mostSelectTeam.get(2))
                .setMostSelectTeamMid(mostSelectTeam.get(3))
                .setMostSelectTeamFwd(mostSelectTeam.get(4));
        MybatisPlusConfig.season.remove();
        return data;
    }

    private List<PlayerSelectData> getMostTransferIn(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize,
                                                     Map<Integer, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        if (event <= 1) {
            return Lists.newArrayList();
        }
        // current gw
        Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(leagueEventReportList);
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
        return this.collectSelectDataList(elementList, teamSize, 5, playerMap, teamNameMap, teamShortNameMap, Maps.newHashMap());
    }

    private List<PlayerSelectData> getMostTransferOut(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize,
                                                      Map<Integer, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        if (event <= 1) {
            return Lists.newArrayList();
        }
        // current gw
        Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(leagueEventReportList);
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
        return this.collectSelectDataList(elementList, teamSize, 5, playerMap, teamNameMap, teamShortNameMap, Maps.newHashMap());
    }

    private Map<Integer, List<Integer>> collectPreviousEntrySelectedMap(String leagueName, int event) {
        List<LeagueEventReportEntity> previousSelectList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .eq(LeagueEventReportEntity::getEvent, event - 1));
        return this.collectEntrySelectedMap(previousSelectList);
    }

    private Map<Integer, List<Integer>> collectEntrySelectedMap(List<LeagueEventReportEntity> leagueEventReportList) {
        Map<Integer, List<Integer>> teamSelectMap = Maps.newHashMap();
        leagueEventReportList.forEach(o -> {
            List<Integer> elementList = Lists.newArrayList(
                    o.getPosition1(), o.getPosition2(), o.getPosition3(), o.getPosition4(), o.getPosition5(),
                    o.getPosition6(), o.getPosition7(), o.getPosition8(), o.getPosition9(), o.getPosition10(),
                    o.getPosition11(), o.getPosition12(), o.getPosition13(), o.getPosition14(), o.getPosition15()
            );
            teamSelectMap.put(o.getEntry(), elementList);
        });
        return teamSelectMap;
    }

    private List<PlayerSelectData> getCaptainSelect(List<LeagueEventReportEntity> leagueEventReportList, int teamSize,
                                                    Map<Integer, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        List<Integer> elementList = leagueEventReportList
                .stream()
                .map(LeagueEventReportEntity::getCaptain)
                .collect(Collectors.toList());
        return this.collectSelectDataList(elementList, teamSize, 5, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
    }

    private List<PlayerSelectData> getViceCaptainSelect(List<LeagueEventReportEntity> leagueEventReportList, int teamSize,
                                                        Map<Integer, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        // collect
        List<Integer> elementList = leagueEventReportList
                .stream()
                .map(LeagueEventReportEntity::getViceCaptain)
                .collect(Collectors.toList());
        return this.collectSelectDataList(elementList, teamSize, 5, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
    }

    private List<PlayerSelectData> getMostSelectPlayer(List<LeagueEventReportEntity> leagueEventReportList, int teamSize,
                                                       Map<Integer, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        List<Integer> elementList = Lists.newArrayList();
        leagueEventReportList.forEach(o -> {
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
        return this.collectSelectDataList(elementList, teamSize, 10, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
    }

    private List<PlayerSelectData> collectSelectDataList(List<Integer> elementList, int teamSize, int limit,
                                                         Map<Integer, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        Map<Integer, Long> groupingMap = elementList
                .stream()
                .collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
        Map<Integer, Integer> result = groupingMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        return result.entrySet()
                .stream()
                .filter(o -> playerMap.containsKey(o.getKey()))
                .map(o -> this.initCollectSelectData(o.getValue(), teamSize, playerMap.get(o.getKey()), teamNameMap, teamShortNameMap, leagueEventEoMap))
                .collect(Collectors.toList());
    }

    private PlayerSelectData initCollectSelectData(int number, int teamSize, PlayerEntity playerEntity, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        return new PlayerSelectData()
                .setElement(playerEntity.getElement())
                .setWebName(playerEntity.getWebName())
                .setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                .setSelectByPercent(CommonUtils.getPercentResult(number, teamSize))
                .setEoByPercent(leagueEventEoMap.getOrDefault(playerEntity.getWebName(), ""));
    }

    private Map<Integer, List<PlayerSelectData>> getMostSelectTeam(List<LeagueEventReportEntity> leagueEventReportList, int teamSize,
                                                                   Map<Integer, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        // element list
        List<PlayerEntity> elementPlayerInfoList = Lists.newArrayList();
        leagueEventReportList.forEach(o -> {
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
            playerSelectedMap.putAll(result);
        });
        // add key:element_type
        Map<Integer, Map<Integer, Integer>> elementTypeMap = this.collectPlayerSelectedMap(playerSelectedMap, playerMap); // element_type -> elementCountMap
        // sort by select
        LinkedHashMap<Integer, Integer> elementSelectedSortMap = playerSelectedMap.entrySet() // element -> count(sort by count)
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        List<PlayerEntity> elementList = Lists.newArrayList();
        elementSelectedSortMap.forEach((k, v) -> elementList.add(playerMap.get(k)));
        // select line up
        Map<Integer, List<PlayerSelectData>> map = Maps.newHashMap(); // element_type -> list
        LinkedHashMap<Integer, Integer> lineupMap = this.getLineupMapByElementList(elementTypeMap, elementList); // position -> element
        lineupMap.forEach((position, element) -> {
            long count = playerSelectedMap.get(element);
            PlayerEntity playerEntity = playerMap.get(element);
            int elementType = playerEntity.getElementType();
            List<PlayerSelectData> valueList = Lists.newArrayList();
            if (map.containsKey(elementType)) {
                valueList = map.get(elementType);
            }
            valueList.add(
                    new PlayerSelectData()
                            .setElement(playerEntity.getElement())
                            .setWebName(playerEntity.getWebName())
                            .setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                            .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                            .setSelectByPercent(CommonUtils.getPercentResult((int) count, teamSize))
                            .setEoByPercent(leagueEventEoMap.getOrDefault(playerEntity.getWebName(), ""))

            );
            map.put(elementType, valueList);
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
        // line up
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

    @Cacheable(
            value = "api::qryPlayerInfo",
            key = "#season+'::'+#code",
            cacheManager = "apiCacheManager",
            unless = "#result.element eq 0"
    )
    @Override
    public PlayerInfoData qryPlayerInfo(String season, int code) {
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda()
                .eq(PlayerEntity::getCode, code));
        if (playerEntity == null) {
            return new PlayerInfoData();
        }
        EventLiveSummaryEntity eventLiveSummaryEntity = this.queryService.qryEventLiveSummary(season, playerEntity.getElement());
        MybatisPlusConfig.season.remove();
        return new PlayerInfoData()
                .setElement(playerEntity.getElement())
                .setCode(playerEntity.getCode())
                .setWebName(playerEntity.getWebName())
                .setElementType(playerEntity.getElementType())
                .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()))
                .setTeamId(playerEntity.getTeamId())
                .setTeamName(this.queryService.getTeamNameByTeam(season, playerEntity.getTeamId()))
                .setTeamShortName(this.queryService.getTeamShortNameByTeam(season, playerEntity.getTeamId()))
                .setPrice(playerEntity.getPrice() / 10.0)
                .setStartPrice(playerEntity.getStartPrice() / 10.0)
                .setPoints(eventLiveSummaryEntity == null ? 0 : eventLiveSummaryEntity.getTotalPoints());
    }

    @Cacheable(
            value = "api::qryPlayerSummary",
            key = "#season+'::'+#code",
            cacheManager = "apiCacheManager",
            unless = "#result.element eq 0"
    )
    @Override
    public PlayerSummaryData qryPlayerSummary(String season, int code) {
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda()
                .eq(PlayerEntity::getCode, code));
        if (playerEntity == null) {
            return new PlayerSummaryData();
        }
        MybatisPlusConfig.season.remove();
        return new PlayerSummaryData()
                .setElement(playerEntity.getElement())
                .setCode(playerEntity.getCode())
                .setPrice(playerEntity.getPrice() / 10.0)
                .setElementType(playerEntity.getElementType())
                .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()))
                .setWebName(playerEntity.getWebName())
                .setTeamId(playerEntity.getTeamId())
                .setTeamName(this.queryService.getTeamNameByTeam(season, playerEntity.getTeamId()))
                .setTeamShortName(this.queryService.getTeamShortNameByTeam(season, playerEntity.getTeamId()))
                .setDetailData(this.queryService.qryPlayerDetailData(season, playerEntity.getElement()))
                .setFixtureList(this.queryService.qryPlayerFixtureList(season, playerEntity.getTeamId(), -1, -1));
    }

    @Cacheable(
            value = "api::qryTeamSummary",
            key = "#season+'::'+#name",
            cacheManager = "apiCacheManager",
            unless = "#result.teamId eq 0"
    )
    @Override
    public TeamSummaryData qryTeamSummary(String season, String name) {
        TeamSummaryData data = new TeamSummaryData();
        MybatisPlusConfig.season.set(season);
        TeamEntity teamEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda()
                .eq(TeamEntity::getName, name));
        if (teamEntity == null) {
            return data;
        }
        int teamId = teamEntity.getId();
        List<PlayerEntity> playerList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
                .eq(PlayerEntity::getTeamId, teamId));
        if (CollectionUtils.isEmpty(playerList)) {
            return data;
        }
        MybatisPlusConfig.season.remove();
        List<Integer> playerElementList = playerList
                .stream()
                .map(PlayerEntity::getElement)
                .collect(Collectors.toList());
        Map<Integer, String> webNameMap = playerList
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        data
                .setTeamId(teamId)
                .setSeason(season)
                .setTeamName(teamEntity.getName())
                .setTeamShortName(teamEntity.getShortName())
                .setPlayerMap(this.qryTeamPlayerDetailMap(playerList))
                .setFixtureList(this.queryService.qryPlayerFixtureList(season, teamId, -1, -1))
                .setCornersAndIndirectFreekicksOrders(Lists.newArrayList())
                .setDirectFreekicksOrders(Lists.newArrayList())
                .setPenaltiesOrders(Lists.newArrayList());
        if (StringUtils.equals(season, "2122")) {
            int event = this.queryService.getCurrentEvent();
            if (event <= 0) {
                event = 1;
            }
            List<PlayerStatEntity> playerStatList = this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                    .eq(PlayerStatEntity::getEvent, event)
                    .in(PlayerStatEntity::getElement, playerElementList));
            data
                    .setCornersAndIndirectFreekicksOrders(
                            playerStatList
                                    .stream()
                                    .filter(o -> o.getCornersAndIndirectFreekicksOrder() > 0)
                                    .sorted(Comparator.comparing(PlayerStatEntity::getCornersAndIndirectFreekicksOrder))
                                    .map(o -> webNameMap.getOrDefault(o.getElement(), ""))
                                    .collect(Collectors.toList())
                    )
                    .setDirectFreekicksOrders(
                            playerStatList
                                    .stream()
                                    .filter(o -> o.getDirectFreekicksOrder() > 0)
                                    .sorted(Comparator.comparing(PlayerStatEntity::getDirectFreekicksOrder))
                                    .map(o -> webNameMap.getOrDefault(o.getElement(), ""))
                                    .collect(Collectors.toList())
                    )
                    .setPenaltiesOrders(
                            playerStatList
                                    .stream()
                                    .filter(o -> o.getPenaltiesOrder() > 0)
                                    .sorted(Comparator.comparing(PlayerStatEntity::getPenaltiesOrder))
                                    .map(o -> webNameMap.getOrDefault(o.getElement(), ""))
                                    .collect(Collectors.toList())
                    );
        }
        List<PlayerDetailData> playerDetailDataList = Lists.newArrayList();
        data.getPlayerMap().values().forEach(o -> o.forEach(i -> playerDetailDataList.add(i.getDetailData())));
        if (CollectionUtils.isEmpty(playerDetailDataList)) {
            return data;
        }
        List<PlayerFixtureData> fixtureList = data.getFixtureList();
        if (CollectionUtils.isEmpty(fixtureList)) {
            return data;
        }
        data.setDetailData(
                new TeamDetailData()
                        .setTeamId(data.getTeamId())
                        .setSeason(data.getSeason())
                        .setWin(
                                (int) fixtureList
                                        .stream()
                                        .filter(o -> !o.isBgw() || o.isFinished())
                                        .filter(o -> StringUtils.equalsIgnoreCase("W", o.getResult()))
                                        .count()
                        )
                        .setLose(
                                (int) fixtureList
                                        .stream()
                                        .filter(o -> !o.isBgw() || o.isFinished())
                                        .filter(o -> StringUtils.equalsIgnoreCase("L", o.getResult()))
                                        .count()
                        )
                        .setDraw(
                                (int) fixtureList
                                        .stream()
                                        .filter(o -> !o.isBgw() || o.isFinished())
                                        .filter(o -> StringUtils.equalsIgnoreCase("D", o.getResult()))
                                        .count()
                        )
                        .setForm(this.getTeamForm(fixtureList))
                        .setGoalsScored(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getGoalsScored)
                                        .sum()
                        )
                        .setAssists(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getAssists)
                                        .sum()
                        )
                        .setCleanSheets(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getCleanSheets)
                                        .sum()
                        )
                        .setGoalsConceded(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getGoalsConceded)
                                        .sum()
                        )
                        .setYellowCards(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getYellowCards)
                                        .sum()
                        )
                        .setRedCards(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getRedCards)
                                        .sum()
                        )
                        .setPenaltiesSaved(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getPenaltiesSaved)
                                        .sum()
                        )
                        .setPenaltiesMissed(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getPenaltiesMissed)
                                        .sum()
                        )
                        .setSaves(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getSaves)
                                        .sum()
                        )
                        .setBonus(
                                playerDetailDataList
                                        .stream()
                                        .mapToInt(PlayerDetailData::getBonus)
                                        .sum()
                        )
        );
        return data;
    }

    private String getTeamForm(List<PlayerFixtureData> fixtureList) {
        fixtureList = fixtureList
                .stream()
                .filter(o -> !o.isBgw() || o.isFinished())
                .filter(o -> !StringUtils.isEmpty(o.getResult()))
                .sorted(Comparator.comparing(PlayerFixtureData::getEvent))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fixtureList)) {
            return "";
        }
        if (fixtureList.size() > 5) {
            fixtureList = fixtureList
                    .stream()
                    .sorted(Comparator.comparing(PlayerFixtureData::getEvent).reversed())
                    .limit(5)
                    .sorted(Comparator.comparing(PlayerFixtureData::getEvent))
                    .collect(Collectors.toList());
        }
        StringBuilder builder = new StringBuilder();
        for (PlayerFixtureData data :
                fixtureList) {
            builder.append(data.getResult()).append(",");
        }
        return builder.substring(0, builder.lastIndexOf(","));
    }

    private Map<Integer, List<PlayerSummaryData>> qryTeamPlayerDetailMap(List<PlayerEntity> playerList) {
        Multimap<Integer, PlayerSummaryData> multimap = HashMultimap.create();
        playerList
                .stream()
                .sorted(Comparator.comparing(PlayerEntity::getElementType)
                        .thenComparing(PlayerEntity::getElement))
                .forEach(o -> multimap.put(o.getElementType(),
                        new PlayerSummaryData()
                                .setElement(o.getElement())
                                .setCode(o.getCode())
                                .setPrice(o.getPrice() / 10.0)
                                .setElementType(o.getElementType())
                                .setElementTypeName(Position.getNameFromElementType(o.getElementType()))
                                .setWebName(o.getWebName())
                                .setDetailData(this.queryService.qryPlayerDetailData(o.getElement()))
                ));
        Map<Integer, List<PlayerSummaryData>> map = Maps.newHashMap();
        // gkp
        map.put(1, multimap.get(1)
                .stream()
                .sorted(Comparator.comparing(PlayerSummaryData::getPrice))
                .collect(Collectors.toList())
        );
        // def
        map.put(2, multimap.get(2)
                .stream()
                .sorted(Comparator.comparing(PlayerSummaryData::getPrice).reversed())
                .collect(Collectors.toList())
        );
        // mid
        map.put(3, multimap.get(3)
                .stream()
                .sorted(Comparator.comparing(PlayerSummaryData::getPrice).reversed())
                .collect(Collectors.toList())
        );
        // fwd
        map.put(4, multimap.get(4)
                .stream()
                .sorted(Comparator.comparing(PlayerSummaryData::getPrice).reversed())
                .collect(Collectors.toList())
        );
        return map;
    }

    /**
     * @implNote scout
     */
    @Cacheable(
            value = "api::qryScoutEntry",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public Map<String, String> qryScoutEntry() {
        Map<String, String> map = Maps.newHashMap();
        RedisUtils.getHashByKey("scoutEntry").forEach((k, v) -> map.put(k.toString(), v.toString()));
        return map;
    }

    // do not cache
    @Override
    public EventScoutData qryEventScoutPickResult(int event, int entry) {
        EventScoutData data = new EventScoutData();
        // prepare
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        ScoutEntity scoutEntity = this.scoutService.getOne(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, event)
                .eq(ScoutEntity::getEntry, entry));
        if (scoutEntity != null) {
            return this.initScoutData(scoutEntity, playerMap, teamShortNameMap);
        }
        scoutEntity = this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                        .lt(ScoutEntity::getEvent, event)
                        .eq(ScoutEntity::getEntry, entry))
                .stream()
                .max(Comparator.comparing(ScoutEntity::getEvent))
                .orElse(new ScoutEntity());
        data
                .setEvent(event)
                .setEntry(entry)
                .setScoutName(scoutEntity.getScoutName())
                .setTransfers(0)
                .setLeftTransfers(this.calcEventScoutLeftTransfers(entry, event))
                .setGkpInfo(this.initScoutElementData(scoutEntity.getGkp(), scoutEntity.getGkpTeamId(), 0, playerMap.get(scoutEntity.getGkp()), teamShortNameMap))
                .setDefInfo(this.initScoutElementData(scoutEntity.getDef(), scoutEntity.getDefTeamId(), 0, playerMap.get(scoutEntity.getDef()), teamShortNameMap))
                .setMidInfo(this.initScoutElementData(scoutEntity.getMid(), scoutEntity.getMidTeamId(), 0, playerMap.get(scoutEntity.getMid()), teamShortNameMap))
                .setFwdInfo(this.initScoutElementData(scoutEntity.getFwd(), scoutEntity.getFwdTeamId(), 0, playerMap.get(scoutEntity.getFwd()), teamShortNameMap))
                .setCaptainInfo(this.initScoutElementData(scoutEntity.getCaptain(), scoutEntity.getCaptainTeamId(), 0, playerMap.get(scoutEntity.getCaptain()), teamShortNameMap))
                .setReason(scoutEntity.getReason())
                .setEventPoints(0)
                .setTotalPoints(0);
        return data;
    }

    private int calcEventScoutLeftTransfers(int entry, int event) {
        if (event <= 0 || event > 38 || event == 1 || event == 2) {
            return -1;
        }
        ScoutEntity scoutEntity = this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                        .lt(ScoutEntity::getEvent, event)
                        .eq(ScoutEntity::getEntry, entry))
                .stream()
                .max(Comparator.comparing(ScoutEntity::getEvent))
                .orElse(null);
        if (scoutEntity == null) {
            return -1;
        }
        int leftTransfers = scoutEntity.getLeftTransfers();
        if (leftTransfers == -1) {
            leftTransfers = 0;
        }
        return Math.min(leftTransfers + 1, 4);
    }

    @Cacheable(
            value = "api::qryEventScoutResult",
            key = "#event",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<EventScoutData> qryEventScoutResult(int event) {
        if (event == 0) {
            return this.qrySeasonScoutResult();
        }
        if (event > 38) {
            return Lists.newArrayList();
        }
        // prepare
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        // return
        return this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                        .eq(ScoutEntity::getEvent, event))
                .stream()
                .map(o -> this.initScoutData(o, playerMap, teamShortNameMap))
                .sorted(Comparator.comparing(EventScoutData::getEventPoints).reversed())
                .collect(Collectors.toList());
    }

    private List<EventScoutData> qrySeasonScoutResult() {
        Multimap<Integer, ScoutEntity> map = HashMultimap.create();
        this.scoutService.list()
                .stream()
                .filter(o -> o.getEventPoints() > 0)
                .forEach(o -> map.put(o.getEntry(), o));
        List<EventScoutData> list = Lists.newArrayList();
        map.keySet().forEach(entry -> {
            Collection<ScoutEntity> scoutList = map.get(entry);
            if (CollectionUtils.isEmpty(scoutList)) {
                return;
            }
            ScoutEntity lastScout = scoutList
                    .stream()
                    .max(Comparator.comparing(ScoutEntity::getEvent))
                    .orElse(null);
            if (lastScout == null) {
                return;
            }
            list.add(
                    new EventScoutData()
                            .setEvent(0)
                            .setEntry(entry)
                            .setScoutName(lastScout.getScoutName())
                            .setGkpInfo(
                                    new PlayerInfoData()
                                            .setPoints(
                                                    scoutList
                                                            .stream()
                                                            .mapToInt(ScoutEntity::getGkpPoints)
                                                            .sum()
                                            )
                            )
                            .setDefInfo(
                                    new PlayerInfoData()
                                            .setPoints(
                                                    scoutList
                                                            .stream()
                                                            .mapToInt(ScoutEntity::getDefPoints)
                                                            .sum()
                                            )
                            )
                            .setMidInfo(
                                    new PlayerInfoData()
                                            .setPoints(
                                                    scoutList
                                                            .stream()
                                                            .mapToInt(ScoutEntity::getMidPoints)
                                                            .sum()
                                            )
                            )
                            .setFwdInfo(
                                    new PlayerInfoData()
                                            .setPoints(
                                                    scoutList
                                                            .stream()
                                                            .mapToInt(ScoutEntity::getFwdPoints)
                                                            .sum()
                                            )
                            )
                            .setCaptainInfo(
                                    new PlayerInfoData()
                                            .setPoints(
                                                    scoutList
                                                            .stream()
                                                            .mapToInt(ScoutEntity::getCaptainPoints)
                                                            .sum()
                                            )
                            )
                            .setReason("")
                            .setEventPoints(lastScout.getEventPoints())
                            .setTotalPoints(lastScout.getTotalPoints())
            );
        });
        return list
                .stream()
                .sorted(Comparator.comparing(EventScoutData::getTotalPoints).reversed())
                .collect(Collectors.toList());
    }

    private EventScoutData initScoutData(ScoutEntity scoutEntity, Map<Integer, PlayerEntity> playerMap, Map<String, String> teamShortNameMap) {
        return new EventScoutData()
                .setEvent(scoutEntity.getEvent())
                .setEntry(scoutEntity.getEntry())
                .setScoutName(scoutEntity.getScoutName())
                .setTransfers(scoutEntity.getTransfers())
                .setLeftTransfers(scoutEntity.getLeftTransfers())
                .setGkpInfo(this.initScoutElementData(scoutEntity.getGkp(), scoutEntity.getGkpTeamId(), scoutEntity.getGkpPoints(), playerMap.get(scoutEntity.getGkp()), teamShortNameMap))
                .setDefInfo(this.initScoutElementData(scoutEntity.getDef(), scoutEntity.getDefTeamId(), scoutEntity.getDefPoints(), playerMap.get(scoutEntity.getDef()), teamShortNameMap))
                .setMidInfo(this.initScoutElementData(scoutEntity.getMid(), scoutEntity.getMidTeamId(), scoutEntity.getMidPoints(), playerMap.get(scoutEntity.getMid()), teamShortNameMap))
                .setFwdInfo(this.initScoutElementData(scoutEntity.getFwd(), scoutEntity.getFwdTeamId(), scoutEntity.getFwdPoints(), playerMap.get(scoutEntity.getFwd()), teamShortNameMap))
                .setCaptainInfo(this.initScoutElementData(scoutEntity.getCaptain(), scoutEntity.getCaptainTeamId(), scoutEntity.getCaptainPoints(), playerMap.get(scoutEntity.getCaptain()), teamShortNameMap))
                .setReason(scoutEntity.getReason())
                .setEventPoints(scoutEntity.getEventPoints())
                .setTotalPoints(scoutEntity.getTotalPoints());
    }

    private PlayerInfoData initScoutElementData(int element, int teamId, int points, PlayerEntity playerEntity, Map<String, String> teamShortNameMap) {
        return new PlayerInfoData()
                .setElement(element)
                .setWebName(playerEntity.getWebName())
                .setTeamId(teamId)
                .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), ""))
                .setPrice(playerEntity.getPrice() / 10.0)
                .setPoints(points);
    }

    /**
     * @implNote tournament
     */
    @Cacheable(
            value = "api::qryEntryTournamentEntry",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<Integer> qryEntryTournamentEntry(int entry) {
        return this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                        .eq(TournamentEntryEntity::getEntry, entry))
                .stream()
                .map(TournamentEntryEntity::getTournamentId)
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "api::qryEntryPointsRaceTournament",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<TournamentInfoData> qryEntryPointsRaceTournament(int entry) {
        return this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                        .in(TournamentInfoEntity::getId, this.qryEntryTournamentEntry(entry)))
                .stream()
                .filter(o -> StringUtils.equalsIgnoreCase(GroupMode.Points_race.name(), o.getGroupMode()))
                .map(o -> BeanUtil.copyProperties(o, TournamentInfoData.class))
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "api::qryTournamentInfoById",
            key = "#id",
            cacheManager = "apiCacheManager",
            unless = "#result.id eq 0"
    )
    @Override
    public TournamentInfoData qryTournamentInfo(int id) {
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getById(id);
        if (tournamentInfoEntity == null) {
            return new TournamentInfoData();
        }
        return BeanUtil.copyProperties(tournamentInfoEntity, TournamentInfoData.class);
    }

    @Cacheable(
            value = "api::qryTournamentEventResult",
            key = "#event+'::'+#tournamentId",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<EntryEventResultData> qryTournamentEventResult(int event, int tournamentId) {
        // entry list
        List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                        .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, Integer> eventGroupRankMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                        .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentPointsGroupResultEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, TournamentPointsGroupResultEntity::getEventGroupRank));
        Map<Integer, Integer> eventTournamentRankMap = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                        .eq(TournamentGroupEntity::getTournamentId, tournamentId))
                .stream()
                .collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getGroupRank));
        // entry_event_result
        return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .map(o -> this.initEntryEventResultData(event, o, eventGroupRankMap, eventTournamentRankMap, teamNameMap, teamShortNameMap, playerMap, eventLiveMap))
                .sorted(Comparator.comparing(EntryEventResultData::getPoints).reversed())
                .collect(Collectors.toList());
    }

    private List<ElementEventResultData> qryTournamentEntryPickList(int event, String eventPicks,
                                                                    Map<String, String> teamNameMap, Map<String, String> teamShortNameMap,
                                                                    Map<Integer, PlayerEntity> playerMap, Map<Integer, EventLiveEntity> eventLiveMap) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(eventPicks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return Lists.newArrayList();
        }
        List<ElementEventResultData> list = Lists.newArrayList();
        pickList.forEach(o -> {
            int element = o.getElement();
            if (!playerMap.containsKey(element)) {
                return;
            }
            PlayerEntity playerEntity = playerMap.get(element);
            ElementEventResultData data = BeanUtil.copyProperties(o, ElementEventResultData.class);
            data
                    .setEvent(event)
                    .setElement(element)
                    .setElementType(playerEntity.getElementType())
                    .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()))
                    .setWebName(playerEntity.getWebName())
                    .setTeamId(playerEntity.getTeamId())
                    .setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                    .setTeamShortName(
                            teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                    .setPrice(playerEntity.getPrice() / 10.0);
            EventLiveEntity eventLiveEntity = eventLiveMap.get(element);
            if (eventLiveEntity != null) {
                data
                        .setMinutes(eventLiveEntity.getMinutes())
                        .setGoalsScored(eventLiveEntity.getGoalsScored())
                        .setAssists(eventLiveEntity.getAssists())
                        .setCleanSheets(eventLiveEntity.getCleanSheets())
                        .setGoalsConceded(eventLiveEntity.getGoalsConceded())
                        .setOwnGoals(eventLiveEntity.getOwnGoals())
                        .setPenaltiesSaved(eventLiveEntity.getPenaltiesSaved())
                        .setPenaltiesMissed(eventLiveEntity.getPenaltiesSaved())
                        .setYellowCards(eventLiveEntity.getYellowCards())
                        .setRedCards(eventLiveEntity.getRedCards())
                        .setSaves(eventLiveEntity.getSaves())
                        .setBonus(eventLiveEntity.getBonus())
                        .setBps(eventLiveEntity.getBps());
            }
            list.add(data);
        });
        return list;
    }

    private EntryEventResultData initEntryEventResultData(int event, EntryEventResultEntity entryEventResultEntity,
                                                          Map<Integer, Integer> eventGroupRankMap, Map<Integer, Integer> eventTournamentRankMap,
                                                          Map<String, String> teamNameMap, Map<String, String> teamShortNameMap,
                                                          Map<Integer, PlayerEntity> playerMap, Map<Integer, EventLiveEntity> eventLiveMap) {
        int entry = entryEventResultEntity.getEntry();
        EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(entry);
        return new EntryEventResultData()
                .setEvent(event)
                .setEntry(entry)
                .setEntryName(entryInfoEntity == null ? "" : entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity == null ? "" : entryInfoEntity.getPlayerName())
                .setTransfers(entryEventResultEntity.getEventTransfers())
                .setPoints(entryEventResultEntity.getEventPoints())
                .setTransfersCost(entryEventResultEntity.getEventTransfersCost())
                .setNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost())
                .setBenchPoints(entryEventResultEntity.getEventBenchPoints())
                .setRank(entryEventResultEntity.getEventRank())
                .setChip(StringUtils.isBlank(entryEventResultEntity.getEventChip()) ? Chip.NONE.getValue() : entryEventResultEntity.getEventChip())
                .setPlayedCaptain(entryEventResultEntity.getPlayedCaptain())
                .setCaptainName(playerMap.get(entryEventResultEntity.getPlayedCaptain()).getWebName())
                .setCaptainPoints(eventLiveMap.get(entryEventResultEntity.getPlayedCaptain()).getTotalPoints())
                .setValue(entryEventResultEntity.getTeamValue() / 10.0)
                .setBank(entryEventResultEntity.getBank() / 10.0)
                .setTeamValue((entryEventResultEntity.getTeamValue() - entryEventResultEntity.getBank()) / 10.0)
                .setOverallPoints(entryEventResultEntity.getOverallPoints())
                .setOverallRank(entryEventResultEntity.getOverallRank())
                .setEventTournamentRank(eventGroupRankMap.getOrDefault(entry, 0))
                .setTournamentRank(eventTournamentRankMap.getOrDefault(entry, 0))
                .setPickList(this.qryTournamentEntryPickList(event, entryEventResultEntity.getEventPicks(), teamNameMap, teamShortNameMap, playerMap, eventLiveMap));
    }

    @Cacheable(
            value = "api::qryTournamentEventSearchResult",
            key = "#event+'::'+#tournamentId+'::'+#element",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<EntryEventResultData> qryTournamentEventSearchResult(int event, int tournamentId, int element) {
        // entry list
        List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                        .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, Integer> eventGroupRankMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                        .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentPointsGroupResultEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, TournamentPointsGroupResultEntity::getEventGroupRank));
        Map<Integer, Integer> eventTournamentRankMap = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                        .eq(TournamentGroupEntity::getTournamentId, tournamentId))
                .stream()
                .collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getGroupRank));
        // entry_event_result
        return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .filter(o -> this.entryContainElement(element, o.getEventPicks(), o.getEventChip()))
                .map(o -> this.initEntryEventResultData(event, o, eventGroupRankMap, eventTournamentRankMap, teamNameMap, teamShortNameMap, playerMap, eventLiveMap))
                .sorted(Comparator.comparing(EntryEventResultData::getPoints).reversed())
                .collect(Collectors.toList());
    }

    private boolean entryContainElement(int element, String eventPicks, String eventChip) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(eventPicks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return false;
        }
        for (EntryPickData data :
                pickList) {
            if (!StringUtils.equalsIgnoreCase(Chip.BB.getValue(), eventChip)) {
                if (data.getPosition() > 12) {
                    continue;
                }
            }
            if (data.getElement() == element) {
                return true;
            }
        }
        return false;
    }

    @Cacheable(
            value = "api::qryTournamentEventSummary",
            key = "#event+'::'+#tournamentId",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId) {
        return this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                        .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentPointsGroupResultEntity::getEvent, event)
                        .orderByAsc(TournamentPointsGroupResultEntity::getEntry))
                .stream()
                .map(o -> {
                    TournamentPointsGroupEventResultData data = new TournamentPointsGroupEventResultData()
                            .setTournamentId(tournamentId)
                            .setEvent(o.getEvent())
                            .setEntry(o.getEntry())
                            .setGroupRank(o.getEventGroupRank())
                            .setPoints(o.getEventPoints())
                            .setCost(o.getEventCost())
                            .setNetPoints(o.getEventNetPoints())
                            .setRank(o.getEventRank());
                    EntryInfoData entryInfoData = this.qryEntryInfo(o.getEntry());
                    if (entryInfoData != null) {
                        data
                                .setEntryName(entryInfoData.getEntryName())
                                .setPlayerName(entryInfoData.getPlayerName());
                    }
                    return data;
                })
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "api::qryTournamentEntryEventSummary",
            key = "#tournamentId+'::'+#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry) {
        int event = this.queryService.getCurrentEvent();
        return this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                        .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentPointsGroupResultEntity::getEntry, entry)
                        .le(TournamentPointsGroupResultEntity::getEvent, event)
                        .orderByAsc(TournamentPointsGroupResultEntity::getEntry)
                        .orderByAsc(TournamentPointsGroupResultEntity::getEvent))
                .stream()
                .map(o ->
                        new TournamentPointsGroupEventResultData()
                                .setTournamentId(tournamentId)
                                .setEvent(o.getEvent())
                                .setEntry(o.getEntry())
                                .setGroupRank(o.getEventGroupRank())
                                .setPoints(o.getEventPoints())
                                .setCost(o.getEventCost())
                                .setNetPoints(o.getEventNetPoints())
                                .setRank(o.getEventRank())
                )
                .collect(Collectors.toList());
    }

}
