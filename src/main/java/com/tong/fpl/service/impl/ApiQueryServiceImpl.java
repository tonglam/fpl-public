package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.constant.enums.ValueChangeType;
import com.tong.fpl.domain.data.response.UserPicksRes;
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
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.IStaticService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
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
    private final EntryEventResultService entryEventResultService;
    private final ScoutService scoutService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentInfoService tournamentInfoService;
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
        // from db
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity != null) {
            return new EntryEventResultData()
                    .setEntry(entry)
                    .setEvent(event)
                    .setTransfers(entryEventResultEntity.getEventTransfers())
                    .setPoints(entryEventResultEntity.getEventPoints())
                    .setTransfersCost(entryEventResultEntity.getEventTransfersCost())
                    .setNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost())
                    .setBenchPoints(entryEventResultEntity.getEventBenchPoints())
                    .setRank(entryEventResultEntity.getEventRank())
                    .setChip(StringUtils.isBlank(entryEventResultEntity.getEventChip()) ? Chip.NONE.getValue() : entryEventResultEntity.getEventChip())
                    .setValue(entryEventResultEntity.getTeamValue() / 10.0)
                    .setBank(entryEventResultEntity.getBank() / 10.0)
                    .setTeamValue((entryEventResultEntity.getTeamValue() - entryEventResultEntity.getBank()) / 10.0);
        }
        // from fpl server
        UserPicksRes userPick = this.queryService.getUserPicks(event, entry);
        if (userPick == null) {
            return new EntryEventResultData();
        }
        // entry_event_result
        return new EntryEventResultData()
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
                .setTeamValue((userPick.getEntryHistory().getValue() - userPick.getEntryHistory().getBank()) / 10.0);
    }

    /**
     * @implNote league
     */

    /**
     * @implNote liv (do not cache)
     */
    @Override
    public List<LiveMatchData> qryLiveMatchByStatus(String playStatus) {
        List<LiveMatchData> list = Lists.newArrayList();
        // prepare
        int event = this.queryService.getCurrentEvent();
        Collection<EventLiveEntity> eventLiveList = this.queryService.getEventLiveByEvent(event).values();
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
        // collect
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
     * @implNote report
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
            return Maps.newHashMap();
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
            value = "api::qryLeagueName",
            cacheManager = "apiCacheManager"
    )
    @Override
    public List<String> qryLeagueName() {
        return this.leagueEventReportService.getBaseMapper().qryLeagueNameList();
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
        LeagueStatData data = new LeagueStatData().setName(leagueName).setEvent(event);
        // player_info
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        // team_select
        List<LeagueEventReportEntity> leagueEventReportList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .eq(LeagueEventReportEntity::getEvent, event));
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
        return this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, event))
                .stream()
                .map(o -> this.initScoutData(event, o))
                .sorted(Comparator.comparing(EventScoutData::getEventPoints).reversed())
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
            value = "api::qryEntryTournament",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<TournamentInfoData> qryEntryTournament(int entry) {
        return this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                .in(TournamentInfoEntity::getId, this.qryEntryTournamentEntry(entry)))
                .stream()
                .map(o -> BeanUtil.copyProperties(o, TournamentInfoData.class))
                .collect(Collectors.toList());
    }

}
