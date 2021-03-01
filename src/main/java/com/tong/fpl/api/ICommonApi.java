package com.tong.fpl.api;

/**
 * Create by tong on 2021/2/26
 */
public interface ICommonApi {

	int getNextEvent();

	String getUtcDeadlineByEvent(int event);

}
