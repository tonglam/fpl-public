package com.tong.fpl.domain.letletme.element;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/11/4
 */
@Data
@Accessors(chain = true)
public class ElementCaptainData {

	private int element;
	private int event;
	private String elementTypeName;
	private String webName;
	private int points;
	private String selectByPercent;
	private String pointsByPercent;
	private boolean blank;

}
