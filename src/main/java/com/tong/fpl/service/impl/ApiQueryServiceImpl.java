package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.ScoutEntity;
import com.tong.fpl.domain.entity.TeamEntity;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.ScoutService;
import com.tong.fpl.service.db.TeamService;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.RedisUtils;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** Create by tong on 2021/5/10 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiQueryServiceImpl implements IApiQueryService {

  private final IQueryService queryService;
  private final IRedisCacheService redisCacheService;
  private final TeamService teamService;
  private final PlayerService playerService;
  private final ScoutService scoutService;

  /** @implNote common */
  @Cacheable(
      value = "api::qryCurrentEventAndNextUtcDeadline",
      cacheManager = "apiCacheManager",
      unless = "#result.size() != 2")
  @Override
  public Map<String, String> qryCurrentEventAndNextUtcDeadline() {
    Map<String, String> map = Maps.newHashMap();
    int event = this.queryService.getCurrentEvent();
    map.put("event", String.valueOf(event));
    String utcDeadline = this.queryService.getUtcDeadlineByEvent(event + 1);
    map.put("utcDeadline", utcDeadline);
    return map;
  }

  /** @implNote entry */
  @Cacheable(
      value = "api::qryEntryInfoData",
      key = "#entry",
      cacheManager = "apiCacheManager",
      unless = "#result.entry eq 0")
  @Override
  public EntryInfoData qryEntryInfoData(int entry) {
    EntryInfoEntity entryInfoEntity =
        this.queryService.qryEntryInfo(CommonUtils.getCurrentSeason(), entry);
    if (entryInfoEntity == null) {
      return new EntryInfoData();
    }
    return BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class);
  }

  /** @implNote league */

  /** @implNote live(do not cache) */
  @Override
  public List<LiveMatchData> qryLiveMatchDataByStatus(String playStatus) {
    List<LiveMatchData> list = Lists.newArrayList();
    // prepare
    int event = this.queryService.getCurrentEvent();
    Collection<EventLiveEntity> eventLiveList =
        this.queryService.getEventLiveByEvent(event).values();
    Map<Integer, PlayerEntity> playerMap =
        this.playerService.list().stream()
            .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
    Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
    // collect
    Map<String, Map<String, List<LiveFixtureData>>> eventLiveFixtureMap =
        this.redisCacheService.getEventLiveFixtureMap();
    eventLiveFixtureMap
        .keySet()
        .forEach(
            teamId ->
                eventLiveFixtureMap
                    .get(teamId)
                    .forEach(
                        (status, fixtureList) -> {
                          if (!StringUtils.equalsIgnoreCase(status, playStatus)) {
                            return;
                          }
                          fixtureList.forEach(
                              o -> {
                                if (!o.isWasHome()) {
                                  return;
                                }
                                list.add(
                                    new LiveMatchData()
                                        .setMatchId(list.size() + 1)
                                        .setHomeTeamId(o.getTeamId())
                                        .setHomeTeamName(o.getTeamName())
                                        .setHomeTeamShortName(o.getTeamShortName())
                                        .setHomeScore(o.getTeamScore())
                                        .setHomeTeamDataList(
                                            this.qryLiveTeamData(
                                                o.getTeamId(),
                                                eventLiveList,
                                                playerMap,
                                                teamShortNameMap))
                                        .setAwayTeamId(o.getAgainstId())
                                        .setAwayTeamName(o.getAgainstName())
                                        .setAwayTeamShortName(o.getAgainstShortName())
                                        .setAwayScore(o.getAgainstTeamScore())
                                        .setAwayTeamDataList(
                                            this.qryLiveTeamData(
                                                o.getAgainstId(),
                                                eventLiveList,
                                                playerMap,
                                                teamShortNameMap))
                                        .setKickoffTime(o.getKickoffTime()));
                              });
                        }));
    return list.stream()
        .sorted(Comparator.comparing(LiveMatchData::getKickoffTime).reversed())
        .collect(Collectors.toList());
  }

  private List<ElementEventResultData> qryLiveTeamData(
      int teamId,
      Collection<EventLiveEntity> eventLiveList,
      Map<Integer, PlayerEntity> playerMap,
      Map<String, String> teamShortNameMap) {
    List<ElementEventResultData> list = Lists.newArrayList();
    // team data
    Map<Integer, Integer> liveBonusMap = this.getLiveBonusMap(teamId);
    eventLiveList.forEach(
        o -> {
          if (o.getTeamId() != teamId || o.getMinutes() <= 0) {
            return;
          }
          ElementEventResultData elementEventResultData = new ElementEventResultData();
          elementEventResultData
              .setEvent(o.getEvent())
              .setElement(o.getElement())
              .setWebName(
                  playerMap.containsKey(o.getElement())
                      ? playerMap.get(o.getElement()).getWebName()
                      : "")
              .setElementType(o.getElementType())
              .setElementTypeName(Position.getNameFromElementType(o.getElementType()))
              .setTeamId(
                  playerMap.containsKey(o.getElement())
                      ? playerMap.get(o.getElement()).getTeamId()
                      : 0)
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
                .setTotalPoints(
                    elementEventResultData.getTotalPoints() + elementEventResultData.getBonus());
          }
          elementEventResultData.setTeamShortName(
              teamShortNameMap.getOrDefault(
                  String.valueOf(elementEventResultData.getTeamId()), ""));
          list.add(elementEventResultData);
        });
    return list;
  }

  private Map<Integer, Integer> getLiveBonusMap(int teamId) {
    Map<Integer, Integer> map = Maps.newHashMap();
    this.queryService
        .getLiveBonusCacheMap()
        .forEach(
            (team, list) -> {
              if (!StringUtils.equals(team, String.valueOf(teamId))) {
                return;
              }
              list.forEach((element, bonus) -> map.put(Integer.valueOf(element), bonus));
            });
    return map;
  }

  /** @implNote player */
  @Cacheable(
      value = "api::qryPlayerInfoByElementType",
      key = "#elementType",
      cacheManager = "apiCacheManager",
      unless = "#result.size() eq 0")
  @Override
  public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType) {
    LinkedHashMap<String, List<PlayerInfoData>> map = Maps.newLinkedHashMap();
    Multimap<String, PlayerInfoData> multimap = HashMultimap.create();
    // prepare
    Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
    Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
    // init
    this.playerService
        .list(
            new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getElementType, elementType))
        .forEach(
            o -> {
              PlayerInfoData data = BeanUtil.copyProperties(o, PlayerInfoData.class);
              data.setElementTypeName(Position.getNameFromElementType(data.getElementType()))
                  .setTeamName(teamNameMap.getOrDefault(String.valueOf(data.getTeamId()), ""))
                  .setTeamShortName(
                      teamShortNameMap.getOrDefault(String.valueOf(data.getTeamId()), ""))
                  .setPrice(data.getPrice() / 10);
              multimap.put(data.getTeamShortName(), data);
            });
    // collect
    List<String> shortNameSortedList =
        multimap.keySet().stream()
            .sorted(Comparator.comparing(String::toUpperCase))
            .collect(Collectors.toList());
    shortNameSortedList.forEach(
        team ->
            map.put(
                team,
                multimap.get(team).stream()
                    .sorted(Comparator.comparing(PlayerInfoData::getPrice).reversed())
                    .collect(Collectors.toList())));
    return map;
  }

  @Cacheable(
      value = "api::qryPlayerDetailData",
      key = "#element",
      cacheManager = "apiCacheManager",
      unless = "#result.element eq 0")
  @Override
  public PlayerDetailData qryPlayerDetailData(int element) {
    return this.queryService.qryPlayerDetailData(element);
  }

  @Cacheable(
      value = "api::qryTeamFixtureByShortName",
      key = "#shortName",
      cacheManager = "apiCacheManager",
      unless = "#result.size() eq 0")
  @Override
  public Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName) {
    int teamId =
        this.teamService
            .getOne(new QueryWrapper<TeamEntity>().lambda().eq(TeamEntity::getShortName, shortName))
            .getId();
    return this.queryService.getEventFixtureByTeamId(teamId);
  }

  /** @implNote report */

  /** @implNote scout */
  @Cacheable(
      value = "api::qryScoutEntry",
      cacheManager = "apiCacheManager",
      unless = "#result.size() eq 0")
  @Override
  public Map<String, String> qryScoutEntry() {
    Map<String, String> map = Maps.newHashMap();
    RedisUtils.getHashByKey("scoutEntry").forEach((k, v) -> map.put(k.toString(), v.toString()));
    return map;
  }

  // do not cache
  @Override
  public ScoutData qryEventScoutPickResult(int event, int entry) {
    ScoutEntity scoutEntity =
        this.scoutService.getOne(
            new QueryWrapper<ScoutEntity>()
                .lambda()
                .eq(ScoutEntity::getEvent, event)
                .eq(ScoutEntity::getEntry, entry));
    if (scoutEntity == null) {
      return new ScoutData();
    }
    return this.initScoutData(scoutEntity, this.queryService.getTeamShortNameMap());
  }

  // do not cache
  @Override
  public List<ScoutData> qryEventScoutResult(int event) {
    Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
    return this.scoutService
        .list(new QueryWrapper<ScoutEntity>().lambda().eq(ScoutEntity::getEvent, event))
        .stream()
        .map(o -> this.initScoutData(o, teamShortNameMap))
        .collect(Collectors.toList());
  }

  private ScoutData initScoutData(ScoutEntity scoutEntity, Map<String, String> teamShortNameMap) {
    return new ScoutData()
        .setEvent(scoutEntity.getEvent())
        .setEntry(scoutEntity.getEntry())
        .setScoutName(scoutEntity.getScoutName())
        .setGkpName(this.queryService.qryPlayerWebNameByElement(scoutEntity.getGkp()))
        .setGkpTeamShortName(
            teamShortNameMap.getOrDefault(String.valueOf(scoutEntity.getGkpTeamId()), ""))
        .setDefName(this.queryService.qryPlayerWebNameByElement(scoutEntity.getDef()))
        .setDefTeamShortName(
            teamShortNameMap.getOrDefault(String.valueOf(scoutEntity.getDefTeamId()), ""))
        .setMidName(this.queryService.qryPlayerWebNameByElement(scoutEntity.getMid()))
        .setMidTeamShortName(
            teamShortNameMap.getOrDefault(String.valueOf(scoutEntity.getMidTeamId()), ""))
        .setFwdName(this.queryService.qryPlayerWebNameByElement(scoutEntity.getFwd()))
        .setFwdTeamShortName(
            teamShortNameMap.getOrDefault(String.valueOf(scoutEntity.getFwdTeamId()), ""))
        .setCaptainName(this.queryService.qryPlayerWebNameByElement(scoutEntity.getCaptain()))
        .setCaptainTeamShortName(
            teamShortNameMap.getOrDefault(String.valueOf(scoutEntity.getCaptainTeamId()), ""))
        .setReason(scoutEntity.getReason());
  }

  /** @implNote tournament */
}
