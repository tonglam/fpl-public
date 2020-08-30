package com.tong.fpl.domain.letletme.global;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/8/18
 */
@Data
@Accessors(chain = true)
public class TablePageData<T> {

	private int code;
	private String msg;
	private long count;
	private List<T> data;

	public static <T> TablePageData<T> success(Page<T> page) {
		return new TablePageData<T>()
				.setCode(0)
				.setMsg("success")
				.setCount(page.getTotal())
				.setData(page.getRecords());
	}

}
