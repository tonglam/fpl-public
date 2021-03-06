package com.tong.fpl.constant;

/**
 * Create by tong on 2020/1/19
 */
public class Constant {

    // date_format
    public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE = "yyyy-MM-dd";
    public static final String SHORTDAY = "yyyyMMdd";

    // location
    public static final String SUBTITLE_FILE_LOCATION = "/home/workspace/subtitle/";

    // fantasy_url
    private static final String PREFIX = "https://fantasy.premierleague.com/api/";
    public static final String BOOTSTRAP_STATIC = PREFIX + "bootstrap-static/";
    public static final String ENTRY = PREFIX + "entry/%s/";
    public static final String ENTRY_CUP = PREFIX + "entry/%s/cup/";
    public static final String USER_HISTORY = PREFIX + "entry/%s/history/";
    public static final String USER_PICKS = PREFIX + "entry/%s/event/%s/picks/";
    public static final String LEAGUES_CLASSIC = PREFIX + "leagues-classic/%s/standings/?page_standings=%s";
    public static final String LEAGUES_H2H = PREFIX + "leagues-h2h/%s/standings/?page_standings=%s";
    public static final String LEAGUES_CLASSIC_NEW = PREFIX + "leagues-classic/%s/standings/?page_new_entries=%s";
    public static final String LEAGUES_H2H_NEW = PREFIX + "leagues-h2h/%s/standings/?page_new_entries=%s";
    public static final String EVENT_LIVE = PREFIX + "event/%s/live/";
    public static final String EVENT_FIXTURES = PREFIX + "fixtures/?event=%s";
    public static final String TRANSFER = PREFIX + "entry/%s/transfers/";

    // fpl-data_url
    private static final String DATA_PREFIX = "http://139.198.118.60:8080/";
    private static final String DATA_ENTRY_PREFIX = DATA_PREFIX + "entry/";
    public static final String DATA_ENTRY_INFO = DATA_ENTRY_PREFIX + "upsertEntryInfo/?entry=%s";
    public static final String DATA_ENTRY_HISTORY_INFO = DATA_ENTRY_PREFIX + "upsertEntryHistoryInfo/?entry=%s";

}
