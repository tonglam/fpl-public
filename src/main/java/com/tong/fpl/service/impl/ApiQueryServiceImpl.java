package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.ElementTeamAgainstCollector;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.*;
import com.tong.fpl.domain.data.response.UserTransfersRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.element.*;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.global.MapData;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.scout.PopularScoutData;
import com.tong.fpl.domain.letletme.team.*;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.*;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2021/5/10
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiQueryServiceImpl implements IApiQueryService {

    private final IInterfaceService interfaceService;
    private final IRedisCacheService redisCacheService;
    private final IQueryService queryService;
    private final IDataService dataService;
    private final ILiveService liveService;

    private final TeamService teamService;
    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final PlayerValueService playerValueService;
    private final EventFixtureService eventFixtureService;
    private final EventLiveSummaryService eventLiveSummaryService;
    private final EventLiveService eventLiveService;
    private final EntryInfoService entryInfoService;
    private final EntryLeagueInfoService entryLeagueInfoService;
    private final EntryHistoryInfoService entryHistoryInfoService;
    private final EntryEventCupResultService entryEventCupResultService;
    private final EntryEventTransfersService entryEventTransfersService;
    private final EntryEventResultService entryEventResultService;
    private final ScoutService scoutService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;
    private final LeagueEventReportService leagueEventReportService;
    private final PopularScoutResultService popularScoutResultService;

    /**
     * @implNote common
     */
    @Cacheable(value = "api::qryCurrentEventAndNextUtcDeadline", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() ne 2")
    @Override
    public Map<String, String> qryCurrentEventAndNextUtcDeadline() {
        Map<String, String> map = Maps.newHashMap();
        int event = this.queryService.getCurrentEvent();
        map.put("event", String.valueOf(event));
        String utcDeadline = this.queryService.getUtcDeadlineByEvent(event + 1);
        map.put("utcDeadline", utcDeadline);
        return map;
    }

    @Cacheable(value = "api::qryEventAverageScore", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, Integer> qryEventAverageScore() {
        return this.redisCacheService.getEventOverallResultMap(CommonUtils.getCurrentSeason()).values().stream().collect(Collectors.toMap(o -> String.valueOf(o.getEvent()), EventOverallResultData::getAverageEntryScore));
    }

    @Cacheable(value = "api::qryTeamList", key = "#season", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TeamData> qryTeamList(String season) {
        if (!Season.legalSeason(season)) {
            return Lists.newArrayList();
        }
        MybatisPlusConfig.season.set(season);
        List<TeamData> list = this.teamService.list().stream().map(o -> BeanUtil.copyProperties(o, TeamData.class)).collect(Collectors.toList());
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Cacheable(value = "api::qryAllLeagueName", key = "#season", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<LeagueInfoData> qryAllLeagueName(String season) {
        if (!Season.legalSeason(season)) {
            return Lists.newArrayList();
        }
        MybatisPlusConfig.season.set(season);
        List<LeagueInfoData> list = this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda().eq(TournamentInfoEntity::getGroupMode, GroupMode.Points_race.name()).eq(TournamentInfoEntity::getKnockoutMode, KnockoutMode.No_knockout.name())).stream().distinct().map(o -> new LeagueInfoData().setId(o.getLeagueId()).setName(o.getName())).collect(Collectors.toList());
        MybatisPlusConfig.season.remove();
        list.add(new LeagueInfoData().setId(65).setName("China"));
        return list;
    }

    @Cacheable(value = "api::qryNextFixture", key = "#event", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() == 0")
    @Override
    public List<PlayerFixtureData> qryNextFixture(int event) {
        int nextEvent = event + 1;
        if (nextEvent < 1 || nextEvent > 38) {
            return Lists.newArrayList();
        }
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        return this.queryService.getEventFixtureByEvent(nextEvent).stream().map(o -> new PlayerFixtureData().setEvent(nextEvent).setTeamId(o.getTeamH()).setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamH()), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamH()), "")).setAgainstTeamId(o.getTeamA()).setAgainstTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamA()), "")).setAgainstTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamA()), "")).setDifficulty(0).setKickoffTime(o.getKickoffTime()).setStarted(false).setFinished(false).setWasHome(false).setTeamScore(0).setAgainstTeamScore(0).setScore("").setResult("").setBgw(false).setDgw(false)).sorted(Comparator.comparing(PlayerFixtureData::getKickoffTime)).collect(Collectors.toList());
    }

    /**
     * @implNote entry
     */
    // do not cache
    @Override
    public List<EntryInfoData> fuzzyQueryEntry(EntryQueryParam param) {
        List<EntryInfoData> list = Lists.newArrayList();
        LambdaQueryWrapper<EntryInfoEntity> queryWrapper = new QueryWrapper<EntryInfoEntity>().lambda();
        if (StringUtils.isNotBlank(param.getEntryName())) {
            queryWrapper.like(EntryInfoEntity::getEntryName, param.getEntryName());
            list.addAll(this.fuzzyQueryEntryInfo(queryWrapper));
        }
        if (StringUtils.isNotBlank(param.getPlayerName())) {
            queryWrapper.like(EntryInfoEntity::getPlayerName, param.getPlayerName());
            list.addAll(this.fuzzyQueryEntryInfo(queryWrapper));
        }
        if (!CollectionUtils.isEmpty(list)) {
            return list.stream().distinct().collect(Collectors.toList());
        }
        LambdaQueryWrapper<LeagueEventReportEntity> leagueReportQueryWrapper = new QueryWrapper<LeagueEventReportEntity>().lambda();
        if (StringUtils.isNotBlank(param.getEntryName())) {
            leagueReportQueryWrapper.like(LeagueEventReportEntity::getEntryName, param.getEntryName());
            list.addAll(this.fuzzyQueryLeagueReport(leagueReportQueryWrapper));
        }
        if (StringUtils.isNotBlank(param.getPlayerName())) {
            leagueReportQueryWrapper.like(LeagueEventReportEntity::getPlayerName, param.getPlayerName());
            list.addAll(this.fuzzyQueryLeagueReport(leagueReportQueryWrapper));
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    private List<EntryInfoData> fuzzyQueryEntryInfo(LambdaQueryWrapper<EntryInfoEntity> queryWrapper) {
        return this.entryInfoService.list(queryWrapper).stream().map(o -> new EntryInfoData().setEntry(o.getEntry()).setEntryName(o.getEntryName()).setPlayerName(o.getPlayerName())).collect(Collectors.toList());
    }

    private List<EntryInfoData> fuzzyQueryLeagueReport(LambdaQueryWrapper<LeagueEventReportEntity> leagueReportQueryWrapper) {
        return this.leagueEventReportService.list(leagueReportQueryWrapper).stream().map(o -> new EntryInfoData().setEntry(o.getEntry()).setEntryName(o.getEntryName()).setPlayerName(o.getPlayerName())).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryEntryInfo", key = "#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.entry eq 0")
    @Override
    public EntryInfoData qryEntryInfo(int entry) {
        if (entry <= 0) {
            return new EntryInfoData();
        }
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(CommonUtils.getCurrentSeason(), entry);
        if (entryInfoData == null) {
            return new EntryInfoData();
        }
        return new EntryInfoData().setEntry(entryInfoData.getEntry()).setEntryName(entryInfoData.getEntryName()).setPlayerName(entryInfoData.getPlayerName()).setRegion(entryInfoData.getRegion()).setStartedEvent(entryInfoData.getStartedEvent()).setOverallPoints(entryInfoData.getOverallPoints()).setOverallRank(entryInfoData.getOverallRank()).setTotalTransfers(entryInfoData.getTotalTransfers()).setValue(entryInfoData.getTeamValue() / 10.0).setBank(entryInfoData.getBank() / 10.0).setTeamValue((entryInfoData.getTeamValue() - entryInfoData.getBank()) / 10.0);
    }

    @Cacheable(value = "api::qryEntryLeagueInfo", key = "#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.entry eq 0")
    @Override
    public EntryLeagueData qryEntryLeagueInfo(int entry) {
        if (entry <= 0) {
            return new EntryLeagueData();
        }
        List<EntryLeagueInfoData> entryLeagueInfoList = this.entryLeagueInfoService.list(new QueryWrapper<EntryLeagueInfoEntity>().lambda().eq(EntryLeagueInfoEntity::getEntry, entry)).stream().map(o -> new EntryLeagueInfoData().setEntry(entry).setLeagueId(o.getLeagueId()).setType(o.getType()).setLeagueType(o.getLeagueType()).setLeagueName(o.getLeagueName()).setEntryRank(o.getEntryRank()).setEntryLastRank(o.getEntryLastRank()).setStartEvent(o.getStartEvent())).toList();
        if (CollectionUtils.isEmpty(entryLeagueInfoList)) {
            return new EntryLeagueData();
        }
        return new EntryLeagueData().setEntry(entry).setEvent(this.queryService.getCurrentEvent()).setClassic(entryLeagueInfoList.stream().filter(o -> StringUtils.equals("private", o.getType()) && StringUtils.equals(LeagueType.Classic.name(), o.getLeagueType())).collect(Collectors.toList())).setH2h(entryLeagueInfoList.stream().filter(o -> StringUtils.equals("private", o.getType()) && StringUtils.equals(LeagueType.H2h.name(), o.getLeagueType())).collect(Collectors.toList())).setPublicLeague(entryLeagueInfoList.stream().filter(o -> StringUtils.equals("public", o.getType())).collect(Collectors.toList())).setCup(this.entryEventCupResultService.list(new QueryWrapper<EntryEventCupResultEntity>().lambda().eq(EntryEventCupResultEntity::getEntry, entry).orderByAsc(EntryEventCupResultEntity::getEvent)).stream().map(o -> new EntryCupData().setEvent(o.getEvent()).setEntry(o.getEntry()).setEntryName(o.getEntryName()).setPlayerName(o.getPlayerName()).setEventPoints(o.getEventPoints()).setAgainstEntry(o.getAgainstEntry()).setAgainstEntryName(o.getAgainstEntryName()).setAgainstPlayerName(o.getAgainstPlayerName()).setAgainstEventPoints(o.getAgainstEventPoints()).setResult(o.getResult())).collect(Collectors.toList()));
    }

    @Cacheable(value = "api::qryEntryHistoryInfo", key = "#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.entry eq 0")
    @Override
    public EntryHistoryData qryEntryHistoryInfo(int entry) {
        if (entry <= 0) {
            return new EntryHistoryData();
        }
        // history
        List<EntryHistoryInfoData> historyList = this.entryHistoryInfoService.list(new QueryWrapper<EntryHistoryInfoEntity>().lambda().eq(EntryHistoryInfoEntity::getEntry, entry)).stream().map(o -> new EntryHistoryInfoData().setEntry(o.getEntry()).setSeason(o.getSeason()).setTotalPoints(o.getTotalPoints()).setOverallRank(o.getOverallRank())).sorted(Comparator.comparing(EntryHistoryInfoData::getSeason).reversed()).collect(Collectors.toList());
        // chip
        List<MapData<String>> chips = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).ne(EntryEventResultEntity::getEventChip, Chip.NONE.getValue())).stream().map(o -> new MapData<String>().setKey(String.valueOf(o.getEvent())).setValue(o.getEventChip())).collect(Collectors.toList());
        // return
        return new EntryHistoryData().setEntry(entry).setHistoryList(historyList).setChips(chips);
    }

    @Cacheable(value = "api::qryEntryEventResult", key = "#event+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.entry eq 0")
    @Override
    public EntryEventResultData qryEntryEventResult(int event, int entry) {
        EntryEventResultData data = new EntryEventResultData().setPicks(Lists.newArrayList()).setPickList(Lists.newArrayList());
        if (event <= 0 || entry <= 0) {
            return data;
        }
        // from db
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity != null) {
            data.setEvent(event).setEntry(entry).setTransfers(entryEventResultEntity.getEventTransfers()).setPoints(entryEventResultEntity.getEventPoints()).setTransfersCost(entryEventResultEntity.getEventTransfersCost()).setNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost()).setBenchPoints(entryEventResultEntity.getEventBenchPoints()).setRank(entryEventResultEntity.getEventRank()).setChip(StringUtils.isBlank(entryEventResultEntity.getEventChip()) ? Chip.NONE.getValue() : entryEventResultEntity.getEventChip()).setValue(entryEventResultEntity.getTeamValue() / 10.0).setBank(entryEventResultEntity.getBank() / 10.0).setTeamValue((entryEventResultEntity.getTeamValue() - entryEventResultEntity.getBank()) / 10.0).setOverallPoints(entryEventResultEntity.getOverallPoints()).setOverallRank(entryEventResultEntity.getOverallRank());
            data.setPickList(this.getPickListFromDB(event, data.getChip(), entryEventResultEntity.getEventPicks()));
            data.setCaptainName(this.getPlayedCaptainName(data.getPickList()));
            return data;
        }
        // from fpl server
        this.interfaceService.getUserPicks(event, entry).ifPresent(userPicksRes -> {
            data.setEntry(entry).setEvent(event).setTransfers(userPicksRes.getEntryHistory().getEventTransfers()).setPoints(userPicksRes.getEntryHistory().getPoints()).setTransfersCost(userPicksRes.getEntryHistory().getEventTransfersCost()).setNetPoints(userPicksRes.getEntryHistory().getPoints() - userPicksRes.getEntryHistory().getEventTransfersCost()).setBenchPoints(userPicksRes.getEntryHistory().getPointsOnBench()).setRank(userPicksRes.getEntryHistory().getRank()).setChip(StringUtils.isBlank(userPicksRes.getActiveChip()) ? Chip.NONE.getValue() : userPicksRes.getActiveChip()).setValue(userPicksRes.getEntryHistory().getValue() / 10.0).setBank(userPicksRes.getEntryHistory().getBank() / 10.0).setTeamValue((userPicksRes.getEntryHistory().getValue() - userPicksRes.getEntryHistory().getBank()) / 10.0).setOverallPoints(userPicksRes.getEntryHistory().getTotalPoints()).setOverallRank(userPicksRes.getEntryHistory().getOverallRank());
            data.setPickList(this.getPickListFromServer(event, data.getChip(), userPicksRes.getPicks()));
            data.setCaptainName(this.getPlayedCaptainName(data.getPickList()));
        });
        // refresh
        this.dataService.upsertEntryEventResult(event, entry);
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
        picks.forEach(o -> pickList.add(new EntryPickData().setElement(o.getElement()).setPosition(o.getPosition()).setMultiplier(o.getMultiplier()).setCaptain(o.isCaptain()).setViceCaptain(o.isViceCaptain())));
        return this.qryEntryEventPicksResult(event, chip, pickList);
    }

    private String getPlayedCaptainName(List<ElementEventResultData> picks) {
        ElementEventResultData captain = picks.stream().filter(ElementEventResultData::isCaptain).findFirst().orElse(null);
        ElementEventResultData viceCaptain = picks.stream().filter(ElementEventResultData::isViceCaptain).findFirst().orElse(null);
        if (captain == null || viceCaptain == null) {
            return "";
        }
        if (captain.getMinutes() == 0 && viceCaptain.getMinutes() > 0) {
            return viceCaptain.getWebName();
        }
        return captain.getWebName();
    }

    @Override
    public List<ElementEventResultData> qryEntryEventPicksResult(int event, String chip, List<EntryPickData> pickList) {
        List<ElementEventResultData> list = Lists.newArrayList();
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        // collect
        pickList.forEach(pick -> {
            int element = pick.getElement();
            if (!playerMap.containsKey(String.valueOf(element))) {
                return;
            }
            PlayerEntity playerEntity = playerMap.get(String.valueOf(element));
            ElementEventResultData data = BeanUtil.copyProperties(pick, ElementEventResultData.class);
            data.setEvent(event).setElement(element).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setWebName(playerEntity.getWebName()).setTeamId(playerEntity.getTeamId()).setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setPrice(playerEntity.getPrice() / 10.0);
            EventLiveEntity eventLiveEntity = eventLiveMap.get(element);
            if (eventLiveEntity != null) {
                data.setMinutes(eventLiveEntity.getMinutes()).setGoalsScored(eventLiveEntity.getGoalsScored()).setAssists(eventLiveEntity.getAssists()).setCleanSheets(eventLiveEntity.getCleanSheets()).setGoalsConceded(eventLiveEntity.getGoalsConceded()).setOwnGoals(eventLiveEntity.getOwnGoals()).setPenaltiesSaved(eventLiveEntity.getPenaltiesSaved()).setPenaltiesMissed(eventLiveEntity.getPenaltiesSaved()).setYellowCards(eventLiveEntity.getYellowCards()).setRedCards(eventLiveEntity.getRedCards()).setSaves(eventLiveEntity.getSaves()).setBonus(eventLiveEntity.getBonus()).setBps(eventLiveEntity.getBps()).setTotalPoints(eventLiveEntity.getTotalPoints()).setPickActive(true);
            }
            if (!StringUtils.equalsIgnoreCase(Chip.BB.getValue(), chip) && data.getPosition() > 11) {
                data.setPickActive(false);
            }
            list.add(data);
        });
        return list;
    }

    @Cacheable(value = "api::qryEntryEventTransfers", key = "#event+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventTransfersData> qryEntryEventTransfers(int event, int entry) {
        if (event <= 0 || entry <= 0) {
            return Lists.newArrayList();
        }
        //prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, EventLiveEntity> pointsMap = this.queryService.getEventLiveByEvent(event);
        //collect
        List<EntryEventTransfersData> list = Lists.newArrayList();
        this.getEntryEventTransfers(event, entry).forEach(o -> {
            int elementIn = o.getElementIn();
            int elementOut = o.getElementOut();
            EntryEventTransfersData data = new EntryEventTransfersData().setEvent(o.getEvent()).setEntry(o.getEntry()).setElementIn(elementIn).setElementInCost(o.getElementInCost() / 10.0).setElementInPlayed(o.getElementInPlayed()).setElementOut(elementOut).setElementOutCost(o.getElementOutCost() / 10.0).setTime(o.getTime());
            PlayerEntity playerInEntity = playerMap.get(String.valueOf(elementIn));
            if (playerInEntity != null) {
                data.setElementInWebName(playerInEntity.getWebName()).setElementInType(playerInEntity.getElementType()).setElementInTypeName(Position.getNameFromElementType(playerInEntity.getElementType())).setElementInTeamId(playerInEntity.getTeamId()).setElementInTeamName(teamNameMap.getOrDefault(String.valueOf(playerInEntity.getTeamId()), "")).setElementInTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerInEntity.getTeamId()), "")).setElementInPoints(pointsMap.get(String.valueOf(elementIn)).getTotalPoints());
            }
            PlayerEntity playerOutEntity = playerMap.get(String.valueOf(o.getElementOut()));
            if (playerInEntity != null) {
                data.setElementOutWebName(playerOutEntity.getWebName()).setElementOutType(playerOutEntity.getElementType()).setElementOutTypeName(Position.getNameFromElementType(playerOutEntity.getElementType())).setElementOutTeamId(playerOutEntity.getTeamId()).setElementOutTeamName(teamNameMap.getOrDefault(String.valueOf(playerOutEntity.getTeamId()), "")).setElementOutTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerOutEntity.getTeamId()), "")).setElementOutPoints(pointsMap.get(String.valueOf(elementOut)).getTotalPoints());
            }
            list.add(data);
        });
        return list.stream().sorted(Comparator.comparing(EntryEventTransfersData::getTime)).collect(Collectors.toList());
    }

    private List<EntryEventTransfersEntity> getEntryEventTransfers(int event, int entry) {
        List<EntryEventTransfersEntity> list;
        list = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda().eq(EntryEventTransfersEntity::getEvent, event).eq(EntryEventTransfersEntity::getEntry, entry));
        if (!CollectionUtils.isEmpty(list)) {
            return list;
        }
        // collect
        List<UserTransfersRes> userTransfersResList = this.interfaceService.getUserTransfers(entry).orElse(null);
        if (CollectionUtils.isEmpty(userTransfersResList)) {
            return list;
        }
        userTransfersResList.forEach(o -> {
            if (o.getEvent() != event) {
                return;
            }
            list.add(new EntryEventTransfersEntity().setEvent(o.getEvent()).setEntry(o.getEntry()).setElementIn(o.getElementIn()).setElementInCost(o.getElementInCost()).setElementInPlayed(false).setElementOut(o.getElementOut()).setElementOutCost(o.getElementOutCost()).setTime(o.getTime()));
        });
        this.dataService.insertEntryEventTransfers(entry);
        return list;
    }

    @Cacheable(value = "api::qryEntryAllTransfers", key = "#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventTransfersData> qryEntryAllTransfers(int entry) {
        if (entry <= 0) {
            return Lists.newArrayList();
        }
        //prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        //collect
        List<Integer> eventList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).ne(EntryEventResultEntity::getEventChip, Chip.WC.getValue()).ne(EntryEventResultEntity::getEventChip, Chip.FH.getValue())).stream().map(EntryEventResultEntity::getEvent).collect(Collectors.toList());
        List<EntryEventTransfersData> list = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda().eq(EntryEventTransfersEntity::getEntry, entry).in(EntryEventTransfersEntity::getEvent, eventList)).stream().map(o -> this.initEntryEventTransfersData(entry, o, playerMap, teamNameMap, teamShortNameMap)).toList();
        return list.stream().sorted(Comparator.comparing(EntryEventTransfersData::getEvent).thenComparing(EntryEventTransfersData::getTime)).collect(Collectors.toList());
    }

    private EntryEventTransfersData initEntryEventTransfersData(int entry, EntryEventTransfersEntity entryEventTransfersEntity, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        EntryEventTransfersData data = new EntryEventTransfersData().setEvent(entryEventTransfersEntity.getEvent()).setEntry(entry).setTime(entryEventTransfersEntity.getTime());
        // transfersIn
        int elementIn = entryEventTransfersEntity.getElementIn();
        data.setElementIn(elementIn).setElementInCost(entryEventTransfersEntity.getElementInCost() / 10.0).setElementInPoints(entryEventTransfersEntity.getElementInPoints()).setElementInPlayed(entryEventTransfersEntity.getElementInPlayed());
        PlayerEntity inPlayer = playerMap.getOrDefault(String.valueOf(elementIn), null);
        if (inPlayer != null) {
            data.setElementInWebName(inPlayer.getWebName()).setElementInType(inPlayer.getElementType()).setElementInTypeName(Position.getNameFromElementType(inPlayer.getElementType())).setElementInTeamId(inPlayer.getTeamId()).setElementInTeamName(teamNameMap.getOrDefault(String.valueOf(inPlayer.getTeamId()), "")).setElementInTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(inPlayer.getTeamId()), ""));
        }
        // transfersOut
        int elementOut = entryEventTransfersEntity.getElementOut();
        data.setElementOut(elementOut).setElementOutCost(entryEventTransfersEntity.getElementOutCost() / 10.0).setElementOutPoints(entryEventTransfersEntity.getElementOutPoints());
        PlayerEntity outPlayer = playerMap.getOrDefault(String.valueOf(elementOut), null);
        if (outPlayer != null) {
            data.setElementOutWebName(outPlayer.getWebName()).setElementOutType(outPlayer.getElementType()).setElementOutTypeName(Position.getNameFromElementType(outPlayer.getElementType())).setElementOutTeamId(outPlayer.getTeamId()).setElementOutTeamName(teamNameMap.getOrDefault(String.valueOf(outPlayer.getTeamId()), "")).setElementOutTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(outPlayer.getTeamId()), ""));
        }
        return data;
    }

    /**
     * @implNote scout
     */
    @Cacheable(value = "api::qryScoutEntry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, String> qryScoutEntry() {
        Map<String, String> map = Maps.newHashMap();
        RedisUtils.getHashByKey("scoutEntry").forEach((k, v) -> map.put(k.toString(), v.toString()));
        return map;
    }

    @Cacheable(value = "api::qryEventScoutPickResult", key = "#event+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.entry eq 0")
    @Override
    public EventScoutData qryEventScoutPickResult(int event, int entry) {
        if (event <= 0 || entry <= 0) {
            return new EventScoutData();
        }
        Map<String, String> scoutMap = this.qryScoutEntry();
        if (!scoutMap.containsKey(String.valueOf(entry))) {
            return new EventScoutData();
        }
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        ScoutEntity scoutEntity = this.scoutService.getOne(new QueryWrapper<ScoutEntity>().lambda().eq(ScoutEntity::getEvent, event).eq(ScoutEntity::getEntry, entry));
        if (scoutEntity != null) {
            return this.initScoutData(scoutEntity, playerMap, teamShortNameMap);
        }
        scoutEntity = this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda().lt(ScoutEntity::getEvent, event).eq(ScoutEntity::getEntry, entry)).stream().max(Comparator.comparing(ScoutEntity::getEvent)).orElse(null);
        if (scoutEntity == null) {
            return new EventScoutData().setLeftTransfers(this.qryEventScoutLeftTransfers(event, entry));
        }
        return new EventScoutData().setEvent(event).setEntry(entry).setTransfers(0).setLeftTransfers(this.qryEventScoutLeftTransfers(event, entry)).setEventPoints(0).setTotalPoints(0).setScoutName(scoutEntity.getScoutName()).setGkpInfo(this.initScoutElementData(scoutEntity.getGkp(), scoutEntity.getGkpTeamId(), 0, playerMap.get(String.valueOf(scoutEntity.getGkp())), teamShortNameMap)).setDefInfo(this.initScoutElementData(scoutEntity.getDef(), scoutEntity.getDefTeamId(), 0, playerMap.get(String.valueOf(scoutEntity.getDef())), teamShortNameMap)).setMidInfo(this.initScoutElementData(scoutEntity.getMid(), scoutEntity.getMidTeamId(), 0, playerMap.get(String.valueOf(scoutEntity.getMid())), teamShortNameMap)).setFwdInfo(this.initScoutElementData(scoutEntity.getFwd(), scoutEntity.getFwdTeamId(), 0, playerMap.get(String.valueOf(scoutEntity.getFwd())), teamShortNameMap)).setCaptainInfo(this.initScoutElementData(scoutEntity.getCaptain(), scoutEntity.getCaptainTeamId(), 0, playerMap.get(String.valueOf(scoutEntity.getCaptain())), teamShortNameMap)).setReason(scoutEntity.getReason());
    }

    // do not cache
    @Override
    public int qryEventScoutLeftTransfers(int event, int entry) {
        if (event <= 0 || event > 38 || event == 1) {
            return -1;
        }
        ScoutEntity scoutEntity = this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda().lt(ScoutEntity::getEvent, event).eq(ScoutEntity::getEntry, entry)).stream().max(Comparator.comparing(ScoutEntity::getEvent)).orElse(null);
        if (scoutEntity == null) {
            return -1;
        }
        int lastEvent = scoutEntity.getEvent();
        int leftTransfers = scoutEntity.getLeftTransfers();
        if (leftTransfers == -1) {
            leftTransfers = 0;
        }
        return Math.min(leftTransfers + (event - lastEvent), 4);
    }

    @Cacheable(value = "api::qryPopularScoutSourceList", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<String> qryAllPopularScoutSource() {
        List<String> list = this.popularScoutResultService.list().stream().map(PopularScoutResultEntity::getSource).collect(Collectors.toList());
        list.add("Overall");
        return list;
    }

    @Cacheable(value = "api::qryEventSourceScoutResult", key = "#event+'::'+#source", cacheManager = "apiCacheManager", unless = "#result eq null")
    @Override
    public PopularScoutData qryEventSourceScoutResult(int event, String source) {
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        PopularScoutResultEntity popularScoutResultEntity = this.popularScoutResultService.getOne(new QueryWrapper<PopularScoutResultEntity>().lambda().eq(PopularScoutResultEntity::getEvent, event).eq(PopularScoutResultEntity::getSource, source));
        if (popularScoutResultEntity == null) {
            return new PopularScoutData();
        }
        return new PopularScoutData().setEvent(event).setSource(source).setCaptain(popularScoutResultEntity.getCaptain()).setCaptainPoints(popularScoutResultEntity.getCaptainPoints()).setViceCaptain(popularScoutResultEntity.getViceCaptain()).setViceCaptainPoints(popularScoutResultEntity.getViceCaptainPoints()).setPlayedCaptain(popularScoutResultEntity.getPlayedCaptain()).setPlayedCaptainPoints(popularScoutResultEntity.getPlayedCaptainPoints()).setPlayedCaptainName(webNameMap.getOrDefault(String.valueOf(popularScoutResultEntity.getPlayedCaptain()), "")).setRawTotalPoints(popularScoutResultEntity.getRawTotalPoints()).setTotalPoints(popularScoutResultEntity.getTotalPoints()).setAveragePoints(popularScoutResultEntity.getAveragePoints()).setChip(popularScoutResultEntity.getChip()).setElementList(this.getEventSourceScoutElementList(popularScoutResultEntity));
    }

    private List<ElementEventResultData> getEventSourceScoutElementList(PopularScoutResultEntity popularScoutResultEntity) {
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        // init
        List<ElementEventResultData> list = Lists.newArrayList();
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition1(), popularScoutResultEntity.getPosition1Points(), 1, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition2(), popularScoutResultEntity.getPosition2Points(), 2, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition3(), popularScoutResultEntity.getPosition3Points(), 3, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition4(), popularScoutResultEntity.getPosition4Points(), 4, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition5(), popularScoutResultEntity.getPosition5Points(), 5, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition6(), popularScoutResultEntity.getPosition6Points(), 6, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition7(), popularScoutResultEntity.getPosition7Points(), 7, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition8(), popularScoutResultEntity.getPosition8Points(), 8, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition9(), popularScoutResultEntity.getPosition9Points(), 9, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition10(), popularScoutResultEntity.getPosition10Points(), 10, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition11(), popularScoutResultEntity.getPosition11Points(), 11, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition12(), popularScoutResultEntity.getPosition12Points(), 12, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition13(), popularScoutResultEntity.getPosition13Points(), 13, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition14(), popularScoutResultEntity.getPosition14Points(), 14, playerMap, teamNameMap, teamShortNameMap));
        list.add(this.getEventSourceScoutElementData(popularScoutResultEntity.getPosition15(), popularScoutResultEntity.getPosition15Points(), 15, playerMap, teamNameMap, teamShortNameMap));
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private ElementEventResultData getEventSourceScoutElementData(int element, int points, int position, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
        if (playerEntity == null) {
            return null;
        }
        return new ElementEventResultData().setElement(element).setCode(playerEntity.getCode()).setWebName(playerEntity.getWebName()).setPrice(playerEntity.getPrice() / 10.0).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId()).setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setTotalPoints(points).setPosition(position);
    }

    @Cacheable(value = "api::qryOverallEventScoutResult", key = "#event", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<PopularScoutData> qryOverallEventScoutResult(int event) {
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        return this.popularScoutResultService.list(new QueryWrapper<PopularScoutResultEntity>().lambda().eq(PopularScoutResultEntity::getEvent, event)).stream().map(o -> new PopularScoutData().setEvent(event).setSource(o.getSource()).setCaptain(o.getCaptain()).setCaptainPoints(o.getCaptainPoints()).setViceCaptain(o.getViceCaptain()).setViceCaptainPoints(o.getViceCaptainPoints()).setPlayedCaptain(o.getPlayedCaptain()).setPlayedCaptainName(webNameMap.getOrDefault(String.valueOf(o.getPlayedCaptain()), "")).setPlayedCaptainPoints(o.getPlayedCaptainPoints()).setRawTotalPoints(o.getRawTotalPoints()).setTotalPoints(o.getTotalPoints()).setAveragePoints(o.getAveragePoints()).setChip(o.getChip())).sorted(Comparator.comparing(PopularScoutData::getTotalPoints).reversed()).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryEventScoutResult", key = "#event", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EventScoutData> qryEventScoutResult(int event) {
        if (event == 0) {
            return this.qrySeasonScoutResult();
        }
        if (event > 38) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        // return
        return this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda().eq(ScoutEntity::getEvent, event)).stream().map(o -> this.initScoutData(o, playerMap, teamShortNameMap)).sorted(Comparator.comparing(EventScoutData::getEventPoints).reversed().thenComparing(EventScoutData::getUpdateTime)).collect(Collectors.toList());
    }

    private List<EventScoutData> qrySeasonScoutResult() {
        Multimap<Integer, ScoutEntity> map = HashMultimap.create();
        this.scoutService.list().stream().filter(o -> o.getEventPoints() > 0).forEach(o -> map.put(o.getEntry(), o));
        List<EventScoutData> list = Lists.newArrayList();
        map.keySet().forEach(entry -> {
            Collection<ScoutEntity> scoutList = map.get(entry);
            if (CollectionUtils.isEmpty(scoutList)) {
                return;
            }
            ScoutEntity lastScout = scoutList.stream().max(Comparator.comparing(ScoutEntity::getEvent)).orElse(null);
            list.add(new EventScoutData().setEvent(0).setEntry(entry).setScoutName(lastScout.getScoutName()).setGkpInfo(new PlayerInfoData().setPoints(scoutList.stream().mapToInt(ScoutEntity::getGkpPoints).sum())).setDefInfo(new PlayerInfoData().setPoints(scoutList.stream().mapToInt(ScoutEntity::getDefPoints).sum())).setMidInfo(new PlayerInfoData().setPoints(scoutList.stream().mapToInt(ScoutEntity::getMidPoints).sum())).setFwdInfo(new PlayerInfoData().setPoints(scoutList.stream().mapToInt(ScoutEntity::getFwdPoints).sum())).setCaptainInfo(new PlayerInfoData().setPoints(scoutList.stream().mapToInt(ScoutEntity::getCaptainPoints).sum())).setReason("").setEventPoints(lastScout.getEventPoints()).setTotalPoints(lastScout.getTotalPoints()).setUpdateTime(lastScout.getUpdateTime()));
        });
        return list.stream().sorted(Comparator.comparing(EventScoutData::getTotalPoints).reversed().thenComparing(EventScoutData::getUpdateTime)).collect(Collectors.toList());
    }

    private EventScoutData initScoutData(ScoutEntity scoutEntity, Map<String, PlayerEntity> playerMap, Map<String, String> teamShortNameMap) {
        return new EventScoutData().setEvent(scoutEntity.getEvent()).setEntry(scoutEntity.getEntry()).setScoutName(scoutEntity.getScoutName()).setTransfers(scoutEntity.getTransfers()).setLeftTransfers(scoutEntity.getLeftTransfers()).setGkpInfo(this.initScoutElementData(scoutEntity.getGkp(), scoutEntity.getGkpTeamId(), scoutEntity.getGkpPoints(), playerMap.get(String.valueOf(scoutEntity.getGkp())), teamShortNameMap)).setDefInfo(this.initScoutElementData(scoutEntity.getDef(), scoutEntity.getDefTeamId(), scoutEntity.getDefPoints(), playerMap.get(String.valueOf(scoutEntity.getDef())), teamShortNameMap)).setMidInfo(this.initScoutElementData(scoutEntity.getMid(), scoutEntity.getMidTeamId(), scoutEntity.getMidPoints(), playerMap.get(String.valueOf(scoutEntity.getMid())), teamShortNameMap)).setFwdInfo(this.initScoutElementData(scoutEntity.getFwd(), scoutEntity.getFwdTeamId(), scoutEntity.getFwdPoints(), playerMap.get(String.valueOf(scoutEntity.getFwd())), teamShortNameMap)).setCaptainInfo(this.initScoutElementData(scoutEntity.getCaptain(), scoutEntity.getCaptainTeamId(), scoutEntity.getCaptainPoints(), playerMap.get(String.valueOf(scoutEntity.getCaptain())), teamShortNameMap)).setReason(scoutEntity.getReason()).setEventPoints(scoutEntity.getEventPoints()).setTotalPoints(scoutEntity.getTotalPoints()).setUpdateTime(scoutEntity.getUpdateTime());
    }

    private PlayerInfoData initScoutElementData(int element, int teamId, int points, PlayerEntity playerEntity, Map<String, String> teamShortNameMap) {
        return new PlayerInfoData().setElement(element).setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setTeamId(teamId).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), "")).setPrice(playerEntity.getPrice() / 10.0).setPoints(points);
    }

    /**
     * @implNote live (do not cache)
     */
    @Override
    public List<LiveMatchData> qryLiveMatchByStatus(String playStatus) {
        if (!MatchPlayStatus.legalStatusName(playStatus)) {
            return Lists.newArrayList();
        }
        // prepare
        int event = this.queryService.getCurrentEvent();
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Collection<EventLiveEntity> eventLiveList = this.queryService.getEventLiveByEvent(event).values();
        // next event
        if (StringUtils.equalsIgnoreCase(MatchPlayStatus.Next_Event.name(), playStatus)) {
            return this.qryNextEventMatch(event, teamNameMap, teamShortNameMap);
        }
        // collect
        List<LiveMatchData> list = Lists.newArrayList();
        Map<String, Map<String, List<LiveFixtureData>>> eventLiveFixtureMap = this.redisCacheService.getEventLiveFixtureMap();
        eventLiveFixtureMap.keySet().forEach(teamId -> eventLiveFixtureMap.get(teamId).forEach((status, fixtureList) -> {
            if (!StringUtils.equalsIgnoreCase(status, playStatus)) {
                return;
            }
            fixtureList.forEach(o -> {
                if (!o.isWasHome()) {
                    return;
                }
                LiveMatchData liveMatchData = new LiveMatchData().setMatchId(list.size() + 1).setHomeTeamId(o.getTeamId()).setHomeTeamName(o.getTeamName()).setHomeTeamShortName(o.getTeamShortName()).setHomeScore(o.getTeamScore()).setHomeTeamDataList(this.qryLiveTeamData(o.getTeamId(), eventLiveList, playerMap, teamShortNameMap)).setAwayTeamId(o.getAgainstId()).setAwayTeamName(o.getAgainstName()).setAwayTeamShortName(o.getAgainstShortName()).setAwayScore(o.getAgainstTeamScore()).setAwayTeamDataList(this.qryLiveTeamData(o.getAgainstId(), eventLiveList, playerMap, teamShortNameMap)).setKickoffTime(o.getKickoffTime());
                liveMatchData.setMinutes(liveMatchData.getHomeTeamDataList().stream().max(Comparator.comparing(ElementEventResultData::getMinutes)).map(ElementEventResultData::getMinutes).orElse(0));
                list.add(liveMatchData);
            });
        }));
        return list.stream().sorted(Comparator.comparing(o -> LocalDateTime.parse(o.getKickoffTime().replaceAll(" ", "T")))).collect(Collectors.toList());
    }

    private List<LiveMatchData> qryNextEventMatch(int event, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        List<LiveMatchData> list = Lists.newArrayList();
        if (event > 38) {
            return list;
        }
        this.eventFixtureService.list(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event + 1).orderByAsc(EventFixtureEntity::getKickoffTime)).forEach(o -> list.add(new LiveMatchData().setMatchId(list.size() + 1).setMinutes(0).setHomeTeamId(o.getTeamH()).setHomeTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamH()), "")).setHomeTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamH()), "")).setHomeScore(0).setHomeTeamDataList(Lists.newArrayList()).setAwayTeamId(o.getTeamA()).setAwayTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamA()), "")).setAwayTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamA()), "")).setAwayScore(0).setAwayTeamDataList(Lists.newArrayList()).setKickoffTime(o.getKickoffTime())));
        return list.stream().sorted(Comparator.comparing(o -> LocalDateTime.parse(o.getKickoffTime().replaceAll(" ", "T")))).collect(Collectors.toList());
    }

    private List<ElementEventResultData> qryLiveTeamData(int teamId, Collection<EventLiveEntity> eventLiveList, Map<String, PlayerEntity> playerMap, Map<String, String> teamShortNameMap) {
        List<ElementEventResultData> list = Lists.newArrayList();
        // team data
        Map<Integer, Integer> liveBonusMap = this.getLiveBonusMap(teamId);
        eventLiveList.forEach(o -> {
            if (o.getTeamId() != teamId || o.getMinutes() <= 0) {
                return;
            }
            ElementEventResultData elementEventResultData = new ElementEventResultData().setEvent(o.getEvent()).setElement(o.getElement()).setWebName(playerMap.containsKey(String.valueOf(o.getElement())) ? playerMap.get(String.valueOf(o.getElement())).getWebName() : "").setElementType(o.getElementType()).setElementTypeName(Position.getNameFromElementType(o.getElementType())).setTeamId(playerMap.containsKey(String.valueOf(o.getElement())) ? playerMap.get(String.valueOf(o.getElement())).getTeamId() : 0).setMinutes(o.getMinutes()).setGoalsScored(o.getGoalsScored()).setAssists(o.getAssists()).setGoalsConceded(o.getGoalsConceded()).setOwnGoals(o.getOwnGoals()).setPenaltiesSaved(o.getPenaltiesSaved()).setPenaltiesMissed(o.getPenaltiesMissed()).setYellowCards(o.getYellowCards()).setRedCards(o.getRedCards()).setSaves(o.getSaves()).setBps(o.getBps()).setTotalPoints(o.getTotalPoints());
            if (o.getBonus() > 0) {
                elementEventResultData.setBonus(o.getBonus()).setTotalPoints(elementEventResultData.getTotalPoints());
            } else {
                elementEventResultData.setBonus(liveBonusMap.getOrDefault(o.getElement(), 0)).setTotalPoints(elementEventResultData.getTotalPoints() + elementEventResultData.getBonus());
            }
            elementEventResultData.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(elementEventResultData.getTeamId()), ""));
            list.add(elementEventResultData);
        });
        return list.stream().sorted(Comparator.comparing(ElementEventResultData::getTotalPoints).reversed()).collect(Collectors.toList());
    }

    private Map<Integer, Integer> getLiveBonusMap(int teamId) {
        Map<Integer, Integer> map = Maps.newHashMap();
        this.queryService.getLiveBonusCacheMap().forEach((team, list) -> {
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
    @Cacheable(value = "api::qryPlayerInfoByElement", key = "#season+'::'+#element", cacheManager = "apiCacheManager", unless = "#result eq null or #result.element eq 0")
    @Override
    public PlayerInfoData qryPlayerInfoByElement(String season, int element) {
        if (element <= 0) {
            return new PlayerInfoData();
        }
        if (!Season.legalSeason(season)) {
            return new PlayerInfoData();
        }
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getById(element);
        if (playerEntity == null) {
            MybatisPlusConfig.season.remove();
            return new PlayerInfoData();
        }
        PlayerStatEntity playerStatEntity = this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda().eq(PlayerStatEntity::getElement, element)).stream().max(Comparator.comparing(PlayerStatEntity::getEvent)).orElse(null);
        if (playerStatEntity == null) {
            MybatisPlusConfig.season.remove();
            return new PlayerInfoData();
        }
        EventLiveSummaryEntity eventLiveSummaryEntity = this.queryService.qryEventLiveSummaryByElement(season, element);
        if (eventLiveSummaryEntity == null) {
            MybatisPlusConfig.season.remove();
            return new PlayerInfoData();
        }
        MybatisPlusConfig.season.remove();
        return this.initPlayerInfoData(season, playerEntity, playerStatEntity, eventLiveSummaryEntity);
    }

    @Cacheable(value = "api::qryPlayerInfoByCode", key = "#season+'::'+#code", cacheManager = "apiCacheManager", unless = "#result eq null or #result.element eq 0")
    @Override
    public PlayerInfoData qryPlayerInfoByCode(String season, int code) {
        if (code <= 0) {
            return new PlayerInfoData();
        }
        if (!Season.legalSeason(season)) {
            return new PlayerInfoData();
        }
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getCode, code));
        if (playerEntity == null) {
            MybatisPlusConfig.season.remove();
            return new PlayerInfoData();
        }
        PlayerStatEntity playerStatEntity = this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda().eq(PlayerStatEntity::getCode, code)).stream().max(Comparator.comparing(PlayerStatEntity::getEvent)).orElse(null);
        if (playerStatEntity == null) {
            MybatisPlusConfig.season.remove();
            return new PlayerInfoData();
        }
        EventLiveSummaryEntity eventLiveSummaryEntity = this.queryService.qryEventLiveSummaryByElement(season, playerEntity.getElement());
        MybatisPlusConfig.season.remove();
        return this.initPlayerInfoData(season, playerEntity, playerStatEntity, eventLiveSummaryEntity);
    }

    private PlayerInfoData initPlayerInfoData(String season, PlayerEntity playerEntity, PlayerStatEntity playerStatEntity, EventLiveSummaryEntity eventLiveSummaryEntity) {
        return new PlayerInfoData().setElement(playerEntity.getElement()).setCode(playerEntity.getCode()).setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId()).setTeamName(this.queryService.getTeamNameByTeam(season, playerEntity.getTeamId())).setTeamShortName(this.queryService.getTeamShortNameByTeam(season, playerEntity.getTeamId())).setPrice(playerEntity.getPrice() / 10.0).setStartPrice(playerEntity.getStartPrice() / 10.0).setPoints(eventLiveSummaryEntity == null ? 0 : eventLiveSummaryEntity.getTotalPoints()).setSelectedByPercent(playerStatEntity.getSelectedByPercent());
    }

    @Cacheable(value = "api::qryPlayerInfoByElementType", key = "#elementType", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType) {
        if (elementType < 1 || elementType > 4) {
            return Maps.newLinkedHashMap();
        }
        LinkedHashMap<String, List<PlayerInfoData>> map = Maps.newLinkedHashMap();
        Multimap<String, PlayerInfoData> multimap = HashMultimap.create();
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<Integer, Integer> totalPointsMap = this.eventLiveSummaryService.list().stream().collect(Collectors.toMap(EventLiveSummaryEntity::getElement, EventLiveSummaryEntity::getTotalPoints));
        // init
        this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getElementType, elementType)).forEach(o -> {
            PlayerInfoData data = BeanUtil.copyProperties(o, PlayerInfoData.class);
            data.setElementTypeName(Position.getNameFromElementType(o.getElementType())).setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamId()), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamId()), "")).setPrice(o.getPrice() / 10.0).setStartPrice(o.getStartPrice() / 10.0).setPoints(totalPointsMap.getOrDefault(o.getElement(), 0));
            multimap.put(data.getTeamShortName(), data);
        });
        // collect
        List<String> shortNameSortedList = multimap.keySet().stream().sorted(Comparator.comparing(String::toUpperCase)).toList();
        shortNameSortedList.forEach(team -> map.put(team, multimap.get(team).stream().sorted(Comparator.comparing(PlayerInfoData::getPrice).reversed()).collect(Collectors.toList())));
        return map;
    }

    @Cacheable(value = "api::qryPlayerDetailByElement", key = "#element", cacheManager = "apiCacheManager", unless = "#result eq null or #result.element eq 0")
    @Override
    public PlayerDetailData qryPlayerDetailByElement(int element) {
        if (element <= 0) {
            return new PlayerDetailData();
        }
        return this.queryService.qryPlayerDetailData(element);
    }

    @Cacheable(value = "api::qryTeamFixtureByShortName", key = "#shortName", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName) {
        if (StringUtils.isEmpty(shortName)) {
            return Maps.newHashMap();
        }
        TeamEntity teamEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getShortName, shortName));
        if (teamEntity == null) {
            return Maps.newHashMap();
        }
        return this.queryService.getEventFixtureByTeamId(teamEntity.getId());
    }

    @Cacheable(value = "api::qryFilterPlayers", key = "#season", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<PlayerFilterData> qryFilterPlayers(String season) {
        List<PlayerFilterData> list = Lists.newArrayList();
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap(season);
        Map<String, PlayerStatEntity> playerStatMap = this.queryService.getPlayerStatMap(season);
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap(season);
        Map<String, EventLiveSummaryEntity> eventLiveSummaryMap = this.queryService.getEventLiveSummaryMap(season);
        // calc
        playerMap.values().forEach(o -> {
            int element = o.getElement();
            EventLiveSummaryEntity eventLiveSummaryEntity = eventLiveSummaryMap.getOrDefault(String.valueOf(element), null);
            PlayerFilterData data = new PlayerFilterData().setCode(o.getCode()).setWebName(o.getWebName()).setElementTypeName(Position.getNameFromElementType(o.getElementType())).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamId()), "")).setPrice(o.getPrice() / 10.0).setPoints(eventLiveSummaryEntity == null ? 0 : eventLiveSummaryEntity.getTotalPoints());
            // player_stat
            PlayerStatEntity playerStatEntity = playerStatMap.getOrDefault(String.valueOf(element), null);
            if (playerStatEntity != null) {
                BeanUtil.copyProperties(playerStatEntity, data);
            }
            list.add(data);
        });
        return list;
    }

    /**
     * @implNote stat
     */
    @Cacheable(value = "api::qryPlayerValueByDate", key = "#date", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, List<PlayerValueData>> qryPlayerValueByDate(String date) {
        Map<String, List<PlayerValueData>> map = Maps.newHashMap();
        if (StringUtils.isEmpty(date)) {
            return map;
        }
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        List<PlayerValueEntity> playerValueList = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda().eq(PlayerValueEntity::getChangeDate, date));
        if (CollectionUtils.isEmpty(playerValueList)) {
            return map;
        }
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        // collect
        List<PlayerValueData> playerValueDataList = playerValueList.stream().map(o -> this.initPlayerValueData(o, teamNameMap, teamShortNameMap, playerMap)).toList();
        map.put(ValueChangeType.Start.name(), playerValueDataList.stream().filter(o -> StringUtils.equals(ValueChangeType.Start.name(), o.getChangeType())).collect(Collectors.toList()));
        map.put(ValueChangeType.Rise.name(), playerValueDataList.stream().filter(o -> StringUtils.equals(ValueChangeType.Rise.name(), o.getChangeType())).collect(Collectors.toList()));
        map.put(ValueChangeType.Faller.name(), playerValueDataList.stream().filter(o -> StringUtils.equals(ValueChangeType.Faller.name(), o.getChangeType())).collect(Collectors.toList()));
        return map;
    }

    @Cacheable(value = "api::qryPlayerPriceChange", key = "#date", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<PlayerValueData> qryPlayerPriceChange(String date) {
        List<PlayerValueData> list = Lists.newArrayList();
        Map<String, List<PlayerValueData>> map = this.qryPlayerValueByDate(date);
        if(CollectionUtils.isEmpty(map)) {
            return list;
        }
        list.addAll(map.getOrDefault(ValueChangeType.Rise.name(), Lists.newArrayList()));
        list.addAll(map.getOrDefault(ValueChangeType.Faller.name(), Lists.newArrayList()));
        list.addAll(map.getOrDefault(ValueChangeType.Start.name(), Lists.newArrayList()));
        return list;
    }

    @Cacheable(value = "api::qryPlayerValueByElement", key = "#element", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<PlayerValueData> qryPlayerValueByElement(int element) {
        if (element <= 0) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        List<PlayerValueEntity> playerValueList = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda().eq(PlayerValueEntity::getElement, element).orderByAsc(PlayerValueEntity::getChangeDate));
        if (CollectionUtils.isEmpty(playerValueList)) {
            return Lists.newArrayList();
        }
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        // collect
        return playerValueList.stream().map(o -> this.initPlayerValueData(o, teamNameMap, teamShortNameMap, playerMap)).collect(Collectors.toList());
    }

    private PlayerValueData initPlayerValueData(PlayerValueEntity playerValueEntity, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, PlayerEntity> playerMap) {
        int element = playerValueEntity.getElement();
        PlayerValueData data = BeanUtil.copyProperties(playerValueEntity, PlayerValueData.class);
        PlayerEntity playerEntity = playerMap.get(String.valueOf(element));
        if (playerEntity != null) {
            data.setWebName(playerEntity.getWebName()).setTeamId(playerEntity.getTeamId()).setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setValue(playerValueEntity.getValue() / 10.0).setChangeDate(StringUtils.joinWith("-", data.getChangeDate().substring(0, 4), data.getChangeDate().substring(4, 6), data.getChangeDate().substring(6, 8))).setLastValue(playerValueEntity.getLastValue() / 10.0);
        }
        return data;
    }

    @Cacheable(value = "api::qrySeasonFixture", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<List<String>> qrySeasonFixture() {
        List<List<String>> list = Lists.newArrayList();
        // prepare
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        // fixture
        this.queryService.getTeamShortNameMap().keySet().stream().mapToInt(Integer::parseInt).sorted().forEach(teamId -> {
            List<String> elementList = Lists.newArrayList(teamShortNameMap.get(String.valueOf(teamId)));
            List<PlayerFixtureData> fixtureList = Lists.newArrayList();
            this.queryService.getEventFixtureByTeamId(teamId).values().forEach(fixtureList::addAll);
            fixtureList.stream().sorted(Comparator.comparing(PlayerFixtureData::getEvent).thenComparing(PlayerFixtureData::getKickoffTime)).forEach(o -> elementList.add(o.getAgainstTeamShortName()));
            list.add(elementList);
        });
        return list;
    }

    @Cacheable(value = "api::qryLeagueEventEoWebNameMap", key = "#season+'::'+#event+'::'+#leagueId+'::'+#leagueType", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, String> qryLeagueEventEoWebNameMap(String season, int event, int leagueId, String leagueType) {
        Map<String, String> map = Maps.newHashMap();
        // prepare
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        // collect
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getLeagueId, leagueId).eq(LeagueEventReportEntity::getLeagueType, leagueType).eq(LeagueEventReportEntity::getEvent, event));
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
        Map<Integer, Long> countMap = elementList.stream().collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
        countMap.forEach((element, count) -> map.put(webNameMap.getOrDefault(String.valueOf(element), ""), NumberUtil.formatPercent(NumberUtil.div(count.doubleValue(), size), 1)));
        return map;
    }

    @Cacheable(value = "api::qryLeagueSelectByName", key = "#season+'::'+#event+'::'+#leagueId+'::'+#leagueName", cacheManager = "apiCacheManager", unless = "#result eq null or #result.captainSelect eq null ")
    @Override
    public LeagueEventSelectData qryLeagueSelectByName(String season, int event, int leagueId, String leagueName) {
        if (event <= 0 || leagueId <= 0) {
            return new LeagueEventSelectData();
        }
        if (!Season.legalSeason(season)) {
            return new LeagueEventSelectData();
        }
        MybatisPlusConfig.season.set(season);
        LeagueEventSelectData data = new LeagueEventSelectData().setEvent(event).setName(leagueName);
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap(season);
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap(season);
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        // team_select
        List<LeagueEventReportEntity> leagueEventReportList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getEvent, event).eq(LeagueEventReportEntity::getLeagueId, leagueId));
        int teamSize = leagueEventReportList.size();
        if (CollectionUtils.isEmpty(leagueEventReportList)) {
            MybatisPlusConfig.season.remove();
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
        data.setMostSelectTeamGkp(mostSelectTeam.get(1)).setMostSelectTeamDef(mostSelectTeam.get(2)).setMostSelectTeamMid(mostSelectTeam.get(3)).setMostSelectTeamFwd(mostSelectTeam.get(4));
        MybatisPlusConfig.season.remove();
        return data;
    }

    private List<PlayerSelectData> getMostTransferIn(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
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
            currentList.stream().filter(o -> !previousList.contains(o)).forEach(elementList::add);
        });
        return this.collectSelectDataList(elementList, teamSize, 5, playerMap, teamNameMap, teamShortNameMap, Maps.newHashMap());
    }

    private List<PlayerSelectData> getMostTransferOut(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
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
            previousList.stream().filter(o -> !currentList.contains(o)).forEach(elementList::add);
        });
        return this.collectSelectDataList(elementList, teamSize, 5, playerMap, teamNameMap, teamShortNameMap, Maps.newHashMap());
    }

    private Map<Integer, List<Integer>> collectPreviousEntrySelectedMap(String leagueName, int event) {
        List<LeagueEventReportEntity> previousSelectList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getLeagueName, leagueName).eq(LeagueEventReportEntity::getEvent, event - 1));
        return this.collectEntrySelectedMap(previousSelectList);
    }

    private Map<Integer, List<Integer>> collectEntrySelectedMap(List<LeagueEventReportEntity> leagueEventReportList) {
        Map<Integer, List<Integer>> teamSelectMap = Maps.newHashMap();
        leagueEventReportList.forEach(o -> {
            List<Integer> elementList = Lists.newArrayList(o.getPosition1(), o.getPosition2(), o.getPosition3(), o.getPosition4(), o.getPosition5(), o.getPosition6(), o.getPosition7(), o.getPosition8(), o.getPosition9(), o.getPosition10(), o.getPosition11(), o.getPosition12(), o.getPosition13(), o.getPosition14(), o.getPosition15());
            teamSelectMap.put(o.getEntry(), elementList);
        });
        return teamSelectMap;
    }

    private List<PlayerSelectData> getCaptainSelect(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        List<Integer> elementList = leagueEventReportList.stream().map(LeagueEventReportEntity::getCaptain).collect(Collectors.toList());
        return this.collectSelectDataList(elementList, teamSize, 5, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
    }

    private List<PlayerSelectData> getViceCaptainSelect(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        // collect
        List<Integer> elementList = leagueEventReportList.stream().map(LeagueEventReportEntity::getViceCaptain).collect(Collectors.toList());
        return this.collectSelectDataList(elementList, teamSize, 5, playerMap, teamNameMap, teamShortNameMap, leagueEventEoMap);
    }

    private List<PlayerSelectData> getMostSelectPlayer(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
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

    private List<PlayerSelectData> collectSelectDataList(List<Integer> elementList, int teamSize, int limit, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        Map<Integer, Long> groupingMap = elementList.stream().collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
        Map<Integer, Integer> result = groupingMap.entrySet().stream().sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()).limit(limit).collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        return result.entrySet().stream().filter(o -> playerMap.containsKey(String.valueOf(o.getKey()))).map(o -> this.initCollectSelectData(o.getValue(), teamSize, playerMap.get(String.valueOf(o.getKey())), teamNameMap, teamShortNameMap, leagueEventEoMap)).collect(Collectors.toList());
    }

    private PlayerSelectData initCollectSelectData(int number, int teamSize, PlayerEntity playerEntity, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        return new PlayerSelectData().setElement(playerEntity.getElement()).setWebName(playerEntity.getWebName()).setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setSelectByPercent(CommonUtils.getPercentResult(number, teamSize)).setEoByPercent(leagueEventEoMap.getOrDefault(playerEntity.getWebName(), ""));
    }

    private Map<Integer, List<PlayerSelectData>> getMostSelectTeam(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, String> leagueEventEoMap) {
        // element list
        List<PlayerEntity> elementPlayerInfoList = Lists.newArrayList();
        leagueEventReportList.forEach(o -> {
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition1())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition2())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition3())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition4())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition5())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition6())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition7())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition8())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition9())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition10())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition11())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition12())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition13())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition14())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition15())));
        });
        // collect
        Map<Integer, Map<Integer, Long>> elementTypeCountMap = elementPlayerInfoList.stream().collect(Collectors.groupingBy(PlayerEntity::getElementType, Collectors.groupingBy(PlayerEntity::getElement, Collectors.counting())));
        // sort by element type
        Map<Integer, Integer> playerSelectedMap = Maps.newHashMap(); // key:element -> value: count
        elementTypeCountMap.keySet().forEach(elementType -> {
            Map<Integer, Integer> result = elementTypeCountMap.get(elementType).entrySet().stream().sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()).limit(this.getLimitByElementType(elementType)).collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
            playerSelectedMap.putAll(result);
        });
        // add key:element_type
        Map<Integer, Map<Integer, Integer>> elementTypeMap = this.collectPlayerSelectedMap(playerSelectedMap, playerMap); // element_type -> elementCountMap
        // sort by select
        LinkedHashMap<Integer, Integer> elementSelectedSortMap = playerSelectedMap.entrySet() // element -> count(sort by count)
                .stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        List<PlayerEntity> elementList = Lists.newArrayList();
        elementSelectedSortMap.forEach((k, v) -> elementList.add(playerMap.get(String.valueOf(k))));
        // select line up
        Map<Integer, List<PlayerSelectData>> map = Maps.newHashMap(); // element_type -> list
        LinkedHashMap<Integer, Integer> lineupMap = this.getLineupMapByElementList(elementTypeMap, elementList); // position -> element
        lineupMap.forEach((position, element) -> {
            long count = playerSelectedMap.get(element);
            PlayerEntity playerEntity = playerMap.get(String.valueOf(element));
            int elementType = playerEntity.getElementType();
            List<PlayerSelectData> valueList = Lists.newArrayList();
            if (map.containsKey(elementType)) {
                valueList = map.get(elementType);
            }
            valueList.add(new PlayerSelectData().setElement(playerEntity.getElement()).setWebName(playerEntity.getWebName()).setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setSelectByPercent(CommonUtils.getPercentResult((int) count, teamSize)).setEoByPercent(leagueEventEoMap.getOrDefault(playerEntity.getWebName(), ""))

            );
            map.put(elementType, valueList);
        });
        return map;
    }

    private Map<Integer, Map<Integer, Integer>> collectPlayerSelectedMap(Map<Integer, Integer> playerSelectedMap, Map<String, PlayerEntity> playerMap) {
        Map<Integer, Map<Integer, Integer>> map = Maps.newHashMap();
        playerSelectedMap.forEach((k, v) -> {
            int elementType = playerMap.get(String.valueOf(k)).getElementType();
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
        elementTypeMap.get(1).entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).forEachOrdered(o -> gkpList.add(o.getKey()));
        // def
        List<Integer> defList = Lists.newArrayList();
        elementTypeMap.get(2).entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).forEachOrdered(o -> defList.add(o.getKey()));
        // mid
        List<Integer> midList = Lists.newArrayList();
        elementTypeMap.get(3).entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).forEachOrdered(o -> midList.add(o.getKey()));
        // fwd
        List<Integer> fwdList = Lists.newArrayList();
        elementTypeMap.get(4).entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).forEachOrdered(o -> fwdList.add(o.getKey()));
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
        return switch (elementType) {
            case 1 -> 2;
            case 2, 3 -> 5;
            case 4 -> 3;
            default -> 0;
        };
    }

    @Cacheable(value = "api::qryPlayerSummary", key = "#season+'::'+#code", cacheManager = "apiCacheManager", unless = "#result eq null or #result.element eq 0")
    @Override
    public PlayerSummaryData qryPlayerSummary(String season, int code) {
        if (code <= 0) {
            return new PlayerSummaryData();
        }
        if (!Season.legalSeason(season)) {
            return new PlayerSummaryData();
        }
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getCode, code));
        MybatisPlusConfig.season.remove();
        if (playerEntity == null) {
            return new PlayerSummaryData();
        }
        return new PlayerSummaryData().setElement(playerEntity.getElement()).setCode(playerEntity.getCode()).setPrice(playerEntity.getPrice() / 10.0).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setWebName(playerEntity.getWebName()).setTeamId(playerEntity.getTeamId()).setTeamName(this.queryService.getTeamNameByTeam(season, playerEntity.getTeamId())).setTeamShortName(this.queryService.getTeamShortNameByTeam(season, playerEntity.getTeamId())).setDetailData(this.queryService.qryPlayerDetailData(season, playerEntity.getElement())).setFixtureList(this.queryService.qryPlayerFixtureList(season, playerEntity.getTeamId(), -1, -1));
    }

    @Cacheable(value = "api::qryPlayerAllTimeSummary", key = "#code", cacheManager = "apiCacheManager", unless = "#result eq null or #result.element eq 0")
    @Override
    public List<PlayerSeasonSummaryData> qryPlayerSeasonSummary(int code) {
        if (code <= 0) {
            return Lists.newArrayList();
        }
        Map<String, List<PlayerSummaryEntity>> playerSummaryMap = this.redisCacheService.getPlayerSummaryMap();
        if (CollectionUtils.isEmpty(playerSummaryMap)) {
            return Lists.newArrayList();
        }
        List<PlayerSummaryEntity> list = playerSummaryMap.getOrDefault(String.valueOf(code), null);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(o -> BeanUtil.copyProperties(o, PlayerSeasonSummaryData.class)).sorted(Comparator.comparing(PlayerSeasonSummaryData::getSeason)).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryTeamSummary", key = "#season+'::'+#name", cacheManager = "apiCacheManager", unless = "#result.teamId eq 0")
    @Override
    public TeamSummaryData qryTeamSummary(String season, String name) {
        TeamSummaryData data = new TeamSummaryData();
        if (!Season.legalSeason(season) || StringUtils.isEmpty(name)) {
            return data;
        }
        MybatisPlusConfig.season.set(season);
        TeamEntity teamEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getName, name));
        if (teamEntity == null) {
            MybatisPlusConfig.season.remove();
            return data;
        }
        int teamId = teamEntity.getId();
        List<PlayerEntity> playerList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getTeamId, teamId));
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(playerList)) {
            return data;
        }
        List<Integer> playerElementList = playerList.stream().map(PlayerEntity::getElement).collect(Collectors.toList());
        Map<Integer, String> webNameMap = playerList.stream().collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        data.setTeamId(teamId).setSeason(season).setTeamName(teamEntity.getName()).setTeamShortName(teamEntity.getShortName()).setPlayerMap(this.qryTeamPlayerDetailMap(playerList)).setFixtureList(this.queryService.qryPlayerFixtureList(season, teamId, -1, -1)).setCornersAndIndirectFreekicksOrders(Lists.newArrayList()).setDirectFreekicksOrders(Lists.newArrayList()).setPenaltiesOrders(Lists.newArrayList());
        if (StringUtils.equals(season, CommonUtils.getCurrentSeason())) {
            int event = this.queryService.getCurrentEvent();
            if (event <= 0) {
                event = 1;
            }
            List<PlayerStatEntity> playerStatList = this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda().eq(PlayerStatEntity::getEvent, event).in(PlayerStatEntity::getElement, playerElementList));
            data.setCornersAndIndirectFreekicksOrders(playerStatList.stream().filter(o -> o.getCornersAndIndirectFreekicksOrder() > 0).sorted(Comparator.comparing(PlayerStatEntity::getCornersAndIndirectFreekicksOrder)).map(o -> webNameMap.getOrDefault(o.getElement(), "")).collect(Collectors.toList())).setDirectFreekicksOrders(playerStatList.stream().filter(o -> o.getDirectFreekicksOrder() > 0).sorted(Comparator.comparing(PlayerStatEntity::getDirectFreekicksOrder)).map(o -> webNameMap.getOrDefault(o.getElement(), "")).collect(Collectors.toList())).setPenaltiesOrders(playerStatList.stream().filter(o -> o.getPenaltiesOrder() > 0).sorted(Comparator.comparing(PlayerStatEntity::getPenaltiesOrder)).map(o -> webNameMap.getOrDefault(o.getElement(), "")).collect(Collectors.toList()));
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
        data.setDetailData(new TeamDetailData().setTeamId(data.getTeamId()).setSeason(data.getSeason()).setWin((int) fixtureList.stream().filter(o -> !o.isBgw() || o.isFinished()).filter(o -> StringUtils.equalsIgnoreCase("W", o.getResult())).count()).setLose((int) fixtureList.stream().filter(o -> !o.isBgw() || o.isFinished()).filter(o -> StringUtils.equalsIgnoreCase("L", o.getResult())).count()).setDraw((int) fixtureList.stream().filter(o -> !o.isBgw() || o.isFinished()).filter(o -> StringUtils.equalsIgnoreCase("D", o.getResult())).count()).setForm(this.getTeamForm(fixtureList)).setGoalsScored(playerDetailDataList.stream().mapToInt(PlayerDetailData::getGoalsScored).sum()).setAssists(playerDetailDataList.stream().mapToInt(PlayerDetailData::getAssists).sum()).setCleanSheets(playerDetailDataList.stream().mapToInt(PlayerDetailData::getCleanSheets).sum()).setGoalsConceded(playerDetailDataList.stream().mapToInt(PlayerDetailData::getGoalsConceded).sum()).setYellowCards(playerDetailDataList.stream().mapToInt(PlayerDetailData::getYellowCards).sum()).setRedCards(playerDetailDataList.stream().mapToInt(PlayerDetailData::getRedCards).sum()).setPenaltiesSaved(playerDetailDataList.stream().mapToInt(PlayerDetailData::getPenaltiesSaved).sum()).setPenaltiesMissed(playerDetailDataList.stream().mapToInt(PlayerDetailData::getPenaltiesMissed).sum()).setSaves(playerDetailDataList.stream().mapToInt(PlayerDetailData::getSaves).sum()).setBonus(playerDetailDataList.stream().mapToInt(PlayerDetailData::getBonus).sum()));
        return data;
    }

    private String getTeamForm(List<PlayerFixtureData> fixtureList) {
        fixtureList = fixtureList.stream().filter(o -> !o.isBgw() || o.isFinished()).filter(o -> !StringUtils.isEmpty(o.getResult())).sorted(Comparator.comparing(PlayerFixtureData::getEvent)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fixtureList)) {
            return "";
        }
        if (fixtureList.size() > 5) {
            fixtureList = fixtureList.stream().sorted(Comparator.comparing(PlayerFixtureData::getEvent).reversed()).limit(5).sorted(Comparator.comparing(PlayerFixtureData::getEvent)).collect(Collectors.toList());
        }
        StringBuilder builder = new StringBuilder();
        for (PlayerFixtureData data : fixtureList) {
            builder.append(data.getResult()).append(",");
        }
        return builder.substring(0, builder.lastIndexOf(","));
    }

    private Map<Integer, List<PlayerSummaryData>> qryTeamPlayerDetailMap(List<PlayerEntity> playerList) {
        Multimap<Integer, PlayerSummaryData> multimap = HashMultimap.create();
        playerList.stream().sorted(Comparator.comparing(PlayerEntity::getElementType).thenComparing(PlayerEntity::getElement)).forEach(o -> multimap.put(o.getElementType(), new PlayerSummaryData().setElement(o.getElement()).setCode(o.getCode()).setPrice(o.getPrice() / 10.0).setElementType(o.getElementType()).setElementTypeName(Position.getNameFromElementType(o.getElementType())).setWebName(o.getWebName()).setDetailData(this.queryService.qryPlayerDetailData(o.getElement()))));
        Map<Integer, List<PlayerSummaryData>> map = Maps.newHashMap();
        // gkp
        map.put(1, multimap.get(1).stream().sorted(Comparator.comparing(PlayerSummaryData::getPrice)).collect(Collectors.toList()));
        // def
        map.put(2, multimap.get(2).stream().sorted(Comparator.comparing(PlayerSummaryData::getPrice).reversed()).collect(Collectors.toList()));
        // mid
        map.put(3, multimap.get(3).stream().sorted(Comparator.comparing(PlayerSummaryData::getPrice).reversed()).collect(Collectors.toList()));
        // fwd
        map.put(4, multimap.get(4).stream().sorted(Comparator.comparing(PlayerSummaryData::getPrice).reversed()).collect(Collectors.toList()));
        return map;
    }

    @Cacheable(value = "api::qryElementEventExplainResult", key = "#event+'::'+#element", cacheManager = "apiCacheManager", unless = "#result.element eq 0")
    @Override
    public ElementEventLiveExplainData qryElementEventExplainResult(int event, int element) {
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, EventLiveExplainEntity> eventLiveExplainMap = this.queryService.getEventLiveExplainByEvent(event);
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(playerMap) || CollectionUtils.isEmpty(eventLiveExplainMap) || CollectionUtils.isEmpty(teamShortNameMap)) {
            return new ElementEventLiveExplainData();
        }
        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
        if (playerEntity == null) {
            return new ElementEventLiveExplainData();
        }
        PlayerStatEntity playerStatEntity = this.queryService.getPlayerStatByElement(element);
        if (playerStatEntity == null) {
            return new ElementEventLiveExplainData();
        }
        EventLiveExplainEntity eventLiveExplainEntity = eventLiveExplainMap.getOrDefault(String.valueOf(element), null);
        if (eventLiveExplainEntity == null) {
            return new ElementEventLiveExplainData();
        }
        ElementEventLiveExplainData data = BeanUtil.copyProperties(eventLiveExplainEntity, ElementEventLiveExplainData.class);
        data.setWebName(playerEntity.getWebName()).setElementTypeName(Position.getNameFromElementType(data.getElementType())).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(data.getTeamId()), "")).setSelectedByPercent(playerStatEntity.getSelectedByPercent());
        return data;
    }

    @Cacheable(value = "api::qryTeamAgainstRecordInfo", key = "#teamId+'::'+#againstId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.teamId eq 0")
    @Override
    public TeamAgainstInfoData qryTeamAgainstRecordInfo(int teamId, int againstId) {
        if (teamId <= 0 || teamId > 20 || againstId <= 0 || againstId > 20) {
            return new TeamAgainstInfoData();
        }
        // team
        TeamEntity teamEntity = this.teamService.getById(teamId);
        TeamEntity againstEntity = this.teamService.getById(againstId);
        if (teamEntity == null || againstEntity == null) {
            return new TeamAgainstInfoData();
        }
        int teamCode = teamEntity.getCode();
        int againstCode = againstEntity.getCode();
        TeamAgainstInfoData data = new TeamAgainstInfoData().setTeamId(teamId).setTeamCode(teamCode).setTeamName(teamEntity.getName()).setTeamShortName(teamEntity.getShortName()).setAgainstId(againstId).setAgainstCode(againstCode).setAgainstName(againstEntity.getName()).setAgainstShortName(againstEntity.getShortName());
        List<TeamAgainstSeasonInfoData> recordDataList = Lists.newArrayList();
        List<TeamAgainstRecordData> seasonFixtureRecordsList = Lists.newArrayList();
        this.queryService.qryTeamAgainstFixture(teamCode, againstCode).forEach((season, list) -> {
            list.forEach(i -> {
                TeamAgainstRecordData teamAgainstRecordData = this.qrySeasonTeamAgainstRecord(season, teamCode, againstCode, i);
                if (teamAgainstRecordData == null) {
                    return;
                }
                seasonFixtureRecordsList.add(teamAgainstRecordData);
            });
            recordDataList.add(new TeamAgainstSeasonInfoData().setSeason(season).setSeasonDataList(seasonFixtureRecordsList.stream().filter(o -> StringUtils.equals(o.getSeason(), season)).sorted(Comparator.comparing(TeamAgainstRecordData::getEvent)).collect(Collectors.toList())));
        });
        data.setRecordDataList(recordDataList.stream().sorted(Comparator.comparing(TeamAgainstSeasonInfoData::getSeason).reversed()).collect(Collectors.toList())).setPlayed(seasonFixtureRecordsList.size()).setWin((int) seasonFixtureRecordsList.stream().filter(o -> teamCode == o.getTeamHCode() && o.getTeamHScore() > o.getTeamAScore()).count() + (int) seasonFixtureRecordsList.stream().filter(o -> teamCode == o.getTeamACode() && o.getTeamAScore() > o.getTeamHScore()).count()).setDraw((int) seasonFixtureRecordsList.stream().filter(o -> o.getTeamHScore() == o.getTeamAScore()).count()).setLose((int) seasonFixtureRecordsList.stream().filter(o -> teamCode == o.getTeamHCode() && o.getTeamHScore() < o.getTeamAScore()).count() + (int) seasonFixtureRecordsList.stream().filter(o -> teamCode == o.getTeamACode() && o.getTeamAScore() < o.getTeamHScore()).count()).setGoalScoreed(seasonFixtureRecordsList.stream().filter(o -> teamCode == o.getTeamHCode()).mapToInt(TeamAgainstRecordData::getTeamHScore).sum() + seasonFixtureRecordsList.stream().filter(o -> teamCode == o.getTeamACode()).mapToInt(TeamAgainstRecordData::getTeamAScore).sum()).setGoalsConceded(seasonFixtureRecordsList.stream().filter(o -> teamCode == o.getTeamHCode()).mapToInt(TeamAgainstRecordData::getTeamAScore).sum() + seasonFixtureRecordsList.stream().filter(o -> teamCode == o.getTeamACode()).mapToInt(TeamAgainstRecordData::getTeamHScore).sum());
        data.setAverageGoalScoreed(NumberUtil.div(data.getGoalScoreed(), data.getPlayed(), 2)).setAverageGoalsConceded(NumberUtil.div(data.getGoalsConceded(), data.getPlayed(), 2));
        return data;
    }

    private TeamAgainstRecordData qrySeasonTeamAgainstRecord(String season, int teamCode, int againstCode, EventFixtureEntity eventFixtureEntity) {
        // prepare
        MybatisPlusConfig.season.set(season);
        TeamEntity teamEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getCode, teamCode));
        TeamEntity againstEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getCode, againstCode));
        if (teamEntity == null || againstEntity == null) {
            MybatisPlusConfig.season.remove();
            return null;
        }
        int event = eventFixtureEntity.getEvent();
        int teamId = teamEntity.getId();
        int teamH = eventFixtureEntity.getTeamH();
        int teamA = eventFixtureEntity.getTeamA();
        // info
        TeamAgainstRecordData data = new TeamAgainstRecordData().setSeason(season).setEvent(event).setTeamHId(teamH).setTeamHCode(teamId == teamH ? teamEntity.getCode() : againstEntity.getCode()).setTeamHName(teamId == teamH ? teamEntity.getName() : againstEntity.getName()).setTeamHShortName(teamId == teamH ? teamEntity.getShortName() : againstEntity.getShortName()).setTeamHScore(eventFixtureEntity.getTeamHScore()).setTeamAId(teamA).setTeamACode(teamId == teamH ? againstEntity.getCode() : teamEntity.getCode()).setTeamAName(teamId == teamH ? againstEntity.getName() : teamEntity.getName()).setTeamAShortName(teamId == teamH ? againstEntity.getShortName() : teamEntity.getShortName()).setTeamAScore(eventFixtureEntity.getTeamAScore()).setKickoffDate(StringUtils.substringBefore(eventFixtureEntity.getKickoffTime(), " "));
        // event_live
        List<EventLiveEntity> eventLiveList = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().gt(EventLiveEntity::getMinutes, 0).eq(EventLiveEntity::getFixture, eventFixtureEntity.getId()));
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(eventLiveList)) {
            return data;
        }
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap(season);
        data.setGoalScored(eventLiveList.stream().filter(o -> o.getGoalsScored() > 0).sorted(Comparator.comparing(EventLiveEntity::getGoalsScored).reversed()).map(o -> new MapData<Integer>().setKey(webNameMap.getOrDefault(String.valueOf(o.getElement()), "")).setValue(o.getGoalsScored())).collect(Collectors.toList())).setAssists(eventLiveList.stream().filter(o -> o.getAssists() > 0).sorted(Comparator.comparing(EventLiveEntity::getAssists).reversed()).map(o -> new MapData<Integer>().setKey(webNameMap.getOrDefault(String.valueOf(o.getElement()), "")).setValue(o.getAssists())).collect(Collectors.toList())).setOwnGoals(eventLiveList.stream().filter(o -> o.getOwnGoals() > 0).sorted(Comparator.comparing(EventLiveEntity::getOwnGoals).reversed()).map(o -> new MapData<Integer>().setKey(webNameMap.getOrDefault(String.valueOf(o.getElement()), "")).setValue(o.getOwnGoals())).collect(Collectors.toList())).setPenaltiesSaved(eventLiveList.stream().filter(o -> o.getPenaltiesSaved() > 0).sorted(Comparator.comparing(EventLiveEntity::getPenaltiesSaved).reversed()).map(o -> new MapData<Integer>().setKey(webNameMap.getOrDefault(String.valueOf(o.getElement()), "")).setValue(o.getPenaltiesSaved())).collect(Collectors.toList())).setPenaltiesMissed(eventLiveList.stream().filter(o -> o.getPenaltiesMissed() > 0).sorted(Comparator.comparing(EventLiveEntity::getPenaltiesMissed).reversed()).map(o -> new MapData<Integer>().setKey(webNameMap.getOrDefault(String.valueOf(o.getElement()), "")).setValue(o.getPenaltiesMissed())).collect(Collectors.toList())).setRedCards(eventLiveList.stream().filter(o -> o.getRedCards() > 0).sorted(Comparator.comparing(EventLiveEntity::getRedCards).reversed()).map(o -> new MapData<Integer>().setKey(webNameMap.getOrDefault(String.valueOf(o.getElement()), "")).setValue(o.getRedCards())).collect(Collectors.toList()));
        return data;
    }

    @Cacheable(value = "api::qryTeamAgainstRecordResult", key = "#season+'::'+#event+'::'+#teamHId+'::'+#teamAId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<ElementSummaryData> qryTeamAgainstRecordResult(String season, int event, int teamHId, int teamAId) {
        if (StringUtils.isEmpty(season) || event < 1 || event > 38 || teamHId <= 0 || teamHId > 20 || teamAId <= 0 || teamAId > 20) {
            return Lists.newArrayList();
        }
        MybatisPlusConfig.season.set(season);
        // prepare
        TeamEntity teamHEntity = this.teamService.getById(teamHId);
        TeamEntity teamAEntity = this.teamService.getById(teamAId);
        if (teamHEntity == null || teamAEntity == null) {
            MybatisPlusConfig.season.remove();
            return Lists.newArrayList();
        }
        EventFixtureEntity eventFixtureEntity = this.eventFixtureService.getOne(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event).eq(EventFixtureEntity::getTeamH, teamHId).eq(EventFixtureEntity::getTeamA, teamAId));
        if (eventFixtureEntity == null) {
            MybatisPlusConfig.season.remove();
            return Lists.newArrayList();
        }
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap(season);
        List<ElementSummaryData> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getFixture, eventFixtureEntity.getId()).gt(EventLiveEntity::getMinutes, 0).orderByDesc(EventLiveEntity::getTotalPoints)).stream().map(o -> {
            boolean wasHome = o.getTeamId() == teamHId;
            TeamEntity elementTeamEntity = wasHome ? teamHEntity : teamAEntity;
            TeamEntity elementAgainstTeamEntity = wasHome ? teamAEntity : teamHEntity;
            ElementSummaryData data = this.qrySeasonFixtureElementResult(season, elementTeamEntity, elementAgainstTeamEntity, o, playerMap);
            if (data == null) {
                return null;
            }
            data.setWasHome(wasHome).setTeamScore(o.getTeamId() == teamHId ? eventFixtureEntity.getTeamHScore() : eventFixtureEntity.getTeamAScore()).setAgainstTeamScore(o.getTeamId() == teamHId ? eventFixtureEntity.getTeamAScore() : eventFixtureEntity.getTeamHScore());
            return data;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        MybatisPlusConfig.season.remove();
        return list;
    }

    private ElementSummaryData qrySeasonFixtureElementResult(String season, TeamEntity teamEntity, TeamEntity againstEntity, EventLiveEntity eventLiveEntity, Map<String, PlayerEntity> playerMap) {
        int element = eventLiveEntity.getElement();
        PlayerEntity playerEntity = playerMap.get(String.valueOf(element));
        if (playerEntity == null) {
            return null;
        }
        ElementSummaryData data = BeanUtil.copyProperties(eventLiveEntity, ElementSummaryData.class);
        data.setSeason(season).setWebName(playerEntity.getWebName()).setPrice(playerEntity.getPrice() / 10.0).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamCode(teamEntity.getCode()).setTeamName(teamEntity.getName()).setTeamShortName(teamEntity.getShortName()).setAgainstTeamCode(againstEntity.getCode()).setAgainstTeamName(againstEntity.getName()).setAgainstTeamShortName(againstEntity.getShortName());
        return data;
    }

    @Cacheable(value = "api::qryTopElementAgainstInfo", key = "#teamId+'::'+#againstId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<ElementAgainstInfoData> qryTopElementAgainstInfo(int teamId, int againstId, boolean active) {
        if (teamId <= 0 || teamId > 20 || againstId <= 0 || againstId > 20) {
            return Lists.newArrayList();
        }
        int returnNum = 15;
        // team
        TeamEntity teamEntity = this.teamService.getById(teamId);
        TeamEntity againstEntity = this.teamService.getById(againstId);
        if (teamEntity == null || againstEntity == null) {
            return Lists.newArrayList();
        }
        int teamCode = teamEntity.getCode();
        int againstCode = againstEntity.getCode();
        // match_info
        Map<String, TeamAgainstMatchInfoData> matchInfoMap = this.queryService.qryTeamAgainstMatchInfo(teamCode, againstCode);
        if (CollectionUtils.isEmpty(matchInfoMap)) {
            return Lists.newArrayList();
        }
        List<ElementSummaryData> elementSummaryList = Lists.newArrayList();
        matchInfoMap.values().forEach(o -> {
            if (CollectionUtils.isEmpty(o.getElementSummaryList())) {
                return;
            }
            elementSummaryList.addAll(o.getElementSummaryList());
        });
        // collect
        List<ElementAgainstInfoData> list = elementSummaryList.stream().collect(new ElementTeamAgainstCollector(matchInfoMap));
        // return
        if (!active) { // 现役与不现役都算
            return list.stream().filter(o -> o.getTotalPoints() > 0).limit(returnNum).collect(Collectors.toList());
        }
        // 只算现役
        Map<Integer, Integer> playerTeamIdMap = this.queryService.getPlayerMap().values().stream().collect(Collectors.toMap(PlayerEntity::getCode, PlayerEntity::getTeamId));
        Map<Integer, Integer> teamCodeMap = this.teamService.list().stream().collect(Collectors.toMap(TeamEntity::getId, TeamEntity::getCode));
        return list.stream().filter(o -> o.getTotalPoints() > 0).filter(o -> teamCodeMap.getOrDefault(playerTeamIdMap.getOrDefault(o.getCode(), 0), 0) == o.getTeamCode()).limit(returnNum).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryElementAgainstRecord", key = "#teamId+'::'+#againstId+'::'+#elementCode", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<ElementAgainstRecordData> qryElementAgainstRecord(int teamId, int againstId, int elementCode) {
        if (teamId <= 0 || teamId > 20 || againstId <= 0 || againstId > 20 || elementCode < 0) {
            return Lists.newArrayList();
        }
        // player
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getCode, elementCode));
        if (playerEntity == null) {
            return Lists.newArrayList();
        }
        // team
        TeamEntity teamEntity = this.teamService.getById(teamId);
        TeamEntity againstEntity = this.teamService.getById(againstId);
        if (teamEntity == null || againstEntity == null) {
            return Lists.newArrayList();
        }
        int teamCode = teamEntity.getCode();
        int againstCode = againstEntity.getCode();
        Map<String, TeamAgainstMatchInfoData> matchInfoMap = this.queryService.qryTeamAgainstMatchInfo(teamCode, againstCode);
        if (CollectionUtils.isEmpty(matchInfoMap)) {
            return Lists.newArrayList();
        }
        List<ElementAgainstRecordData> list = Lists.newArrayList();
        matchInfoMap.values().stream().sorted(Comparator.comparing(TeamAgainstMatchInfoData::getSeason).thenComparing(TeamAgainstMatchInfoData::getKickoffDate)).forEachOrdered(o -> o.getElementSummaryList().forEach(i -> {
            if (i.getCode() != elementCode) {
                return;
            }
            ElementAgainstRecordData data = BeanUtil.copyProperties(i, ElementAgainstRecordData.class);
            if (data == null) {
                return;
            }
            data.setPoints(i.getTotalPoints()).setTeamHId(o.getTeamHId()).setTeamHName(o.getTeamHName()).setTeamHShortName(o.getTeamHShortName()).setTeamHScore(o.getTeamHScore()).setTeamAId(o.getTeamAId()).setTeamAName(o.getTeamAName()).setTeamAShortName(o.getTeamAShortName()).setTeamAScore(o.getTeamAScore()).setKickoffDate(o.getKickoffDate());
            list.add(data);
        }));
        return list;
    }

    /**
     * @implNote tournament
     */
    @Cacheable(value = "api::qryEntryTournamentEntry", key = "#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<Integer> qryEntryTournamentEntry(int entry) {
        return this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda().eq(TournamentEntryEntity::getEntry, entry)).stream().map(TournamentEntryEntity::getTournamentId).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryEntryPointsRaceTournament", key = "#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TournamentInfoData> qryEntryPointsRaceTournament(int entry) {
        if (entry <= 0) {
            return Lists.newArrayList();
        }
        if (entry != Constant.ADMIN_ENTRY) {
            List<Integer> tournamentList = this.qryEntryTournamentEntry(entry);
            if (CollectionUtils.isEmpty(tournamentList)) {
                return Lists.newArrayList();
            }
            return this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda().in(TournamentInfoEntity::getId, tournamentList)).stream().filter(o -> StringUtils.equalsIgnoreCase(GroupMode.Points_race.name(), o.getGroupMode()) && StringUtils.equalsIgnoreCase(KnockoutMode.No_knockout.name(), o.getKnockoutMode())).map(o -> BeanUtil.copyProperties(o, TournamentInfoData.class)).collect(Collectors.toList());
        }
        return this.tournamentInfoService.list().stream().filter(o -> StringUtils.equalsIgnoreCase(GroupMode.Points_race.name(), o.getGroupMode()) && StringUtils.equalsIgnoreCase(KnockoutMode.No_knockout.name(), o.getKnockoutMode())).map(o -> BeanUtil.copyProperties(o, TournamentInfoData.class)).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryEntryKnockoutTournament", key = "#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TournamentInfoData> qryEntryKnockoutTournament(int entry) {
        if (entry <= 0) {
            return Lists.newArrayList();
        }
        if (entry != Constant.ADMIN_ENTRY) {
            List<Integer> tournamentList = this.qryEntryTournamentEntry(entry);
            if (CollectionUtils.isEmpty(tournamentList)) {
                return Lists.newArrayList();
            }
            return this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda().in(TournamentInfoEntity::getId, tournamentList)).stream().filter(o -> StringUtils.equalsIgnoreCase(GroupMode.No_group.name(), o.getGroupMode()) && !StringUtils.equalsIgnoreCase(KnockoutMode.No_knockout.name(), o.getKnockoutMode())).map(o -> BeanUtil.copyProperties(o, TournamentInfoData.class)).collect(Collectors.toList());
        }
        return this.tournamentInfoService.list().stream().filter(o -> StringUtils.equalsIgnoreCase(GroupMode.No_group.name(), o.getGroupMode()) && !StringUtils.equalsIgnoreCase(KnockoutMode.No_knockout.name(), o.getKnockoutMode())).map(o -> BeanUtil.copyProperties(o, TournamentInfoData.class)).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryEntryChampionLeagueStage", key = "#entry+'::'+#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result eq ''")
    @Override
    public String qryEntryChampionLeagueStage(int entry, int tournamentId) {
        if (entry <= 0 || tournamentId <= 0) {
            return "";
        }
        // tournament info
        TournamentInfoData tournamentInfoData = this.queryService.qryTournamentDataById(tournamentId);
        if (tournamentInfoData == null) {
            return "";
        }
        int event = this.queryService.getCurrentEvent();
        int knockoutStartGw = Integer.parseInt(tournamentInfoData.getKnockoutStartGw());
        int knockoutEndGw = Integer.parseInt(tournamentInfoData.getKnockoutEndGw());
        // group
        if (event >= knockoutStartGw && event <= knockoutEndGw) {
            int matchId = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).eq(TournamentKnockoutResultEntity::getEvent, event)).stream().map(TournamentKnockoutResultEntity::getMatchId).findFirst().orElse(0);
            if (matchId == 0) {
                return "";
            }
            return this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getMatchId, matchId)).stream().findFirst().map(o -> StringUtils.join("淘汰赛", "-", "第" + o.getRound(), "轮")).orElse("");
        }
        return this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId).eq(TournamentGroupEntity::getEntry, entry)).stream().map(o -> StringUtils.join("小组赛", "-", o.getGroupName(), "组")).distinct().findFirst().orElse("");
    }

    @Cacheable(value = "api::qryChampionLeagueStage", key = "#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public LinkedHashMap<String, List<String>> qryChampionLeagueStage(int tournamentId) {
        LinkedHashMap<String, List<String>> map = Maps.newLinkedHashMap();
        if (tournamentId <= 0) {
            return map;
        }
        // group
        List<String> groupNameList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId)).stream().sorted(Comparator.comparing(TournamentGroupEntity::getGroupId)).map(o -> StringUtils.join(o.getGroupName() + "组")).distinct().collect(Collectors.toList());
        map.put("小组赛", groupNameList);
        // qualifications
        map.put("晋级名单", Lists.newArrayList("晋级名单"));
        // knockout
        List<String> knockoutNameList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getTournamentId, tournamentId)).stream().sorted(Comparator.comparing(TournamentKnockoutEntity::getRound)).map(o -> StringUtils.join("第" + o.getRound() + "轮")).distinct().collect(Collectors.toList());
        map.put("淘汰赛", knockoutNameList);
        return map;
    }

    @Cacheable(value = "api::qryChampionLeagueStageGroup", key = "#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public LinkedHashMap<String, List<String>> qryChampionLeagueStageGroup(int tournamentId) {
        LinkedHashMap<String, List<String>> map = Maps.newLinkedHashMap();
        if (tournamentId <= 0) {
            return map;
        }
        // group
        List<String> groupNameList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId)).stream().sorted(Comparator.comparing(TournamentGroupEntity::getGroupId)).map(o -> StringUtils.join(o.getGroupName() + "组")).distinct().collect(Collectors.toList());
        map.put("小组赛", groupNameList);
        return map;
    }

    @Cacheable(value = "api::qryChampionLeagueGroupQualifications", key = "#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<List<TournamentGroupData>> qryChampionLeagueGroupQualifications(int tournamentId) {
        List<List<TournamentGroupData>> list = Lists.newArrayList();
        // 查配置
        TournamentInfoData tournamentInfoData = this.qryTournamentInfo(tournamentId);
        if (tournamentInfoData == null) {
            return list;
        }
        int groupNum = tournamentInfoData.getGroupNum();
        int qualifiers = tournamentInfoData.getGroupQualifiers();
        // 查分数
        List<TournamentGroupEntity> groupResultList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId));
        if (CollectionUtils.isEmpty(groupResultList)) {
            return list;
        }
        IntStream.rangeClosed(1, groupNum).forEach(groupId -> {
            List<TournamentGroupData> groupList = groupResultList.stream().filter(o -> o.getGroupId() == groupId).sorted(Comparator.comparing(TournamentGroupEntity::getGroupRank)).limit(qualifiers).map(o -> {
                int entry = o.getEntry();
                EntryInfoData entryInfoData = this.qryEntryInfo(entry);
                if (entryInfoData == null) {
                    return null;
                }
                return new TournamentGroupData().setTournamentId(tournamentId).setGroupId(groupId).setGroupName(o.getGroupName()).setGroupPoints(o.getGroupPoints()).setGroupRank(o.getGroupRank()).setEntry(entry).setEntryName(entryInfoData.getEntryName()).setPlayerName(entryInfoData.getPlayerName());
            }).filter(Objects::nonNull).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(groupList)) {
                list.add(groupList);
            }
        });
        return list;
    }

    // do not cache
    @Override
    public List<List<TournamentKnockoutData>> qryChampionLeagueStageKnockoutRound(int tournamentId, int round) {
        // tournament_knockout
        Map<Integer, Integer> roundWinnerMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getTournamentId, tournamentId).eq(TournamentKnockoutEntity::getRound, round)).stream().collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, TournamentKnockoutEntity::getRoundWinner));
        if (CollectionUtils.isEmpty(roundWinnerMap)) {
            return Lists.newArrayList();
        }
        // tournament_knockout_result
        List<TournamentKnockoutResultEntity> knockoutResultList = this.qryChampionLeagueStageKnockoutRoundResult(tournamentId, round);
        if (CollectionUtils.isEmpty(knockoutResultList)) {
            return Lists.newArrayList();
        }
        int event = this.queryService.getCurrentEvent();
        // event_live
        String stage = StringUtils.join("淘汰赛第", round, "轮");
        Map<Integer, LiveCalcData> entryEventLiveMap = this.liveService.calcLiveGwPointsByChampionLeague(event, tournamentId, stage).stream().collect(Collectors.toMap(LiveCalcData::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryEventLiveMap)) {
            return Lists.newArrayList();
        }
        // tournament_knockout_result
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<CompletableFuture<TournamentKnockoutData>> future = knockoutResultList.stream().map(o -> CompletableFuture.supplyAsync(() -> this.initTournamentKnockoutData(tournamentId, round, event, o, entryEventLiveMap, roundWinnerMap), forkJoinPool)).toList();
        List<TournamentKnockoutData> tournamentKnockoutDataList = future.stream().map(CompletableFuture::join).toList();
        Multimap<Integer, TournamentKnockoutData> multimap = HashMultimap.create();
        tournamentKnockoutDataList.forEach(o -> multimap.put(o.getMatchId(), o));
        if (multimap.isEmpty()) {
            return Lists.newArrayList();
        }
        // output
        List<List<TournamentKnockoutData>> list = Lists.newArrayList();
        multimap.keySet().stream().sorted(Comparator.comparing(Integer::intValue)).forEach(matchId -> {
            List<TournamentKnockoutData> subList = new ArrayList<>(multimap.get(matchId));
            list.add(subList.stream().sorted(Comparator.comparing(TournamentKnockoutData::getEvent)).collect(Collectors.toList()));
        });
        return list;
    }

    private TournamentKnockoutData initTournamentKnockoutData(int tournamentId, int round, int event, TournamentKnockoutResultEntity tournamentKnockoutResultEntity, Map<Integer, LiveCalcData> entryEventLiveMap, Map<Integer, Integer> roundWinnerMap) {
        LiveCalcData homeLiveCalcData = new LiveCalcData();
        if (tournamentKnockoutResultEntity.getHomeEntry() > 0) {
            homeLiveCalcData = entryEventLiveMap.getOrDefault(tournamentKnockoutResultEntity.getHomeEntry(), new LiveCalcData());
        }
        LiveCalcData awayLiveCalcData = new LiveCalcData();
        if (tournamentKnockoutResultEntity.getAwayEntry() > 0) {
            awayLiveCalcData = entryEventLiveMap.getOrDefault(tournamentKnockoutResultEntity.getAwayEntry(), new LiveCalcData());
        }
        TournamentKnockoutData data = new TournamentKnockoutData().setId(tournamentKnockoutResultEntity.getId()).setEvent(tournamentKnockoutResultEntity.getEvent()).setTournamentId(tournamentId).setRound(round).setMatchId(tournamentKnockoutResultEntity.getMatchId()).setHomeEntry(tournamentKnockoutResultEntity.getHomeEntry()).setHomeEntryNetPoints(tournamentKnockoutResultEntity.getEvent() == event ? homeLiveCalcData.getLiveNetPoints() : tournamentKnockoutResultEntity.getHomeEntryNetPoints()).setHomeEntryName(this.getKnockoutEntryName(tournamentKnockoutResultEntity.getHomeEntry(), homeLiveCalcData)).setHomePlayerName(this.getKnockoutPlayerName(tournamentKnockoutResultEntity.getHomeEntry(), homeLiveCalcData)).setHomeEntryGoalsScored(tournamentKnockoutResultEntity.getHomeEntryGoalsScored()).setHomeEntryGoalsConceded(tournamentKnockoutResultEntity.getHomeEntryGoalsConceded()).setAwayEntry(tournamentKnockoutResultEntity.getAwayEntry()).setAwayEntryNetPoints(tournamentKnockoutResultEntity.getEvent() == event ? awayLiveCalcData.getLiveNetPoints() : tournamentKnockoutResultEntity.getAwayEntryNetPoints()).setAwayEntryName(this.getKnockoutEntryName(tournamentKnockoutResultEntity.getAwayEntry(), awayLiveCalcData)).setAwayPlayerName(this.getKnockoutPlayerName(tournamentKnockoutResultEntity.getAwayEntry(), awayLiveCalcData)).setAwayEntryGoalsScored(tournamentKnockoutResultEntity.getAwayEntryGoalsScored()).setAwayEntryGoalsConceded(tournamentKnockoutResultEntity.getAwayEntryGoalsConceded()).setMatchWinner(tournamentKnockoutResultEntity.getMatchWinner());
        data.setRoundWinnerName(this.getKnockoutWinnerName(data, roundWinnerMap));
        return data;
    }

    private String getKnockoutWinnerName(TournamentKnockoutData data, Map<Integer, Integer> roundWinnerMap) {
        int winner = roundWinnerMap.getOrDefault(data.getMatchId(), 0);
        if (winner < 0) {
            return "BLANK";
        } else if (winner == 0) {
            return "待定";
        } else {
            if (winner == data.getHomeEntry()) {
                return data.getHomeEntryName();
            }
            return data.getAwayEntryName();
        }
    }

    private String getKnockoutEntryName(int entry, LiveCalcData data) {
        if (entry == 0) {
            return "待定";
        } else if (entry < 0) {
            return "BLANK";
        }
        return data.getEntryName();
    }

    private String getKnockoutPlayerName(int entry, LiveCalcData data) {
        if (entry == 0) {
            return "待定";
        } else if (entry < 0) {
            return "BLANK";
        }
        return data.getPlayerName();
    }

    @Override
    public List<TournamentKnockoutResultEntity> qryChampionLeagueStageKnockoutRoundResult(int tournamentId, int round) {
        List<Integer> matchIdList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getTournamentId, tournamentId).eq(TournamentKnockoutEntity::getRound, round)).stream().map(TournamentKnockoutEntity::getMatchId).collect(Collectors.toList());
        return this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).in(TournamentKnockoutResultEntity::getMatchId, matchIdList));
    }

    @Cacheable(value = "api::qryEntryChampionLeague", key = "#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TournamentInfoData> qryEntryChampionLeague(int entry) {
        if (entry <= 0) {
            return Lists.newArrayList();
        }
        if (entry != Constant.ADMIN_ENTRY) {
            List<Integer> tournamentList = this.qryEntryTournamentEntry(entry);
            if (CollectionUtils.isEmpty(tournamentList)) {
                return Lists.newArrayList();
            }
            return this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda().in(TournamentInfoEntity::getId, tournamentList)).stream().filter(o -> StringUtils.equalsIgnoreCase(GroupMode.Points_race.name(), o.getGroupMode()) && !StringUtils.equalsIgnoreCase(KnockoutMode.No_knockout.name(), o.getKnockoutMode())).map(o -> BeanUtil.copyProperties(o, TournamentInfoData.class)).collect(Collectors.toList());
        }
        return this.tournamentInfoService.list().stream().filter(o -> StringUtils.equalsIgnoreCase(GroupMode.Points_race.name(), o.getGroupMode()) && !StringUtils.equalsIgnoreCase(KnockoutMode.No_knockout.name(), o.getKnockoutMode())).map(o -> BeanUtil.copyProperties(o, TournamentInfoData.class)).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryTournamentInfoById", key = "#id", cacheManager = "apiCacheManager", unless = "#result eq null or #result.id eq 0")
    @Override
    public TournamentInfoData qryTournamentInfo(int id) {
        if (id <= 0) {
            return new TournamentInfoData();
        }
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getById(id);
        if (tournamentInfoEntity == null) {
            return new TournamentInfoData();
        }
        return BeanUtil.copyProperties(tournamentInfoEntity, TournamentInfoData.class);
    }

    @Cacheable(value = "api::qryTournamentEventResult", key = "#event+'::'+#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventResultData> qryTournamentEventResult(int event, int tournamentId) {
        if (event <= 0 || tournamentId <= 0) {
            return Lists.newArrayList();
        }
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return Lists.newArrayList();
        }
        List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda().eq(TournamentEntryEntity::getTournamentId, tournamentId)).stream().map(TournamentEntryEntity::getEntry).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().in(EntryInfoEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryInfoMap)) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, Integer> eventGroupRankMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda().eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId).eq(TournamentPointsGroupResultEntity::getEvent, event)).stream().collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, TournamentPointsGroupResultEntity::getEventGroupRank));
        Map<Integer, Integer> eventTournamentRankMap = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId)).stream().collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getGroupRank));
        // entry_event_result
        return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList)).stream().map(o -> this.initEntryEventResultData(event, o, entryInfoMap, eventGroupRankMap, eventTournamentRankMap, playerMap, eventLiveMap)).sorted(Comparator.comparing(EntryEventResultData::getPoints).reversed()).collect(Collectors.toList());
    }

    private EntryEventResultData initEntryEventResultData(int event, EntryEventResultEntity entryEventResultEntity, Map<Integer, EntryInfoEntity> entryInfoMap, Map<Integer, Integer> eventGroupRankMap, Map<Integer, Integer> eventTournamentRankMap, Map<String, PlayerEntity> playerMap, Map<Integer, EventLiveEntity> eventLiveMap) {
        int entry = entryEventResultEntity.getEntry();
        EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, new EntryInfoEntity());
        return new EntryEventResultData().setEvent(event).setEntry(entry).setEntryName(entryInfoEntity.getEntryName()).setPlayerName(entryInfoEntity.getPlayerName()).setTransfers(entryEventResultEntity.getEventTransfers()).setPoints(entryEventResultEntity.getEventPoints()).setTransfersCost(entryEventResultEntity.getEventTransfersCost()).setNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost()).setBenchPoints(entryEventResultEntity.getEventBenchPoints()).setRank(entryEventResultEntity.getEventRank()).setChip(StringUtils.isBlank(entryEventResultEntity.getEventChip()) ? Chip.NONE.getValue() : entryEventResultEntity.getEventChip()).setPlayedCaptain(entryEventResultEntity.getPlayedCaptain()).setCaptainName(playerMap.get(String.valueOf(entryEventResultEntity.getPlayedCaptain())).getWebName()).setCaptainPoints(eventLiveMap.get(entryEventResultEntity.getPlayedCaptain()).getTotalPoints()).setValue(entryEventResultEntity.getTeamValue() / 10.0).setBank(entryEventResultEntity.getBank() / 10.0).setTeamValue((entryEventResultEntity.getTeamValue() - entryEventResultEntity.getBank()) / 10.0).setOverallPoints(entryEventResultEntity.getOverallPoints()).setOverallRank(entryEventResultEntity.getOverallRank()).setEventTournamentRank(eventGroupRankMap.getOrDefault(entry, 0)).setTournamentRank(eventTournamentRankMap.getOrDefault(entry, 0)).setPickList(null);
    }

    @Cacheable(value = "api::qryTournamentEventSearchResult", key = "#event+'::'+#tournamentId+'::'+#element", cacheManager = "apiCacheManager", unless = "#result eq null or #result.eventResultList.size() eq 0")
    @Override
    public SearchEntryEventResultData qryTournamentEventSearchResult(int event, int tournamentId, int element) {
        if (event <= 0 || tournamentId <= 0 || element <= 0) {
            return new SearchEntryEventResultData();
        }
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return new SearchEntryEventResultData();
        }
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        if (CollectionUtils.isEmpty(entryList)) {
            return new SearchEntryEventResultData();
        }
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().in(EntryInfoEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryInfoMap)) {
            return new SearchEntryEventResultData();
        }
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, Integer> eventGroupRankMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda().eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId).eq(TournamentPointsGroupResultEntity::getEvent, event)).stream().collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, TournamentPointsGroupResultEntity::getEventGroupRank));
        Map<Integer, Integer> eventTournamentRankMap = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId)).stream().collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getGroupRank));
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList));
        // entry_event_result
        List<EntryEventResultData> list = entryEventResultEntityList.stream().filter(o -> this.entryContainElement(element, o.getEventPicks(), o.getEventChip())).map(o -> this.initEntryEventResultData(event, o, entryInfoMap, eventGroupRankMap, eventTournamentRankMap, playerMap, eventLiveMap)).sorted(Comparator.comparing(EntryEventResultData::getPoints).reversed()).collect(Collectors.toList());
        // return
        return new SearchEntryEventResultData().setElement(element).setWebName(playerMap.get(String.valueOf(element)).getWebName()).setSelectNum(list.size()).setSelectByPercent(list.isEmpty() ? "0%" : CommonUtils.getPercentResult(list.size(), entryEventResultEntityList.size())).setEventResultList(list);
    }

    private boolean entryContainElement(int element, String eventPicks, String eventChip) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(eventPicks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return false;
        }
        for (EntryPickData data : pickList) {
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

    @Cacheable(value = "api::qryChampionLeagueEventResult", key = "#event+'::'+#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventResultData> qryChampionLeagueEventResult(int event, int tournamentId) {
        if (event <= 0 || tournamentId <= 0) {
            return Lists.newArrayList();
        }
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return Lists.newArrayList();
        }
        List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda().eq(TournamentEntryEntity::getTournamentId, tournamentId)).stream().map(TournamentEntryEntity::getEntry).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().in(EntryInfoEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryInfoMap)) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, Integer> eventGroupRankMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda().eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId).eq(TournamentPointsGroupResultEntity::getEvent, event)).stream().collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, TournamentPointsGroupResultEntity::getEventGroupRank));
        Map<Integer, Integer> eventTournamentRankMap = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId)).stream().collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getGroupRank));
        // entry_event_result
        return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList)).stream().map(o -> this.initEntryEventResultData(event, o, entryInfoMap, eventGroupRankMap, eventTournamentRankMap, playerMap, eventLiveMap)).sorted(Comparator.comparing(EntryEventResultData::getPoints).reversed()).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryTournamentEventSummary", key = "#event+'::'+#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId) {
        if (event <= 0 || tournamentId <= 0) {
            return Lists.newArrayList();
        }
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return Lists.newArrayList();
        }
        return this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda().eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId).eq(TournamentPointsGroupResultEntity::getEvent, event).orderByAsc(TournamentPointsGroupResultEntity::getEntry)).stream().map(o -> {
            TournamentPointsGroupEventResultData data = new TournamentPointsGroupEventResultData().setTournamentId(tournamentId).setEvent(o.getEvent()).setEntry(o.getEntry()).setGroupRank(o.getEventGroupRank()).setPoints(o.getEventPoints()).setCost(o.getEventCost()).setNetPoints(o.getEventNetPoints()).setRank(o.getEventRank());
            EntryInfoData entryInfoData = this.qryEntryInfo(o.getEntry());
            if (entryInfoData != null) {
                data.setEntryName(entryInfoData.getEntryName()).setPlayerName(entryInfoData.getPlayerName());
            }
            return data;
        }).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryTournamentEntryEventSummary", key = "#tournamentId+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry) {
        if (tournamentId <= 0 || entry <= 0) {
            return Lists.newArrayList();
        }
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return Lists.newArrayList();
        }
        return this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda().eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId).eq(TournamentPointsGroupResultEntity::getEntry, entry).le(TournamentPointsGroupResultEntity::getEvent, this.queryService.getCurrentEvent()).orderByAsc(TournamentPointsGroupResultEntity::getEntry).orderByAsc(TournamentPointsGroupResultEntity::getEvent)).stream().map(o -> new TournamentPointsGroupEventResultData().setTournamentId(tournamentId).setEvent(o.getEvent()).setEntry(o.getEntry()).setGroupRank(o.getEventGroupRank()).setPoints(o.getEventPoints()).setCost(o.getEventCost()).setNetPoints(o.getEventNetPoints()).setRank(o.getEventRank())).collect(Collectors.toList());
    }

    @Cacheable(value = "api::qryTournamentEventChampion", key = "#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.tournamentId eq 0")
    @Override
    public TournamentGroupEventChampionData qryTournamentEventChampion(int tournamentId) {
        if (tournamentId <= 0) {
            return new TournamentGroupEventChampionData();
        }
        // prepare
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return new TournamentGroupEventChampionData();
        }
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().in(EntryInfoEntity::getEntry, this.queryService.qryEntryListByTournament(tournamentId))).stream().collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryInfoMap)) {
            return new TournamentGroupEventChampionData();
        }
        // tournament_points_group_result
        int current = this.queryService.getCurrentEvent();
        List<Integer> eventList = Lists.newArrayList();
        IntStream.rangeClosed(1, current).forEach(eventList::add);
        Multimap<Integer, TournamentPointsGroupResultEntity> eventResultMap = HashMultimap.create();
        this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda().eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId).in(TournamentPointsGroupResultEntity::getEvent, eventList)).forEach(o -> eventResultMap.put(o.getEvent(), o));
        if (eventResultMap.isEmpty()) {
            return new TournamentGroupEventChampionData();
        }
        // calc
        List<TournamentPointsGroupEventResultData> championList = Lists.newArrayList();
        List<TournamentPointsGroupEventResultData> runnerUpList = Lists.newArrayList();
        List<TournamentPointsGroupEventResultData> secondRunnerUpList = Lists.newArrayList();
        for (int event : eventResultMap.keySet()) {
            Collection<TournamentPointsGroupResultEntity> eventResultList = eventResultMap.get(event);
            List<Integer> eventPointsList = eventResultList.stream().map(TournamentPointsGroupResultEntity::getEventPoints).sorted(Comparator.reverseOrder()).distinct().toList();
            if (!eventPointsList.isEmpty()) {
                championList.addAll(eventResultList.stream().filter(o -> o.getEventPoints() == eventPointsList.get(0).intValue()).map(o -> this.initTournamentPointsGroupEventResultData(o, entryInfoMap)).toList());
            }
            if (eventPointsList.size() >= 2) {
                runnerUpList.addAll(eventResultList.stream().filter(o -> o.getEventPoints() == eventPointsList.get(1).intValue()).map(o -> this.initTournamentPointsGroupEventResultData(o, entryInfoMap)).toList());
            }
            if (eventPointsList.size() >= 3) {
                secondRunnerUpList.addAll(eventResultList.stream().filter(o -> o.getEventPoints() == eventPointsList.get(2).intValue()).map(o -> this.initTournamentPointsGroupEventResultData(o, entryInfoMap)).toList());
            }
        }
        // count
        List<TournamentGroupChampionCountData> countList = championList.stream().map(o -> this.initChampionCountData(o, entryInfoMap, championList, runnerUpList, secondRunnerUpList)).distinct().sorted(Comparator.comparing(TournamentGroupChampionCountData::getChampionNum).thenComparing(TournamentGroupChampionCountData::getTotalNum).thenComparing(TournamentGroupChampionCountData::getOverallRank).reversed()).collect(Collectors.toList());
        for (int i = 0; i < countList.size(); i++) {
            countList.get(i).setRank(i + 1);
        }
        return new TournamentGroupEventChampionData().setTournamentId(tournamentId).setTournamentName(tournamentInfoEntity.getName()).setEventChampionResultList(championList).setEventRunnerUpResultList(runnerUpList).setEventSecondRunnerUpResultList(secondRunnerUpList).setChampionCountList(countList);
    }

    private TournamentPointsGroupEventResultData initTournamentPointsGroupEventResultData(TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity, Map<Integer, EntryInfoEntity> entryInfoMap) {
        EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(tournamentPointsGroupResultEntity.getEntry(), null);
        return new TournamentPointsGroupEventResultData().setTournamentId(tournamentPointsGroupResultEntity.getTournamentId()).setEvent(tournamentPointsGroupResultEntity.getEvent()).setGroupId(tournamentPointsGroupResultEntity.getGroupId()).setEntry(tournamentPointsGroupResultEntity.getEntry()).setEntryName(entryInfoEntity == null ? "" : entryInfoEntity.getEntryName()).setPlayerName(entryInfoEntity == null ? "" : entryInfoEntity.getPlayerName()).setGroupRank(tournamentPointsGroupResultEntity.getEventGroupRank()).setPoints(tournamentPointsGroupResultEntity.getEventPoints()).setCost(tournamentPointsGroupResultEntity.getEventCost()).setNetPoints(tournamentPointsGroupResultEntity.getEventNetPoints()).setRank(tournamentPointsGroupResultEntity.getEventRank());
    }

    private TournamentGroupChampionCountData initChampionCountData(TournamentPointsGroupEventResultData tournamentPointsGroupEventResultData, Map<Integer, EntryInfoEntity> entryInfoMap, List<TournamentPointsGroupEventResultData> championList, List<TournamentPointsGroupEventResultData> runnerUpList, List<TournamentPointsGroupEventResultData> secondRunnerUpList) {
        int entry = tournamentPointsGroupEventResultData.getEntry();
        EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, null);
        TournamentGroupChampionCountData data = new TournamentGroupChampionCountData().setRank(0).setEntry(entry).setEntryName(entryInfoEntity == null ? "" : entryInfoEntity.getEntryName()).setPlayerName(entryInfoEntity == null ? "" : entryInfoEntity.getPlayerName()).setOverallPoints(entryInfoEntity == null ? 0 : entryInfoEntity.getOverallPoints()).setOverallRank(entryInfoEntity == null ? 0 : entryInfoEntity.getOverallRank()).setChampionNum((int) championList.stream().filter(o -> o.getEntry() == entry).count()).setRunnerUpNum((int) runnerUpList.stream().filter(o -> o.getEntry() == entry).count()).setSecondRunnerUpNum((int) secondRunnerUpList.stream().filter(o -> o.getEntry() == entry).count());
        data.setTotalNum(data.getChampionNum() + data.getRunnerUpNum() + data.getSecondRunnerUpNum());
        return data;
    }

    @Cacheable(value = "api::qryDrawKnockoutEntries", key = "#tournamentId", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<Integer> qryDrawKnockoutEntries(int tournamentId) {
        List<Integer> list = Lists.newArrayList();
        String candidatesKey = Constant.DRAW_KNOCKOUT_CANDIDATE + tournamentId;
        RedisUtils.getHashByKey(candidatesKey).forEach((groupName, groupEntryStr) -> {
            List<Integer> entryList = JsonUtils.json2Collection((String) groupEntryStr, List.class, Integer.class);
            if (CollectionUtils.isEmpty(entryList)) {
                return;
            }
            list.addAll(entryList);
        });
        return list;
    }

    @Override
    public List<EntryAgainstInfoData> qryDrawKnockoutResults(int tournamentId) {
        List<EntryAgainstInfoData> list = Lists.newArrayList();
        String redisKey = Constant.DRAW_KNOCKOUT_RESULT + tournamentId;
        RedisUtils.getHashByKey(redisKey).forEach((home, away) -> {
            int homeEntry = Integer.parseInt((String) home);
            int awayEntry = Integer.parseInt((String) away);
            EntryInfoData homeEntryInfoData = this.qryEntryInfo(homeEntry);
            EntryInfoData awayEntryInfoData = this.qryEntryInfo(awayEntry);
            list.add(new EntryAgainstInfoData().setHomeEntryInfoData(homeEntryInfoData).setAwayEntryInfoData(awayEntryInfoData));
        });
        return list;
    }

    @Override
    public List<EntryInfoData> qryDrawKnockoutOpponents(int tournamentId, int entry) {
        List<EntryInfoData> list = Lists.newArrayList();
        // get candidates
        String candidatesKey = Constant.DRAW_KNOCKOUT_CANDIDATE + tournamentId;
        RedisUtils.getHashByKey(candidatesKey).forEach((groupName, groupEntryStr) -> {
            // get draw pair
            String pairKey = Constant.DRAW_KNOCKOUT_PAIR + tournamentId + "::" + groupName;
            List<Integer> drawPairList = Lists.newArrayList();
            RedisUtils.getHashByKey(pairKey).keySet().forEach(o -> {
                int position = Integer.parseInt((String) o);
                drawPairList.add(position);
            });
            List<Integer> entryList = JsonUtils.json2Collection((String) groupEntryStr, List.class, Integer.class);
            if (CollectionUtils.isEmpty(entryList) || !entryList.contains(entry)) {
                return;
            }
            entryList.remove(Integer.valueOf(entry));
            int size = entryList.size();
            if (size == 0) {
                return;
            }
            IntStream.rangeClosed(1, size + 1).forEach(i -> {
                EntryInfoData data = new EntryInfoData().setEntry(i);
                data.setDrawable(!drawPairList.contains(i));
                data.setDrawGroupName((String) groupName);
                list.add(data);
            });
        });
        return list;
    }

    @Override
    public String qryDrawKnockoutNotice(int tournamentId) {
        String redisKey = Constant.DRAW_KNOCKOUT_NOTICE + tournamentId;
        return (String) RedisUtils.getValueByKey(redisKey).orElse("");
    }

    @Override
    public List<List<EntryInfoData>> qryDrawKnockoutPairs(int tournamentId) {
        List<List<EntryInfoData>> list = Lists.newArrayList();
        // get groups
        String candidatesKey = Constant.DRAW_KNOCKOUT_CANDIDATE + tournamentId;
        List<String> groupNameList = Lists.newArrayList();
        RedisUtils.getHashByKey(candidatesKey).keySet().forEach(o -> groupNameList.add((String) o));
        // get pairs
        groupNameList.forEach(groupName -> {
            List<EntryInfoData> pairList = Lists.newArrayList();
            String pairKey = Constant.DRAW_KNOCKOUT_PAIR + tournamentId + "::" + groupName;
            RedisUtils.getHashByKey(pairKey).forEach((position, entryStr) -> {
                int entry = Integer.parseInt((String) entryStr);
                EntryInfoData data = this.qryEntryInfo(entry);
                if (data == null) {
                    return;
                }
                data.setDrawPosition(Integer.parseInt((String) position)).setDrawGroupName(groupName);
                pairList.add(data);
            });
            list.add(pairList.stream().sorted(Comparator.comparing(EntryInfoData::getDrawPosition)).collect(Collectors.toList()));
        });
        return list;
    }

    /**
     * @implNote summary
     */
    @Cacheable(value = "api::qryEventOverallResult", key = "#event", cacheManager = "apiCacheManager", unless = "#result eq null or #result.averageEntryScore == 0")
    @Override
    public EventOverallResultData qryEventOverallResult(int event) {
        Map<Integer, String> webNameMap = this.queryService.getPlayerMap().values().stream().collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        EventOverallResultData data = this.redisCacheService.getEventOverallResultByEvent(CommonUtils.getCurrentSeason(), event);
        data.setMostSelectedWebName(webNameMap.getOrDefault(data.getMostSelected(), "")).setMostTransferredInWebName(webNameMap.getOrDefault(data.getMostTransferredIn(), "")).setMostCaptainedWebName(webNameMap.getOrDefault(data.getMostCaptained(), "")).setMostViceCaptainedWebName(webNameMap.getOrDefault(data.getMostViceCaptained(), ""));
        return data;
    }

    @Cacheable(value = "api::qryEventDreamTeam", key = "#event", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() == 0")
    @Override
    public List<ElementEventData> qryEventDreamTeam(int event) {
        if (event < 1 || event > 38) {
            return Lists.newArrayList();
        }
        Map<String, EventLiveSummaryEntity> eventLiveSummaryMap = this.queryService.getEventLiveSummaryMap();
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        return this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda().eq(PlayerStatEntity::getEvent, event).eq(PlayerStatEntity::getInDreamteam, true)).stream().map(o -> this.initEventDreamData(event, o, eventLiveSummaryMap, playerMap, teamShortNameMap)).filter(Objects::nonNull).sorted(Comparator.comparing(ElementEventData::getElementType)).collect(Collectors.toList());
    }

    private ElementEventData initEventDreamData(int event, PlayerStatEntity playerStatEntity, Map<String, EventLiveSummaryEntity> eventLiveSummaryMap, Map<String, PlayerEntity> playerMap, Map<String, String> teamShortNameMap) {
        int element = playerStatEntity.getElement();
        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
        if (playerEntity == null) {
            return null;
        }
        EventLiveSummaryEntity eventLiveSummaryEntity = eventLiveSummaryMap.get(String.valueOf(element));
        return new ElementEventData().setEvent(event).setElement(element).setCode(playerEntity.getCode()).setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId()).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setPoints(playerStatEntity.getEventPoints()).setTotalPoints(eventLiveSummaryEntity == null ? 0 : eventLiveSummaryEntity.getTotalPoints()).setSelectedByPercent(playerStatEntity.getSelectedByPercent());
    }

    @Cacheable(value = "api::qryEventEliteElements", key = "#event", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() == 0")
    @Override
    public List<ElementEventData> qryEventEliteElements(int event) {
        if (event < 1 || event > 38) {
            return Lists.newArrayList();
        }
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, PlayerStatEntity> playerStatMap = this.queryService.getPlayerStatMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        return this.queryService.getEventLiveByEvent(event).values().stream().filter(o -> o.getTotalPoints() >= 10).map(o -> this.initEventEliteData(event, o, playerMap, playerStatMap, teamShortNameMap)).filter(Objects::nonNull).sorted(Comparator.comparing(ElementEventData::getPoints).reversed().thenComparing(ElementEventData::getElement)).collect(Collectors.toList());
    }

    private ElementEventData initEventEliteData(int event, EventLiveEntity eventLiveEntity, Map<String, PlayerEntity> playerMap, Map<String, PlayerStatEntity> playerStatMap, Map<String, String> teamShortNameMap) {
        int element = eventLiveEntity.getElement();
        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
        if (playerEntity == null) {
            return null;
        }
        return new ElementEventData().setEvent(event).setElement(element).setCode(playerEntity.getCode()).setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId()).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setPoints(eventLiveEntity.getTotalPoints()).setSelectedByPercent(playerStatMap.get(String.valueOf(element)).getSelectedByPercent());
    }

    @Cacheable(value = "api::qryEventOverallTransfers", key = "#event", cacheManager = "apiCacheManager", unless = "#result eq null or #result.size() == 0")
    @Override
    public Map<String, List<ElementEventData>> qryEventOverallTransfers(int event) {
        if (event < 1 || event > 38) {
            return Maps.newHashMap();
        }
        Map<String, List<ElementEventData>> map = Maps.newHashMap();
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        // transfers_in
        List<ElementEventData> transfersInList = this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda().eq(PlayerStatEntity::getEvent, event).orderByDesc(PlayerStatEntity::getTransfersInEvent)).stream().limit(10).map(o -> this.initEventOverallTransfersData(event, o, playerMap, teamShortNameMap)).sorted(Comparator.comparing((ElementEventData elementEventData) -> elementEventData != null ? elementEventData.getTransfersInEvent() : 0).reversed()).collect(Collectors.toList());
        map.put("transfers_in", transfersInList);
        // transfers_out
        List<ElementEventData> transfersOutList = this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda().eq(PlayerStatEntity::getEvent, event).orderByDesc(PlayerStatEntity::getTransfersOutEvent)).stream().limit(10).map(o -> this.initEventOverallTransfersData(event, o, playerMap, teamShortNameMap)).sorted(Comparator.comparing((ElementEventData elementEventData) -> elementEventData != null ? elementEventData.getTransfersOutEvent() : 0).reversed()).collect(Collectors.toList());
        map.put("transfers_out", transfersOutList);
        return map;
    }

    private ElementEventData initEventOverallTransfersData(int event, PlayerStatEntity playerStatEntity, Map<String, PlayerEntity> playerMap, Map<String, String> teamShortNameMap) {
        int element = playerStatEntity.getElement();
        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
        if (playerEntity == null) {
            return null;
        }
        return new ElementEventData().setEvent(event).setElement(element).setCode(playerEntity.getCode()).setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId()).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setPoints(playerStatEntity.getEventPoints()).setSelectedByPercent(playerStatEntity.getSelectedByPercent()).setTransfersInEvent(playerStatEntity.getTransfersInEvent()).setTransfersOutEvent(playerStatEntity.getTransfersOutEvent());
    }

}
