package com.tong.fpl.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.log.HttpCallLog;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    public Optional<EntryRes> getEntry(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.ENTRY, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EntryRes.class));
        } catch (IOException e) {
            HttpCallLog.error("entry:{}, get entry error:{}", entry, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<EntryCupRes> getEntryCup(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.ENTRY_CUP, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EntryCupRes.class));
        } catch (IOException e) {
            HttpCallLog.error("entry:{}, get entry cup error:{}", entry, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserPicksRes> getUserPicks(int event, int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.USER_PICKS, entry, event)).orElse("");
            if (StringUtils.isBlank(result) || result.contains("Not found")) {
                return Optional.empty();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, UserPicksRes.class));
        } catch (IOException e) {
            HttpCallLog.error("event:{}, entry:{}, get user picks error:{}", event, entry, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserHistoryRes> getUserHistory(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.USER_HISTORY, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, UserHistoryRes.class));
        } catch (IOException e) {
            HttpCallLog.error("entry:{}, get user history error:{}", entry, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueClassicRes> getNewLeaguesClassic(int classicId, int page) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_CLASSIC_NEW, classicId, page)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueClassicRes.class));
        } catch (IOException e) {
            HttpCallLog.error("classicId:{}, page:{}, get leagues classic error:{}", classicId, page, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueH2hRes> getNewLeagueH2H(int h2hId, int page) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_H2H_NEW, h2hId, page)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueH2hRes.class));
        } catch (IOException e) {
            HttpCallLog.error("classicId:{}, page:{}, get leagues H2h error:{}", h2hId, page, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_CLASSIC, classicId, page)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueClassicRes.class));
        } catch (IOException e) {
            HttpCallLog.error("classicId:{}, page:{}, get leagues classic error:{}", classicId, page, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueH2hRes> getLeagueH2H(int h2hId, int page) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_H2H, h2hId, page)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueH2hRes.class));
        } catch (IOException e) {
            HttpCallLog.error("classicId:{}, page:{}, get leagues H2h error:{}", h2hId, page, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<EventLiveRes> getEventLive(int event) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.EVENT_LIVE, event)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EventLiveRes.class));
        } catch (IOException e) {
            HttpCallLog.error("event:{}, get event live error:{}", event, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<EventFixturesRes>> getEventFixture(int event) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.EVENT_FIXTURES, event)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, new TypeReference<List<EventFixturesRes>>() {
            }));
        } catch (IOException e) {
            HttpCallLog.error("event:{}, getEventFixture error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<StaticRes> getBootstrapStatic() {
        try {
            String result = HttpUtils.httpGet(Constant.BOOTSTRAP_STATIC).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, StaticRes.class));
        } catch (IOException e) {
            HttpCallLog.error("get boot strap static error:{}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<TransferRes>> getTransfer(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.TRANSFER, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, new TypeReference<List<TransferRes>>() {
            }));
        } catch (IOException e) {
            HttpCallLog.error("entry:{}, get transfer error:{}", entry, e.getMessage());
        }
        return Optional.empty();
    }

}
