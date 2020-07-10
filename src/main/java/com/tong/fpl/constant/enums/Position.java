package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/5/20
 */
@Getter
@AllArgsConstructor
public enum Position {
	GKP(1), DEF(2), MID(3), FWD(4);

	private int position;
}
