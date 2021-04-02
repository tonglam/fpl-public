package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * Create by tong on 2020/5/20
 */
@Getter
@AllArgsConstructor
public enum Chip {

	NONE("n/a"), BB("bboost"), FH("freehit"), WC("wildcard"), TC("3xc");

	private final String value;

	public static Chip getChipFromValue(String value) {
		return Stream.of(Chip.values())
				.filter(o -> StringUtils.equals(o.getValue(), value))
				.findFirst()
				.orElse(Chip.NONE);
	}

}
