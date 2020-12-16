package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.subtitle.ISubtitleService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2020/12/16
 */
public class SubtitleTest extends FplApplicationTests {

	@Autowired
	private ISubtitleService subtitleService;

	@ParameterizedTest
	@CsvSource({"How to Speak with Power and Charisma  -  Julian Treasure.txt, true"})
	void mergeSubtitle(String fileName, boolean engSub) {
		this.subtitleService.mergeSubtitle(fileName, engSub);
		System.out.println(1);
	}

}
