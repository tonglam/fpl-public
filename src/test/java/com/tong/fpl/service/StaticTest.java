package com.tong.fpl.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.response.LeagueClassicRes;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.data.userHistory.Current;
import com.tong.fpl.service.impl.InterfaceServiceImpl;
import com.tong.fpl.service.impl.StaticServiceImpl;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.HttpUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Optional;

/**
 * Create by tong on 2020/1/20
 */
public class StaticTest extends FplApplicationTests {

	@Autowired
	private StaticServiceImpl staticService;
	@Autowired
	private InterfaceServiceImpl interfaceService;

	@Test
	void insertEvents() {
		this.staticService.insertEvent();
	}

	@Test
	void insertPlayers() {
		this.staticService.insertPlayers();
	}

	@Test
	void insertPlayerValue() {
		this.staticService.insertPlayerValue();
	}

	@ParameterizedTest
	@CsvSource({"45"})
	void insertGwFixture(int event) {
		this.staticService.insertEventFixture(event);
	}

	@ParameterizedTest
	@CsvSource({"45"})
	void insertGwLive(int event) {
		this.staticService.insertEventLive(event);
	}

	@ParameterizedTest
	@CsvSource({"44"})
	void userHostory(int entry) {
		this.interfaceService.getUserHistory(entry);
	}

	@Test
	void classic() {
		Optional<LeagueClassicRes> leagueClassic = this.interfaceService.getLeaguesClassic(710, 1);
		System.out.println("done!");
	}

	@Test
	void chinaUsers() {
		try {
			String result = HttpUtils.httpGet(String.format(Constant.USER_HISTORY, 3212061)).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			UserHistoryRes historyRes = mapper.readValue(result, UserHistoryRes.class);
			int transfer = getTransferNum(historyRes);
			int cost = getTransferCost(historyRes);
			System.out.println(CommonUtils.checkActive(43, historyRes));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(1);
	}

	int getTransferCost(UserHistoryRes historyRes) {
		return historyRes.getCurrent().stream().map(Current::getEventTransfersCost).reduce(0, (sum, i) -> sum += i);
	}

	int getTransferNum(UserHistoryRes historyRes) {
		return historyRes.getCurrent().stream().map(Current::getEventTransfers).reduce(0, (sum, i) -> sum += i);
	}

}
