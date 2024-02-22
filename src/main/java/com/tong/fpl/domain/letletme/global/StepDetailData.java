package com.tong.fpl.domain.letletme.global;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/10/21
 */
@Data
@Accessors(chain = true)
public class StepDetailData {

	private String title;
	private String description;

}
