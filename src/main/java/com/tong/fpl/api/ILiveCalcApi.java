package com.tong.fpl.api;

import com.tong.fpl.domain.data.letletme.LiveCalaData;

import java.util.Map;

/**
 * Create by tong on 2020/8/3
 */
public interface ILiveCalcApi {

	LiveCalaData calcLivePointsByEntry(int event, int entry);

	LiveCalaData calcLivePointsByElementList(int event, Map<Integer, Integer> elementMap, int captain, int viceCaptain);

}
