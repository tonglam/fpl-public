package com.tong.fpl.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.letletme.groupTournament.GroupTournamentResultData;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.special.EventResultData;
import com.tong.fpl.service.*;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2023/11/4
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroupTournamentServiceImpl implements IGroupTournamentService {

    private final IRedisCacheService redisCacheService;
    private final IQueryService queryService;
    private final IInterfaceService interfaceService;
    private final ILiveService liveService;

    @Override
    public void insertGroupEntry(List<GroupTournamentResultData> groupInfoList) {
        String keyPattern = StringUtils.joinWith("::", Constant.GROUP_TOURNAMENT_GROUP, "*");
        int count = RedisUtils.countCacheByKeyPattern(keyPattern);
        int groupTournamentId = count + 1;
        String groupKey = StringUtils.joinWith("::", Constant.GROUP_TOURNAMENT_GROUP, groupTournamentId);
        RedisUtils.removeCacheByKey(groupKey);
        Map<String, Map<String, Object>> groupCacheMap = Maps.newHashMap();
        Map<String, Object> groupValueMap = Maps.newHashMap();
        groupInfoList.forEach(o -> groupValueMap.put(String.valueOf(o.getGroupId()), o));
        groupCacheMap.put(groupKey, groupValueMap);
        RedisUtils.pipelineHashCache(groupCacheMap, -1, null);
    }

    @Override
    public void insertEntryEventResult(int groupTournamentId, int event) {
        // prepare
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        // get group data
        String groupKey = StringUtils.joinWith("::", Constant.GROUP_TOURNAMENT_GROUP, groupTournamentId);
        List<GroupTournamentResultData> groupTournamentResultList = Lists.newArrayList();
        RedisUtils.getHashByKey(groupKey).values().forEach(o -> groupTournamentResultList.add((GroupTournamentResultData) o));
        // get live points
        List<Integer> entryList = groupTournamentResultList.stream().flatMap(o -> o.getEntryList().stream()).toList();
        Map<Integer, LiveCalcData> entryLiveDataMap = this.liveService.calcLivePointsByEntryList(event, entryList)
                .stream()
                .collect(Collectors.toMap(LiveCalcData::getEntry, o -> o));
        // entry_event_result
        Map<Integer, EventResultData> entryEventDataMap = Maps.newHashMap();
        entryLiveDataMap.forEach((entry, liveCalcData) -> {
            EventResultData data = new EventResultData()
                    .setEvent(event)
                    .setEntry(entry)
                    .setEntryName(liveCalcData.getEntryName())
                    .setPlayerName(liveCalcData.getPlayerName())
                    .setPoints(liveCalcData.getLivePoints())
                    .setNetPoints(liveCalcData.getLiveNetPoints())
                    .setChip(Chip.getChipFromValue(liveCalcData.getChip()).name())
                    .setPlayedCaptain(liveCalcData.getPlayedCaptain())
                    .setCaptainName(webNameMap.getOrDefault(String.valueOf(liveCalcData.getPlayedCaptain()), ""))
                    .setCaptainPoints(0)
                    .setElementEventResultList(liveCalcData.getPickList());
            entryEventDataMap.put(entry, data);
        });
        groupTournamentResultList.forEach(o -> {
            // set entry_event_result
            List<EventResultData> eventResultList = Lists.newArrayList();
            o.getEntryList().forEach(entry -> {
                EventResultData data = entryEventDataMap.get(entry);
                eventResultList.add(data);
            });
            o.setEventResultList(eventResultList);
            // captain points
            o.setCaptainPoints(eventResultList.stream().filter(i -> i.getEntry() == o.getCaptainId()).map(EventResultData::getPoints).findFirst().orElse(0));
            // total group points
            int totalPoints = eventResultList.stream().mapToInt(EventResultData::getPoints).sum();
            o.setTotalGroupPoints(totalPoints);
            // total group cost
            int totalCost = eventResultList.stream().mapToInt(i -> i.getPoints() - i.getNetPoints()).sum();
            o.setTotalGroupCost(totalCost);
            // update time
            o.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        // rank
        List<GroupTournamentResultData> resultList = groupTournamentResultList
                .stream()
                .sorted(Comparator.comparing(GroupTournamentResultData::getTotalGroupPoints, Comparator.reverseOrder())
                        .thenComparing(GroupTournamentResultData::getCaptainPoints, Comparator.reverseOrder())
                        .thenComparing(GroupTournamentResultData::getTotalGroupCost))
                .toList();
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setRank(i + 1);
        }
        // redis
        String key = StringUtils.joinWith("::", Constant.GROUP_TOURNAMENT_GROUP_RESULT, groupTournamentId, event);
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        resultList.forEach(o -> valueMap.put(String.valueOf(o.getRank()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public List<GroupTournamentResultData> getEventGroupTournamentResult(int groupTournamentId, int event) {
        String key = StringUtils.joinWith("::", Constant.GROUP_TOURNAMENT_GROUP_RESULT, groupTournamentId, event);
        List<GroupTournamentResultData> groupTournamentResultList = Lists.newArrayList();
        RedisUtils.getHashByKey(key).values().forEach(o -> groupTournamentResultList.add((GroupTournamentResultData) o));
        // hide some unnecessary data
        groupTournamentResultList.forEach(o -> o.setEntryList(Lists.newArrayList()));
        // return
        return groupTournamentResultList.stream().sorted(Comparator.comparing(GroupTournamentResultData::getRank).reversed()).toList();
    }

    @Override
    public void refreshGroupTournamentResult(int groupTournamentId, int event) {
        // event_live
        this.interfaceService.getEventLive(event).ifPresent(res -> this.redisCacheService.insertEventLiveCache(event, res));
        // entry_event_result
        this.insertEntryEventResult(groupTournamentId, event);
    }

}
