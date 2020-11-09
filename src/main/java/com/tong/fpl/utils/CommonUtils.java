package com.tong.fpl.utils;

import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/6/28
 */
public class CommonUtils {

	public static String getCapitalLetterFromNum(int number) {
		return (char) (number + 64) + "";
	}

	public static int getRealGw(String inputGw) {
		return inputGw.contains("GW") ? Integer.parseInt(StringUtils.substringAfter(inputGw, "GW"))
				: Integer.parseInt(inputGw);
	}

	public static String setRealGw(int gw) {
		if (gw == -1) {
			return "无";
		}
		return "GW" + gw;
	}

	public static String getZoneDate(String time) {
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
		LocalDate endYear = LocalDate.now().plusYears(1);
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
		return String.valueOf(LocalDate.now().getYear()).substring(2, 4) +
				String.valueOf(LocalDate.now().plusYears(1).getYear()).substring(2, 4);
	}

	public static <T> List<List<T>> batchList(List<T> sourceList) {
		int batchCount = 1000;
		List<List<T>> returnList = new ArrayList<>();
		int startIndex = 0;
		while (startIndex < sourceList.size()) {
			int endIndex;
			if (sourceList.size() - batchCount < startIndex) {
				endIndex = sourceList.size();
			} else {
				endIndex = startIndex + batchCount;
			}
			returnList.add(sourceList.subList(startIndex, endIndex));
			startIndex = startIndex + batchCount;
		}
		return returnList;
	}

}
