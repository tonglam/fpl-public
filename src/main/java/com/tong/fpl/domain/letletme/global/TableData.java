package com.tong.fpl.domain.letletme.global;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Create by tong on 2020/9/1
 */
@Data
public class TableData<T> implements Serializable {

	private static final long serialVersionUID = 5090888303718566883L;

	private int code;
	private String msg;
	private long count;
	private List<T> data;

	public TableData() {
		this.code = 0;
		this.msg = "success";
		this.count = 0;
		this.data = Lists.newArrayList();
	}

	public TableData(T body) {
		this.code = 0;
		this.msg = "success";
		this.count = 0;
		List<T> data = Lists.newArrayList();
		data.add(body);
		this.data = data;
	}

	public TableData(List<T> data) {
		this.code = 0;
		this.msg = "success";
		this.count = data.size();
		this.data = data;
	}

	public TableData(Page<T> page) {
		this.code = 0;
		this.msg = "success";
		this.count = page.getTotal();
		this.data = page.getRecords();
	}

}
