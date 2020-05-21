package com.tong.fpl.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/5/20
 */
@Getter
@AllArgsConstructor
public enum ChipEnum {
	NONE("n/a"), FH("bboost"), WC("freehit"), BB("wildcard"), TC("3xc");

	private String value;
}
