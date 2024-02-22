package com.tong.fpl.domain.data.entry;

import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/1/21
 */
@Data
public class League {

	private List<Classic> classic;
	private List<H2h> h2h;
	private Cup cup;

}
