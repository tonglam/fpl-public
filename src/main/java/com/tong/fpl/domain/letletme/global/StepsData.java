package com.tong.fpl.domain.letletme.global;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/10/21
 */
@Data
@Accessors(chain = true)
public class StepsData {

	private List<StepDetailData> dataList;
	private int active;

}
