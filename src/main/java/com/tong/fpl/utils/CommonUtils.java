package com.tong.fpl.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/6/28
 */
@Component
public class CommonUtils {

    private static ApplicationContext applicationContext;

    public static String getCapitalLetterFromNum(int number) {
        return (char) (number + 64) + "";
    }

    public static String setRealGw(int gw) {
        if (gw == -1) {
            return "无";
        }
        return "GW" + gw;
    }

    public static String getLocalZoneDateTime(String time) {
        ZoneId zoneId = ZonedDateTime.now().getZone();
        return LocalDateTime.ofInstant(Instant.parse(time), zoneId).atZone(zoneId).format(DateTimeFormatter.ofPattern(Constant.DATETIME));
    }

    public static Map<String, String> createGwMapForOption() {
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("", "请选择");
        IntStream.rangeClosed(1, 38).forEachOrdered(i -> map.put(String.valueOf(i), "GW" + i));
        return map;
    }

    public static Map<String, String> createCurrentGwMapForOption(int currentGw) {
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("", "请选择");
        IntStream.rangeClosed(1, currentGw).forEachOrdered(i -> map.put(String.valueOf(i), "GW" + i));
        return map;
    }

    public static LinkedHashMap<String, String> createSeasonMapForOption() {
        Map<Integer, String> seasonMap = Maps.newLinkedHashMap();
        LocalDate startYear = LocalDate.of(2019, 1, 1);
        LocalDate endYear;
        if (LocalDate.now().getMonth().getValue() < 8) {
            endYear = LocalDate.now();
        } else {
            endYear = LocalDate.now().plusYears(1);
        }
        IntStream.rangeClosed(0, 10).forEach(i -> {
            LocalDate year = startYear.plusYears(i);
            LocalDate nextYear = year.plusYears(1);
            if (nextYear.isAfter(endYear)) {
                return;
            }
            String season = String.valueOf(year).substring(2, 4) + String.valueOf(nextYear).substring(2, 4);
            seasonMap.put(Integer.valueOf(season), season);
        });
        LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
        seasonMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, String>comparingByKey().reversed())
                .forEachOrdered(o -> map.put(String.valueOf(o.getKey()), o.getValue()));
        return map;
    }

    public static String getCurrentSeason() {
        if (LocalDate.now().getMonth().getValue() < 6) {
            return String.valueOf(LocalDate.now().minusYears(1).getYear()).substring(2, 4) +
                    String.valueOf(LocalDate.now().getYear()).substring(2, 4);
        }
        return String.valueOf(LocalDate.now().getYear()).substring(2, 4) +
                String.valueOf(LocalDate.now().plusYears(1).getYear()).substring(2, 4);
    }

    public static int getLeagueId(String url) {
        Matcher matcher = Pattern.compile("(\\d+)").matcher(url);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    public static String getPercentResult(int number1, int number2) {
        return NumberUtil.formatPercent(NumberUtil.div(number1, number2), 2);
    }

    public static void invokeRedisEventDataService(String methodName, Object... args) {
        Class<?> entryClass = ClassUtil.loadClass("com.tong.fpl.config.redis.RedisEventDataService");
        Method method = Arrays.stream(entryClass.getMethods())
                .filter(o -> StringUtils.equals(o.getName(), methodName))
                .findFirst()
                .orElse(null);
        if (method == null) {
            return;
        }
        Class<?>[] constructorClass = entryClass.getDeclaredConstructors()[0].getParameterTypes();
        Object[] params = new Object[constructorClass.length];
        for (int i = 0; i < constructorClass.length; i++) {
            params[i] = ReflectUtil.newInstanceIfPossible(constructorClass[i]);
        }
        try {
            ReflectUtil.invoke(applicationContext.getBean(entryClass, params), method, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private void setApplicationContext(ApplicationContext applicationContext) {
        CommonUtils.applicationContext = applicationContext;
    }

}
