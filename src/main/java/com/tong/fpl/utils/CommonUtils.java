package com.tong.fpl.utils;

import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.service.IRedisCacheSerive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/6/28
 */
@Component
public class CommonUtils {

    private static IRedisCacheSerive redisCacheSerive;

    public static int getRealGw(String inputGw) {
        return inputGw.contains("GW") ? Integer.parseInt(StringUtils.substringAfter(inputGw, "GW"))
                : Integer.parseInt(inputGw);
    }

    public static int getCurrentEvent() {
        int event = 1;
        for (int i = 1; i < 39; i++) {
            String deadline = CommonUtils.redisCacheSerive.getDeadlineByEvent(CommonUtils.getCurrentSeason(), i);
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
                event = i;
                break;
            }
        }
        return event;
    }

    public static String getZoneDate(String time) {
        ZoneId zoneId = ZonedDateTime.now().getZone();
        return LocalDateTime.ofInstant(Instant.parse(time), zoneId).atZone(zoneId).format(DateTimeFormatter.ofPattern(Constant.DATETIME));
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

    @Autowired
    private void setRedisCacheSerive(IRedisCacheSerive redisCacheSerive) {
        CommonUtils.redisCacheSerive = redisCacheSerive;
    }

}
