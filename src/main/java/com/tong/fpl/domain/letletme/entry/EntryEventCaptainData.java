package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/2
 */
@Data
@Accessors(chain = true)
public class EntryEventCaptainData {

	private int entry;
	private int event;
	private String chip;
	private int element;
	private String webName;
	private int points;
	private int totalPoints;

}
