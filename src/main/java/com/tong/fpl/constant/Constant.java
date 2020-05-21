package com.tong.fpl.constant;

/**
 * Create by tong on 2020/1/19
 */
public class Constant {
    //for test
    public static final String PL_PROFILE = "eyJzIjogIld6SXNNalF5TURrMk16QmQ6MWpCWjJuOnAxMzFnOUNwalJBOEo3ajVPV1JxODNaaExDYyIsICJ1IjogeyJpZCI6IDI0MjA5NjMwLCAiZm4iOiAidG9uZyIsICJsbiI6ICJsYW0iLCAiZmMiOiA0M319";
    // url
    public static final String LOGIN = "https://users.premierleague.com/accounts/login/";
    private static final String PREFIX = "https://fantasy.premierleague.com/api/";
    public static final String BOOTSTRAP_STATIC = PREFIX + "bootstrap-static/";
    public static final String USER_HISTORY = PREFIX + "entry/%s/history/";
    public static final String USER_PICKS = PREFIX + "entry/%s/event/%s/picks/";
    public static final String LEAGUES_CLASSIC = PREFIX + "leagues-classic/%s/standings/?page_standings=%s";
    public static final String EVENT_LIVE = PREFIX + "event/%s/live/";
}
