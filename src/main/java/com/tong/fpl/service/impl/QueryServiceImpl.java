package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.BattleGroupResultCollector;
import com.tong.fpl.config.collector.KnockoutResultCollector;
import com.tong.fpl.config.collector.PlayerPickDataCollector;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.*;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.element.ElementSummaryData;
import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.league.LeagueEventInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.team.TeamAgainstMatchInfoData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import com.tong.fpl.utils.RedisUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/31
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueryServiceImpl implements IQueryService {

    private final IInterfaceService interfaceService;
    private final IRedisCacheService redisCacheService;

    private final TeamService teamService;
    private final EventFixtureService eventFixtureService;
    private final PlayerService playerService;
    private final EntryInfoService entryInfoService;
    private final EventLiveService eventLiveService;
    private final EventLiveSummaryService eventLiveSummaryService;
    private final EntryEventSimulatePickService entryEventSimulatePickService;
    private final EntryEventSimulateTransfersService entryEventSimulateTransfersService;
    private final EntryEventPickService entryEventPickService;
    private final EntryEventResultService entryEventResultService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;
    private final TournamentRoyaleService tournamentRoyaleService;
    private final LeagueEventReportService leagueEventReportService;
    private final ScoutService scoutService;

    /**
     * @implNote time
     */
    @Override
    public boolean isAfterMatchDay(int event) {
        return this.getAfterMatchDayByEvent(event).contains(LocalDate.now());
    }

    @Override
    public List<LocalDate> getAfterMatchDayByEvent(int event) {
        List<LocalDate> afterDateList = Lists.newArrayList();
        this.getMatchDayTimeByEvent(event).forEach(localDateTime -> {
            LocalDate localDate = localDateTime.toLocalDate();
            LocalDateTime baseTime = localDate.atTime(6, 0);
            if (!localDateTime.isBefore(baseTime)) {
                localDate = localDate.plusDays(1);
            }
            if (!afterDateList.contains(localDate)) {
                afterDateList.add(localDate);
            }
        });
        return afterDateList;
    }

    @Override
    public boolean isMatchDayTime(int event) {
        List<LocalDateTime> matchDayTimeList = this.getMatchDayTimeByEvent(event);
        LocalDateTime start = matchDayTimeList.stream().min(LocalDateTime::compareTo).orElse(null);
        LocalDateTime last = matchDayTimeList.stream().max(LocalDateTime::compareTo).orElse(null);
        if (start == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(start) && LocalDateTime.now().minusHours(2).isBefore(last);
    }

    // do not cache
    @Override
    public List<LocalDateTime> getMatchDayTimeByEvent(int event) {
        List<LocalDateTime> matchDayTimeList = Lists.newArrayList();
        this.getEventFixtureByEvent(event).forEach(eventFixtureEntity -> {
            String kickoffTime = eventFixtureEntity.getKickoffTime().replace(" ", "T");
            LocalDateTime dateTime = LocalDateTime.parse(kickoffTime);
            if (!matchDayTimeList.contains(dateTime)) {
                matchDayTimeList.add(dateTime);
            }
        });
        return matchDayTimeList.stream().sorted(LocalDateTime::compareTo).collect(Collectors.toList());
    }

    /**
     * @implNote player
     */
    @Override
    public Map<String, PlayerEntity> getPlayerMap(String season) {
        return this.redisCacheService.getPlayerMap(season);
    }

    @Override
    public PlayerEntity getPlayerByElement(String season, int element) {
        return this.redisCacheService.getPlayerByElement(season, element);
    }

    @Override
    public Map<String, PlayerStatEntity> getPlayerStatMap(String season) {
        return this.redisCacheService.getPlayerStatMap(season);
    }

    @Override
    public PlayerStatEntity getPlayerStatByElement(String season, int element) {
        return this.redisCacheService.getPlayerStatByElement(season, element);
    }

    @Cacheable(value = "qryPlayerElementByWebName", key = "#season+'::'+#webName", unless = "#result eq 0")
    @Override
    public int qryPlayerElementByWebName(String season, String webName) throws Exception {
        MybatisPlusConfig.season.set(season);
        List<PlayerEntity> playerList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getWebName, webName));
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(playerList)) {
            return 0;
        } else if (playerList.size() > 1) {
            throw new Exception("webName不止一个球员，请用element或code查询!");
        }
        return playerList.get(0) == null ? 0 : playerList.get(0).getElement();
    }

    @Cacheable(value = "qryPlayerWebNameMap", key = "#season", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, String> qryPlayerWebNameMap(String season) {
        return this.getPlayerMap(season).values().stream().collect(Collectors.toMap(k -> String.valueOf(k.getElement()), PlayerEntity::getWebName));
    }

    @Cacheable(value = "qryPlayerElementByCode", key = "#season+'::'+#code", unless = "#result eq 0")
    @Override
    public int qryPlayerElementByCode(String season, int code) {
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getCode, code));
        MybatisPlusConfig.season.remove();
        return playerEntity == null ? 0 : playerEntity.getElement();
    }

    @Cacheable(value = "qryPlayerData", key = "#element", unless = "#result eq null or #result.infoData.element eq 0")
    @Override
    public PlayerData qryPlayerData(int element) {
        PlayerEntity playerEntity = this.getPlayerByElement(element);
        if (playerEntity == null) {
            return null;
        }
        PlayerData playerData = new PlayerData();
        // info
        playerData.setInfoData(this.initPlayerInfo(CommonUtils.getCurrentSeason(), playerEntity));
        // fixture, next 5 gw
        playerData.setFixtureDataList(this.qryPlayerFixtureList(playerEntity.getTeamId(), 2, 3));
        // current season data
        playerData.setCurrentSeason(this.qryPlayerDetailData(CommonUtils.getCurrentSeason(), playerEntity.getElement()));
        // history season data（use code as unique index）
        playerData.setHistorySeasonList(this.qryHistorySeasonData(playerEntity.getCode()));
        return playerData;
    }

    @Cacheable(value = "initPlayerInfo", key = "#season+'::'+#playerEntity.element", unless = "#result eq null or #result.element eq 0")
    @Override
    public PlayerInfoData initPlayerInfo(String season, PlayerEntity playerEntity) {
        Map<String, String> teamNameMap = this.getTeamNameMap(season);
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(season);
        return new PlayerInfoData().setElement(playerEntity.getElement()).setCode(playerEntity.getCode()).setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId()).setTeamName(teamNameMap.get(String.valueOf(playerEntity.getTeamId()))).setTeamShortName(teamShortNameMap.get(String.valueOf(playerEntity.getTeamId()))).setPrice(NumberUtil.div(playerEntity.getPrice().intValue(), 10, 2));
    }

    @Cacheable(value = "qryPlayerFixtureList", key = "#season+'::'+#teamId+'::'+#previous+'::'+#next", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<PlayerFixtureData> qryPlayerFixtureList(String season, int teamId, int previous, int next) {
        MybatisPlusConfig.season.set(season);
        List<PlayerFixtureData> list = Lists.newArrayList();
        // prepare
        String currentSeason = CommonUtils.getCurrentSeason();
        int currentEvent = 38;
        if (StringUtils.equals(currentSeason, season)) {
            currentEvent = this.getCurrentEvent();
        }
        Map<String, String> teamNameMap = this.getTeamNameMap(season);
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(season);
        Map<String, List<PlayerFixtureData>> teamFixtureMap = this.getEventFixtureByTeamId(season, teamId);
        // calc event
        int startEvent = currentEvent - previous + 1;
        if (startEvent < 1) {
            startEvent = 1;
        }
        if (previous == -1) {
            startEvent = 1;
        }
        int endEvent = currentEvent + next;
        if (endEvent > 38) {
            endEvent = 38;
        }
        if (next == -1) {
            if (StringUtils.equals("1920", season)) {
                endEvent = 47;
            } else {
                endEvent = 38;
            }
        }
        // fixture
        IntStream.rangeClosed(startEvent, endEvent).forEach(event -> {
            if (teamFixtureMap.containsKey(String.valueOf(event))) {
                List<PlayerFixtureData> eventFixtureList = teamFixtureMap.get(String.valueOf(event));
                if (eventFixtureList.size() == 1) {
                    eventFixtureList.forEach(o -> {
                        o.setAgainstTeamName(teamNameMap.get(String.valueOf(o.getAgainstTeamId()))).setAgainstTeamShortName(teamShortNameMap.get(String.valueOf(o.getAgainstTeamId()))).setKickoffTime(StringUtils.substringBefore(o.getKickoffTime(), " ")).setBgw(false).setDgw(false);
                        list.add(o);
                    });
                } else {
                    eventFixtureList.forEach(o -> {
                        o.setAgainstTeamName(teamNameMap.get(String.valueOf(o.getAgainstTeamId()))).setAgainstTeamShortName(teamShortNameMap.get(String.valueOf(o.getAgainstTeamId()))).setKickoffTime(StringUtils.substringBefore(o.getKickoffTime(), " ")).setDifficulty(-1).setBgw(false).setDgw(true);
                        list.add(o);
                    });
                }
            } else {
                list.add(new PlayerFixtureData().setTeamId(teamId).setEvent(event).setAgainstTeamId(0).setAgainstTeamName("BLANK").setAgainstTeamShortName("BLANK").setDifficulty(0).setKickoffTime("").setStarted(false).setFinished(false).setWasHome(false).setScore("").setBgw(true).setDgw(false));
            }
        });
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Cacheable(value = "qryPlayerDetailData", key = "#season+'::'+#element", unless = "#result eq null or #result.element eq 0")
    @Override
    public PlayerDetailData qryPlayerDetailData(String season, int element) {
        PlayerDetailData playerDetailData = new PlayerDetailData().setElement(element).setSeason(season);
        PlayerStatEntity playerStatEntity = this.getPlayerStatByElement(season, element);
        if (playerStatEntity == null) {
            return playerDetailData;
        }
        BeanUtil.copyProperties(playerStatEntity, playerDetailData, CopyOptions.create().ignoreNullValue());
        // event_live
        MybatisPlusConfig.season.set(season);
        int event = playerStatEntity.getEvent();
        EventLiveEntity eventLiveEntity = this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event).eq(EventLiveEntity::getElement, playerStatEntity.getElement()));
        if (eventLiveEntity != null) {
            playerDetailData.setEvent(event).setEventPoints(eventLiveEntity.getTotalPoints());
        }
        MybatisPlusConfig.season.remove();
        return playerDetailData;
    }

    @Cacheable(value = "qryHistorySeasonData", key = "#code", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<PlayerDetailData> qryHistorySeasonData(int code) {
        List<PlayerDetailData> historySeasonList = Lists.newArrayList();
        Season.getHistorySeason().forEach(season -> {
            int element = this.qryPlayerElementByCode(season, code);
            historySeasonList.add(this.qryPlayerDetailData(season, element));
        });
        return historySeasonList;
    }

    @Cacheable(value = "qryAllPlayers", key = "#season", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<PlayerInfoData> qryAllPlayers(String season) {
        List<PlayerInfoData> list = Lists.newArrayList();
        this.playerService.list().forEach(o -> list.add(this.initPlayerInfo(season, o)));
        return list;
    }

    // do not cache here
    @Override
    public PlayerShowData qryPlayerShowData(int event, int element, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, Map<String, PlayerEntity> playerMap, Map<Integer, PlayerStatEntity> playerStatMap, Multimap<Integer, EventLiveEntity> eventLiveMap, Map<Integer, Map<String, List<PlayerFixtureData>>> teamFixtureMap) {
        PlayerShowData data = new PlayerShowData().setElement(element);
        // prepare
        if (CollectionUtils.isEmpty(teamNameMap)) {
            teamNameMap = this.getTeamNameMap();
        }
        if (CollectionUtils.isEmpty(teamShortNameMap)) {
            teamShortNameMap = this.getTeamShortNameMap();
        }
        if (eventLiveMap.isEmpty()) {
            this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getElement, element)).forEach(o -> eventLiveMap.put(o.getElement(), o));
        }
        // player
        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
        if (playerEntity == null) {
            playerEntity = this.getPlayerByElement(element);
            if (playerEntity == null) {
                return data;
            }
        }
        int teamId = playerEntity.getTeamId();
        data.setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setWebName(playerEntity.getWebName()).setTeamId(teamId).setTeamName(teamNameMap.getOrDefault(String.valueOf(teamId), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), "")).setPrice(playerEntity.getPrice() / 10.0);
        // event_live
        if (eventLiveMap.containsKey(element)) {
            data.setTotalPoints(eventLiveMap.get(element).stream().mapToInt(EventLiveEntity::getTotalPoints).sum());
        }
        // player_stat
        PlayerStatEntity playerStatEntity = playerStatMap.get(element);
        if (playerStatEntity != null) {
            data.setChancePlayingNextRound(playerStatEntity.getChanceOfPlayingNextRound()).setChancePlayingThisRound(playerStatEntity.getChanceOfPlayingThisRound()).setSelectedByPercent(playerStatEntity.getSelectedByPercent()).setNews(playerStatEntity.getNews()).setPointsPerGame(playerStatEntity.getPointsPerGame()).setForm(playerStatEntity.getForm()).setInDreamteam(playerStatEntity.getInDreamteam());
        }
        // fixture
        if (event > 38) {
            return data;
        }
        int endEvent = event + 5;
        if (endEvent > 38) {
            endEvent = 38;
        }
        Map<String, List<PlayerFixtureData>> teamEventFixtureMap = teamFixtureMap.get(teamId);
        if (CollectionUtils.isEmpty(teamEventFixtureMap)) {
            return data;
        }
        List<PlayerFixtureData> teamEventFixtureList = Lists.newArrayList();
        for (int i = event; i < endEvent + 1; i++) {
            if (teamEventFixtureMap.containsKey(String.valueOf(i))) {
                List<PlayerFixtureData> eventFixtureList = teamEventFixtureMap.get(String.valueOf(i));
                if (eventFixtureList.size() == 1) {
                    for (PlayerFixtureData playerFixtureData : eventFixtureList) {
                        playerFixtureData.setAgainstTeamName(teamNameMap.get(String.valueOf(playerFixtureData.getAgainstTeamId()))).setAgainstTeamShortName(teamShortNameMap.get(String.valueOf(playerFixtureData.getAgainstTeamId()))).setBgw(false).setDgw(false);
                        teamEventFixtureList.add(playerFixtureData);
                    }
                } else {
                    for (PlayerFixtureData playerFixtureData : eventFixtureList) {
                        playerFixtureData.setAgainstTeamName(teamNameMap.get(String.valueOf(playerFixtureData.getAgainstTeamId()))).setAgainstTeamShortName(teamShortNameMap.get(String.valueOf(playerFixtureData.getAgainstTeamId()))).setBgw(false).setDgw(true);
                        teamEventFixtureList.add(playerFixtureData);
                    }
                }
            } else {
                teamEventFixtureList.add(new PlayerFixtureData().setTeamId(teamId).setEvent(i).setAgainstTeamId(0).setAgainstTeamName("BLANK").setAgainstTeamShortName("BLANK").setDifficulty(0).setKickoffTime("").setStarted(false).setFinished(false).setWasHome(false).setScore("").setBgw(true).setDgw(false));
            }
        }
        // collect
        List<PlayerShowFixtureData> fixtureList = Lists.newArrayList();
        Map<Integer, PlayerShowFixtureData> fixtureDataMap = Maps.newHashMap();
        teamEventFixtureList.forEach(o -> {
            int checkEvent = o.getEvent();
            if (!fixtureDataMap.containsKey(checkEvent)) {
                PlayerShowFixtureData playerShowFixtureData = new PlayerShowFixtureData().setEvent(o.getEvent()).setAgainstTeamShortName(o.getAgainstTeamShortName()).setDifficulty(o.getDifficulty()).setWasHome(String.valueOf(o.isWasHome())).setBgw(o.isBgw()).setDgw(o.isDgw());
                fixtureDataMap.put(checkEvent, playerShowFixtureData);
            } else { // dgw
                fixtureDataMap.put(checkEvent, this.mergeFixtureData(fixtureDataMap.get(checkEvent), o));
            }
        });
        fixtureList.addAll(fixtureDataMap.values());
        fixtureList = fixtureList.stream().sorted(Comparator.comparing(PlayerShowFixtureData::getEvent)).collect(Collectors.toList());
        data.setFixtureList(fixtureList);
        return data;
    }

    private PlayerShowFixtureData mergeFixtureData(PlayerShowFixtureData oldData, PlayerFixtureData newData) {
        return new PlayerShowFixtureData().setEvent(oldData.getEvent()).setAgainstTeamShortName(oldData.getAgainstTeamShortName() + "||" + newData.getAgainstTeamShortName()).setDifficulty(-1).setWasHome(oldData.getWasHome() + "||" + newData.isWasHome()).setBgw(false).setDgw(true);
    }

    /**
     * @implNote entry
     */
    @Cacheable(value = "qryEntryInfo", key = "#season+'::'+#entry", unless = "#result eq null or #result.entry eq 0")
    @Override
    public EntryInfoData qryEntryInfo(String season, int entry) {
        if (entry <= 0) {
            return new EntryInfoData();
        }
        MybatisPlusConfig.season.set(season);
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        MybatisPlusConfig.season.remove();
        if (entryInfoEntity != null) {
            return BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class);
        }
        if (!StringUtils.equals(CommonUtils.getCurrentSeason(), season)) {
            return new EntryInfoData();
        }
        // return
        EntryRes entryRes = this.interfaceService.getEntry(entry).orElse(null);
        if (entryRes == null) {
            return new EntryInfoData();
        }
        entryInfoEntity = new EntryInfoEntity().setEntry(entryRes.getId()).setEntryName(entryRes.getName()).setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName()).setRegion(entryRes.getPlayerRegionName()).setStartedEvent(entryRes.getStartedEvent()).setOverallPoints(entryRes.getSummaryOverallPoints()).setOverallRank(entryRes.getSummaryOverallRank()).setBank(entryRes.getLastDeadlineBank()).setTeamValue(entryRes.getLastDeadlineValue()).setTotalTransfers(entryRes.getLastDeadlineTotalTransfers());
        return BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class);
    }

    @Cacheable(value = "qryEntryTournamentEntryList", key = "#entry", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<Integer> qryEntryTournamentEntryList(int entry) {
        return this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda().eq(TournamentEntryEntity::getEntry, entry)).stream().map(TournamentEntryEntity::getTournamentId).collect(Collectors.toList());
    }

    @Cacheable(value = "qryTournamentEntryEventPick", key = "#event+'::'+#entryList", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventPickEntity> qryEventPickByEntryList(int event, List<Integer> entryList) {
        return this.entryEventPickService.list(new QueryWrapper<EntryEventPickEntity>().lambda().eq(EntryEventPickEntity::getEvent, event).in(EntryEventPickEntity::getEntry, entryList));
    }

    /**
     * @implNote event
     */
    @Cacheable(value = "getCurrentEvent", unless = "#result eq 0")
    @Override
    public int getCurrentEvent() {
        int event = 0;
        for (int i = 1; i < 39; i++) {
            String deadline = this.getDeadlineByEvent(i);
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
                event = i;
            } else {
                break;
            }
        }
        return event;
    }

    @Override
    public int getLastEvent() {
        return this.getCurrentEvent() - 1;
    }

    @Override
    public int getNextEvent() {
        return this.getCurrentEvent() + 1;
    }

    @Cacheable(value = "getUtcDeadlineByEvent", key = "#season+'::'+#event", unless = "#result eq ''")
    @Override
    public String getUtcDeadlineByEvent(String season, int event) {
        return this.redisCacheService.getUtcDeadlineByEvent(season, event);
    }

    @Cacheable(value = "getDeadlineByEvent", key = "#season+'::'+#event", unless = "#result eq ''")
    @Override
    public String getDeadlineByEvent(String season, int event) {
        return this.redisCacheService.getDeadlineByEvent(season, event);
    }

    /**
     * @implNote team
     */
    @Cacheable(value = "getTeamNameMap", key = "#season", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, String> getTeamNameMap(String season) {
        return this.redisCacheService.getTeamNameMap(season);
    }

    @Cacheable(value = "getTeamShortNameMap", key = "#season", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, String> getTeamShortNameMap(String season) {
        return this.redisCacheService.getTeamShortNameMap(season);
    }

    @Cacheable(value = "qryTeamAgainstFixture", key = "#teamCode+'::'+#againstCode", unless = "#result eq null or #result.size() eq 0")
    @Override
    public LinkedHashMap<String, List<EventFixtureEntity>> qryTeamAgainstFixture(int teamCode, int againstCode) {
        LinkedHashMap<String, List<EventFixtureEntity>> map = Maps.newLinkedHashMap();
        Arrays.stream(Season.values()).forEach(o -> {
            String season = o.getSeasonValue();
            List<EventFixtureEntity> list = this.qryTeamAgainstSeasonFixture(season, teamCode, againstCode);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            map.put(season, list);
        });
        return map;
    }

    private List<EventFixtureEntity> qryTeamAgainstSeasonFixture(String season, int teamCode, int againstCode) {
        MybatisPlusConfig.season.set(season);
        TeamEntity teamEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getCode, teamCode));
        TeamEntity againstEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getCode, againstCode));
        if (teamEntity == null || againstEntity == null) {
            MybatisPlusConfig.season.remove();
            return null;
        }
        int teamId = teamEntity.getId();
        int againstId = againstEntity.getId();
        List<EventFixtureEntity> list = this.eventFixtureService.list(new QueryWrapper<EventFixtureEntity>().lambda().gt(EventFixtureEntity::getMinutes, 0).and(o -> o.and(i -> i.eq(EventFixtureEntity::getTeamH, teamId).eq(EventFixtureEntity::getTeamA, againstId)).or(i -> i.eq(EventFixtureEntity::getTeamA, teamId).eq(EventFixtureEntity::getTeamH, againstId))));
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Cacheable(value = "qryTeamAgainstMatchInfo", key = "#teamCode+'::'+#againstCode", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, TeamAgainstMatchInfoData> qryTeamAgainstMatchInfo(int teamCode, int againstCode) {
        // fixture
        Map<String, List<EventFixtureEntity>> seasonFixtureMap = this.qryTeamAgainstFixture(teamCode, againstCode);
        if (CollectionUtils.isEmpty(seasonFixtureMap)) {
            return Maps.newHashMap();
        }
        Map<String, TeamAgainstMatchInfoData> map = Maps.newHashMap();
        seasonFixtureMap.forEach((season, list) -> {
            MybatisPlusConfig.season.set(season);
            TeamEntity teamEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getCode, teamCode));
            TeamEntity againstEntity = this.teamService.getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getCode, againstCode));
            if (teamEntity == null || againstEntity == null) {
                return;
            }
            int teamId = teamEntity.getId();
            Map<String, PlayerEntity> playerMap = this.getPlayerMap(season);
            list.forEach(o -> {
                int event = o.getEvent();
                int teamH = o.getTeamH();
                int teamA = o.getTeamA();
                TeamAgainstMatchInfoData data = new TeamAgainstMatchInfoData().setSeason(season).setEvent(event).setTeamHId(teamH).setTeamHCode(teamId == teamH ? teamEntity.getCode() : againstEntity.getCode()).setTeamHName(teamId == teamH ? teamEntity.getName() : againstEntity.getName()).setTeamHShortName(teamId == teamH ? teamEntity.getShortName() : againstEntity.getShortName()).setTeamHScore(o.getTeamHScore()).setTeamAId(teamA).setTeamACode(teamId == teamH ? againstEntity.getCode() : teamEntity.getCode()).setTeamAName(teamId == teamH ? againstEntity.getName() : teamEntity.getName()).setTeamAShortName(teamId == teamH ? againstEntity.getShortName() : teamEntity.getShortName()).setTeamAScore(o.getTeamAScore()).setKickoffDate(StringUtils.substringBefore(o.getKickoffTime(), " "));
                // player_summary
                List<ElementSummaryData> elementSummaryList = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getFixture, o.getId())).stream().map(i -> {
                    int element = i.getElement();
                    PlayerEntity playerEntity = playerMap.get(String.valueOf(element));
                    if (playerEntity == null) {
                        return null;
                    }
                    ElementSummaryData elementSummaryData = BeanUtil.copyProperties(i, ElementSummaryData.class);
                    elementSummaryData.setSeason(season).setWebName(playerEntity.getWebName()).setPrice(playerEntity.getPrice() / 10.0).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamCode(playerEntity.getTeamId().equals(teamEntity.getId()) ? teamEntity.getCode() : againstEntity.getCode()).setTeamName(playerEntity.getTeamId().equals(teamEntity.getId()) ? teamEntity.getName() : againstEntity.getName()).setTeamShortName(playerEntity.getTeamId().equals(teamEntity.getId()) ? teamEntity.getShortName() : againstEntity.getShortName()).setAgainstTeamCode(playerEntity.getTeamId().equals(teamEntity.getId()) ? againstEntity.getCode() : teamEntity.getCode()).setAgainstTeamName(playerEntity.getTeamId().equals(teamEntity.getId()) ? againstEntity.getName() : teamEntity.getName()).setAgainstTeamShortName(playerEntity.getTeamId().equals(teamEntity.getId()) ? againstEntity.getShortName() : teamEntity.getShortName());
                    return elementSummaryData;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                MybatisPlusConfig.season.remove();
                if (!CollectionUtils.isEmpty(elementSummaryList)) {
                    data.setElementSummaryList(elementSummaryList);
                }
                String key = StringUtils.joinWith("-", season, event);
                map.put(key, data);
            });
        });
        return map;
    }

    /**
     * @implNote fixture
     */
    @Cacheable(value = "getEventFixtureByEvent", key = "#season+'::'+#event", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EventFixtureEntity> getEventFixtureByEvent(String season, int event) {
        return this.redisCacheService.getEventFixtureByEvent(season, event);
    }

    @Cacheable(value = "getEventFixtureByTeamId", key = "#season+'::'+#teamId", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId) {
        return this.redisCacheService.getEventFixtureByTeamId(season, teamId);
    }

    @Cacheable(value = "getTeamEventFixtureMap", key = "#season", unless = "#result.size() eq 0")
    @Override
    public Map<Integer, Map<String, List<PlayerFixtureData>>> getTeamEventFixtureMap(String season) {
        return this.redisCacheService.getTeamEventFixtureMap(season);
    }

    @Cacheable(value = "qryGroupFixtureListById", key = "#tournamentId", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId) {
        List<TournamentGroupFixtureData> list = Lists.newArrayList();
        TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return list;
        }
        if (!StringUtils.equals(tournamentInfoEntity.getGroupMode(), GroupMode.Battle_race.name())) {
            return list;
        }
        int currentGw = this.getCurrentEvent();
        List<TournamentBattleGroupResultEntity> battleGroupResultEntityList = this.tournamentBattleGroupResultService.list(new QueryWrapper<TournamentBattleGroupResultEntity>().lambda().eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId).orderByAsc(TournamentBattleGroupResultEntity::getEvent).orderByAsc(TournamentBattleGroupResultEntity::getGroupId));
        // entryInfo map
        List<Integer> entryList = battleGroupResultEntityList.stream().map(TournamentBattleGroupResultEntity::getHomeEntry).collect(Collectors.toList());
        entryList.addAll(battleGroupResultEntityList.stream().map(TournamentBattleGroupResultEntity::getAwayEntry).toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return list;
        }
        Map<Integer, EntryInfoData> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().in(EntryInfoEntity::getEntry, entryList)).stream().map(o -> BeanUtil.copyProperties(o, EntryInfoData.class)).collect(Collectors.toMap(EntryInfoData::getEntry, o -> o));
        // return list
        list = battleGroupResultEntityList.stream().collect(new BattleGroupResultCollector());
        list.forEach(o -> {
            o.setTournamentId(tournamentId);
            o.getGroupEventFixtureList().forEach(i -> i.getEventEntryFixtureList().forEach(tournamentGroupFixtureData -> tournamentGroupFixtureData.setShowMessage(this.setBattleFixtureMsg(currentGw, o.getEvent(), entryInfoMap, tournamentGroupFixtureData.getHomeEntry(), tournamentGroupFixtureData.getAwayEntry(), tournamentGroupFixtureData.getHomeEntryNetPoints(), tournamentGroupFixtureData.getAwayEntryNetPoints()))));
        });
        return list;
    }

    @Cacheable(value = "qryKnockoutFixtureListById", key = "#tournamentId", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId) {
        List<TournamentKnockoutFixtureData> list = Lists.newArrayList();
        int currentGw = this.getCurrentEvent();
        List<TournamentKnockoutResultEntity> knockoutResultEntityList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).orderByAsc(TournamentKnockoutResultEntity::getEvent).orderByAsc(TournamentKnockoutResultEntity::getMatchId));
        if (CollectionUtils.isEmpty(knockoutResultEntityList)) {
            return list;
        }
        // entryInfo map
        List<Integer> entryList = knockoutResultEntityList.stream().map(TournamentKnockoutResultEntity::getHomeEntry).collect(Collectors.toList());
        entryList.addAll(knockoutResultEntityList.stream().map(TournamentKnockoutResultEntity::getAwayEntry).toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return list;
        }
        Map<Integer, EntryInfoData> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().in(EntryInfoEntity::getEntry, entryList)).stream().map(o -> BeanUtil.copyProperties(o, EntryInfoData.class)).collect(Collectors.toMap(EntryInfoData::getEntry, o -> o));
        // return list
        List<TournamentKnockoutFixtureData> knockoutFixtureList = knockoutResultEntityList.stream().collect(new KnockoutResultCollector());
        knockoutFixtureList.stream().filter(o -> o.getEvent() <= currentGw).forEach(o -> {
            o.setTournamentId(tournamentId);
            o.getKnockoutEventFixtureList().forEach(i -> i.setShowMessage(this.setBattleFixtureMsg(currentGw, o.getEvent(), entryInfoMap, i.getHomeEntry(), i.getAwayEntry(), i.getHomeEntryPoints(), i.getAwayEntryPoints())));
            list.add(o);
        });
        return list;
    }

    private String setBattleFixtureMsg(int currentGw, int event, Map<Integer, EntryInfoData> entryInfoMap, int homeEntry, int awayEntry, int homeEntryNetPoints, int awayEntryNetPoints) {
        // home
        String homeMsg = this.setBattleEntryMsg(entryInfoMap, homeEntry);
        // away
        String awayMsg = this.setBattleEntryMsg(entryInfoMap, awayEntry);
        // points
        String pointsMsg = this.setBattlePointsMsg(currentGw, event, homeEntryNetPoints, awayEntryNetPoints);
        return homeMsg + " " + pointsMsg + " " + awayMsg;
    }

    private String setBattleEntryMsg(Map<Integer, EntryInfoData> entryInfoMap, int entry) {
        String entryName = "";
        String playerName = "";
        if (entry == 0) {
            entryName = "平均分";
            playerName = "平均分";
        } else if (entry < 0) {
            entryName = "轮空";
            playerName = "轮空";
        } else {
            EntryInfoData orDefault = entryInfoMap.getOrDefault(entry, null);
            if (orDefault != null) {
                entryName = orDefault.getEntryName();
                playerName = orDefault.getPlayerName();
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(" ");
        if (StringUtils.isNotEmpty(entryName)) {
            builder.append(entryName);
        } else {
            builder.append("TBD");
        }
        if (StringUtils.isNotEmpty(playerName)) {
            builder.append(" (").append(playerName).append(") ");
        }
        return builder.toString();
    }

    private String setBattlePointsMsg(int currentGw, int event, int homeEntryNetPoints, int awayEntryNetPoints) {
        if (event > currentGw) {
            return " vs ";
        } else {
            return homeEntryNetPoints + " - " + awayEntryNetPoints;
        }
    }

    /**
     * @implNote event_live
     */

    @Override
    public Map<String, EventLiveExplainEntity> getEventLiveExplainByEvent(int event) {
        return this.redisCacheService.getEventLiveExplainByEvent(event);
    }

    @Cacheable(value = "getEventLiveSummaryMap", key = "#season", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, EventLiveSummaryEntity> getEventLiveSummaryMap(String season) {
        return this.redisCacheService.getEventLiveSummaryMap(season);
    }

    @Cacheable(value = "qryEventLive", key = "#season+'::'+#event+'::'+#element", unless = "#result eq null or #result.id eq 0")
    @Override
    public EventLiveEntity qryEventLiveByElement(String season, int event, int element) {
        MybatisPlusConfig.season.set(season);
        EventLiveEntity eventLiveEntity = this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event).eq(EventLiveEntity::getElement, element));
        MybatisPlusConfig.season.remove();
        return eventLiveEntity;
    }

    @Cacheable(value = "qryEventLiveSummary", key = "#season+'::'+#element", unless = "#result eq null or #result.element eq 0")
    @Override
    public EventLiveSummaryEntity qryEventLiveSummaryByElement(String season, int element) {
        MybatisPlusConfig.season.set(season);
        EventLiveSummaryEntity eventLiveSummaryEntity = this.eventLiveSummaryService.getById(element);
        MybatisPlusConfig.season.remove();
        return eventLiveSummaryEntity;
    }

    /**
     * @implNote entry_event_result
     */
    @Cacheable(value = "qryEntryResult", key = "#season+'::'+#entry", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventResultData> qryEntryResult(String season, int entry) {
        List<EntryEventResultData> list = Lists.newArrayList();
        if (StringUtils.equals(season, "1920")) {
            IntStream.rangeClosed(1, 47).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
        } else {
            IntStream.rangeClosed(1, 38).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
        }
        return list;
    }

    @Cacheable(value = "qryEntryEventResult", key = "#season+'::'+#event+'::'+#entry", unless = "#result eq null or #result.entry eq 0")
    @Override
    public EntryEventResultData qryEntryEventResult(String season, int event, int entry) {
        return this.setEntryEventResult(season, event, entry);
    }

    private EntryEventResultData setEntryEventResult(String season, int event, int entry) {
        EntryEventResultData entryEventResultData = new EntryEventResultData();
        MybatisPlusConfig.season.set(season);
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
        MybatisPlusConfig.season.remove();
        if (entryEventResultEntity == null) {
            return entryEventResultData;
        }
        entryEventResultData.setEntry(entry).setEvent(event).setPoints(entryEventResultEntity.getEventPoints()).setTransfers(entryEventResultEntity.getEventTransfers()).setTransfersCost(entryEventResultEntity.getEventTransfersCost()).setNetPoints(entryEventResultEntity.getEventNetPoints()).setBenchPoints(entryEventResultEntity.getEventBenchPoints()).setRank(entryEventResultEntity.getEventRank()).setChip(entryEventResultEntity.getEventChip()).setTeamValue(entryEventResultEntity.getTeamValue()).setBank(entryEventResultEntity.getBank()).setPicks(this.qryPickListFromPicks(entryEventResultEntity.getEventPicks())).setPlayedCaptain(entryEventResultEntity.getPlayedCaptain()).setCaptainPoints(entryEventResultEntity.getCaptainPoints());
        return entryEventResultData;
    }

    @Cacheable(value = "qryPickListFromPicks", key = "#season+'::'+#picks", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryPickData> qryPickListFromPicks(String season, @NotNull String picks) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return Lists.newArrayList();
        }
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
        Map<String, PlayerEntity> playerMap = this.getPlayerMap();
        pickList.forEach(pick -> {
            PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(pick.getElement()), null);
            if (playerEntity != null) {
                pick.setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setWebName(playerEntity.getWebName()).setTeamId(playerEntity.getTeamId()).setTeamName(teamNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), "")).setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""));
            }
        });
        return pickList;
    }

    @Cacheable(value = "qryPickListByPosition", key = "#season+'::'+#picks", unless = "#result eq null or #result.entry eq 0")
    @Override
    public PlayerPickData qryPickListByPosition(String season, String picks) {
        if (StringUtils.isEmpty(picks)) {
            return new PlayerPickData();
        }
        List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return new PlayerPickData();
        }
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(season);
        // collect
        List<EntryPickData> gkpList = Lists.newArrayList();
        List<EntryPickData> defList = Lists.newArrayList();
        List<EntryPickData> midList = Lists.newArrayList();
        List<EntryPickData> fwdList = Lists.newArrayList();
        List<EntryPickData> subList = Lists.newArrayList();
        pickList.forEach(pick -> {
            PlayerEntity playerEntity = this.getPlayerByElement(season, pick.getElement());
            if (playerEntity != null) {
                pick.setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId());
            }
            pick.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(pick.getTeamId()), ""));
            // add into list
            if (pick.getPosition() <= 11) {
                switch (pick.getElementType()) {
                    case 1: {
                        gkpList.add(pick);
                        break;
                    }
                    case 2: {
                        defList.add(pick);
                        break;
                    }
                    case 3: {
                        midList.add(pick);
                        break;
                    }
                    case 4: {
                        fwdList.add(pick);
                        break;
                    }
                }
            } else {
                subList.add(pick);
            }
        });
        return new PlayerPickData().setGkps(gkpList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setDefs(defList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setMids(midList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setFwds(fwdList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setSubs(subList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setFormation(StringUtils.joinWith("-", defList.size(), midList.size(), fwdList.size()));
    }

    @Cacheable(value = "qryPickListByPositionForTransfers", key = "#season+'::'+#picks", unless = "#result eq null or #result.entry eq 0")
    @Override
    public PlayerPickData qryPickListByPositionForTransfers(String season, String picks) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return new PlayerPickData();
        }
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(season);
        // collect
        List<EntryPickData> gkpList = Lists.newArrayList();
        List<EntryPickData> defList = Lists.newArrayList();
        List<EntryPickData> midList = Lists.newArrayList();
        List<EntryPickData> fwdList = Lists.newArrayList();
        List<EntryPickData> subList = Lists.newArrayList();
        pickList.forEach(pick -> {
            PlayerEntity playerEntity = this.getPlayerByElement(season, pick.getElement());
            if (playerEntity != null) {
                pick.setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId());
            }
            pick.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(pick.getTeamId()), ""));
            // add into list
            switch (pick.getElementType()) {
                case 1: {
                    gkpList.add(pick);
                    break;
                }
                case 2: {
                    defList.add(pick);
                    break;
                }
                case 3: {
                    midList.add(pick);
                    break;
                }
                case 4: {
                    fwdList.add(pick);
                    break;
                }
            }
        });
        return new PlayerPickData().setGkps(gkpList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setDefs(defList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setMids(midList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setFwds(fwdList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setSubs(subList);
    }

    @Cacheable(value = "qryEntryPickData", key = "#event+'::'+#entry", unless = "#result eq null or #result.entry eq 0")
    @Override
    public PlayerPickData qryEntryPickData(int event, int entry) {
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).eq(EntryEventResultEntity::getEvent, event));
        if (entryEventResultEntity == null) {
            return new PlayerPickData();
        }
        PlayerPickData playerPickData = this.qryPickListByPosition(entryEventResultEntity.getEventPicks());
        playerPickData.setEntry(entry).setEvent(event).setTeamValue(entryEventResultEntity.getTeamValue()).setBank(entryEventResultEntity.getBank());
        EntryInfoData entryInfoData = this.qryEntryInfo(entry);
        if (entryInfoData != null) {
            playerPickData.setEntryName(entryInfoData.getEntryName()).setPlayerName(entryInfoData.getPlayerName());
        }
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        playerPickData.getGkps().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        playerPickData.getDefs().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        playerPickData.getMids().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        playerPickData.getFwds().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        playerPickData.getSubs().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        return playerPickData;
    }

    @Cacheable(value = "qryEntryPickDataForTransfers", key = "#event+'::'+#entry", unless = "#result eq null or #result.entry eq 0")
    @Override
    public PlayerPickData qryEntryPickDataForTransfers(int event, int entry) {
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).eq(EntryEventResultEntity::getEvent, event));
        if (entryEventResultEntity == null) {
            return new PlayerPickData();
        }
        if (StringUtils.equals(entryEventResultEntity.getEventChip(), Chip.FH.getValue())) {
            entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).eq(EntryEventResultEntity::getEvent, event - 1));
        }
        PlayerPickData playerPickData = this.qryPickListByPositionForTransfers(entryEventResultEntity.getEventPicks());
        playerPickData.setEntry(entry).setEvent(event).setTeamValue(entryEventResultEntity.getTeamValue()).setBank(entryEventResultEntity.getBank());
        Map<Integer, Integer> freeTransfersMap = this.qryEntryFreeTransfersMap(entry);
        playerPickData.setFreeTransfers(freeTransfersMap.getOrDefault(event + 1, 1));
        EntryInfoData entryInfoData = this.qryEntryInfo(entry);
        if (entryInfoData != null) {
            playerPickData.setEntryName(entryInfoData.getEntryName()).setPlayerName(entryInfoData.getPlayerName());
        }
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        playerPickData.getGkps().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        playerPickData.getDefs().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        playerPickData.getMids().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        playerPickData.getFwds().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        playerPickData.getSubs().forEach(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(o.getElement());
            if (eventLiveEntity == null) {
                return;
            }
            o.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
        });
        return playerPickData;
    }

    // do not cache here
    @Override
    public List<PlayerPickData> qryLeaguePickDataList(int leagueId, String leagueType, List<Integer> entryList) {
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        // prepare
        Multimap<Integer, EntryEventResultEntity> entryEventResultMap = HashMultimap.create();
        this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().in(EntryEventResultEntity::getEntry, entryList)).forEach(o -> entryEventResultMap.put(o.getEntry(), o));
        Map<String, PlayerEntity> playerMap = this.getPlayerMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
        // pick list
        Multimap<Integer, EntryPickData> pickMap = HashMultimap.create(); // event -> entryPickData
        entryEventResultMap.forEach((entry, entryEventResult) -> {
            List<EntryPickData> pickList = JsonUtils.json2Collection(entryEventResult.getEventPicks(), List.class, EntryPickData.class);
            if (CollectionUtils.isEmpty(pickList)) {
                return;
            }
            int event = entryEventResult.getEvent();
            pickList.forEach(o -> {
                o.setEvent(event).setEntry(entry);
                pickMap.put(event, o);
            });
        });
        // data
        List<CompletableFuture<List<EntryPickData>>> future = pickMap.keySet().stream().map(event -> CompletableFuture.supplyAsync(() -> this.initEventLeagueEntryPickData(event, pickMap, playerMap, teamShortNameMap), new ForkJoinPool(4))).toList();
        List<EntryPickData> pickDataList = Lists.newArrayList();
        future.stream().map(CompletableFuture::join).forEach(pickDataList::addAll);
        // collect
        return pickDataList.stream().collect(new PlayerPickDataCollector());
    }

    private List<EntryPickData> initEventLeagueEntryPickData(int event, Multimap<Integer, EntryPickData> pickMap, Map<String, PlayerEntity> playerMap, Map<String, String> teamShortNameMap) {
        List<EntryPickData> list = Lists.newArrayList();
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        pickMap.get(event).forEach(pick -> {
            int element = pick.getElement();
            // player
            PlayerEntity playerEntity = playerMap.get(String.valueOf(element));
            if (playerEntity != null) {
                pick.setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId());
            }
            pick.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(pick.getTeamId()), ""));
            // element live
            EventLiveEntity eventLiveEntity = eventLiveMap.get(element);
            if (eventLiveEntity != null) {
                pick.setMinutes(eventLiveEntity.getMinutes());
            }
            list.add(pick);
        });
        return list;
    }

    // do not cache
    @Override
    public List<PlayerPickData> qryLeagueEventPickDataList(int event, int leagueId, String leagueType) {
        List<Integer> entryList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getLeagueId, leagueId).eq(LeagueEventReportEntity::getLeagueType, leagueType).eq(LeagueEventReportEntity::getEvent, event)).stream().map(LeagueEventReportEntity::getEntry).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        // prepare
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        Map<String, PlayerEntity> playerMap = this.getPlayerMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        // collect
        List<PlayerPickData> list = Lists.newArrayList();
        entryEventResultMap.keySet().forEach(entry -> {
            // pick
            List<EntryPickData> pickList = JsonUtils.json2Collection(entryEventResultMap.get(entry).getEventPicks(), List.class, EntryPickData.class);
            if (CollectionUtils.isEmpty(pickList)) {
                return;
            }
            List<EntryPickData> gkpList = Lists.newArrayList();
            List<EntryPickData> defList = Lists.newArrayList();
            List<EntryPickData> midList = Lists.newArrayList();
            List<EntryPickData> fwdList = Lists.newArrayList();
            List<EntryPickData> subList = Lists.newArrayList();
            pickList.forEach(pick -> {
                int element = pick.getElement();
                // player
                PlayerEntity playerEntity = playerMap.get(String.valueOf(element));
                if (playerEntity != null) {
                    pick.setWebName(playerEntity.getWebName()).setElementType(playerEntity.getElementType()).setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType())).setTeamId(playerEntity.getTeamId());
                }
                pick.setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(pick.getTeamId()), ""));
                // element live
                EventLiveEntity eventLiveEntity = eventLiveMap.get(element);
                if (eventLiveEntity != null) {
                    pick.setMinutes(eventLiveEntity.getMinutes()).setPoints(eventLiveEntity.getTotalPoints());
                }
                // add into list
                if (pick.getPosition() <= 11) {
                    switch (pick.getElementType()) {
                        case 1: {
                            gkpList.add(pick);
                            break;
                        }
                        case 2: {
                            defList.add(pick);
                            break;
                        }
                        case 3: {
                            midList.add(pick);
                            break;
                        }
                        case 4: {
                            fwdList.add(pick);
                            break;
                        }
                    }
                } else {
                    subList.add(pick);
                }
            });
            list.add(new PlayerPickData().setEntry(entry).setEvent(event).setGkps(gkpList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setDefs(defList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setMids(midList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setFwds(fwdList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())).setSubs(subList.stream().sorted(Comparator.comparing(EntryPickData::getPosition)).collect(Collectors.toList())));
        });
        return list;
    }

    @Override
    public List<PlayerPickData> qryOffiaccountPickList() {
        int event = this.getNextEvent();
        Map<Object, Object> scoutEntryMap = RedisUtils.getHashByKey("scoutEntry");
        List<Object> scoutList = new ArrayList<>(scoutEntryMap.keySet());
        List<EntryEventSimulatePickEntity> entryEventSimulatePickEntityList = this.entryEventSimulatePickService.list(new QueryWrapper<EntryEventSimulatePickEntity>().lambda().eq(EntryEventSimulatePickEntity::getEntry, FollowAccount.Offiaccount.getEntry()).eq(EntryEventSimulatePickEntity::getEvent, event).in(EntryEventSimulatePickEntity::getOperator, scoutList));
        if (CollectionUtils.isEmpty(entryEventSimulatePickEntityList)) {
            return Lists.newArrayList();
        }
        return entryEventSimulatePickEntityList.stream().map(o -> {
            int entry = o.getOperator();
            PlayerPickData playerPickData = this.qryPickListByPosition(o.getLineup());
            playerPickData.setEntry(entry).setEntryName((String) scoutEntryMap.get(String.valueOf(entry))).setEvent(event).setTeamValue(0).setBank(0).setFreeTransfers(0).setTransfers(0).setTransfersCost(0);
            return playerPickData;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PlayerPickData> qryOffiaccountLineupForTransfers() {
        int event = this.getNextEvent();
        Map<Object, Object> scoutEntryMap = RedisUtils.getHashByKey("scoutEntry");
        List<Object> scoutList = new ArrayList<>(scoutEntryMap.keySet());
        List<EntryEventSimulateTransfersEntity> entryEventLineupEntityList = this.entryEventSimulateTransfersService.list(new QueryWrapper<EntryEventSimulateTransfersEntity>().lambda().eq(EntryEventSimulateTransfersEntity::getEntry, FollowAccount.Offiaccount.getEntry()).eq(EntryEventSimulateTransfersEntity::getEvent, event).in(EntryEventSimulateTransfersEntity::getOperator, scoutList));
        if (CollectionUtils.isEmpty(entryEventLineupEntityList)) {
            return Lists.newArrayList();
        }
        Multimap<Integer, Integer> transfersInMap = HashMultimap.create();
        entryEventLineupEntityList.forEach(o -> {
            int entry = o.getEntry();
            String[] transfersIns = StringUtils.split(o.getTransfersIn(), ',');
            for (String transfersInStr : transfersIns) {
                transfersInMap.put(entry, Integer.valueOf(transfersInStr));
            }
        });
        return entryEventLineupEntityList.stream().map(o -> {
            int entry = o.getOperator();
            PlayerPickData playerPickData = this.qryPickListByPositionForTransfers(o.getLineup());
            playerPickData.setEntry(entry).setEntryName((String) scoutEntryMap.get(String.valueOf(entry))).setEvent(event).setTeamValue(o.getTeamValue()).setBank(o.getBank()).setFreeTransfers(o.getFreeTransfers()).setTransfers(o.getTransfers()).setTransfersCost(o.getTransfersCost());
            Collection<Integer> entryTransfersIns = transfersInMap.get(entry);
            if (CollectionUtils.isEmpty(entryTransfersIns)) {
                return playerPickData;
            }
            playerPickData.getGkps().forEach(data -> {
                if (entryTransfersIns.contains(data.getElement())) {
                    data.setEventTransferIn(true);
                }
            });
            playerPickData.getDefs().forEach(data -> {
                if (entryTransfersIns.contains(data.getElement())) {
                    data.setEventTransferIn(true);
                }
            });
            playerPickData.getMids().forEach(data -> {
                if (entryTransfersIns.contains(data.getElement())) {
                    data.setEventTransferIn(true);
                }
            });
            playerPickData.getFwds().forEach(data -> {
                if (entryTransfersIns.contains(data.getElement())) {
                    data.setEventTransferIn(true);
                }
            });
            return playerPickData;
        }).collect(Collectors.toList());
    }

    @Override
    public List<EntryEventAutoSubsData> qryEntryAutoSubDataList(String season, int event, int entry) {
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).eq(EntryEventResultEntity::getEvent, event));
        if (entryEventResultEntity == null) {
            return Lists.newArrayList();
        }
        return this.qryAutoSubListFromAutoSubs(season, event, entryEventResultEntity.getEventAutoSubs());
    }

    @Cacheable(value = "qryAutoSubListFromAutoSubs", key = "#season+'::'+#event+'::'+#autoSubs", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventAutoSubsData> qryAutoSubListFromAutoSubs(String season, int event, String autoSubs) {
        if (StringUtils.isBlank(autoSubs)) {
            return Lists.newArrayList();
        }
        List<EntryEventAutoSubsData> autoSubList = JsonUtils.json2Collection(autoSubs, List.class, EntryEventAutoSubsData.class);
        if (CollectionUtils.isEmpty(autoSubList)) {
            return Lists.newArrayList();
        }
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
        Map<Integer, Integer> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        Map<String, PlayerEntity> playerMap = this.getPlayerMap();
        autoSubList.forEach(o -> {
            PlayerEntity playerInEntity = playerMap.getOrDefault(String.valueOf(o.getElementIn()), null);
            if (playerInEntity != null) {
                o.setElementInType(playerInEntity.getElementType()).setElementInTypeName(Position.getNameFromElementType(playerInEntity.getElementType())).setElementInWebName(playerInEntity.getWebName()).setElementInTeamId(playerInEntity.getTeamId()).setElementInTeamName(teamNameMap.getOrDefault(String.valueOf(playerInEntity.getTeamId()), "")).setElementInTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerInEntity.getTeamId()), "")).setElementInPoints(eventLiveMap.getOrDefault(o.getElementIn(), 0));
            }
            PlayerEntity playerOutEntity = playerMap.getOrDefault(String.valueOf(o.getElementOut()), null);
            if (playerOutEntity != null) {
                o.setElementOutType(playerOutEntity.getElementType()).setElementOutTypeName(Position.getNameFromElementType(playerOutEntity.getElementType())).setElementOutWebName(playerOutEntity.getWebName()).setElementOutTeamId(playerOutEntity.getTeamId()).setElementOutTeamName(teamNameMap.getOrDefault(String.valueOf(playerOutEntity.getTeamId()), "")).setElementOutTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerOutEntity.getTeamId()), "")).setElementOutPoints(eventLiveMap.getOrDefault(o.getElementOut(), 0));
            }
        });
        return autoSubList;
    }

    @Cacheable(value = "qryLeagueEventAutoSubDataList", key = "#event+'::'+#leagueId+'::'+#leagueType", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventAutoSubsData> qryLeagueEventAutoSubDataList(int event, int leagueId, String leagueType) {
        List<Integer> entryList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getLeagueId, leagueId).eq(LeagueEventReportEntity::getLeagueType, leagueType).eq(LeagueEventReportEntity::getEvent, event)).stream().map(LeagueEventReportEntity::getEntry).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        Map<String, PlayerEntity> playerMap = this.getPlayerMap();
        Map<Integer, Integer> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        // collect
        List<EntryEventAutoSubsData> list = Lists.newArrayList();
        entryEventResultMap.keySet().forEach(entry -> {
            EntryEventResultEntity entryEventResultEntity = entryEventResultMap.get(entry);
            if (StringUtils.isBlank(entryEventResultEntity.getEventAutoSubs())) {
                return;
            }
            List<EntryEventAutoSubsData> autoSubList = JsonUtils.json2Collection(entryEventResultEntity.getEventAutoSubs(), List.class, EntryEventAutoSubsData.class);
            if (CollectionUtils.isEmpty(autoSubList)) {
                return;
            }
            autoSubList.forEach(o -> {
                o.setEntry(entry).setEvent(event);
                PlayerEntity playerInEntity = playerMap.getOrDefault(String.valueOf(o.getElementIn()), null);
                if (playerInEntity != null) {
                    o.setElementInType(playerInEntity.getElementType()).setElementInTypeName(Position.getNameFromElementType(playerInEntity.getElementType())).setElementInWebName(playerInEntity.getWebName()).setElementInTeamId(playerInEntity.getTeamId()).setElementInTeamName(teamNameMap.getOrDefault(String.valueOf(playerInEntity.getTeamId()), "")).setElementInTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerInEntity.getTeamId()), "")).setElementInPoints(eventLiveMap.getOrDefault(o.getElementIn(), 0));
                }
                PlayerEntity playerOutEntity = playerMap.getOrDefault(String.valueOf(o.getElementOut()), null);
                if (playerOutEntity != null) {
                    o.setElementOutType(playerOutEntity.getElementType()).setElementOutTypeName(Position.getNameFromElementType(playerOutEntity.getElementType())).setElementOutWebName(playerOutEntity.getWebName()).setElementOutTeamId(playerOutEntity.getTeamId()).setElementOutTeamName(teamNameMap.getOrDefault(String.valueOf(playerOutEntity.getTeamId()), "")).setElementOutTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(playerOutEntity.getTeamId()), "")).setElementOutPoints(eventLiveMap.getOrDefault(o.getElementOut(), 0));
                }
                list.add(o);
            });
        });
        return list;
    }

    @Cacheable(value = "qryEntryFreeTransfersMap", key = "#entry", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<Integer, Integer> qryEntryFreeTransfersMap(int entry) {
        Map<Integer, Integer> map = Maps.newHashMap();
        // prepare
        List<EntryEventResultEntity> entryEventResultList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).orderByAsc(EntryEventResultEntity::getEvent));
        Map<Integer, String> eventChipMap = entryEventResultList.stream().filter(o -> !StringUtils.equals(o.getEventChip(), Chip.NONE.getValue())).collect(Collectors.toMap(EntryEventResultEntity::getEvent, EntryEventResultEntity::getEventChip));
        Map<Integer, Integer> eventTransfersMap = entryEventResultList.stream().filter(o -> o.getEventTransfers() > 0).collect(Collectors.toMap(EntryEventResultEntity::getEvent, EntryEventResultEntity::getEventTransfers));
        List<Integer> eventCostList = entryEventResultList.stream().filter(o -> o.getEventTransfersCost() > 0).map(EntryEventResultEntity::getEvent).toList();
        List<Integer> eventList = entryEventResultList.stream().map(EntryEventResultEntity::getEvent).collect(Collectors.toList());
        int startEvent = eventList.get(0);
        eventList.add(eventList.get(eventList.size() - 1) + 1);
        // calc
        eventList.forEach(event -> {
            // 开卡周无限转会
            if (event == startEvent || (eventChipMap.containsKey(event) && (StringUtils.equals(eventChipMap.get(event), Chip.WC.getValue()) || StringUtils.equals(eventChipMap.get(event), Chip.FH.getValue())))) {
                map.put(event, 99);
                return;
            }
            // 上周开卡或者扣分，本周必为1次
            if ((eventChipMap.containsKey(event - 1) && (StringUtils.equals(eventChipMap.get(event - 1), Chip.WC.getValue()) || StringUtils.equals(eventChipMap.get(event - 1), Chip.FH.getValue()))) || (eventCostList.contains(event - 1))) {
                map.put(event, 1);
                return;
            }
            // 上周剩余，加一次，减去本周转会数
            int freeTransfers = map.getOrDefault(event - 1, 0) + 1 - eventTransfersMap.getOrDefault(event, 0);
            if (freeTransfers < 0) {
                freeTransfers = 0;
            }
            if (freeTransfers > 2) {
                freeTransfers = 1;
            }
            map.put(event, freeTransfers);
        });
        return map;
    }

    // do not cache
    @Override
    public String qryEntryEventPicks(int event, int entry, int operator) {
        EntryEventSimulatePickEntity entryEventSimulatePickEntity = this.entryEventSimulatePickService.getOne(new QueryWrapper<EntryEventSimulatePickEntity>().lambda().eq(EntryEventSimulatePickEntity::getEntry, entry).eq(EntryEventSimulatePickEntity::getEvent, event).eq(EntryEventSimulatePickEntity::getOperator, operator));
        if (entryEventSimulatePickEntity != null) {
            return entryEventSimulatePickEntity.getLineup();
        }
        EntryEventSimulateTransfersEntity entryEventSimulateTransfersEntity = this.entryEventSimulateTransfersService.getOne(new QueryWrapper<EntryEventSimulateTransfersEntity>().lambda().eq(EntryEventSimulateTransfersEntity::getEntry, entry).eq(EntryEventSimulateTransfersEntity::getEvent, event).eq(EntryEventSimulateTransfersEntity::getOperator, operator));
        if (entryEventSimulateTransfersEntity != null) {
            return entryEventSimulateTransfersEntity.getLineup();
        }
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).eq(EntryEventResultEntity::getEvent, event - 1));
        if (entryEventResultEntity != null) {
            return entryEventResultEntity.getEventPicks();
        }
        return "";
    }

    /**
     * @implNote league
     */
    @Cacheable(value = "qryCountTournamentLeagueTeams", key = "#url", unless = "#result eq 0")
    @Override
    public int qryCountTournamentLeagueTeams(String url) {
        int count = url.contains("/standings/c") ? this.interfaceService.getEntryInfoListFromClassic(CommonUtils.getLeagueId(url)).size() : this.interfaceService.getEntryInfoListFromH2h(CommonUtils.getLeagueId(url)).size();
        if (count == 0) {
            count = url.contains("/standings/c") ? this.interfaceService.getNewEntryInfoListFromClassic(CommonUtils.getLeagueId(url)).size() : this.interfaceService.getNewEntryInfoListFromH2h(CommonUtils.getLeagueId(url)).size();
        }
        return count;
    }

    /**
     * @implNote tournament
     */
    @Cacheable(value = "qryTournamentDataById", key = "#tournamentId", unless = "#result eq null or #result.id eq 0")
    @Override
    public TournamentInfoData qryTournamentDataById(int tournamentId) {
        TournamentInfoData tournamentInfoData = new TournamentInfoData();
        TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return tournamentInfoData;
        }
        BeanUtil.copyProperties(tournamentInfoEntity, tournamentInfoData);
        tournamentInfoData.setGroupModeName(GroupMode.valueOf(tournamentInfoData.getGroupMode()).getModeName()).setKnockoutModeName(KnockoutMode.valueOf(tournamentInfoData.getKnockoutMode()).getModeName());
        return tournamentInfoData;
    }

    @Cacheable(value = "qryTournamentInfoById", key = "#tournamentId", unless = "#result eq null or #result.id eq 0")
    @Override
    public TournamentInfoEntity qryTournamentInfoById(int tournamentId) {
        return this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda().eq(TournamentInfoEntity::getId, tournamentId).eq(TournamentInfoEntity::getState, 1));
    }

    @Cacheable(value = "qryEntryListByTournament", key = "#tournamentId", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<Integer> qryEntryListByTournament(int tournamentId) {
        return this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda().eq(TournamentEntryEntity::getTournamentId, tournamentId)).stream().map(TournamentEntryEntity::getEntry).filter(o -> o > 0).collect(Collectors.toList());
    }

    @Cacheable(value = "qryEntryListByKnockout", key = "#tournamentId+'::'+#event", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<Integer> qryEntryListByKnockout(int tournamentId, int event) {
        List<Integer> list = Lists.newArrayList();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).eq(TournamentKnockoutResultEntity::getEvent, event)).stream().filter(o -> o.getHomeEntry() > 0 && o.getAwayEntry() > 0).forEach(o -> {
            list.add(o.getHomeEntry());
            list.add(o.getAwayEntry());
        });
        return list;
    }

    @Cacheable(value = "qryKnockoutMapByTournament", key = "#tournamentId+'::'+#event", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, TournamentKnockoutEntity> qryKnockoutMapByTournament(int tournamentId, int event) {
        // get round matchIds
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultEntityList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).eq(TournamentKnockoutResultEntity::getEvent, event).orderByAsc(TournamentKnockoutResultEntity::getId));
        if (CollectionUtils.isEmpty(tournamentKnockoutResultEntityList)) {
            return Maps.newHashMap();
        }
        List<Integer> matchIdList = tournamentKnockoutResultEntityList.stream().map(TournamentKnockoutResultEntity::getMatchId).toList();
        // get all tournament knockouts
        return this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getTournamentId, tournamentId).in(TournamentKnockoutEntity::getMatchId, matchIdList)).stream().collect(Collectors.toMap(o -> String.valueOf(o.getMatchId()), o -> o));
    }

    @Cacheable(value = "qryKnockoutRoundMapByTournament", key = "#tournamentId+'::'+#round+'::'+#event", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, List<TournamentKnockoutEventResultData>> qryKnockoutRoundMapByTournament(int tournamentId, int round, int event) {
        Map<String, List<TournamentKnockoutEventResultData>> map = Maps.newHashMap(); // one knockoutData(matchId) -> multi knockoutResultData(playAgainstId)
        // get round matchIds
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultEntityList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).eq(TournamentKnockoutResultEntity::getEvent, event).orderByAsc(TournamentKnockoutResultEntity::getId));
        if (CollectionUtils.isEmpty(tournamentKnockoutResultEntityList)) {
            return map;
        }
        List<Integer> matchIdList = tournamentKnockoutResultEntityList.stream().map(TournamentKnockoutResultEntity::getMatchId).toList();
        int playAgainstId = tournamentKnockoutResultEntityList.stream().max(Comparator.comparing(TournamentKnockoutResultEntity::getId)).map(TournamentKnockoutResultEntity::getPlayAgainstId).orElse(0);
        // get all tournament knockouts
        Map<Integer, TournamentKnockoutEntity> knockoutMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getTournamentId, tournamentId).eq(TournamentKnockoutEntity::getRound, round).in(TournamentKnockoutEntity::getMatchId, matchIdList)).stream().collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, o -> o));
        if (CollectionUtils.isEmpty(knockoutMap)) {
            return map;
        }
        // get all tournament knockout results
        Map<Integer, List<TournamentKnockoutEventResultData>> knockoutResultMap = Maps.newHashMap();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).le(TournamentKnockoutResultEntity::getPlayAgainstId, playAgainstId).in(TournamentKnockoutResultEntity::getMatchId, matchIdList)).forEach(o -> {
            List<TournamentKnockoutEventResultData> knockoutResultList = Lists.newArrayList();
            if (knockoutResultMap.containsKey(o.getMatchId())) {
                knockoutResultList = knockoutResultMap.get(o.getMatchId());
            }
            TournamentKnockoutEventResultData data = new TournamentKnockoutEventResultData();
            BeanUtil.copyProperties(o, data);
            // knockout data
            TournamentKnockoutEntity tournamentKnockoutEntity = knockoutMap.get(o.getMatchId());
            if (tournamentKnockoutEntity != null) {
                data.setNextMatchId(tournamentKnockoutEntity.getNextMatchId()).setRound(tournamentKnockoutEntity.getRound()).setRoundWinner(tournamentKnockoutEntity.getRoundWinner());
            }
            knockoutResultList.add(data);
            knockoutResultMap.put(o.getMatchId(), knockoutResultList);
        });
        return knockoutResultMap.values().stream().collect(Collectors.toMap(o -> String.valueOf(o.get(0).getMatchId()), o -> o));
    }

    @Cacheable(value = "qryEntryListByChampionLeagueGroup", key = "#tournamentId+'::'+#groupName", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<Integer> qryEntryListByChampionLeagueGroup(int tournamentId, String groupName) {
        return this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId).eq(TournamentGroupEntity::getGroupName, groupName)).stream().map(TournamentGroupEntity::getEntry).collect(Collectors.toList());
    }

    @Cacheable(value = "qryEntryListByChampionLeagueKnockout", key = "#tournamentId+'::'+#round", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<Integer> qryEntryListByChampionLeagueKnockout(int tournamentId, int round) {
        List<Integer> list = Lists.newArrayList();
        this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getTournamentId, tournamentId).eq(TournamentKnockoutEntity::getRound, round)).forEach(o -> {
            list.add(o.getHomeEntry());
            list.add(o.getAwayEntry());
        });
        return list.stream().distinct().collect(Collectors.toList());
    }

