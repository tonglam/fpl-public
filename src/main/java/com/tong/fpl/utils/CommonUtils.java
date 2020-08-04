package com.tong.fpl.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.data.userHistory.Current;
import com.tong.fpl.domain.data.userHistory.HistoryChips;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.EventEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EventService;
import com.tong.fpl.service.db.PlayerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/6/28
 */
@Component
public class CommonUtils {

    private static PlayerService playerService;
    private static EventService eventService;
    private static EntryEventResultService entryEventResultService;

    public static String getCapitalLetterFromNum(int number) {
        return (char) (number + 64) + "";
    }

    public static int getRealGw(String inputGw) {
        return inputGw.contains("+") ? Integer.parseInt(StringUtils.remove(inputGw, "+")) + 9 : Integer.parseInt(inputGw);
    }

    public static boolean checkActive(int event, UserHistoryRes historyRes) {
        List<Integer> lastEvents = Lists.newArrayList(event - 2, event - 1, event);
        return (checkActiveTransfer(historyRes.getCurrent(), lastEvents)) || (checkActiveChips(historyRes.getChips(), lastEvents));
    }

    private static boolean checkActiveTransfer(List<Current> currents, List<Integer> lastEvents) {
        return currents.stream().filter(current -> lastEvents.contains(current.getEvent())).anyMatch(current -> current.getEventTransfers() > 0);
    }

    private static boolean checkActiveChips(List<HistoryChips> chips, List<Integer> lastEvents) {
        return chips.stream().anyMatch(chip -> lastEvents.contains(chip.getEvent()));
    }

    public static int getTransferCost(UserHistoryRes historyRes) {
        return historyRes.getCurrent().stream().map(Current::getEventTransfersCost).reduce(0, (sum, i) -> sum += i);
    }

    public static int getTransferNum(UserHistoryRes historyRes) {
        return historyRes.getCurrent().stream().map(Current::getEventTransfers).reduce(0, (sum, i) -> sum += i);
    }

    public static int getNowEvent() {
        int event = 0;
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

    public static List<Pick> getPickListFromPicks(String picks) {
	    List<Pick> pickList = (List<Pick>) JsonUtils.json2Collection(picks, List.class, Pick.class);
	    if (CollectionUtils.isEmpty(pickList)) {
		    return Lists.newArrayList();
	    }
	    pickList.parallelStream().forEach(pick -> {
		    PlayerEntity playerEntity = playerService.getById(pick.getElement());
		    if (playerEntity != null) {
			    pick
					    .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()).name())
					    .setWebName(playerEntity.getWebName());
		    }
	    });
	    return pickList;
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
