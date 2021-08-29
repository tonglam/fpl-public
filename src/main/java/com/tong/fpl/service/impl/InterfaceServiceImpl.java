package com.tong.fpl.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.log.HttpCallLog;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/3/10
 */
@Service
public class InterfaceServiceImpl implements IInterfaceService {

    @Override
    public Optional<StaticRes> getBootstrapStatic() {
        try {
            HttpCallLog.info("start get bootstrap_static from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(Constant.BOOTSTRAP_STATIC).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get bootstrap_static from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, StaticRes.class));
        } catch (IOException e) {
            HttpCallLog.error("get bootstrap_static error:{}", e.getMessage());
            return this.getWangBootstrapStatic();
        }
    }

    @Override
    public Optional<StaticRes> getWangBootstrapStatic() {
        try {
            HttpCallLog.info("start get wang_bootstrap_static from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(Constant.WANG_BOOTSTRAP_STATIC).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get wang_bootstrap_static from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, StaticRes.class));
        } catch (IOException e) {
            HttpCallLog.error("get wang_bootstrap_static error:{}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<EntryRes> getEntry(int entry) {
        try {
            HttpCallLog.info("start get entry from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.ENTRY, entry)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get entry from server, escape:{} s!", (end - start) / 1000);
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
            HttpCallLog.info("start get entry_cup from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.ENTRY_CUP, entry)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get entry_cup from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EntryCupRes.class));
        } catch (IOException e) {
            HttpCallLog.error("entry:{}, get entry_cup error:{}", entry, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserPicksRes> getUserPicks(int event, int entry) {
        try {
            HttpCallLog.info("start get user_picks from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.USER_PICKS, entry, event)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get user_picks from server, escape:{} s!", (end - start) / 1000);
            if (StringUtils.isBlank(result) || result.contains("Not found")) {
                return Optional.empty();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, UserPicksRes.class));
        } catch (IOException e) {
            HttpCallLog.error("event:{}, entry:{}, get user_picks error:{}", event, entry, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserHistoryRes> getUserHistory(int entry) {
        try {
            HttpCallLog.info("start get user_history from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.USER_HISTORY, entry)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get user_history from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, UserHistoryRes.class));
        } catch (IOException e) {
            HttpCallLog.error("entry:{}, get user_history error:{}", entry, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueClassicRes> getNewLeaguesClassic(int classicId, int page) {
        try {
            HttpCallLog.info("start get leagues_classic_new from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_CLASSIC_NEW, classicId, page)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get leagues_classic_new from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueClassicRes.class));
        } catch (IOException e) {
            HttpCallLog.error("classicId:{}, page:{}, get leagues_classic_new error:{}", classicId, page, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueH2hRes> getNewLeagueH2H(int h2hId, int page) {
        try {
            HttpCallLog.info("start get leagues_h2h_new from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_H2H_NEW, h2hId, page)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get leagues_h2h_new from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueH2hRes.class));
        } catch (IOException e) {
            HttpCallLog.error("classicId:{}, page:{}, get leagues_h2h_new error:{}", h2hId, page, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page) {
        try {
            HttpCallLog.info("start get leagues_classic from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_CLASSIC, classicId, page)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get leagues_classic from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueClassicRes.class));
        } catch (IOException e) {
            HttpCallLog.error("classicId:{}, page:{}, get leagues_classic error:{}", classicId, page, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<LeagueH2hRes> getLeagueH2H(int h2hId, int page) {
        try {
            HttpCallLog.info("start get leagues_h2h from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_H2H, h2hId, page)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get leagues_h2h from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueH2hRes.class));
        } catch (IOException e) {
            HttpCallLog.error("classicId:{}, page:{}, get leagues_h2h error:{}", h2hId, page, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<EventLiveRes> getEventLive(int event) {
        try {
            HttpCallLog.info("start get event_live from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.EVENT_LIVE, event)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get event_live from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EventLiveRes.class));
        } catch (IOException e) {
            HttpCallLog.error("event:{}, get event_live error:{}", event, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<EventFixturesRes>> getEventFixture(int event) {
        try {
            HttpCallLog.info("start get event_fixtures from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.EVENT_FIXTURES, event)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get event_fixtures from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, new TypeReference<List<EventFixturesRes>>() {
            }));
        } catch (IOException e) {
            HttpCallLog.error("event:{}, get event_fixtures error: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<UserTransfersRes>> getUserTransfers(int entry) {
        try {
            HttpCallLog.info("start get transfer from server!");
            long start = System.currentTimeMillis();
            String result = HttpUtils.httpGet(String.format(Constant.TRANSFER, entry)).orElse("");
            long end = System.currentTimeMillis();
            HttpCallLog.info("get transfer from server, escape:{} s!", (end - start) / 1000);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, new TypeReference<List<UserTransfersRes>>() {
            }));
        } catch (IOException e) {
            HttpCallLog.error("entry:{}, get transfer error:{}", entry, e.getMessage());
        }
        return Optional.empty();
    }

}
