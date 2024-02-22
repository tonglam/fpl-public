package com.tong.fpl.service.impl;

import com.google.common.collect.*;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.special.*;
import com.tong.fpl.service.*;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tong on 2022/02/25
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecialTournamentImpl implements ISpecialTournamentService {

    private final IRedisCacheService redisCacheService;
    private final IQueryService queryService;
    private final IInterfaceService interfaceService;
    private final ILiveService liveService;

    @Override
    public List<Integer> getTournamentEntryList(int tournamentId) {
        String key = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_GROUP, tournamentId);
        Map<String, List<EntryInfoData>> map = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> map.put(k.toString(), (List<EntryInfoData>) v));
        List<Integer> entryList = Lists.newArrayList(1713);
        map.values().forEach(list -> list.forEach(o -> entryList.add(o.getEntry())));
        return entryList;
    }

    @Override
    public void insertGroupInfo(int tournamentId, Map<String, String> groupInfoMap) {
        String key = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_INFO, tournamentId);
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        valueMap.putAll(groupInfoMap);
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertGroupEntry(int tournamentId, List<GroupInfoData> groupInfoList) {
        // collect
        Table<String, String, EntryInfoData> table = HashBasedTable.create(); // groupId -> shuffled_group_id -> data
        groupInfoList.forEach(o ->
                o.getEntryList().forEach(i -> {
                    com.tong.fpl.domain.letletme.entry.EntryInfoData entryInfoData = this.queryService.qryEntryInfo(i.getEntry());
                    EntryInfoData data = new EntryInfoData()
                            .setGroupId(o.getGroupId())
                            .setShuffledGroupId(i.getShuffledGroupId())
                            .setEntry(entryInfoData.getEntry())
                            .setEntryName(entryInfoData.getEntryName())
                            .setPlayerName(entryInfoData.getPlayerName())
                            .setOverallPoints(entryInfoData.getOverallPoints())
                            .setOverallRank(entryInfoData.getOverallRank());
                    table.put(String.valueOf(o.getGroupId()), String.valueOf(i.getShuffledGroupId()), data);
                }));
        // group_info
        String groupKey = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_GROUP, tournamentId);
        RedisUtils.removeCacheByKey(groupKey);
        Map<String, Map<String, Object>> groupCacheMap = Maps.newHashMap();
        Map<String, Object> groupValueMap = Maps.newHashMap();
        table.rowKeySet().forEach(groupId -> {
            List<EntryInfoData> groupList = Lists.newArrayList();
            groupList.addAll(table.row(groupId).values());
            groupValueMap.put(groupId, groupList);
        });
        groupCacheMap.put(groupKey, groupValueMap);
        RedisUtils.pipelineHashCache(groupCacheMap, -1, null);
        // shuffled_group_info
        String shuffledKey = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_SHUFFLED_GROUP, tournamentId);
        RedisUtils.removeCacheByKey(shuffledKey);
        Map<String, Map<String, Object>> shuffledCacheMap = Maps.newHashMap();
        Map<String, Object> shuffledValueMap = Maps.newHashMap();
        table.columnKeySet().forEach(shuffleGroupId -> {
            List<EntryInfoData> shuffledList = Lists.newArrayList();
            shuffledList.addAll(table.column(shuffleGroupId).values());
            shuffledValueMap.put(shuffleGroupId, shuffledList);
        });
        shuffledCacheMap.put(shuffledKey, shuffledValueMap);
        RedisUtils.pipelineHashCache(shuffledCacheMap, -1, null);
    }

    @Override
    public void insertEntryEventResult(int tournamentId, int event) {
        List<EventResultData> list = Lists.newArrayList();
        // prepare
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        Map<String, EventLiveEntity> eventLiveMap = this.queryService.getEventLiveByEvent(event);
        // get group data
        String groupKey = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_GROUP, tournamentId);
        List<EntryInfoData> entryInfoList = Lists.newArrayList();
        RedisUtils.getHashByKey(groupKey).values().forEach(o -> entryInfoList.addAll((List<EntryInfoData>) o));
        // entry_event_result
        List<EventResultData> resultList = Lists.newArrayList();
        entryInfoList.forEach(o -> {
            LiveCalcData liveCalcData = this.liveService.calcLivePointsByEntry(event, o.getEntry());
            if (liveCalcData == null) {
                return;
            }
            EventResultData data = new EventResultData()
                    .setGroupId(o.getGroupId())
                    .setShuffledGroupId(o.getShuffledGroupId())
                    .setEvent(event)
                    .setEntry(o.getEntry())
                    .setEntryName(o.getEntryName())
                    .setPlayerName(o.getPlayerName())
                    .setPoints(liveCalcData.getLivePoints())
                    .setNetPoints(liveCalcData.getLiveNetPoints())
                    .setChip(Chip.getChipFromValue(liveCalcData.getChip()).name())
                    .setPlayedCaptain(liveCalcData.getPlayedCaptain())
                    .setCaptainName(webNameMap.getOrDefault(String.valueOf(liveCalcData.getPlayedCaptain()), ""))
                    .setCaptainPoints(0)
                    .setElementEventResultList(liveCalcData.getPickList());
            data
                    .setTotalGoalScored(
                            data.getElementEventResultList()
                                    .stream()
                                    .mapToInt(i -> {
                                        EventLiveEntity elementEventLive = eventLiveMap.getOrDefault(String.valueOf(i.getElement()), null);
                                        if (elementEventLive == null) {
                                            return 0;
                                        }
                                        return elementEventLive.getGoalsScored();
                                    })
                                    .sum()
                    )
                    .setTotalGoalsConceded(
                            data.getElementEventResultList()
                                    .stream()
                                    .mapToInt(i -> {
                                        EventLiveEntity elementEventLive = eventLiveMap.getOrDefault(String.valueOf(i.getElement()), null);
                                        if (elementEventLive == null) {
                                            return 0;
                                        }
                                        return elementEventLive.getGoalsConceded();
                                    })
                                    .sum()
                    );
            resultList.add(data);
        });
        // group points
        Multimap<Integer, EventResultData> groupResultMap = HashMultimap.create();
        resultList.forEach(o -> groupResultMap.put(o.getShuffledGroupId(), o));
        groupResultMap.keySet().forEach(shuffledGroupId -> {
            List<EventResultData> groupResultList = groupResultMap.get(shuffledGroupId)
                    .stream()
                    .sorted((o1, o2) -> {
                        if (o1.getPoints() > o2.getPoints()) {
                            return -1;
                        } else if (o1.getPoints() < o2.getPoints()) {
                            return 1;
                        } else {
                            if (o1.getTotalGoalScored() > o2.getTotalGoalScored()) {
                                return -1;
                            } else if (o1.getTotalGoalScored() < o2.getTotalGoalScored()) {
                                return 1;
                            } else {
                                return Integer.compare(o1.getTotalGoalsConceded(), o2.getTotalGoalsConceded());
                            }
                        }
                    })
                    .collect(Collectors.toList());
            // 一定3个
            list.add(
                    groupResultList.get(0)
                            .setGroupRank(1)
                            .setGroupPoints(3)
            );
            list.add(
                    groupResultList.get(1)
                            .setGroupRank(2)
                            .setGroupPoints(2)

            );
            list.add(
                    groupResultList.get(2)
                            .setGroupRank(3)
                            .setGroupPoints(1)
            );
        });
        // redis
        String key = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_ENTRY_RESULT, tournamentId, event);
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        list.forEach(o -> valueMap.put(String.valueOf(o.getEntry()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertShuffledGroupEventResult(int tournamentId, int event) {
        // shuffled_info
        String shuffledKey = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_SHUFFLED_GROUP, tournamentId);
        Map<Integer, List<EntryInfoData>> shuffledGroupMap = Maps.newHashMap();
        RedisUtils.getHashByKey(shuffledKey).forEach((k, v) -> shuffledGroupMap.put(Integer.parseInt(k.toString()), (List<EntryInfoData>) v));
        // live result
        String resultKey = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_ENTRY_RESULT, tournamentId, event);
        Map<String, EventResultData> resultMap = Maps.newHashMap();
        RedisUtils.getHashByKey(resultKey).forEach((k, v) -> resultMap.put(k.toString(), (EventResultData) v));
        // shuffled data
        Multimap<Integer, ShuffledGroupResultData> map = HashMultimap.create();
        shuffledGroupMap.keySet().forEach(shuffledGroupId -> {
            shuffledGroupMap.get(shuffledGroupId).forEach(o -> {
                int entry = o.getEntry();
                EventResultData eventResultData = resultMap.get(String.valueOf(entry));
                if (eventResultData == null) {
                    return;
                }
                map.put(shuffledGroupId,
                        new ShuffledGroupResultData()
                                .setShuffledGroupId(shuffledGroupId)
                                .setEntry(entry)
                                .setEntryName(eventResultData.getEntryName())
                                .setPlayerName(eventResultData.getPlayerName())
                                .setPoints(eventResultData.getPoints())
                                .setPlayedCaptain(eventResultData.getPlayedCaptain())
                                .setCaptainName(eventResultData.getCaptainName())
                                .setTotalGoalScored(eventResultData.getTotalGoalScored())
                                .setTotalGoalsConceded(eventResultData.getTotalGoalsConceded())
                                .setChip(eventResultData.getChip())
                                .setGroupRank(eventResultData.getGroupRank())
                                .setGroupPoints(eventResultData.getGroupPoints())
                );
            });
        });
        // redis
        String key = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_SHUFFLED_GROUP_RESULT, tournamentId, event);
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        map.keySet().forEach(shuffledGroupId -> valueMap.put(String.valueOf(shuffledGroupId), new ArrayList<>(map.get(shuffledGroupId))));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertGroupEventResult(int tournamentId, int event) {
        List<GroupResultData> list;
        // group_info
        String groupInfoKey = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_INFO, tournamentId);
        Map<String, String> groupInfoMap = Maps.newHashMap();
        RedisUtils.getHashByKey(groupInfoKey).forEach((k, v) -> groupInfoMap.put(k.toString(), v.toString()));
        // get entry_list
        String entryResultKey = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_ENTRY_RESULT, tournamentId, event);
        Multimap<Integer, EventResultData> entryReslutMap = HashMultimap.create(); // group ->data
        RedisUtils.getHashByKey(entryResultKey).values().forEach(o -> {
            EventResultData data = (EventResultData) o;
            entryReslutMap.put(data.getGroupId(), data);
        });
        // group result collect
        List<GroupResultData> groupReslutList = Lists.newArrayList();
        entryReslutMap.keySet().forEach(groupId -> {
            List<EventResultData> groupResultList = new ArrayList<>(entryReslutMap.get(groupId));
            groupReslutList.add(
                    new GroupResultData()
                            .setGroupId(groupId)
                            .setGroupName(groupInfoMap.getOrDefault(String.valueOf(groupId), ""))
                            .setTotalPoints(
                                    groupResultList
                                            .stream()
                                            .mapToInt(EventResultData::getPoints)
                                            .sum()
                            )
                            .setTotalGroupPoints(
                                    groupResultList
                                            .stream()
                                            .mapToInt(EventResultData::getGroupPoints)
                                            .sum()
                            )
                            .setGroupEntryList(groupResultList)
            );
        });
        // sort
        list = groupReslutList
                .stream()
                .sorted(Comparator.comparing(GroupResultData::getTotalGroupPoints)
                        .thenComparing(GroupResultData::getTotalPoints).reversed())
                .collect(Collectors.toList());
        // rank
        for (int i = 1; i < list.size() + 1; i++) {
            list.get(i - 1).setRank(i);
        }
        String groupResultKey = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_GROUP_RESULT, tournamentId, event);
        RedisUtils.removeCacheByKey(groupResultKey);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        list.forEach(o -> valueMap.put(String.valueOf(o.getRank()), o));
        cacheMap.put(groupResultKey, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public Map<Integer, List<ShuffledGroupResultData>> getShuffledGroupResult(int tournamentId, int event) {
        String key = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_SHUFFLED_GROUP_RESULT, tournamentId, event);
        Map<Integer, List<ShuffledGroupResultData>> map = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> {
            List<ShuffledGroupResultData> list = ((List<ShuffledGroupResultData>) v)
                    .stream()
                    .sorted(Comparator.comparing(ShuffledGroupResultData::getGroupRank))
                    .collect(Collectors.toList());
            map.put(Integer.parseInt(k.toString()), list);
        });
        return map;
    }

    @Override
    public List<GroupResultData> getEventGroupResult(int tournamentId, int event) {
        String key = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_GROUP_RESULT, tournamentId, event);
        Map<Integer, GroupResultData> map = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> map.put(Integer.parseInt(k.toString()), (GroupResultData) v));
        return map.values()
                .stream()
                .sorted(Comparator.comparing(GroupResultData::getGroupId))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupRankResultData> getGroupRankResult(int tournamentId, int event) {
        String key = StringUtils.joinWith("::", Constant.SPECIAL_TOURNAMENT_GROUP_RESULT, tournamentId, event);
        Map<Integer, GroupResultData> groupResultMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> groupResultMap.put(Integer.parseInt(k.toString()), (GroupResultData) v));
        return groupResultMap.keySet()
                .stream()
                .map(rank -> {
                    GroupResultData data = groupResultMap.get(rank);
                    return new GroupRankResultData()
                            .setRank(rank)
                            .setGroupName(data.getGroupName())
                            .setTotalGroupPoints(data.getTotalGroupPoints())
                            .setTotalPoints(data.getTotalPoints())
                            .setDetails(this.getGroupPointsDetails(data.getGroupEntryList()));
                })
                .sorted(Comparator.comparing(GroupRankResultData::getRank))
                .collect(Collectors.toList());
    }

    private String getGroupPointsDetails(List<EventResultData> groupEntryList) {
        StringBuilder builder = new StringBuilder();
        groupEntryList.forEach(o -> {
            builder.append(o.getGroupPoints()).append(",");
        });
        return StringUtils.substringBeforeLast(builder.toString(), ",");
    }

    @Override
    public void refreshShuffledGroupResult(int tournamentId, int event) {
        // event_live
        this.interfaceService.getEventLive(event).ifPresent(res -> this.redisCacheService.insertEventLiveCache(event, res));
        // entry_event_result
        this.insertEntryEventResult(tournamentId, event);
        // shuffled_group_result
        this.insertShuffledGroupEventResult(tournamentId, event);
    }

    @Override
    public void refreshEventGroupResult(int tournamentId, int event) {
        // event_live
        this.interfaceService.getEventLive(event).ifPresent(res -> this.redisCacheService.insertEventLiveCache(event, res));
        // entry_event_result
        this.insertEntryEventResult(tournamentId, event);
        // shuffled_group_result
        this.insertGroupEventResult(tournamentId, event);
    }

}
