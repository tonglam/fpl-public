package com.tong.fpl.letletmeApi;

/**
 * Create by tong on 2020/8/28
 */
public interface ICacheApi {

	void insertTeam();

	void insertEvent();

	void insertEventFixture();

	void insertSingleEventFixture(int event);

	void insertPlayer();

	void insertPlayerStat();

	void insertPlayerValue();

	void insertEventLive(int event);

	void updateAllEventResult(int event);

	void deleteKeys(String pattern);

}
