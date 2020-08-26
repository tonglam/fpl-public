package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/8/19
 */
@Getter
@AllArgsConstructor
public enum DynamicTableName {
	Team("team"),
	Player("player"), Player_Stat("player_stat"), Player_Value("player_value"),
	Entry_info("entry_info"), Entry_event_result("entry_event_result"),
	Event("event"), Event_fixture("event_fixture"), Event_live("event_live");

	private final String tableName;

}
