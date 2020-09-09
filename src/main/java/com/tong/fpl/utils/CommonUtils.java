package com.tong.fpl.utils;

import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.Position;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/6/28
 */
public class CommonUtils {

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
		IntStream.range(1, 39).forEachOrdered(i -> map.put(String.valueOf(i), "GW" + i));
		return map;
	}

	public static String getCurrentSeason() {
		return String.valueOf(LocalDate.now().getYear()).substring(2, 4) +
				String.valueOf(LocalDate.now().plusYears(1).getYear()).substring(2, 4);
	}

	public static Map<Integer, String> getPositonMap() {
		return Arrays.stream(Position.values()).collect(Collectors.toMap(Position::getPosition, Enum::name));
	}

}
