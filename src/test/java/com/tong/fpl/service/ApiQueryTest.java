package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public class ApiQueryTest extends FplApplicationTests {

	@Autowired
	private IApiQueryService apiQueryService;

	@ParameterizedTest
	@CsvSource({"1"})
	void qryPlayerInfoListByElementType(int elementType) {
		LinkedHashMap<String, List<PlayerInfoData>> map = this.apiQueryService.qryPlayerInfoByElementType(elementType);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"finished"})
	void qryLiveFixtureByStatus(String playStatus) {
		List<LiveMatchData> list = this.apiQueryService.qryLiveFixtureByStatus(playStatus);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"finished"})
	void qryLiveMatchDataByStatus(String playStatus) {
		Map<String, LiveMatchTeamData> map = this.apiQueryService.qryLiveMatchDataByStatus(playStatus);
		System.out.println(1);
	}


}
