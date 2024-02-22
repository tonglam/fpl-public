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
    public static final String PICTURE = "https://resources.premierleague.com/premierleague/photos/players/110x140/p%s.png";

    // wechat
    private static final String WECHAT_PREFIX = "https://api.weixin.qq.com/";
    public static final String CODE_SESSION = WECHAT_PREFIX + "sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    // special_tournament
    private static final String SPECIAL_TOURNAMENT_PREFIX = "Special_tournament::";
    public static final String SPECIAL_TOURNAMENT_INFO = SPECIAL_TOURNAMENT_PREFIX + "Info";
    public static final String SPECIAL_TOURNAMENT_GROUP = SPECIAL_TOURNAMENT_PREFIX + "Group";
    public static final String SPECIAL_TOURNAMENT_SHUFFLED_GROUP = SPECIAL_TOURNAMENT_PREFIX + "Shuffled_Group";
    public static final String SPECIAL_TOURNAMENT_ENTRY_RESULT = SPECIAL_TOURNAMENT_PREFIX + "Entry_Result";
    public static final String SPECIAL_TOURNAMENT_GROUP_RESULT = SPECIAL_TOURNAMENT_PREFIX + "Group_Result";
    public static final String SPECIAL_TOURNAMENT_SHUFFLED_GROUP_RESULT = SPECIAL_TOURNAMENT_PREFIX + "Shuffled_Group_Result";

    // group_tournament
    private static final String GROUP_TOURNAMENT_PREFIX = "Group_tournament::";
    public static final String GROUP_TOURNAMENT_GROUP = GROUP_TOURNAMENT_PREFIX + "Group";
    public static final String GROUP_TOURNAMENT_GROUP_RESULT = GROUP_TOURNAMENT_PREFIX + "Group_Result";

    // draw_knockout
    private static final String DRAW_KNOCKOUT_PREFIX = "Draw_Knockout::";
    public static final String DRAW_KNOCKOUT_CANDIDATE = DRAW_KNOCKOUT_PREFIX + "Candidates::";
    public static final String DRAW_KNOCKOUT_PAIR = DRAW_KNOCKOUT_PREFIX + "Pairs::";
    public static final String DRAW_KNOCKOUT_RESULT = DRAW_KNOCKOUT_PREFIX + "Results::";
    public static final String DRAW_KNOCKOUT_NOTICE = DRAW_KNOCKOUT_PREFIX + "Notice::";

    // admin_entry
    public static final int ADMIN_ENTRY = 18377;

}
