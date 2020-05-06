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
    // chips
    public static final String NONE = "n/a";
    public static final String BB = "bboost";
    public static final String FH = "freehit";
    public static final String WC = "wildcard";
    public static final String TC = "3xc";
    // position
    public static final int TYPE_GKP = 1;
    public static final int TYPE_DEF = 2;
    public static final int TYPE_MID = 3;
    public static final int TYPE_FWD = 4;
    public static final int MIN_NUM_GKP = 1;
    public static final int MIN_NUM_DEF = 3;
    public static final int MIN_NUM_FWD = 1;
    public static final int MIN_PLAYERS = 11;
}
