package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.letletme.api.EntryEventData;
import com.tong.fpl.domain.data.letletme.player.PlayerData;
import com.tong.fpl.domain.data.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.data.letletme.player.PlayerQueryParam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.IntStream;

public class QueryTest extends FplApplicationTests {

	@Autowired
	private IQuerySerivce querySerivce;

	@Test
	void test() {
		long startTime = System.currentTimeMillis();
		IntStream.range(1, 21).forEach(teamId -> {
//			String name = this.querySerivce.qryTeamPropertyById("2021", teamId, "name");
		});
		long endTime = System.currentTimeMillis();
		System.out.println("esaped: " + (endTime - startTime) + "ms");
	}

	@ParameterizedTest
	@CsvSource({"1920, 1, 1404"})
	void qryEntryEvent(String season, int event, int entry) {
		EntryEventData entryEventData = this.querySerivce.qryEntryEventResult(season, event, entry);
		System.out.println(1);
	}

	@Test
	void qryPlayerData() throws Exception {
		PlayerQueryParam playerQueryParam = new PlayerQueryParam()
//                .setElement(483)
//                .setCode(225321)
				.setWebName("Ward");
		PlayerData playerData = this.querySerivce.qryPlayerData(playerQueryParam);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1"})
	void qryTeamByTeamId(int teamId) {
//		TeamEntity b = this.querySerivce.qryTeamByTeamId(season,teamId);
//		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2021"})
	void qryAllPlayers(String season) {
		List<PlayerInfoData> list = this.querySerivce.qryAllPlayers(season);
		System.out.println(1);
	}

}
