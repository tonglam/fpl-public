package com.tong.fpl.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.response.*;
import com.tong.fpl.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Create by tong on 2020/3/10
 */
@Service
@Slf4j
public class InterfaceService {

    public UserPicksRes getUserPicks(int entry, int event, String profile) {
        try {
            String url = String.format(Constant.USER_PICKS, entry, event);
            String result = HttpUtils.httpGetWithHeader(url, profile);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(result, UserPicksRes.class);
        } catch (IOException e) {
            log.error("getUserPicks error: " + e.getMessage());
        }
        return null;
    }

    public UserHistoryRes getUserHistory(int entry, String profile) {
        try {
            String url = String.format(Constant.USER_HISTORY, entry);
            String result = HttpUtils.httpGetWithHeader(url, profile);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(result, UserHistoryRes.class);
        } catch (IOException e) {
            log.error("getUserHistory error: " + e.getMessage());
        }
        return null;
    }

    public LeagueClassicRes getLeaguesClassic(int classicId, String profile, int page) {
        try {
            String url = String.format(Constant.LEAGUES_CLASSIC, classicId, page);
            String result = HttpUtils.httpGetWithHeader(url, profile);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(result, LeagueClassicRes.class);
        } catch (IOException e) {
            log.error("getLeaguesClassic error: " + e.getMessage());
        }
        return null;
    }

    public EventLiveRes getEventLive(int event, String profile) {
        try {
            String url = String.format(Constant.EVENT_LIVE, event);
            String result = HttpUtils.httpGetWithHeader(url, profile);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(result, EventLiveRes.class);
        } catch (IOException e) {
            log.error("getEventLive error: " + e.getMessage());
        }
        return null;
    }

    public StaticRes getBootstrapStaic() {
        try {
            String result = HttpUtils.httpGet(Constant.BOOTSTRAP_STATIC);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(result, StaticRes.class);
        } catch (IOException e) {
            log.error("get boot-static error: " + e.getMessage());
        }
        return null;
    }

}
