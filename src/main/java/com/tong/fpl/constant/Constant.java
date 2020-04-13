package com.tong.fpl.constant;

/**
 * Create by tong on 2020/1/19
 */
public class Constant {

    // 日期格式
    public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE = "yyyy-MM-dd";
    public static final String SHORTDAY = "yyyyMMdd";

    public static final int BATCH_COUNT = 10000; // 批量插入一次数量
    public static final String LOGIN = "https://users.premierleague.com/accounts/login/";
    public static final String USER_HISTORY = "https://fantasy.premierleague.com/api/entry/%s/history/";
    public static final String USER_PICKS = "https://fantasy.premierleague.com/api/entry/%s/event/%s/picks/";
    public static final String LEAGUES_CLASSIC = "https://fantasy.premierleague.com/api/leagues-classic/%s/standings/?page_standings=%s";
    public static final String EVENT_LIVE = "https://fantasy.premierleague.com/api/event/%s/live/";
    //for test
    public static final String PL_PROFILE = "eyJzIjogIld6SXNNalF5TURrMk16QmQ6MWpCWjJuOnAxMzFnOUNwalJBOEo3ajVPV1JxODNaaExDYyIsICJ1IjogeyJpZCI6IDI0MjA5NjMwLCAiZm4iOiAidG9uZyIsICJsbiI6ICJsYW0iLCAiZmMiOiA0M319";
    // url
    private static final String PREFIX = "https://fantasy.premierleague.com/api/";
    public static final String BOOTSTRAP_STATIC = PREFIX + "bootstrap-static/";
}
