package com.tong.fpl.domain.data.letletme.table;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/28
 */
@Data
@Accessors(chain = true)
public class PlayTableData {

	private int element;
	private String webName;
	private int elementType;
	private String elementTypeName;
	private String teamName;
	private double price;

}
