package com.tong.fpl.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.data.response.*;
import com.tong.fpl.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * Create by tong on 2020/3/10
 */
@Slf4j
@Service
public class InterfaceService {

    public Optional<UserPicksRes> getUserPicks(int entry, int event, String profile) {
        try {
            Optional<String> result = HttpUtils.httpGetWithHeader(String.format(Constant.USER_PICKS, entry, event), profile);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result.toString(), UserPicksRes.class));
        } catch (IOException e) {
            log.error("getUserPicks error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<UserHistoryRes> getUserHistory(int entry, String profile) {
        try {
            Optional<String> result = HttpUtils.httpGetWithHeader(String.format(Constant.USER_HISTORY, entry), profile);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result.toString(), UserHistoryRes.class));
        } catch (IOException e) {
            log.error("getUserHistory error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<LeagueClassicRes> getLeaguesClassic(int classicId, String profile, int page) {
        try {
            Optional<String> result = HttpUtils.httpGetWithHeader(String.format(Constant.LEAGUES_CLASSIC, classicId, page), profile);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result.toString(), LeagueClassicRes.class));
        } catch (IOException e) {
            log.error("getLeaguesClassic error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<EventLiveRes> getEventLive(int event, String profile) {
        try {
            Optional<String> result = HttpUtils.httpGetWithHeader(String.format(Constant.EVENT_LIVE, event), profile);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result.toString(), EventLiveRes.class));
        } catch (IOException e) {
            log.error("getEventLive error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<StaticRes> getBootstrapStaic() {
        try {
            Optional<String> result = HttpUtils.httpGet(Constant.BOOTSTRAP_STATIC);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result.toString(), StaticRes.class));
        } catch (IOException e) {
            log.error("get boot-static error: " + e.getMessage());
        }
        return Optional.empty();
    }

}
