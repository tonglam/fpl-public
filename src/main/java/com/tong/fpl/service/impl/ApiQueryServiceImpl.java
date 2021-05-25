package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.*;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.summary.*;
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
    private final IQueryService queryService;
    private final IRedisCacheService redisCacheService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final PlayerValueService playerValueService;
    private final EventFixtureService eventFixtureService;
    private final EventLiveService eventLiveService;
    private final EntryInfoService entryInfoService;
    private final EntryEventTransfersService entryEventTransfersService;
    private final EntryEventResultService entryEventResultService;
    private final ScoutService scoutService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentInfoService tournamentInfoService;
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
            String key = StringUtils.joinWith("::", "AverageScore", event);
            int score = (int) RedisUtils.getValueByKey(key).orElse(0);
            map.put(String.valueOf(event), score);
        });
        return map;
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
        LambdaQueryWrapper<LeagueEventReportEntity> queryWrapper = new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getEvent, this.queryService.getCurrentEvent());
        if (StringUtils.isNotBlank(param.getEntryName())) {
            queryWrapper.like(LeagueEventReportEntity::getEntryName, param.getEntryName());
            list.addAll(this.fuzzyQueryLeagueReport(queryWrapper));
        }
        if (StringUtils.isNotBlank(param.getPlayerName())) {
            queryWrapper.like(LeagueEventReportEntity::getPlayerName, param.getPlayerName());
            list.addAll(this.fuzzyQueryLeagueReport(queryWrapper));
        }
        return list
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private List<EntryInfoData> fuzzyQueryLeagueReport(LambdaQueryWrapper<LeagueEventReportEntity> queryWrapper) {
        return this.leagueEventReportService.list(queryWrapper)
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
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
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
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
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
                        .setElementInName(playerInEntity.getWebName())
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
                        .setElementOutName(playerOutEntity.getWebName())
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
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
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
     * @implNote team
     */
    @Override
    public Map<String, String> getTeamNameMap() {
        return this.redisCacheService.getTeamNameMap(CommonUtils.getCurrentSeason());
    }

    @Override
    public Map<String, String> getTeamShortNameMap() {
        return this.redisCacheService.getTeamShortNameMap(CommonUtils.getCurrentSeason());
    }

    /**
     * @implNote player
     */
    @Cacheable(
            value = "api::qryPlayerInfoByElement",
            key = "#event+'::'+#element",
            cacheManager = "apiCacheManager",
            unless = "#result.element eq 0"
    )
    @Override
    public PlayerInfoData qryPlayerInfoByElement(int event, int element) {
        PlayerEntity playerEntity = this.playerService.getById(element);
        if (playerEntity == null) {
            return new PlayerInfoData();
        }
        EventLiveEntity eventLiveEntity = this.queryService.qryEventLive(event, element);
        return BeanUtil.copyProperties(playerEntity, PlayerInfoData.class)
                .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()))
                .setTeamName(this.getTeamNameByTeam(playerEntity.getTeamId()))
                .setTeamShortName(this.getShortTeamNameByTeam(playerEntity.getTeamId()))
                .setPrice(playerEntity.getPrice() / 10.0)
                .setPoints(eventLiveEntity == null ? 0 : eventLiveEntity.getTotalPoints());
    }

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
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
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
                            .setPrice(o.getPrice() / 10.0);
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
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
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
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
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
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
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
                    .setLastValue(playerValueEntity.getLastValue() / 10.0);
        }
        return data;
    }

    @Cacheable(
            value = "api::qryAllLeagueName",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<String> qryAllLeagueName() {
        int event = this.queryService.getCurrentEvent();
        return this.leagueEventReportService.getBaseMapper().qryLeagueNameListByEvent(event);
    }

    @Cacheable(
            value = "api::qryLeagueEventEoWebNameMap",
            key = "#event+'::'+#leagueId+'::'+#leagueType",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public Map<String, String> qryLeagueEventEoWebNameMap(int event, int leagueId, String leagueType) {
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
            key = "#event+'::'+#leagueName",
            cacheManager = "apiCacheManager",
            unless = "#result.captainSelectedMap.size() eq 0"
    )
    @Override
    public LeagueStatData qryTeamSelectByLeagueName(int event, String leagueName) {
        LeagueStatData data = new LeagueStatData().setEvent(event).setName(leagueName);
        // player_info
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
        Map<String, String> leagueEventEoMap = this.qryLeagueEventEoWebNameMap(event, leagueEventReportList.get(0).getLeagueId(), leagueEventReportList.get(0).getLeagueType());
        // most transfer in
        LinkedHashMap<String, String> mostTransferInMap = this.getMostTransferInMap(leagueName, event, leagueEventReportList, teamSize, playerMap);
        data.setMostTransferIn(mostTransferInMap);
        // most transfer out
        LinkedHashMap<String, String> mostTransferOutMap = this.getMostTransferOutMap(leagueName, event, leagueEventReportList, teamSize, playerMap);
        data.setMostTransferOut(mostTransferOutMap);
        // captain selected
        LinkedHashMap<String, String> captainSelectedMap = this.getCaptainSelectedMap(leagueEventReportList, teamSize, playerMap);
        data.setCaptainSelectedMap(captainSelectedMap);
        // captain selected eo
        LinkedHashMap<String, String> captainSelectedEoMap = this.getCaptainSelectedEoMap(captainSelectedMap, leagueEventEoMap);
        data.setCaptainSelectedEoMap(captainSelectedEoMap);
        // vice captain selected
        LinkedHashMap<String, String> viceCaptainSelectedMap = this.getViceCaptainSelectedMap(leagueEventReportList, teamSize, playerMap);
        data.setViceCaptainSelectedMap(viceCaptainSelectedMap);
        // top selected player
        LinkedHashMap<String, String> topSelectedPlayerMap = this.getTopSelectedPlayerMap(leagueEventReportList, teamSize, playerMap);
        data.setTopSelectedPlayerMap(topSelectedPlayerMap);
        // top selected player eo
        LinkedHashMap<String, String> topSelectedPlayerEoMap = this.getTopSelectedPlayerEoMap(topSelectedPlayerMap, leagueEventEoMap);
        data.setTopSelectedPlayerEoMap(topSelectedPlayerEoMap);
        // top selected team
        LinkedHashMap<Integer, Map<String, String>> topSelectedTeamMap = this.getTopSelectedTeamMap(leagueEventReportList, teamSize, playerMap);
        data.setTopSelectedTeamMap(topSelectedTeamMap);
        return data;
    }

    private LinkedHashMap<String, String> getMostTransferInMap(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        if (event <= 1) {
            return Maps.newLinkedHashMap();
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
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getMostTransferOutMap(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        if (event <= 1) {
            return Maps.newLinkedHashMap();
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
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
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

    private LinkedHashMap<String, String> getCaptainSelectedMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        List<Integer> elementList = leagueEventReportList
                .stream()
                .map(LeagueEventReportEntity::getCaptain)
                .collect(Collectors.toList());
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getCaptainSelectedEoMap(LinkedHashMap<String, String> captainSelectedMap, Map<String, String> leagueEventEoMap) {
        LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
        captainSelectedMap.keySet().forEach(o -> map.put(o, leagueEventEoMap.getOrDefault(o, "")));
        return map;
    }

    private LinkedHashMap<String, String> getViceCaptainSelectedMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        // collect
        List<Integer> elementList = leagueEventReportList
                .stream()
                .map(LeagueEventReportEntity::getViceCaptain)
                .collect(Collectors.toList());
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getTopSelectedPlayerMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
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
        return this.collectSelectedMap(elementList, teamSize, 10, playerMap);
    }

    private LinkedHashMap<String, String> getTopSelectedPlayerEoMap(LinkedHashMap<String, String> topSelectedPlayerMap, Map<String, String> leagueEventEoMap) {
        LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
        topSelectedPlayerMap.keySet().forEach(o -> map.put(o, leagueEventEoMap.getOrDefault(o, "")));
        return map;
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
                map.put(playerMap.get(k).getWebName(), NumberUtil.formatPercent(NumberUtil.div(v.intValue(), teamSize), 1)));
        return map;
    }

    private LinkedHashMap<Integer, Map<String, String>> getTopSelectedTeamMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
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
            result.forEach(playerSelectedMap::put);
        });
        // add key:element_type
        Map<Integer, Map<Integer, Integer>> elementTypeMap = this.collectPlayerSelectedMap(playerSelectedMap, playerMap); // key:element_type -> value: elementCountMap
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
        ScoutEntity scoutEntity = this.scoutService.getOne(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, event)
                .eq(ScoutEntity::getEntry, entry));
        if (scoutEntity == null) {
            return new EventScoutData();
        }
        return this.initScoutData(event, scoutEntity);
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
        return this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, event))
                .stream()
                .map(o -> this.initScoutData(event, o))
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
                            .setEventPoints(0)
                            .setTotalPoints(lastScout.getTotalPoints())
            );
        });
        return list
                .stream()
                .sorted(Comparator.comparing(EventScoutData::getTotalPoints).reversed())
                .collect(Collectors.toList());
    }

    private EventScoutData initScoutData(int event, ScoutEntity scoutEntity) {
        return new EventScoutData()
                .setEvent(scoutEntity.getEvent())
                .setEntry(scoutEntity.getEntry())
                .setScoutName(scoutEntity.getScoutName())
                .setGkpInfo(this.qryPlayerInfoByElement(event, scoutEntity.getGkp()))
                .setDefInfo(this.qryPlayerInfoByElement(event, scoutEntity.getDef()))
                .setMidInfo(this.qryPlayerInfoByElement(event, scoutEntity.getMid()))
                .setFwdInfo(this.qryPlayerInfoByElement(event, scoutEntity.getFwd()))
                .setCaptainInfo(this.qryPlayerInfoByElement(event, scoutEntity.getCaptain()))
                .setReason(scoutEntity.getReason())
                .setEventPoints(scoutEntity.getEventPoints())
                .setTotalPoints(scoutEntity.getTotalPoints());
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
        List<Integer> list = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getEntry, entry))
                .stream()
                .map(TournamentEntryEntity::getTournamentId)
                .collect(Collectors.toList());
        list.add(13); // 球员联赛
        list.add(14); // 网红联赛
        return list;
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
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        // entry_event_result
        return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .map(o ->
                        new EntryEventResultData()
                                .setEvent(event)
                                .setEntry(o.getEntry())
                                .setTransfers(o.getEventTransfers())
                                .setPoints(o.getEventPoints())
                                .setTransfersCost(o.getEventTransfersCost())
                                .setNetPoints(o.getEventPoints() - o.getEventTransfersCost())
                                .setBenchPoints(o.getEventBenchPoints())
                                .setRank(o.getEventRank())
                                .setChip(StringUtils.isBlank(o.getEventChip()) ? Chip.NONE.getValue() : o.getEventChip())
                                .setValue(o.getTeamValue() / 10.0)
                                .setBank(o.getBank() / 10.0)
                                .setTeamValue((o.getTeamValue() - o.getBank()) / 10.0)
                                .setPickList(this.qryTournamentEntryPickList(event, o.getEventPicks(), teamNameMap, teamShortNameMap, playerMap, eventLiveMap)))
                .sorted(Comparator.comparing(EntryEventResultData::getPoints))
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

    @Cacheable(
            value = "api::qryTournamentEntryContainElement",
            key = "#event+'::'+#tournamentId+'::+#element",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<Integer> qryTournamentEntryContainElement(int event, int tournamentId, int element) {
        // entry list
        List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        // entry_event_result
        List<Integer> list = Lists.newArrayList();
        this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .forEach(o -> {
                    if (this.entryContainElement(element, o.getEventPicks(), o.getEventChip())) {
                        list.add(o.getEntry());
                    }
                });
        return list;
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
            value = "api::qryTournamentEntryPlayElement",
            key = "#event+'::'+#tournamentId+'::+'+#element",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<Integer> qryTournamentEntryPlayElement(int event, int tournamentId, int element) {
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
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        // entry_event_result
        List<Integer> list = Lists.newArrayList();
        this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .forEach(o -> {
                    if (this.entryPlayElement(element, o.getEventPicks(), o.getEventChip(), eventLiveMap)) {
                        list.add(o.getEntry());
                    }
                });
        return list;
    }

    private boolean entryPlayElement(int element, String eventPicks, String eventChip, Map<Integer, EventLiveEntity> eventLiveMap) {
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
            int pickElement = data.getElement();
            if (pickElement == element) {
                EventLiveEntity eventLiveEntity = eventLiveMap.get(pickElement);
                if (eventLiveEntity != null && eventLiveEntity.getMinutes() > 0) {
                    return true;
                }
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

    /**
     * @implNote summary
     */
    @Cacheable(
            value = "api::qryEntrySeasonInfo",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonInfoData qryEntrySeasonInfo(int entry) {
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return new EntrySeasonInfoData();
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return new EntrySeasonInfoData();
        }
        return new EntrySeasonInfoData()
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName())
                .setOverallPoints(entryInfoEntity.getOverallPoints())
                .setOverallRank(entryInfoEntity.getOverallRank())
                .setTotalTransfers(entryInfoEntity.getTotalTransfers())
                .setTotalTransfersCost(
                        entryEventResultEntityList
                                .stream()
                                .mapToInt(EntryEventResultEntity::getEventTransfersCost)
                                .sum()
                )
                .setTotalBenchPoints(
                        entryEventResultEntityList
                                .stream()
                                .mapToInt(EntryEventResultEntity::getEventBenchPoints)
                                .sum()
                )
                .setTotalAutoSubsPoints(
                        entryEventResultEntityList
                                .stream()
                                .mapToInt(EntryEventResultEntity::getEventAutoSubPoints)
                                .sum()
                )
                .setValue(entryInfoEntity.getTeamValue() / 10.0)
                .setBank(entryInfoEntity.getBank() / 10.0)
                .setTeamValue((entryInfoEntity.getTeamValue() - entryInfoEntity.getBank()) / 10.0);
    }

    @Cacheable(
            value = "api::qrySeasonEntrySummary",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonSummaryData qrySeasonEntrySummary(int entry) {
        EntrySeasonSummaryData data = new EntrySeasonSummaryData();
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return data;
        }
        Map<Integer, String> webNameMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        if (CollectionUtils.isEmpty(webNameMap)) {
            return data;
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return data;
        }
        data
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName());
        // entry_event_result
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getEventPoints))
                .ifPresent(o ->
                        data
                                .setHighestScore(o.getEventPoints())
                                .setHighestEvent(o.getEvent())
                );
        entryEventResultEntityList
                .stream()
                .min(Comparator.comparing(EntryEventResultEntity::getEventPoints))
                .ifPresent(o ->
                        data
                                .setLowestScore(o.getEventPoints())
                                .setLowestEvent(o.getEvent())
                );
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getOverallRank))
                .ifPresent(o ->
                        data
                                .setHighestOverallRank(o.getOverallRank())
                                .setHighestOverallRankEvent(o.getEvent())
                );
        entryEventResultEntityList
                .stream()
                .min(Comparator.comparing(EntryEventResultEntity::getOverallRank))
                .ifPresent(o ->
                        data
                                .setLowestOverallRank(o.getOverallRank())
                                .setLowestOverallRankEvent(o.getEvent())
                );
        // average
        Map<String, Integer> averageMap = this.qryEventAverageScore();
        data
                .setBelowAverageEvents(
                        entryEventResultEntityList
                                .stream()
                                .filter(o -> o.getEventPoints() < averageMap.getOrDefault(String.valueOf(o.getEvent()), 0))
                                .collect(Collectors.toMap(EntryEventResultEntity::getEvent, EntryEventResultEntity::getEventPoints))
                );
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getEventBenchPoints))
                .ifPresent(o ->
                        data
                                .setHighestBenchPoints(o.getOverallRank())
                                .setHighestBenchPointsEvent(o.getEvent())
                );
        // autoSub
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getEventAutoSubPoints))
                .ifPresent(o -> {
                            List<EntryEventAutoSubsData> list = JsonUtils.json2Collection(o.getEventAutoSubs(), List.class, EntryEventAutoSubsData.class);
                            if (CollectionUtils.isEmpty(list)) {
                                return;
                            }
                            list.forEach(i ->
                                    i
                                            .setEvent(o.getEvent())
                                            .setElementInWebName(webNameMap.getOrDefault(i.getElementIn(), ""))
                                            .setElementOutWebName(webNameMap.getOrDefault(i.getElementOut(), ""))
                            );
                            data
                                    .setHighestAutoSubs(list);
                        }
                );
        data.setHighestAutoSubsPoints(
                data.getHighestAutoSubs()
                        .stream()
                        .mapToInt(EntryEventAutoSubsData::getElementInPoints)
                        .sum() -
                        data.getHighestAutoSubs()
                                .stream()
                                .mapToInt(EntryEventAutoSubsData::getElementOutPoints)
                                .sum()
        );
        return data;
    }

    @Cacheable(
            value = "api::qryEntrySeasonCaptain",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonCaptainData qryEntrySeasonCaptain(int entry) {
        EntrySeasonCaptainData data = new EntrySeasonCaptainData();
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return data;
        }
        Map<Integer, String> webNameMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        if (CollectionUtils.isEmpty(webNameMap)) {
            return data;
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return data;
        }
        data
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName());
        // collect
        List<Integer> captainList = Lists.newArrayList();
        entryEventResultEntityList.forEach(o -> {
            List<Pick> pickList = JsonUtils.json2Collection(o.getEventPicks(), List.class, Pick.class);
            if (!CollectionUtils.isEmpty(pickList)) {
                captainList.addAll(
                        pickList
                                .stream()
                                .filter(Pick::isCaptain)
                                .map(Pick::getElement)
                                .collect(Collectors.toList())
                );
            }
        });
        List<Integer> viceCaptainList = Lists.newArrayList();
        entryEventResultEntityList.forEach(o -> {
            if (!captainList.contains(o.getPlayedCaptain())) {
                viceCaptainList.add(o.getPlayedCaptain());
            }
        });
        data
                .setTotalPoints(
                        entryEventResultEntityList
                                .stream()
                                .mapToInt(o -> {
                                    if (StringUtils.equals(Chip.TC.getValue(), o.getEventChip())) {
                                        return 3 * o.getCaptainPoints();
                                    }
                                    return 2 * o.getCaptainPoints();
                                })
                                .reduce(0, Integer::sum)
                )
                .setViceTotalPoints(
                        entryEventResultEntityList
                                .stream()
                                .filter(o -> viceCaptainList.contains(o.getPlayedCaptain()))
                                .mapToInt(EntryEventResultEntity::getCaptainPoints)
                                .sum()
                );
        // max
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getCaptainPoints))
                .ifPresent(o ->
                        data
                                .setMaxPointsEvent(o.getEvent())
                                .setMaxPoints(o.getCaptainPoints())
                                .setMaxPointsWebName(webNameMap.getOrDefault(o.getPlayedCaptain(), ""))
                );
        // min
        entryEventResultEntityList
                .stream()
                .min(Comparator.comparing(EntryEventResultEntity::getCaptainPoints))
                .ifPresent(o ->
                        data
                                .setMinPointsEvent(o.getEvent())
                                .setMinPoints(o.getCaptainPoints())
                                .setMinPointsWebName(webNameMap.getOrDefault(o.getPlayedCaptain(), ""))
                );
        // tc
        entryEventResultEntityList
                .stream()
                .filter(o -> StringUtils.equals(Chip.TC.getValue(), o.getEventChip()))
                .findFirst()
                .ifPresent(o ->
                        data
                                .setTcEvent(o.getEvent())
                                .setTcPoints(o.getCaptainPoints())
                                .setTcPointsWebName(webNameMap.getOrDefault(o.getPlayedCaptain(), ""))
                );
        // most selected
        Map<Integer, Long> countMap = entryEventResultEntityList.
                stream()
                .collect(Collectors.groupingBy(EntryEventResultEntity::getPlayedCaptain, Collectors.counting()));
        List<Long> mostCountList = countMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        LinkedHashMap<String, Integer> mostSelectedMap = countMap.entrySet()
                .stream()
                .filter(o -> mostCountList.contains(o.getValue()))
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(k -> webNameMap.getOrDefault(k.getKey(), ""), v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        data.setMostSelected(mostSelectedMap);
        int overallPoints = entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getOverallPoints))
                .map(EntryEventResultEntity::getOverallPoints)
                .orElse(0);
        data.setTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getTotalPoints(), overallPoints), 2));
        return data;
    }

    //    @Cacheable(
