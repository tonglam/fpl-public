package com.tong.fpl.api;

import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiScout {

	/**
	 * 获取球探名单
	 */
	Map<String, String> qryScoutEntry();

}
