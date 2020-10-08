package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.BattleGroupResultCollector;
import com.tong.fpl.config.collector.KnockoutResultCollector;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.HistorySeason;
import com.tong.fpl.constant.enums.MatchPlayStatus;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.domain.letletme.tournament.ZjTournamentCaptainData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/31
 */
@Slf4j
@Valid
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueryServiceImpl implements IQuerySerivce {

    private final IRedisCacheSerive redisCacheSerive;
    private final IStaticSerive staticSerive;
    private final PlayerService playerService;
    private final EntryInfoService entryInfoService;
    private final EventLiveService eventLiveService;
    private final EntryEventResultService entryEventResultService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;
    private final ZjTournamentCaptainService zjTournamentCaptainService;
    private final TeamSelectStatService teamSelectStatService;

    /**
     * @implNote player
     */
    @Override
    public int qryPlayerElementByCode(String season, int code) {
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getCode, code));
        MybatisPlusConfig.season.remove();
        return playerEntity == null ? 0 : playerEntity.getElement();
    }

    @Override
    public int qryPlayerElementByWebName(String season, String webName) throws Exception {
        MybatisPlusConfig.season.set(season);
        List<PlayerEntity> playerList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getWebName, webName));
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(playerList)) {
            return 0;
        } else if (playerList.size() > 1) {
            throw new Exception("webname不止一个球员，请用element或code查询!");
        }
        return playerList.get(0) == null ? 0 : playerList.get(0).getElement();
    }

    @Cacheable(value = "qryPlayerData", key = "#element", cacheManager = "apiCacheManager", unless = "#result == null")
    @Override
    public PlayerData qryPlayerData(int element) {
        PlayerEntity playerEntity = this.getPlayerByElememt(element);
        if (playerEntity == null) {
            return null;
        }
        PlayerData playerData = new PlayerData();
        // info
        playerData.setInfoData(this.initPlayerInfo(CommonUtils.getCurrentSeason(), playerEntity));
        // fixture, next 5 gw
        playerData.setFixtureDataList(this.setPlayerFixture(playerEntity.getTeamId()));
        // current season data
        playerData.setCurrentSeason(this.setSeasonData(CommonUtils.getCurrentSeason(), playerEntity.getCode()));
        // history season data（use code as unique index）
        playerData.setHistorySeasonList(this.setHistorySeasonData(playerEntity.getCode()));
        return playerData;
    }

    @Cacheable(value = "initPlayerInfo", key = "#playerEntity.element", condition = "#playerEntity.element gt 0", unless = "#result == null")
    @Override
    public PlayerInfoData initPlayerInfo(String season, PlayerEntity playerEntity) {
        Map<String, String> teamNameMap = this.getTeamNameMap(season);
        Map<String, String> positionMap = this.getPositionMap();
        return new PlayerInfoData()
                .setElement(playerEntity.getElement())
                .setCode(playerEntity.getCode())
                .setWebName(playerEntity.getWebName())
                .setElementType(playerEntity.getElementType())
                .setElementTypeName(positionMap.get(String.valueOf(playerEntity.getElementType())))
                .setTeamId(playerEntity.getTeamId())
                .setTeamName(teamNameMap.get(String.valueOf(playerEntity.getTeamId())))
                .setPrice(NumberUtil.div(playerEntity.getPrice().intValue(), 10, 2));
    }

    private List<PlayerFixtureData> setPlayerFixture(int teamId) {
        List<PlayerFixtureData> playerFixtureList = Lists.newArrayList();
        int currentEvent = this.getCurrentEvent();
        Map<String, String> teamNameMap = this.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap();
        Map<String, List<PlayerFixtureData>> teamFixtureMap = this.getEventFixtureByTeamId(teamId);
        List<PlayerFixtureData> teamFixtureList = Lists.newArrayList();
        IntStream.range(currentEvent - 1, currentEvent + 4).forEach(event -> teamFixtureList.addAll(teamFixtureMap.get(String.valueOf(event))));
        teamFixtureList.forEach(o -> {
                    o.setAgainstTeamName(teamNameMap.get(String.valueOf(o.getAgainstTeamId())));
                    o.setAgainstTeamShortName(teamShortNameMap.get(String.valueOf(o.getAgainstTeamId())));
                    playerFixtureList.add(o);
                }
        );
        return playerFixtureList;
    }

    private PlayerDetailData setSeasonData(String season, int code) {
        int element = this.qryPlayerElementByCode(season, code);
        PlayerDetailData playerDetailData = new PlayerDetailData().setSeason(season);
        PlayerStatEntity playerStatEntity = this.getPlayerStatByElement(season, element);
        if (playerStatEntity == null) {
            return playerDetailData;
        }
        BeanUtil.copyProperties(playerStatEntity, playerDetailData, CopyOptions.create().ignoreNullValue());
        return playerDetailData;
    }

    private List<PlayerDetailData> setHistorySeasonData(int code) {
        List<PlayerDetailData> historySeasonList = Lists.newArrayList();
        Arrays.stream(HistorySeason.values()).forEach(o ->
                historySeasonList.add(this.setSeasonData(o.getSeason(), code)));
        return historySeasonList;
    }

    @Cacheable(value = "qryAllPlayers", key = "#season", cacheManager = "apiCacheManager", unless = "#result == null")
    @Override
    public List<PlayerInfoData> qryAllPlayers(String season) {
        List<PlayerInfoData> list = Lists.newArrayList();
        this.playerService.list().forEach(o -> list.add(this.initPlayerInfo(season, o)));
        return list;
    }

    @Cacheable(value = "getPlayerByElememt", key = "#season+'::'+#element", unless = "#result == null")
    @Override
    public PlayerEntity getPlayerByElememt(String season, int element) {
        return this.redisCacheSerive.getPlayerByElememt(season, element);
    }

    @Cacheable(value = "getPlayerStatByElement", key = "#season+'::'+#element", unless = "#result == null")
    @Override
    public PlayerStatEntity getPlayerStatByElement(String season, int element) {
        return this.redisCacheSerive.getPlayerStatByElement(season, element);
    }

    @Cacheable(value = "getPlayerValueByChangeDay", key = "#changeDay", unless = "#result == null")
    @Override
    public List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay) {
        return this.redisCacheSerive.getPlayerValueByChangeDay(changeDay);
    }

    /**
     * @implNote entry
     */
    @Cacheable(value = "qryEntryInfo", key = "#season+'::'+#entry", unless = "#result == null")
    @Override
    public EntryInfoEntity qryEntryInfo(String season, int entry) {
        MybatisPlusConfig.season.set(season);
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        MybatisPlusConfig.season.remove();
        if (entryInfoEntity != null) {
            return entryInfoEntity;
        }
        if (!StringUtils.equals(CommonUtils.getCurrentSeason(), season)) {
            return new EntryInfoEntity();
        }
        EntryRes entryRes = this.getEntry(entry);
        return new EntryInfoEntity()
                .setEntry(entryRes.getId())
                .setEntryName(entryRes.getName())
                .setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName())
                .setRegion(entryRes.getPlayerRegionName())
                .setStartedEvent(entryRes.getStartedEvent())
                .setOverallPoints(entryRes.getSummaryOverallPoints())
                .setOverallRank(entryRes.getSummaryOverallRank())
                .setBank(entryRes.getLastDeadlineBank())
                .setTeamValue(entryRes.getLastDeadlineValue())
                .setTotalTransfers(entryRes.getLastDeadlineTotalTransfers());
    }

    @Cacheable(value = "getEntry", key = "#entry", unless = "#result == null")
    @Override
    public EntryRes getEntry(int entry) {
        return this.staticSerive.getEntry(entry).orElse(null);
    }

    @Cacheable(value = "getUserPicks", key = "#event+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result == null")
    @Override
    public UserPicksRes getUserPicks(int event, int entry) {
        return this.staticSerive.getUserPicks(event, entry).orElse(null);
    }

    @Cacheable(value = "getUserHistory", key = "#entry", cacheManager = "apiCacheManager", unless = "#result == null")
    @Override
    public UserHistoryRes getUserHistory(int entry) {
        return this.staticSerive.getUserHistory(entry).orElse(null);
    }

    @Override
    public int getLastEvent() {
        return this.getCurrentEvent() - 1;
    }

    /**
     * @implNote event
     */
    @Override
    public int getCurrentEvent() {
        int event = 1;
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
    public int getNextEvent() {
        return this.getCurrentEvent() + 1;
    }

    @Cacheable(value = "getDeadlineByEvent", key = "#season+'::'+#event", unless = "#result == null")
    @Override
    public String getDeadlineByEvent(String season, int event) {
        return this.redisCacheSerive.getDeadlineByEvent(season, event);
    }

    @Cacheable(value = "getMatchDayByEvent", key = "#event", unless = "#result == null")
    @Override
    public List<LocalDate> getMatchDayByEvent(int event) {
        List<LocalDate> matchDayList = Lists.newArrayList();
        this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event).forEach(eventFixtureEntity -> {
                    String matchDay = StringUtils.substringBefore(eventFixtureEntity.getKickoffTime(), " ");
                    LocalDate date = LocalDate.parse(matchDay);
                    if (!matchDayList.contains(date)) {
                        matchDayList.add(date);
                    }
                }
        );
        return matchDayList
                .stream()
                .sorted(LocalDate::compareTo)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "getMatchDayTimeByEvent", key = "#event", unless = "#result == null")
    @Override
    public List<LocalDateTime> getMatchDayTimeByEvent(int event) {
        List<LocalDateTime> matchDayTimeList = Lists.newArrayList();
        this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event).forEach(eventFixtureEntity -> {
                    String kickoffTime = eventFixtureEntity.getKickoffTime().replace(" ", "T");
                    LocalDateTime dateTime = LocalDateTime.parse(kickoffTime);
                    if (!matchDayTimeList.contains(dateTime)) {
                        matchDayTimeList.add(dateTime);
                    }
                }
        );
        return matchDayTimeList
                .stream()
                .sorted(LocalDateTime::compareTo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isMatchDay(int event) {
        List<LocalDate> matchDayList = this.getMatchDayByEvent(event);
        return matchDayList.contains(LocalDate.now());
    }

    @Override
    public boolean isMatchDayTime(int event) {
        List<LocalDateTime> matchDayTimeList = this.getMatchDayTimeByEvent(event);
        LocalDateTime start = matchDayTimeList.stream().min(LocalDateTime::compareTo).orElse(null);
        LocalDateTime last = matchDayTimeList.stream().max(LocalDateTime::compareTo).orElse(null);
        if (start == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(start) && LocalDateTime.now().isBefore(last);
    }

    @Override
    public boolean isLastMatchDay(int event) {
        LocalDate eventLastDay = this.getMatchDayByEvent(event)
                .stream()
                .max(LocalDate::compareTo)
                .orElse(null);
        if (eventLastDay == null) {
            return false;
        }
        return eventLastDay.isEqual(LocalDate.now());
    }

    /**
     * @implNote team
     */
    @Cacheable(value = "getTeamNameMap", key = "#season", unless = "#result == null")
    @Override
    public Map<String, String> getTeamNameMap(String season) {
        return this.redisCacheSerive.getTeamNameMap(season);
    }

    @Cacheable(value = "getTeamShortNameMap", key = "#season", unless = "#result == null")
    @Override
    public Map<String, String> getTeamShortNameMap(String season) {
        return this.redisCacheSerive.getTeamShortNameMap(season);
    }

    @Cacheable(value = "getPositionMap")
    @Override
    public Map<String, String> getPositionMap() {
        return this.redisCacheSerive.getPositionMap();
    }

    /**
     * @apiNote fixture
     */
    @Cacheable(value = "getEventFixtureByEvent", key = "#season+'::'+#event", unless = "#result == null")
    @Override
    public List<EventFixtureEntity> getEventFixtureByEvent(String season, int event) {
        return this.redisCacheSerive.getEventFixtureByEvent(season, event);
    }

    @Cacheable(value = "getEventFixtureByTeamId", key = "#season+'::'+#teamId", unless = "#result == null")
    @Override
    public Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId) {
        return this.redisCacheSerive.getEventFixtureByTeamId(season, teamId);
    }

    @Cacheable(value = "qryGroupFixtureListById", key = "#tournamentId", unless = "#result.size() eq 0")
    @Override
    public List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId) {
        List<TournamentGroupFixtureData> list = Lists.newArrayList();
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getId, tournamentId)
                .eq(TournamentInfoEntity::getState, 1));
        if (tournamentInfoEntity == null) {
            return list;
        }
        if (!StringUtils.equals(tournamentInfoEntity.getGroupMode(), GroupMode.Battle_race.name())) {
            return list;
        }
        int currentGw = this.getCurrentEvent();
        List<TournamentBattleGroupResultEntity> battleGroupResultEntityList = this.tournamentBattleGroupResultService.list(new QueryWrapper<TournamentBattleGroupResultEntity>().lambda()
                .eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId)
                .orderByAsc(TournamentBattleGroupResultEntity::getEvent)
                .orderByAsc(TournamentBattleGroupResultEntity::getGroupId));
        // entryInfo map
        List<Integer> entryList = battleGroupResultEntityList
                .stream()
                .map(TournamentBattleGroupResultEntity::getHomeEntry)
                .collect(Collectors.toList());
        entryList.addAll(battleGroupResultEntityList
                .stream()
                .map(TournamentBattleGroupResultEntity::getAwayEntry)
                .collect(Collectors.toList()));
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        // return list
        list = battleGroupResultEntityList
                .stream()
                .collect(new BattleGroupResultCollector());
        list.forEach(o -> {
            o.setTournamentId(tournamentId);
            o.getGroupEventFixtureList()
                    .forEach(i -> i.getEventEntryFixtureList()
                            .forEach(tournamentGroupFixtureData ->
                                    tournamentGroupFixtureData.setShowMessage(this.setBattleFixtureMsg(currentGw, o.getEvent(), entryInfoMap,
                                            tournamentGroupFixtureData.getHomeEntry(), tournamentGroupFixtureData.getAwayEntry(),
                                            tournamentGroupFixtureData.getHomeEntryPoints(), tournamentGroupFixtureData.getAwayEntryPoints()))
                            ));
        });
        return list;
    }

    @Cacheable(value = "qryKnockoutFixtureListById", key = "#tournamentId", unless = "#result == null")
    @Override
    public List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId) {
        List<TournamentKnockoutFixtureData> list = Lists.newArrayList();
        int currentGw = this.getCurrentEvent();
        List<TournamentKnockoutResultEntity> knockoutResultEntityList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .orderByAsc(TournamentKnockoutResultEntity::getEvent)
                .orderByAsc(TournamentKnockoutResultEntity::getMatchId));
        if (CollectionUtils.isEmpty(knockoutResultEntityList)) {
            return list;
        }
        // entryInfo map
        List<Integer> entryList = knockoutResultEntityList
                .stream()
                .map(TournamentKnockoutResultEntity::getHomeEntry)
                .collect(Collectors.toList());
        entryList.addAll(knockoutResultEntityList
                .stream()
                .map(TournamentKnockoutResultEntity::getAwayEntry)
                .collect(Collectors.toList()));
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        // return list
        List<TournamentKnockoutFixtureData> knockoutFixtureList = knockoutResultEntityList
                .stream()
                .collect(new KnockoutResultCollector());
        knockoutFixtureList
                .stream()
                .filter(o -> o.getEvent() <= currentGw)
                .forEach(o -> {
                    o.setTournamentId(tournamentId);
                    o.getKnockoutEventFixtureList()
                            .forEach(i -> i.setShowMessage(this.setBattleFixtureMsg(currentGw, o.getEvent(), entryInfoMap,
                                    i.getHomeEntry(), i.getAwayEntry(), i.getHomeEntryPoints(), i.getAwayEntryPoints()))
                            );
                    list.add(o);
                });
        return list;
    }

    private String setBattleFixtureMsg(int currentGw, int event, Map<Integer, EntryInfoEntity> entryInfoMap,
                                       int homeEntry, int awayEntry, int homeEntryPoints, int awayEntryPoints) {
        // home
        String homeMsg = this.setBattleEntryMsg(entryInfoMap, homeEntry);
        // away
        String awayMsg = this.setBattleEntryMsg(entryInfoMap, awayEntry);
        // points
        String pointsMsg = this.setBattlePointsMsg(currentGw, event, homeEntryPoints, awayEntryPoints);
        return homeMsg + " " + pointsMsg + " " + awayMsg;
    }

    private String setBattleEntryMsg(Map<Integer, EntryInfoEntity> entryInfoMap, int entry) {
        String entryName = "";
        String playerName = "";
        if (entry < 0) {
            entryName = "平均分";
            playerName = "";
        } else if (entry == 0) {
            entryName = "轮空";
            playerName = "";
        } else {
            EntryInfoEntity entryInfoEntity = entryInfoMap.get(entry);
            if (entryInfoEntity != null) {
                entryName = entryInfoEntity.getEntryName();
                playerName = entryInfoEntity.getPlayerName();
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

    private String setBattlePointsMsg(int currentGw, int event, int homeEntryPoints, int awayEntryPoints) {
        if (event >= currentGw) {
            return " vs ";
        } else {
            return homeEntryPoints + "-" + awayEntryPoints;
        }
    }

    /**
     * @implNote event_live
     */
    @Cacheable(value = "qryEventLiveAll", key = "#season+'::'+#element", cacheManager = "apiCacheManager", unless = "#result == null")
    @Override
    public List<EventLiveEntity> qryEventLiveAll(String season, int element) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getElement, element));
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Override
    public List<EventLiveEntity> qryEventLive(String season, int event, int element) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event)
                .eq(EventLiveEntity::getElement, element));
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Cacheable(value = "qryPickListFromPicks", key = "#season+'::'+#picks", unless = "#result == null")
    @Override
    public List<EntryPickData> qryPickListFromPicks(String season, @NotNull String picks) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return Lists.newArrayList();
        }
        Map<String, String> positonMap = this.getPositionMap();
        pickList.forEach(pick -> {
            PlayerEntity playerEntity = this.getPlayerByElememt(season, pick.getElement());
            if (playerEntity != null) {
                pick.setElementTypeName(positonMap.get(String.valueOf(playerEntity.getElementType())))
                        .setWebName(playerEntity.getWebName());
            }
        });
        return pickList;
    }

    /**
     * @implNote event_result
     */
    @Cacheable(value = "qryEntryResult", key = "#season+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result == null")
    @Override
    public List<EntryEventResultData> qryEntryResult(String season, int entry) {
        List<EntryEventResultData> list = Lists.newArrayList();
        if (StringUtils.equals(season, "1920")) {
            IntStream.range(1, 48).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
        } else {
            IntStream.range(1, 39).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
        }
        return list;
    }

    @Cacheable(value = "qryEntryEventResult", key = "#season+'::'+#event+'::'+#entry", cacheManager = "apiCacheManager", unless = "#result == null")
    @Override
    public EntryEventResultData qryEntryEventResult(String season, int event, int entry) {
        return this.setEntryEventResult(season, event, entry);
    }

    private EntryEventResultData setEntryEventResult(String season, int event, int entry) {
        EntryEventResultData entryEventResultData = new EntryEventResultData();
        MybatisPlusConfig.season.set(season);
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
        MybatisPlusConfig.season.remove();
        if (entryEventResultEntity == null) {
            return entryEventResultData;
        }
        entryEventResultData
                .setEntry(entry)
                .setEvent(event)
                .setPoints(entryEventResultEntity.getEventPoints())
                .setTransfers(entryEventResultEntity.getEventTransfers())
                .setTransfersCost(entryEventResultEntity.getEventTransfersCost())
                .setNetPoints(entryEventResultEntity.getEventNetPoints())
                .setBenchPoints(entryEventResultEntity.getEventBenchPoints())
                .setRank(entryEventResultEntity.getEventRank())
                .setChip(entryEventResultEntity.getEventChip())
                .setPicks(this.qryPickListFromPicks(entryEventResultEntity.getEventPicks()));
        return entryEventResultData;
    }

    /**
     * @apiNote tournament
     */
    @Override
    public List<TournamentInfoEntity> qryAllTournamentList() {
        return this.tournamentInfoService.list();
    }

    @Override
    public TournamentInfoEntity qryTournamentInfoById(int tournamentId) {
        return this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getId, tournamentId)
                .eq(TournamentInfoEntity::getState, 1));
    }

    @Cacheable(value = "qryEntryListByTournament", key = "#tournamentId", unless = "#result.size() gt 0")
    @Override
    public List<Integer> qryEntryListByTournament(int tournamentId) {
        return this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<TournamentKnockoutEntity> qryKnockoutListByTournamentId(int tournamentId) {
        return this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId));
    }

    @Cacheable(value = "qryKnockoutResultByTournament", key = "#tournamentId", unless = "#result == null")
    @Override
    public List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId) {
        List<TournamentKnockoutResultData> knockoutResultDataList = Lists.newArrayList();
        // knockout
        Map<Integer, TournamentKnockoutEntity> knockoutMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutEntity::getRound, 1))
                .stream()
                .collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, v -> v));
        if (CollectionUtils.isEmpty(knockoutMap)) {
            return knockoutResultDataList;
        }
        // knouckout_result, every match_id return a knockoutResultData
        knockoutMap.keySet().forEach(matchId -> {
            List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                    .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                    .eq(TournamentKnockoutResultEntity::getMatchId, matchId));
            // knockoutResultData
            TournamentKnockoutResultData knockoutResultData = new TournamentKnockoutResultData();
            TournamentKnockoutResultEntity o = knockoutResultList.get(0);
            knockoutResultData
                    .setTournamentId(tournamentId)
                    .setRound(knockoutMap.get(o.getMatchId()).getRound())
                    .setEvent(o.getEvent())
                    .setPlayAgainstId(o.getPlayAginstId())
                    .setMatchId(o.getMatchId())
                    .setHomeEntry(o.getHomeEntry())
                    .setAwayEntry(o.getAwayEntry())
                    .setHomeEntryName(this.getKnockoutResultEntryName(o.getHomeEntry()))
                    .setAwayEntryName(this.getKnockoutResultEntryName(o.getAwayEntry()))
                    .setHomeEntryNetPoint(this.calcKnockoutResultDataNetPoint(knockoutResultList, "home"))
                    .setAwayEntryNetPoint(this.calcKnockoutResultDataNetPoint(knockoutResultList, "away"))
                    .setHomeEntryRank(o.getHomeEntryRank())
                    .setAwayEntryRank(o.getAwayEntryRank())
                    .setMatchWinner(o.getMatchWinner());
            // match informantion
            Map<Integer, String> entryNameMap = ImmutableMap.of(knockoutResultData.getHomeEntry(), knockoutResultData.getHomeEntryName(),
                    knockoutResultData.getAwayEntry(), knockoutResultData.getAwayEntryName());
            knockoutResultData.setMatchInfo(this.setRoundMatchInformation(knockoutResultList, entryNameMap));
            knockoutResultDataList.add(knockoutResultData);
        });
        return knockoutResultDataList;
    }

    private String getKnockoutResultEntryName(int entry) {
        if (entry < 0) {
            return "BYE";
        }
        EntryInfoEntity entryInfoEntity = this.qryEntryInfo(entry);
        if (entryInfoEntity == null) {
            return "";
        }
        return entryInfoEntity.getEntryName();
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
        knockoutResultList.forEach(o ->
                builder
                        .append(entryNameMap.get(o.getHomeEntry()))
                        .append("（").append(o.getHomeEntryNetPoints()).append("）")
                        .append("- ")
                        .append("（").append(o.getAwayEntryNetPoints()).append("）")
                        .append(entryNameMap.get(o.getAwayEntry()))
        );
        return builder.toString();
    }

    @Cacheable(value = "qryZjTournamentCaptain", key = "#tournamentId")
    @Override
    public List<ZjTournamentCaptainData> qryZjTournamentCaptain(int tournamentId) {
        List<ZjTournamentCaptainData> list = Lists.newArrayList();
        this.zjTournamentCaptainService.list(new QueryWrapper<ZjTournamentCaptainEntity>().lambda()
                .eq(ZjTournamentCaptainEntity::getTournamentId, tournamentId))
                .forEach(o ->
                        list.add(BeanUtil.copyProperties(o, ZjTournamentCaptainData.class)));
        return list;
    }

    @Cacheable(value = "qryZjTournamentGroupNameMap", key = "#tournamentId+'::'+#groupNum")
    @Override
    public Map<Integer, String> qryZjTournamentGroupNameMap(int tournamentId, int groupNum) {
        List<Integer> groupList = Lists.newArrayList();
        IntStream.range(1, groupNum + 1).forEach(groupList::add);
        Map<Integer, String> groupNameMap = Maps.newHashMap();
        this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .in(TournamentGroupEntity::getGroupId, groupList))
                .forEach(o -> groupNameMap.put(o.getGroupId(), o.getGroupName()));
        return groupNameMap;
    }

    @Cacheable(value = "qryTournamentRankByGroupId", key = "#tournamentId+'::'+#groupNum+'::'+#currentGroupId")
    @Override
    public int qryTournamentRankByGroupId(int tournamentId, int groupNum, int currentGroupId) {
        Map<Integer, Integer> map = Maps.newHashMap();
        BiMap<Integer, Integer> groupPointsMap = HashBiMap.create();
        // group Entry
        for (int groupId = 1; groupId < groupNum + 1; groupId++) {
            int groupTotalPoints = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                    .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                    .eq(TournamentGroupEntity::getGroupId, groupId))
                    .stream()
                    .mapToInt(TournamentGroupEntity::getTotalPoints)
                    .sum();
            groupPointsMap.put(groupId, groupTotalPoints);
        }
        // group tournament rank
        List<Integer> pointsList = groupPointsMap.values()
                .stream()
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        groupPointsMap = groupPointsMap.inverse();
        for (int i = 0; i < pointsList.size(); i++) {
            int totalPoints = pointsList.get(i);
            map.put(groupPointsMap.get(totalPoints), i + 1);
        }
        return map.getOrDefault(currentGroupId, 0);
    }

    @Cacheable(value = "qryGroupEntryInfoList", key = "#tournamentId+'::'+#groupId")
    @Override
    public List<EntryInfoData> qryGroupEntryInfoList(int tournamentId, int groupId) {
        List<EntryInfoData> list = Lists.newArrayList();
        List<Integer> entryList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .eq(TournamentGroupEntity::getGroupId, groupId))
                .stream()
                .map(TournamentGroupEntity::getEntry)
                .collect(Collectors.toList());
        entryList.forEach(entry -> {
            EntryInfoEntity entryInfoEntity = this.qryEntryInfo(entry);
            if (entryInfoEntity != null) {
                list.add(BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class));
            }
        });
        return list;
    }

    @Cacheable(value = "qryZjTournamentGroupEntryMap", key = "#tournamentId+'::'+#groupNum")
    @Override
    public Map<Integer, String> qryZjTournamentGroupEntryMap(int tournamentId, int groupNum) {
        List<Integer> groupList = Lists.newArrayList();
        IntStream.range(1, groupNum + 1).forEach(groupList::add);
        return this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .in(TournamentGroupEntity::getGroupId, groupList))
                .stream()
                .collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getGroupName));
    }

    /**
     * @apiNote report
     */
    @Override
    public List<String> qryTeamSelectStatList() {
        return this.teamSelectStatService.getBaseMapper().qryLeagueNameList();
    }

    /**
     * @implNote live, cannot be cached
     */
    @Override
    public Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap() {
        return this.redisCacheSerive.getEventLiveFixtureMap();
    }

    @Override
    public Map<String, EventLiveEntity> getEventLiveByEvent(int event) {
        return this.redisCacheSerive.getEventLiveByEvent(event);
    }

    @Override
    public Map<String, Map<String, Integer>> getLiveBonusCacheMap() {
        return this.redisCacheSerive.getLiveBonusCacheMap();
    }

    @Override
    public List<LiveMatchData> qryLiveMatchList() {
        List<LiveMatchData> list = Lists.newArrayList();
        Map<String, Map<String, List<LiveFixtureData>>> eventLiveFixtureMap = this.redisCacheSerive.getEventLiveFixtureMap();
        eventLiveFixtureMap.keySet().forEach(teamId ->
                eventLiveFixtureMap.get(teamId).forEach((status, fixtureList) -> {
                    if (StringUtils.equals(status, MatchPlayStatus.Not_Start.name())) {
                        return;
                    }
                    fixtureList.forEach(o -> {
                        if (!o.isWasHome()) {
                            return;
                        }
                        list.add(new LiveMatchData()
                                .setHomeTeamId(o.getTeamId())
                                .setHomeTeamName(o.getTeamName())
                                .setHomeTeamShortName(o.getTeamShortName())
                                .setHomeScore(o.getTeamScore())
                                .setAwayTeamId(o.getAgainstId())
                                .setAwayTeamName(o.getAgainstName())
                                .setAwayTeamShortName(o.getAgainstShortName())
                                .setAwayScore(o.getAgainstTeamScore())
                                .setKickoffTime(o.getKickoffTime())
                        );
                    });
                }));
        return list
                .stream()
                .sorted(Comparator.comparing(LiveMatchData::getKickoffTime).reversed())
                .collect(Collectors.toList());
    }


}
