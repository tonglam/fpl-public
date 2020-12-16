package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/9
 */
@Data
@Accessors(chain = true)
public class EntryPickData {

	private int element;
	private int position;
	private int elementType;
	private String elementTypeName;
	private String webName;
	private int multiplier;
	private boolean isCaptain;
	private boolean isViceCaptain;
	private int points;
	private int teamId;
	private String teamName;
	private String teamShortName;

}