//            value = "api::qryEntrySeasonTransfers",
//            key = "#entry",
//            cacheManager = "apiCacheManager",
//            unless = "#result.entry eq 0"
//    )
    @Override
    public EntrySeasonTransfersData qryEntrySeasonTransfers(int entry) {
        EntrySeasonTransfersData data = new EntrySeasonTransfersData();
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return data;
        }
        Map<String, String> shortNameMap = this.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        if (CollectionUtils.isEmpty(playerMap)) {
            return data;
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return data;
        }
        List<EntryEventTransfersEntity> entryEventTransfersEntityList = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventTransfersEntityList)) {
            return data;
        }
        data
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName());
        // chip event list
        List<Integer> chipEventList = entryEventResultEntityList
                .stream()
                .filter(o -> StringUtils.equalsIgnoreCase(Chip.WC.getValue(), o.getEventChip()) || StringUtils.equalsIgnoreCase(Chip.FH.getValue(), o.getEventChip()))
                .map(EntryEventResultEntity::getEvent)
                .collect(Collectors.toList());
        // most transfers event
        Map<Integer, Long> mostTransfersCountMap = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .collect(Collectors.groupingBy(EntryEventTransfersEntity::getEvent, Collectors.counting()));
        int mostTransfersEvent = mostTransfersCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
        int mostTransfersCost = entryEventResultEntityList
                .stream()
                .filter(o -> o.getEvent() == mostTransfersEvent)
                .map(EntryEventResultEntity::getEventTransfersCost)
                .findFirst()
                .orElse(0);
        List<EntryEventTransfersData> mostTransfersList = entryEventTransfersEntityList
                .stream()
                .filter(o -> o.getEvent() == mostTransfersEvent)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data
                .setMostTransfersCost(mostTransfersCost)
                .setMostTransfers(mostTransfersList);
        // most transfers in
        Map<Integer, Long> mostTransfersInCountMap = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .collect(Collectors.groupingBy(EntryEventTransfersEntity::getElementIn, Collectors.counting()));
        int mostTransferInElement = mostTransfersInCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
        List<EntryEventTransfersData> mostTransfersInList = entryEventTransfersEntityList
                .stream()
                .filter(o -> o.getElementIn() == mostTransferInElement)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setMostTransfersIn(mostTransfersInList);
        // most transfers out
        Map<Integer, Long> mostTransfersOutCountMap = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .collect(Collectors.groupingBy(EntryEventTransfersEntity::getElementOut, Collectors.counting()));
        int mostTransferOutElement = mostTransfersOutCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
        List<EntryEventTransfersData> mostTransfersOutList = entryEventTransfersEntityList
                .stream()
                .filter(o -> o.getElementOut() == mostTransferOutElement)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setMostTransfersOut(mostTransfersOutList);
        // most transfers in points
        int mostTransfersInPoints = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .map(EntryEventTransfersEntity::getElementInPoints)
                .max(Comparator.comparing(Integer::intValue))
                .orElse(0);
        List<EntryEventTransfersData> mostTransfersInPointsList = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .filter(o -> o.getElementInPoints() == mostTransfersInPoints)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setMostTransferInPointsList(mostTransfersInPointsList);
        // negative transfers in points
        List<EntryEventTransfersData> negativeTransfersInPointsList = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .filter(o -> o.getElementInPoints() < 0)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setNegativeTransferInPointsList(negativeTransfersInPointsList);
        // most transfers out points
        int mostTransfersOutPoints = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .map(EntryEventTransfersEntity::getElementOutPoints)
                .max(Comparator.comparing(Integer::intValue))
                .orElse(0);
        List<EntryEventTransfersData> mostTransfersOutPointsList = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .filter(o -> o.getElementOutPoints() == mostTransfersOutPoints)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setMostTransferOutPointsList(mostTransfersOutPointsList);
        // best transfers
        int bestProfit = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .mapToInt(o -> o.getElementInPoints() - o.getElementOutPoints())
                .max()
                .orElse(0);
        List<EntryEventTransfersData> bestTransfersInList = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .filter(EntryEventTransfersEntity::getElementInPlayed)
                .filter(o -> (o.getElementInPoints() - o.getElementOutPoints()) == bestProfit)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setBestTransferInList(bestTransfersInList);
        // worst transfers
        int worstProfit = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .mapToInt(o -> o.getElementInPoints() - o.getElementOutPoints())
                .min()
                .orElse(0);
        List<EntryEventTransfersData> worstTransfersInList = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .filter(o -> (o.getElementInPoints() - o.getElementOutPoints()) == worstProfit)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setWorstTransferInList(worstTransfersInList);
        return data;

    }

    private EntryEventTransfersData initEntryEventTransfersData(EntryEventTransfersEntity entryEventTransfersEntity, Map<String, String> shortNameMap, Map<Integer, PlayerEntity> playerMap) {
        PlayerEntity elementInEntity = playerMap.get(entryEventTransfersEntity.getElementIn());
        PlayerEntity elementOutEntity = playerMap.get(entryEventTransfersEntity.getElementOut());
        return BeanUtil.copyProperties(entryEventTransfersEntity, EntryEventTransfersData.class)
                .setElementInName(elementInEntity.getWebName())
                .setElementInTeamId(elementInEntity.getTeamId())
                .setElementInTeamShortName(shortNameMap.getOrDefault(String.valueOf(elementInEntity.getTeamId()), ""))
                .setElementInCost(entryEventTransfersEntity.getElementInCost() / 10.0)
                .setElementOutName(elementOutEntity.getWebName())
                .setElementOutTeamId(elementOutEntity.getTeamId())
                .setElementOutTeamShortName(shortNameMap.getOrDefault(String.valueOf(elementOutEntity.getTeamId()), ""))
                .setElementOutCost(entryEventTransfersEntity.getElementOutCost() / 10.0);
    }

    @Cacheable(
            value = "api::qryEntrySeasonScore",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonScoreData qryEntrySeasonScore(int entry) {
        EntrySeasonScoreData data = new EntrySeasonScoreData();
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return data;
        }
        Map<String, String> shortNameMap = this.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        if (CollectionUtils.isEmpty(playerMap)) {
            return data;
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return data;
        }
        // collect event score
        Table<Integer, Integer, List<EntryPickData>> pickTable = HashBasedTable.create(); // elementType -> event -> dataList
        entryEventResultEntityList.forEach(o -> {
            List<EntryPickData> pickList = JsonUtils.json2Collection(o.getEventPicks(), List.class, EntryPickData.class);
            if (CollectionUtils.isEmpty(pickList)) {
                return;
            }
            int event = o.getEvent();
            pickList.forEach(pick -> {
                if (!StringUtils.equalsIgnoreCase(Chip.BB.getValue(), o.getEventChip()) && pick.getPosition() > 11) {
                    return;
                }
                PlayerEntity playerEntity = playerMap.get(pick.getElement());
                if (playerEntity == null) {
                    return;
                }
                int elementType = playerEntity.getElementType();
                List<EntryPickData> list = Lists.newArrayList();
                if (pickTable.contains(elementType, event)) {
                    list = pickTable.get(elementType, event);
                }
                if (CollectionUtils.isEmpty(list)) {
                    list = Lists.newArrayList();
                }
                pick
                        .setEvent(event)
                        .setElementType(elementType)
                        .setElementTypeName(Position.getNameFromElementType(elementType))
                        .setWebName(playerEntity.getWebName())
                        .setTeamId(playerEntity.getTeamId())
                        .setTeamShortName(shortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""));
                list.add(pick);
                pickTable.put(elementType, event, list);
            });
        });
        data
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName())
                .setOverallPoints(entryInfoEntity.getOverallPoints());
        // gkp
        List<EntryPickData> gkpPickList = Lists.newArrayList();
        pickTable.row(1).values().forEach(gkpPickList::addAll);
        data
                .setGkpTotalPoints(
                        gkpPickList
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setGkpTotalNum(
                        (int) gkpPickList
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                );
        data.setGkpTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getGkpTotalPoints(), data.getOverallPoints()), 2));
        Map<String, Long> mostSelectedGkpCountMap = gkpPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedGkp(
                mostSelectedGkpCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(2)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // def
        List<EntryPickData> defPickList = Lists.newArrayList();
        pickTable.row(2).values().forEach(defPickList::addAll);
        data
                .setDefTotalPoints(
                        defPickList
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setDefTotalNum(
                        (int) defPickList
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                );
        data.setDefTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getDefTotalPoints(), data.getOverallPoints()), 2));
        Map<String, Long> mostSelectedDefCountMap = defPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedDef(
                mostSelectedDefCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // mid
        List<EntryPickData> midPickList = Lists.newArrayList();
        pickTable.row(3).values().forEach(midPickList::addAll);
        data
                .setMidTotalPoints(
                        midPickList
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setMidTotalNum(
                        (int) midPickList
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                );
        data.setMidTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getMidTotalPoints(), data.getOverallPoints()), 2));
        Map<String, Long> mostSelectedMidCountMap = midPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedMid(
                mostSelectedMidCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // fwd
        List<EntryPickData> fwdPickList = Lists.newArrayList();
        pickTable.row(4).values().forEach(fwdPickList::addAll);
        data
                .setFwdTotalPoints(
                        fwdPickList
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setFwdTotalNum(
                        (int) fwdPickList
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                );
        data.setFwdTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getFwdTotalPoints(), data.getOverallPoints()), 2));
        Map<String, Long> mostSelectedFwdCountMap = fwdPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedFwd(
                mostSelectedFwdCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(3)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // formation
        List<String> formationList = pickTable.columnMap().values()
                .stream()
                .map(o -> StringUtils.joinWith("-", o.get(2).size(), o.get(3).size(), o.get(4).size()))
                .collect(Collectors.toList());
        Map<String, Long> mostSelectedFormationCountMap = formationList.
                stream()
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()));
        data.setMostSelectedFormation(
                mostSelectedFormationCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(3)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        return data;
    }

    @Override
    public TournamentSeasonData qryTournamentSeasonSummary(int tournamentId) {
        return null;
    }

}
