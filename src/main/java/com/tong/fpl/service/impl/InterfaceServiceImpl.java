package com.tong.fpl.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/3/10
 */
@Slf4j
@Service
public class InterfaceServiceImpl implements IInterfaceService {

	@Override
	public Optional<String> getPlProfileViaLogin(String username, String password) {
		try {
			return Optional.of(HttpUtils.httpLogin(username, password));
		} catch (IOException e) {
			log.error("login error: " + e.getMessage());
		}
		return Optional.empty();
	}

	public Optional<UserPicksRes> getUserPicks(int entry, int event) {
		try {
			String result = HttpUtils.httpGet(String.format(Constant.USER_PICKS, entry, event)).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return Optional.of(mapper.readValue(result, UserPicksRes.class));
		} catch (IOException e) {
			log.error("getUserPicks error: " + e.getMessage());
		}
		return Optional.empty();
	}

	public Optional<UserHistoryRes> getUserHistory(int entry) {
		try {
			String result = HttpUtils.httpGet(String.format(Constant.USER_HISTORY, entry)).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return Optional.of(mapper.readValue(result, UserHistoryRes.class));
		} catch (IOException e) {
			log.error("getUserHistory error: " + e.getMessage());
		}
		return Optional.empty();
	}

	public Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page) {
		try {
			String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_CLASSIC, classicId, page)).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return Optional.of(mapper.readValue(result, LeagueClassicRes.class));
		} catch (IOException e) {
			log.error("getLeaguesClassic error: " + e.getMessage());
		}
		return Optional.empty();
	}

	@Override
	public Optional<LeagueH2hRes> getH2HClassic(int classicId, int page) {
		try {
			String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_H2H, classicId, page)).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return Optional.of(mapper.readValue(result, LeagueH2hRes.class));
		} catch (IOException e) {
			log.error("getLeaguesH2h error: " + e.getMessage());
		}
		return Optional.empty();
	}

	public Optional<EventLiveRes> getEventLive(int event) {
		try {
			String result = HttpUtils.httpGet(String.format(Constant.EVENT_LIVE, event)).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return Optional.of(mapper.readValue(result, EventLiveRes.class));
		} catch (IOException e) {
			log.error("getEventLive error: " + e.getMessage());
		}
		return Optional.empty();
	}

	public Optional<StaticRes> getBootstrapStaic() {
		try {
			String result = HttpUtils.httpGet(Constant.BOOTSTRAP_STATIC).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return Optional.of(mapper.readValue(result, StaticRes.class));
		} catch (IOException e) {
			log.error("get boot-static error: " + e.getMessage());
		}
		return Optional.empty();
	}

	@Override
	public Optional<List<FixturesRes>> getFixturesInfo(int event) {
		try {
			String result = HttpUtils.httpGet(String.format(Constant.FIXTURES, event)).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			return Optional.of(mapper.readValue(result, new TypeReference<List<FixturesRes>>() {}));
		} catch (IOException e) {
			log.error("get fixtures error: " + e.getMessage());
		}
		return Optional.empty();
	}

}
