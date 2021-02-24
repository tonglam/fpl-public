package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.TransferRes;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/1/20
 */
public class StaticTest extends FplApplicationTests {

	@Autowired
	private IStaticService staticSerive;

	@ParameterizedTest
	@CsvSource({"3571"})
	void getEntryInfoListFromClassic(int classicId) {
		long startTime = System.currentTimeMillis();
		List<EntryInfoData> list = this.staticSerive.getEntryInfoListFromClassic(classicId);
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
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

	@ParameterizedTest
	@CsvSource({"1870"})
	void getTransfer(int entry) {
		Optional<List<TransferRes>> a = this.staticSerive.getTransfer(entry);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1870"})
	void getEntry(int entry) {
		Optional<EntryRes> a = this.staticSerive.getEntry(entry);
		System.out.println(1);
	}

}
