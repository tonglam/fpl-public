package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/5/20
 */
@Getter
@AllArgsConstructor
public enum Chip {
	NONE("n/a"), BB("bboost"), FH("freehit"), WC("wildcard"), TC("3xc");

	private String value;
}
