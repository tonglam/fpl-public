package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Create by tong on 2020/1/20
 */
public class StaticTest extends FplApplicationTests {

	@Autowired
	private IStaticSerive staticSerive;

	@ParameterizedTest
	@CsvSource({"3571"})
	void getEntryInfoListFromClassic(int classicId) {
		List<EntryInfoData> list = this.staticSerive.getEntryInfoListFromClassic(classicId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"748496"})
	void getEntryInfoListFromH2h(int h2hId) {
		List<EntryInfoData> list = this.staticSerive.getEntryInfoListFromH2h(h2hId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"3571, 50"})
	void getEntryInfoListFromClassicByLimit(int classicId, int limit) {
		LeagueInfoData leagueInfoData = this.staticSerive.getEntryInfoListFromClassicByLimit(classicId, limit);
		System.out.println(1);
	}

}
