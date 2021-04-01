package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
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
	@CsvSource({"fight back Motivational Speech Video by Walter Bond.txt, false"})
	void mergeSubtitle(String fileName, boolean engSub) {
		this.subtitleService.mergeSubtitle(fileName, engSub);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"/home/workspace/songs/, Creep-Gamper & Dadoni Ember Island"})
	void praseIrcToWord(String dir, String name) {
		this.subtitleService.praseIrcToWord(dir, name);
		System.out.println(1);
	}

}
