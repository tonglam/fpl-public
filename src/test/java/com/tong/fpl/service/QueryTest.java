package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.letletme.api.EntryEventData;
import com.tong.fpl.domain.data.letletme.player.PlayerData;
import com.tong.fpl.domain.data.letletme.player.PlayerQueryParam;
import com.tong.fpl.domain.data.letletme.player.PlayerValueData;
import com.tong.fpl.domain.entity.TeamEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class QueryTest extends FplApplicationTests {

	@Autowired
	private IQuerySerivce querySerivce;

	@ParameterizedTest
	@CsvSource({"20200726"})
	void qryDayChangePlayerValue(String changeDate) {
		List<PlayerValueData> playerValueDataList = this.querySerivce.qryDayChangePlayerValue(changeDate);
		System.out.println(1);
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
		String season = "2021";
		TeamEntity a = this.querySerivce.qryTeamEntityByTeamId(season, teamId);
		System.out.println(1);
//		TeamEntity b = this.querySerivce.qryTeamByTeamId(season,teamId);
//		System.out.println(1);
	}

}
