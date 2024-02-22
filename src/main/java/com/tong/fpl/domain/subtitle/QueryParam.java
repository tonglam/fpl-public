package com.tong.fpl.domain.subtitle;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/30
 */
@Data
@Accessors(chain = true)
public class QueryParam {

	private String title;
	private String jobType;
	private String videoType;
	private String mode;
	private String startDay;
	private String endDay;
	private String translator;
	private String status;


}