//    @Cacheable(value = "qryKnockoutBracketResultByTournament", key = "#tournamentId", unless = "#result eq null or #result.results.size() eq 0")
    @Override
    public KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId) {
        // round -> tournament_knockout_result_data list
        LinkedHashMap<Integer, List<TournamentKnockoutResultData>> knockoutResultRoundDataMap = Maps.newLinkedHashMap();
        this.qryKnockoutResultByTournament(tournamentId).forEach(o -> {
            int round = o.getRound();
            List<TournamentKnockoutResultData> list = Lists.newArrayList();
            if (knockoutResultRoundDataMap.containsKey(round)) {
                list = knockoutResultRoundDataMap.get(round);
            }
            list.add(o);
            knockoutResultRoundDataMap.put(round, list);
        });
        if (CollectionUtils.isEmpty(knockoutResultRoundDataMap)) {
            return new KnockoutBracketData();
        }
        // return
        List<List<TournamentKnockoutResultData>> results = Lists.newArrayList();
        knockoutResultRoundDataMap.keySet().forEach(round -> results.add(knockoutResultRoundDataMap.get(round)));
        return new KnockoutBracketData()
                .setTeams(knockoutResultRoundDataMap.get(1))
                .setResults(results);
    }

    @Cacheable(value = "qryKnockoutResultByTournament", key = "#tournamentId", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId) {
        List<TournamentKnockoutResultData> knockoutResultDataList = Lists.newArrayList();
        // groupName
        Map<String, String> groupNameMap = this.qryTournamentGroupNameMap(tournamentId);
        // entry_group
        Map<String, Integer> entryGroupMap = this.qryTournamentGroupEntryGroupIdMap(tournamentId);
        // knockout
        Map<Integer, TournamentKnockoutEntity> knockoutMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId))
                .stream()
                .collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, v -> v));
        if (CollectionUtils.isEmpty(knockoutMap)) {
            return knockoutResultDataList;
        }
        // knockout_result, every match_id return a knockoutResultData
        knockoutMap.keySet().forEach(matchId -> {
            List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                    .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                    .eq(TournamentKnockoutResultEntity::getMatchId, matchId));
            // knockoutResultData
            TournamentKnockoutResultData knockoutResultData = new TournamentKnockoutResultData();
            TournamentKnockoutResultEntity o = knockoutResultList.get(0);
            int homeEntry = o.getHomeEntry();
            int homeGroupId = entryGroupMap.getOrDefault(String.valueOf(homeEntry), 0);
            String homeGroupName = groupNameMap.getOrDefault(String.valueOf(homeGroupId), "");
            int awayEntry = o.getAwayEntry();
            int awayGroupId = entryGroupMap.getOrDefault(String.valueOf(awayEntry), 0);
            String awayGroupName = groupNameMap.getOrDefault(String.valueOf(awayGroupId), "");
            double homeEntryWinningNum = knockoutMap.get(matchId).getHomeEntryWinningNum() == null ? 0 : knockoutMap.get(matchId).getHomeEntryWinningNum();
            double awayEntryWinningNum = knockoutMap.get(matchId).getAwayEntryWinningNum() == null ? 0 : knockoutMap.get(matchId).getAwayEntryWinningNum();
            knockoutResultData
                    .setTournamentId(tournamentId)
                    .setRound(knockoutMap.get(o.getMatchId()).getRound())
                    .setEvent(o.getEvent())
                    .setPlayAgainstId(o.getPlayAgainstId())
                    .setMatchId(o.getMatchId())
                    .setHomeEntry(o.getHomeEntry())
                    .setAwayEntry(o.getAwayEntry())
                    .setHomeEntryGroupId(homeGroupId)
                    .setAwayEntryGroupId(awayGroupId)
                    .setHomeEntryGroupName(homeGroupName)
                    .setAwayEntryGroupName(awayGroupName)
                    .setHomeEntryName(o.getHomeEntry() == 0 ? "" : this.getKnockoutResultEntryName(o.getHomeEntry()))
                    .setAwayEntryName(o.getHomeEntry() == 0 ? "" : this.getKnockoutResultEntryName(o.getAwayEntry()))
                    .setHomeEntryNetPoints(this.calcKnockoutResultDataNetPoint(knockoutResultList, "home"))
                    .setAwayEntryNetPoints(this.calcKnockoutResultDataNetPoint(knockoutResultList, "away"))
                    .setHomeEntryRank(o.getHomeEntryRank())
                    .setAwayEntryRank(o.getAwayEntryRank())
                    .setHomeEntryWinningNum(homeEntryWinningNum)
                    .setAwayEntryWinningNum(awayEntryWinningNum)
                    .setMatchWinner(o.getMatchWinner());
            // match information
            if (knockoutResultData.getHomeEntry() != 0 && knockoutResultData.getAwayEntry() != 0) {
                Map<Integer, String> entryNameMap = ImmutableMap.of(knockoutResultData.getHomeEntry(), knockoutResultData.getHomeEntryName(), knockoutResultData.getAwayEntry(), knockoutResultData.getAwayEntryName());
                knockoutResultData.setMatchInfo(this.setRoundMatchInformation(knockoutResultList, entryNameMap));
            }
            knockoutResultDataList.add(knockoutResultData);
        });
        return knockoutResultDataList;
    }

    private String getKnockoutResultEntryName(int entry) {
        if (entry < 0) {
            return "BYE";
        }
        EntryInfoData entryInfoData = this.qryEntryInfo(entry);
        if (entryInfoData == null) {
            return "";
        }
        return entryInfoData.getEntryName();
    }

    private int calcKnockoutResultDataNetPoint(List<TournamentKnockoutResultEntity> knockoutResultList, String type) {
        if (StringUtils.equals(type, "home")) {
            return knockoutResultList.stream().mapToInt(TournamentKnockoutResultEntity::getHomeEntryNetPoints).sum();
        } else if (StringUtils.equals(type, "away")) {
            return knockoutResultList.stream().mapToInt(TournamentKnockoutResultEntity::getAwayEntryNetPoints).sum();
        }
        return 0;
    }

    private String setRoundMatchInformation(List<TournamentKnockoutResultEntity> knockoutResultList, Map<Integer, String> entryNameMap) {
        StringBuilder builder = new StringBuilder();
        knockoutResultList.forEach(o -> builder.append(entryNameMap.get(o.getHomeEntry())).append("（").append(o.getHomeEntryNetPoints()).append("）").append("- ").append("（").append(o.getAwayEntryNetPoints()).append("）").append(entryNameMap.get(o.getAwayEntry())));
        return builder.toString();
    }

    @Cacheable(value = "qryTournamentEntryEventPick", key = "#event+'::'+#tournamentId", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<EntryEventPickEntity> qryTournamentEntryEventPick(int event, int tournamentId) {
        List<Integer> entryList = this.qryEntryListByTournament(tournamentId);
        if (CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        return this.entryEventPickService.list(new QueryWrapper<EntryEventPickEntity>().lambda().eq(EntryEventPickEntity::getEvent, event).in(EntryEventPickEntity::getEntry, entryList));
    }

    @Cacheable(value = "qryTournamentGroupNameMap", key = "#tournamentId", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, String> qryTournamentGroupNameMap(int tournamentId) {
        Map<String, String> groupNameMap = Maps.newHashMap();
        TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return groupNameMap;
        }
        List<Integer> groupList = Lists.newArrayList();
        IntStream.rangeClosed(1, tournamentInfoEntity.getGroupNum()).forEach(groupList::add);
        if (CollectionUtils.isEmpty(groupList)) {
            return groupNameMap;
        }
        this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId).in(TournamentGroupEntity::getGroupId, groupList)).forEach(o -> groupNameMap.put(String.valueOf(o.getGroupId()), o.getGroupName()));
        return groupNameMap;
    }

    @Cacheable(value = "qryTournamentGroupEntryGroupIdMap", key = "#tournamentId", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, Integer> qryTournamentGroupEntryGroupIdMap(int tournamentId) {
        List<Integer> groupList = Lists.newArrayList();
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return Maps.newHashMap();
        }
        IntStream.rangeClosed(1, tournamentInfoEntity.getGroupNum()).forEach(groupList::add);
        if (CollectionUtils.isEmpty(groupList)) {
            return Maps.newHashMap();
        }
        return this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId).in(TournamentGroupEntity::getGroupId, groupList)).stream().collect(Collectors.toMap(o -> String.valueOf(o.getEntry()), TournamentGroupEntity::getGroupId));
    }

    @Cacheable(value = "qryEventTournamentRoyale", key = "#event+'::'+#tournamentId", unless = "#result eq null")
    @Override
    public TournamentRoyaleData qryEventTournamentRoyale(int event, int tournamentId) {
        TournamentRoyaleEntity tournamentRoyaleEntity = this.tournamentRoyaleService.getOne(new QueryWrapper<TournamentRoyaleEntity>().lambda().eq(TournamentRoyaleEntity::getEvent, event).eq(TournamentRoyaleEntity::getTournamentId, tournamentId));
        if (tournamentRoyaleEntity == null) {
            return new TournamentRoyaleData();
        }
        return BeanUtil.copyProperties(tournamentRoyaleEntity, TournamentRoyaleData.class);
    }

    /**
     * @implNote report
     */
    @Cacheable(value = "qryLeagueInfoByName", key = "#leagueName", unless = "#result eq null or #result eq null")
    @Override
    public LeagueEventReportEntity qryLeagueInfoByName(String leagueName) {
        int event = this.getCurrentEvent();
        return this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getEvent, event).eq(LeagueEventReportEntity::getLeagueName, leagueName)).stream().findFirst().orElse(null);
    }

    @Cacheable(value = "qryLeagueNameByIdAndType", key = "#leagueId+'::'+#leagueType", unless = "#result eq null or #result eq ''")
    @Override
    public String qryLeagueNameByIdAndType(int leagueId, String leagueType) {
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getLeagueId, leagueId).eq(LeagueEventReportEntity::getLeagueType, leagueType));
        if (!CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return leagueEventReportEntityList.stream().map(LeagueEventReportEntity::getLeagueName).findFirst().orElse("");
        }
        LeagueInfoData leagueInfoData = new LeagueInfoData();
        if (StringUtils.equals(LeagueType.Classic.name(), leagueType)) {
            leagueInfoData = this.interfaceService.getEntryInfoListFromClassicByLimit(leagueId, 1);
        } else if (StringUtils.equals(LeagueType.H2h.name(), leagueType)) {
            leagueInfoData = this.interfaceService.getEntryInfoListFromH2hByLimit(leagueId, 1);
        }
        return leagueInfoData.getName();
    }

    @Cacheable(value = "qryTeamSelectStatList", unless = "#result eq null or #result.size() eq 0")
    @Override
    public List<String> qryTeamSelectStatList() {
        return this.leagueEventReportService.list().stream().map(LeagueEventReportEntity::getLeagueName).distinct().collect(Collectors.toList());
    }

    @Cacheable(value = "qryTeamSelectStatList", key = "#event+'::'+#leagueId+'::'+#leagueType", unless = "#result eq null or #result.size() eq 0")
    @Override
    public Map<String, String> qryLeagueEventEoMap(int event, int leagueId, String leagueType) {
        Map<String, String> map = Maps.newHashMap();
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
        countMap.forEach((element, count) -> map.put(String.valueOf(element), NumberUtil.formatPercent(NumberUtil.div(count.doubleValue(), size), 1)));
        return map;
    }

    @Cacheable(value = "qryLeagueEventReportDataByLeagueId", key = "#leagueId", unless = "#result eq null or #result.leagueId eq 0")
    @Override
    public LeagueEventInfoData qryLeagueEventReportDataByLeagueId(int leagueId) {
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda().eq(TournamentInfoEntity::getLeagueId, leagueId));
        if (tournamentInfoEntity == null) {
            return new LeagueEventInfoData();
        }
        return new LeagueEventInfoData().setId(tournamentInfoEntity.getId()).setLeagueId(tournamentInfoEntity.getLeagueId()).setLeagueType(tournamentInfoEntity.getLeagueType()).setLeagueName(tournamentInfoEntity.getName()).setLimit(0);
    }

    /**
     * @implNote live, cannot be cached
     */
    @Override
    public Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap() {
        return this.redisCacheService.getEventLiveFixtureMap();
    }

    @Override
    public Map<String, EventLiveEntity> getEventLiveByEvent(int event) {
        return this.redisCacheService.getEventLiveByEvent(event);
    }

    @Override
    public Map<String, Map<String, Integer>> getLiveBonusCacheMap() {
        return this.redisCacheService.getLiveBonusCacheMap();
    }

    @Override
    public List<LiveMatchData> qryLiveMatchList(int statusId) {
        List<LiveMatchData> list = Lists.newArrayList();
        String queryStatus = Arrays.stream(MatchPlayStatus.values()).filter(o -> o.getStatus() == statusId).map(Enum::name).findFirst().orElse("");
        if (StringUtils.isEmpty(queryStatus)) {
            return list;
        }
        Map<String, Map<String, List<LiveFixtureData>>> eventLiveFixtureMap = this.redisCacheService.getEventLiveFixtureMap();
        eventLiveFixtureMap.keySet().forEach(teamId -> eventLiveFixtureMap.get(teamId).forEach((status, fixtureList) -> {
            if (!StringUtils.equals(status, queryStatus)) {
                return;
            }
            fixtureList.forEach(o -> {
                if (!o.isWasHome()) {
                    return;
                }
                list.add(new LiveMatchData().setHomeTeamId(o.getTeamId()).setHomeTeamName(o.getTeamName()).setHomeTeamShortName(o.getTeamShortName()).setHomeScore(o.getTeamScore()).setAwayTeamId(o.getAgainstId()).setAwayTeamName(o.getAgainstName()).setAwayTeamShortName(o.getAgainstShortName()).setAwayScore(o.getAgainstTeamScore()).setKickoffTime(o.getKickoffTime()));
            });
        }));
        return list.stream().sorted(Comparator.comparing(LiveMatchData::getKickoffTime).reversed()).collect(Collectors.toList());
    }

    @Override
    public List<LiveMatchTeamData> qryLiveTeamDataList(int statusId) {
        List<LiveMatchTeamData> list = Lists.newArrayList();
        // prepare
        int event = this.getCurrentEvent();
        Collection<EventLiveEntity> eventLiveList = this.getEventLiveByEvent(event).values();
        Map<Integer, PlayerEntity> playerMap = this.getLivePlayerMap();
        Map<Integer, String> teamShortNameMap = this.getLiveTeamShortNameMap();
        // live element event result
        List<Integer> teamIdList = Lists.newArrayList();
        this.qryLiveMatchList(statusId).forEach(o -> {
            teamIdList.add(o.getHomeTeamId());
            teamIdList.add(o.getAwayTeamId());
        });
        teamIdList.forEach(teamId -> list.add(this.qryLiveTeamData(teamId, eventLiveList, playerMap, teamShortNameMap)));
        return list;
    }

    private LiveMatchTeamData qryLiveTeamData(int teamId, Collection<EventLiveEntity> eventLiveList, Map<Integer, PlayerEntity> playerMap, Map<Integer, String> teamShortNameMap) {
        LiveMatchTeamData data = new LiveMatchTeamData().setTeamId(teamId);
        List<ElementEventResultData> teamDataList = Lists.newArrayList();
        // team data
        Map<Integer, Integer> liveBonusMap = this.getLiveBonusMap(teamId);
        eventLiveList.forEach(o -> {
            if (o.getTeamId() != teamId || o.getMinutes() <= 0) {
                return;
            }
            ElementEventResultData elementEventResultData = new ElementEventResultData();
            elementEventResultData.setEvent(o.getEvent()).setElement(o.getElement()).setWebName(playerMap.containsKey(o.getElement()) ? playerMap.get(o.getElement()).getWebName() : "").setElementType(o.getElementType()).setElementTypeName(Position.getNameFromElementType(o.getElementType())).setTeamId(playerMap.containsKey(o.getElement()) ? playerMap.get(o.getElement()).getTeamId() : 0).setMinutes(o.getMinutes()).setGoalsScored(o.getGoalsScored()).setAssists(o.getAssists()).setGoalsConceded(o.getGoalsConceded()).setOwnGoals(o.getOwnGoals()).setPenaltiesSaved(o.getPenaltiesSaved()).setPenaltiesMissed(o.getPenaltiesMissed()).setYellowCards(o.getYellowCards()).setRedCards(o.getRedCards()).setSaves(o.getSaves()).setBps(o.getBps()).setTotalPoints(o.getTotalPoints());
            if (o.getBonus() > 0) {
                elementEventResultData.setBonus(o.getBonus()).setTotalPoints(elementEventResultData.getTotalPoints());
            } else {
                elementEventResultData.setBonus(liveBonusMap.getOrDefault(o.getElement(), 0)).setTotalPoints(elementEventResultData.getTotalPoints() + elementEventResultData.getBonus());
            }
            elementEventResultData.setTeamShortName(teamShortNameMap.getOrDefault(elementEventResultData.getTeamId(), ""));
            teamDataList.add(elementEventResultData);
        });
        data.setElementEventResultList(teamDataList.stream().sorted(Comparator.comparing(ElementEventResultData::getTotalPoints).thenComparing(ElementEventResultData::getBps).reversed()).collect(Collectors.toList()));
        return data;
    }

    private Map<Integer, PlayerEntity> getLivePlayerMap() {
        Map<Integer, PlayerEntity> map = Maps.newHashMap();
        this.getPlayerMap().forEach((k, v) -> map.put(Integer.valueOf(k), v));
        return map;
    }

    private Map<Integer, String> getLiveTeamShortNameMap() {
        Map<Integer, String> map = Maps.newHashMap();
        this.getTeamShortNameMap().forEach((k, v) -> map.put(Integer.valueOf(k), v));
        return map;
    }

    private Map<Integer, Integer> getLiveBonusMap(int teamId) {
        Map<Integer, Integer> map = Maps.newHashMap();
        this.getLiveBonusCacheMap().forEach((team, list) -> {
            if (!StringUtils.equals(team, String.valueOf(teamId))) {
                return;
            }
            list.forEach((element, bonus) -> map.put(Integer.valueOf(element), bonus));
        });
        return map;
    }

    /**
     * @implNote scout
     */
    @Override
    public ScoutData qryScoutEntryEventData(int event, int entry) {
        ScoutEntity scoutEntity = this.scoutService.getOne(new QueryWrapper<ScoutEntity>().lambda().eq(ScoutEntity::getEvent, event).eq(ScoutEntity::getEntry, entry));
        if (scoutEntity == null) {
            return new ScoutData();
        }
        return new ScoutData().setEvent(event).setEntry(entry).setScoutName(scoutEntity.getScoutName()).setGkp(scoutEntity.getGkp()).setGkpName(this.qryPlayerWebNameByElement(scoutEntity.getGkp())).setGkpPrice(this.getElementPrice(scoutEntity.getGkp())).setDef(scoutEntity.getDef()).setDefName(this.qryPlayerWebNameByElement(scoutEntity.getDef())).setDefPrice(this.getElementPrice(scoutEntity.getDef())).setMid(scoutEntity.getMid()).setMidName(this.qryPlayerWebNameByElement(scoutEntity.getMid())).setMidPrice(this.getElementPrice(scoutEntity.getMid())).setFwd(scoutEntity.getFwd()).setFwdName(this.qryPlayerWebNameByElement(scoutEntity.getFwd())).setFwdPrice(this.getElementPrice(scoutEntity.getFwd())).setCaptain(scoutEntity.getCaptain()).setCaptainName(this.qryPlayerWebNameByElement(scoutEntity.getCaptain())).setReason(scoutEntity.getReason());
    }

    private String qryPlayerWebNameByElement(int element) {
        PlayerEntity playerEntity = this.getPlayerByElement(element);
        return playerEntity == null ? "" : playerEntity.getWebName();
    }

    private double getElementPrice(int element) {
        PlayerEntity playerEntity = this.getPlayerByElement(element);
        if (playerEntity != null) {
            return playerEntity.getPrice() / 10.0;
        }
        return 0;
    }

    /**
     * @implNote simulate
     */
    @Override
    public PlayerPickData qryEntryEventPickData(int event, int entry, int operator) {
        String picks = this.qryEntryEventPicks(event, entry, operator);
        PlayerPickData playerPickData = this.qryPickListByPosition(picks);
        playerPickData.setEntry(entry).setEvent(event);
        EntryInfoData entryInfoData = this.qryEntryInfo(entry);
        if (entryInfoData != null) {
            playerPickData.setEntryName(entryInfoData.getEntryName()).setPlayerName(entryInfoData.getPlayerName());
        }
        return playerPickData;
    }

}
