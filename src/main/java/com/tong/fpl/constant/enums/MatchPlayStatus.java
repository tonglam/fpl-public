package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/9/15
 */
@Getter
@AllArgsConstructor
public enum MatchPlayStatus {

	Playing(0), Not_Start(1), Finished(2), Event_Not_Finished(3), Blank(4);

	private final int status;
}
