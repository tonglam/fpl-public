package com.tong.fpl.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.EventEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TeamNameEntity;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EventService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.TeamNameService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/6/28
 */
@Component
public class CommonUtils {

    private static TeamNameService teamNameService;
    private static PlayerService playerService;
    private static EventService eventService;
    private static EntryEventResultService entryEventResultService;

    public static int getRealGw(String inputGw) {
        return inputGw.contains("GW") ? Integer.parseInt(StringUtils.substringAfter(inputGw, "GW"))
                : Integer.parseInt(inputGw);
    }

    public static int getCurrentEvent() {
        int event = 1;
        Map<String, Integer> deadlineMap = CommonUtils.eventService.list()
                .stream().collect(Collectors.toMap(EventEntity::getDeadlineTime, EventEntity::getEvent));
        Map<String, Integer> result = new LinkedHashMap<>();
        // sort by value
        deadlineMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        for (String deadlineTime :
                result.keySet()) {
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(deadlineTime, DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
                event = result.get(deadlineTime);
            } else {
                break;
            }
        }
        return event;
    }

    public static String getDeadlineTime(int event) {
        return CommonUtils.eventService.list(new QueryWrapper<EventEntity>().lambda().eq(EventEntity::getEvent, event))
                .stream()
                .map(EventEntity::getDeadlineTime)
                .findFirst()
                .orElse("");
    }

    public static String getZoneDate(String time) {
        ZoneId zoneId = ZonedDateTime.now().getZone();
        return LocalDateTime.ofInstant(Instant.parse(time), zoneId).atZone(zoneId).format(DateTimeFormatter.ofPattern(Constant.DATETIME));
    }

    public static List<Pick> getPickList(int event, int entry) {
        String eventPick = entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry)).getEventPicks();
        if (StringUtils.isBlank(eventPick)) {
            return Lists.newArrayList();
        }
        return getPickListFromPicks(eventPick);
    }

    @SuppressWarnings("unchecked")
    public static List<Pick> getPickListFromPicks(String picks) {
        List<Pick> pickList = (List<Pick>) JsonUtils.json2Collection(picks, List.class, Pick.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return Lists.newArrayList();
        }
        pickList.forEach(pick -> {
            PlayerEntity playerEntity = playerService.getById(pick.getElement());
            if (playerEntity != null) {
                pick
                        .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()).name())
                        .setWebName(playerEntity.getWebName());
            }
        });
        return pickList;
    }

    public static Map<String, String> createGwMapForOption() {
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("", "请选择");
        IntStream.range(1, 39).forEachOrdered(i -> map.put(String.valueOf(i), "GW" + i));
        return map;
    }

    public static String getCurrentSeason() {
        return String.valueOf(LocalDate.now().getYear()).substring(2, 4) +
                String.valueOf(LocalDate.now().plusYears(1).getYear()).substring(2, 4);
    }

    public static TeamNameEntity getTeamNameEntityByTeamId(int teamId) {
        return teamNameService.getOne(new QueryWrapper<TeamNameEntity>()
                .lambda()
                .eq(TeamNameEntity::getTeamId, teamId)
        );
    }

    public static String getTeamNameByTeamId(int teamId) {
        TeamNameEntity teamNameEntity = getTeamNameEntityByTeamId(teamId);
        if (teamNameEntity != null) {
            return teamNameEntity.getName();
        }
        return "";
    }

    public static String getTeamShortNameByTeamId(int teamId) {
        TeamNameEntity teamNameEntity = getTeamNameEntityByTeamId(teamId);
        if (teamNameEntity != null) {
            return teamNameEntity.getShortName();
        }
        return "";
    }

    @Autowired
    private void setTeamNameService(TeamNameService teamNameService) {
        CommonUtils.teamNameService = teamNameService;
    }

    @Autowired
    private void setPlayerService(PlayerService playerService) {
        CommonUtils.playerService = playerService;
    }

    @Autowired
    private void setEventService(EventService eventService) {
        CommonUtils.eventService = eventService;
    }

    @Autowired
    private void setEventLiveService(EntryEventResultService entryEventResultService) {
        CommonUtils.entryEventResultService = entryEventResultService;
    }

}
