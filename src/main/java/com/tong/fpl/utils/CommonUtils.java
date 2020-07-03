package com.tong.fpl.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Create by tong on 2020/6/28
 */
public class CommonUtils {

	public static String getCapitalLetterFromNum(int number) {
		return (char) (number + 64) + "";
	}

	public static int getRealGw(String inputGw) {
		return inputGw.contains("+") ? Integer.parseInt(StringUtils.remove(inputGw, "+")) + 9 : Integer.parseInt(inputGw);
	}

}
