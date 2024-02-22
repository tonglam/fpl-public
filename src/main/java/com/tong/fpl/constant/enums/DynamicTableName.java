package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 此处定义需要跨赛季取的表
 * <p>
 * Create by tong on 2020/8/19
 */
@Getter
@AllArgsConstructor
public enum DynamicTableName {

    Event("event"), Event_fixture("event_fixture"),

    Team("team"),

    Player("player"), Player_Stat("player_stat"),
    Player_Value("player_value"),

    Entry_info("entry_info"), Entry_event_pick("entry_event_pick"),
    Entry_event_result("entry_event_result"), Entry_event_transfers("entry_event_transfers"),

    Event_live("event_live"), Event_live_summary("event_live_summary"),
    Event_live_explain("event_live_explain"),

    Tournament_info("tournament_entry"), Tournament_entry("tournament_entry"),

    League_event_report("league_event_report"),

    VaastavDataGw("vaastav_data_gw");

    private final String tableName;

}
