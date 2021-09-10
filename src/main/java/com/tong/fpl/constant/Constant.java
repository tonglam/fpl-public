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
    public static final String USER_PICKS = PREFIX + "entry/%s/event/%s/picks/";
    public static final String LEAGUES_CLASSIC = PREFIX + "leagues-classic/%s/standings/?page_standings=%s";
    public static final String LEAGUES_H2H = PREFIX + "leagues-h2h/%s/standings/?page_standings=%s";
    public static final String LEAGUES_CLASSIC_NEW = PREFIX + "leagues-classic/%s/standings/?page_new_entries=%s";
    public static final String LEAGUES_H2H_NEW = PREFIX + "leagues-h2h/%s/standings/?page_new_entries=%s";
    public static final String EVENT_LIVE = PREFIX + "event/%s/live/";
    public static final String EVENT_FIXTURES = PREFIX + "fixtures/?event=%s";
    public static final String TRANSFER = PREFIX + "entry/%s/transfers/";

    // fpl-data_url
    private static final String DATA_PREFIX = "https//139.198.118.60/";

    private static final String DATA_COMMON_PREFIX = DATA_PREFIX + "common/";
    public static final String DATA_EVENT = DATA_COMMON_PREFIX + "updateEvent";
    public static final String DATA_PLAYER_STAT = DATA_COMMON_PREFIX + "updatePlayerStat";
    public static final String DATA_PLAYER_VALUE = DATA_COMMON_PREFIX + "updatePlayerValue";
    public static final String DATA_EVENT_LIVE = DATA_COMMON_PREFIX + "updateEventLive?event=%s";
    public static final String DATA_EVENT_LIVE_CACHE = DATA_COMMON_PREFIX + "updateEventLiveCache?event=%s";
    public static final String DATA_EVENT_LIVE_SUMMARY = DATA_COMMON_PREFIX + "updateEventLiveSummary";
    public static final String DATA_EVENT_OVERALL = DATA_COMMON_PREFIX + "updateEventOverall";

    private static final String DATA_ENTRY_PREFIX = DATA_PREFIX + "entry/";
    public static final String DATA_ENTRY_INFO = DATA_ENTRY_PREFIX + "upsertEntryInfo/?entry=%s";
    public static final String DATA_ENTRY_INFO_LIST = DATA_ENTRY_PREFIX + "upsertEntryInfoByList/?entryList=%s";
    public static final String DATA_ENTRY_HISTORY_INFO = DATA_ENTRY_PREFIX + "upsertEntryHistoryInfo/?entry=%s";
    public static final String DATA_ENTRY_HISTORY_INFO_LIST = DATA_ENTRY_PREFIX + "upsertEntryHistoryInfoByList/?entryList=%s";
    public static final String DATA_ENTRY_EVENT_PICK = DATA_ENTRY_PREFIX + "insertEntryEventPick/?event=%s&entry=%s";
    public static final String DATA_ENTRY_EVENT_PICK_LIST = DATA_ENTRY_PREFIX + "insertEntryEventPickList/?event=%s&entryList=%s";
    public static final String DATA_INSERT_ENTRY_EVENT_TRANSFERS = DATA_ENTRY_PREFIX + "insertEntryEventTransfers/?entry=%s";
    public static final String DATA_INSERT_ENTRY_EVENT_TRANSFERS_LIST = DATA_ENTRY_PREFIX + "insertEntryEventTransfersList/?event=%s&entryList=%s";
    public static final String DATA_UPDATE_ENTRY_EVENT_TRANSFERS = DATA_ENTRY_PREFIX + "updateEntryEventTransfers/?event=%s&entry=%s";
    public static final String DATA_UPDATE_ENTRY_EVENT_TRANSFERS_LIST = DATA_ENTRY_PREFIX + "updateEntryEventTransfers/?event=%s&entry=%s";
    public static final String DATA_ENTRY_EVENT_CUP_RESULT = DATA_ENTRY_PREFIX + "upsertEntryEventCupResult/?event=%s&entry=%s";
    public static final String DATA_ENTRY_EVENT_CUP_RESULT_LIST = DATA_ENTRY_PREFIX + "upsertEntryEventCupResultList/?event=%s&entryList=%s";
    public static final String DATA_ENTRY_EVENT_RESULT = DATA_ENTRY_PREFIX + "upsertEntryEventResult/?event=%s&entry=%s";
    public static final String DATA_ENTRY_EVENT_RESULT_LIST = DATA_ENTRY_PREFIX + "upsertEntryEventResultList/?event=%s&entryList=%s";

    private static final String DATA_TOURNAMENT_PREFIX = DATA_PREFIX + "tournament/";
    public static final String DATA_TOURNAMENT_EVENT_RESULT = DATA_TOURNAMENT_PREFIX + "upsertTournamentEventResult/?event=%s&tournamentId=%s";
    public static final String DATA_POINTS_RACE_GROUP_RESULT = DATA_TOURNAMENT_PREFIX + "updatePointsRaceGroupResult/?event=%s&tournamentId=%s";
    public static final String DATA_BATTLE_RACE_GROUP_RESULT = DATA_TOURNAMENT_PREFIX + "updateBattleRaceGroupResult/?event=%s&tournamentId=%s";
    public static final String DATA_KNOCKOUT_RESULT = DATA_TOURNAMENT_PREFIX + "updateKnockoutResult/?event=%s&tournamentId=%s";

    private static final String DATA_REPORT_PREFIX = DATA_PREFIX + "report/";
    public static final String DATA_INSERT_ENTRY_LEAGUE_EVENT_PICK = DATA_REPORT_PREFIX + "insertEntryLeagueEventPick/?event=%s&tournamentId=%s&entry=%s";
    public static final String DATA_UPDATE_ENTRY_LEAGUE_EVENT_RESULT = DATA_REPORT_PREFIX + "updateEntryLeagueEventResult/?event=%s&tournamentId=%s&entry=%s";
    public static final String DATA_INSERT_LEAGUE_EVENT_PICK = DATA_REPORT_PREFIX + "insertLeagueEventPick/?event=%s&tournamentId=%s";
    public static final String DATA_UPDATE_LEAGUE_EVENT_RESULT = DATA_REPORT_PREFIX + "updateLeagueEventResult/?event=%s&tournamentId=%s";

}
