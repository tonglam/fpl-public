package com.tong.fpl.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.leaguesClassic.ClassicInfo;
import com.tong.fpl.domain.data.leaguesH2h.H2hInfo;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.wechat.AuthSessionData;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/3/10
 */
@Service
public class InterfaceServiceImpl implements IInterfaceService {

    /**
     * @implNote fantasy
     */
    @Override
    public Optional<StaticRes> getBootstrapStatic() {
        try {
            String result = HttpUtils.httpGet(Constant.BOOTSTRAP_STATIC).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, StaticRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<EntryRes> getEntry(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.ENTRY, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EntryRes.class));
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserHistoryRes> getUserHistory(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.USER_HISTORY, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            UserHistoryRes userHistoryRes = mapper.readValue(result, UserHistoryRes.class);
            userHistoryRes.setEntry(entry);
            return Optional.of(userHistoryRes);
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<EntryInfoData> getNewEntryInfoListFromClassic(int classicId) {
        return this.getOnePageNewEntryListFromClassic(new LeagueInfoData(), classicId, 1, 0).getEntryInfoList();
    }

    @Override
    public List<EntryInfoData> getNewEntryInfoListFromH2h(int h2hId) {
        return this.getOnePageNewEntryListFromH2h(new LeagueInfoData(), h2hId, 1, 0).getEntryInfoList();
    }

    private LeagueInfoData getOnePageNewEntryListFromClassic(LeagueInfoData leagueInfoData, int classicId, int page, int endPage) {
        Optional<LeagueClassicRes> resResult = this.getNewLeaguesClassic(classicId, page);
        if (resResult.isPresent()) {
            LeagueClassicRes leagueClassicRes = resResult.get();
            if (page == 1) {
                // league info
                ClassicInfo classicInfo = leagueClassicRes.getLeague();
                leagueInfoData
                        .setId(classicInfo.getId())
                        .setType(LeagueType.Classic.name())
                        .setName(classicInfo.getName())
                        .setCreated(classicInfo.getCreated())
                        .setAdminEntry(classicInfo.getAdminEntry())
                        .setStartEvent(classicInfo.getStartEvent());
            }
            if (!CollectionUtils.isEmpty(leagueClassicRes.getNewEntries().getResults())) {
                List<EntryInfoData> list = Lists.newArrayList();
                leagueClassicRes.getNewEntries().getResults().forEach(o ->
                        list.add(
                                new EntryInfoData()
                                        .setEntry(o.getEntry())
                                        .setEntryName(o.getEntryName())
                                        .setPlayerName(StringUtils.joinWith(" ", o.getPlayerFirstName(), o.getPlayerLastName()))
                        )
                );
                // set entry info list
                if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
                    leagueInfoData.setEntryInfoList(list);
                } else {
                    List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
                    entryInfoDataList.addAll(list);
                    leagueInfoData.setEntryInfoList(entryInfoDataList);
                }
            }
            if (leagueClassicRes.getNewEntries().isHasNext()) {
                page++;
                if (endPage > 0 && page > endPage) {
                    return leagueInfoData;
                }
                this.getOnePageNewEntryListFromClassic(leagueInfoData, classicId, page, endPage);
            }
        }
        return leagueInfoData;
    }

    private LeagueInfoData getOnePageNewEntryListFromH2h(LeagueInfoData leagueInfoData, int h2hId, int page, int endPage) {
        Optional<LeagueH2hRes> resResult = this.getNewLeagueH2H(h2hId, page);
        if (resResult.isPresent()) {
            LeagueH2hRes leagueH2hRes = resResult.get();
            if (page == 1) {
                // league info
                H2hInfo h2hInfo = leagueH2hRes.getLeague();
                leagueInfoData
                        .setId(h2hInfo.getId())
                        .setType(LeagueType.H2h.name())
                        .setName(h2hInfo.getName())
                        .setCreated(h2hInfo.getCreated())
                        .setAdminEntry(h2hInfo.getAdminEntry())
                        .setStartEvent(h2hInfo.getStartEvent())
                        .setEntryInfoList(Lists.newArrayList());
            }
            if (!CollectionUtils.isEmpty(leagueH2hRes.getNewEntries().getResults())) {
                List<EntryInfoData> list = Lists.newArrayList();
                leagueH2hRes.getNewEntries().getResults().forEach(o ->
                        list.add(
                                new EntryInfoData()
                                        .setEntry(o.getEntry())
                                        .setEntryName(o.getEntryName())
                                        .setPlayerName(o.getPlayerName())
                        )
                );
                // set entry info list
                if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
                    leagueInfoData.setEntryInfoList(list);
                } else {
                    List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
                    entryInfoDataList.addAll(list);
                    leagueInfoData.setEntryInfoList(entryInfoDataList);
                }
                if (leagueH2hRes.getNewEntries().isHasNext()) {
                    page++;
                    if (endPage > 0 && page > endPage) {
                        return leagueInfoData;
                    }
                    this.getOnePageNewEntryListFromH2h(leagueInfoData, h2hId, page, endPage);
                }
            }
        }
        return leagueInfoData;
    }

    @Override
    public Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_CLASSIC, classicId, page)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, LeagueClassicRes.class));
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<EntryInfoData> getEntryInfoListFromClassic(int classicId) {
        return this.getOnePageEntryListFromClassic(new LeagueInfoData(), classicId, 1, 0).getEntryInfoList();
    }

    @Override
    public List<EntryInfoData> getEntryInfoListFromH2h(int h2hId) {
        return this.getOnePageEntryListFromH2h(new LeagueInfoData(), h2hId, 1, 0).getEntryInfoList();
    }

    @Override
    public LeagueInfoData getEntryInfoListFromClassicByLimit(int classicId, int limit) {
        int endPage = this.getEndPage(limit);
        return this.getOnePageEntryListFromClassic(new LeagueInfoData(), classicId, 1, endPage);
    }

    @Override
    public LeagueInfoData getEntryInfoListFromH2hByLimit(int h2hId, int limit) {
        int endPage = this.getEndPage(limit);
        return this.getOnePageEntryListFromH2h(new LeagueInfoData(), h2hId, 1, endPage);
    }

    private int getEndPage(int limit) {
        if (limit > 0 && limit <= 50) {
            return 1;
        }
        return (int) Math.ceil(limit * 1.0 / 50);
    }

    private LeagueInfoData getOnePageEntryListFromClassic(LeagueInfoData leagueInfoData, int classicId, int page, int endPage) {
        Optional<LeagueClassicRes> resResult = this.getLeaguesClassic(classicId, page);
        if (resResult.isPresent()) {
            LeagueClassicRes leagueClassicRes = resResult.get();
            if (page == 1) {
                // league info
                ClassicInfo classicInfo = leagueClassicRes.getLeague();
                leagueInfoData
                        .setId(classicInfo.getId())
                        .setType(LeagueType.Classic.name())
                        .setName(classicInfo.getName())
                        .setCreated(classicInfo.getCreated())
                        .setAdminEntry(classicInfo.getAdminEntry())
                        .setStartEvent(classicInfo.getStartEvent());
            }
            if (!CollectionUtils.isEmpty(leagueClassicRes.getStandings().getResults())) {
                List<EntryInfoData> list = Lists.newArrayList();
                leagueClassicRes.getStandings().getResults().forEach(o ->
                        list.add(
                                new EntryInfoData()
                                        .setEntry(o.getEntry())
                                        .setEntryName(o.getEntryName())
                                        .setPlayerName(o.getPlayerName())
                        )
                );
                // set entry info list
                if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
                    leagueInfoData.setEntryInfoList(list);
                } else {
                    List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
                    entryInfoDataList.addAll(list);
                    leagueInfoData.setEntryInfoList(entryInfoDataList);
                }
            }
            if (leagueClassicRes.getStandings().isHasNext()) {
                page++;
                if (endPage > 0 && page > endPage) {
                    return leagueInfoData;
                }
                this.getOnePageEntryListFromClassic(leagueInfoData, classicId, page, endPage);
            }
        }
        if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
            return this.getOnePageNewEntryListFromClassic(leagueInfoData, classicId, page, endPage);
        }
        return leagueInfoData;
    }

    private LeagueInfoData getOnePageEntryListFromH2h(LeagueInfoData leagueInfoData, int h2hId, int page, int endPage) {
        Optional<LeagueH2hRes> resResult = this.getLeagueH2H(h2hId, page);
        if (resResult.isPresent()) {
            LeagueH2hRes leagueH2hRes = resResult.get();
            if (page == 1) {
                // league info
                H2hInfo h2hInfo = leagueH2hRes.getLeague();
                leagueInfoData
                        .setId(h2hInfo.getId())
                        .setType(LeagueType.H2h.name())
                        .setName(h2hInfo.getName())
                        .setCreated(h2hInfo.getCreated())
                        .setAdminEntry(h2hInfo.getAdminEntry())
                        .setStartEvent(h2hInfo.getStartEvent())
                        .setEntryInfoList(Lists.newArrayList());
            }
            if (!CollectionUtils.isEmpty(leagueH2hRes.getStandings().getResults())) {
                List<EntryInfoData> list = Lists.newArrayList();
                leagueH2hRes.getStandings().getResults().forEach(o ->
                        list.add(
                                new EntryInfoData()
                                        .setEntry(o.getEntry())
                                        .setEntryName(o.getEntryName())
                                        .setPlayerName(o.getPlayerName())
                        )
                );
                // set entry info list
                if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
                    leagueInfoData.setEntryInfoList(list);
                } else {
                    List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
                    entryInfoDataList.addAll(list);
                    leagueInfoData.setEntryInfoList(entryInfoDataList);
                }
                if (leagueH2hRes.getStandings().isHasNext()) {
                    page++;
                    if (endPage > 0 && page > endPage) {
                        return leagueInfoData;
                    }
                    this.getOnePageEntryListFromH2h(leagueInfoData, h2hId, page, endPage);
                }
            }
        }
        if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
            return this.getOnePageNewEntryListFromH2h(leagueInfoData, h2hId, page, endPage);
        }
        return leagueInfoData;
    }

    @Override
    public Optional<EventLiveRes> getEventLive(int event) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.EVENT_LIVE, event)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, EventLiveRes.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<EventFixturesRes>> getEventFixture(int event) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.EVENT_FIXTURES, event)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, new TypeReference<>() {
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<UserTransfersRes>> getUserTransfers(int entry) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.TRANSFER, entry)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, new TypeReference<>() {
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<InputStream> getPlayerPicture(int code) {
        try {
            return HttpUtils.httpGetStream(String.format(Constant.PICTURE, code));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * @implNote wechat
     */
    @Override
    public Optional<AuthSessionData> getAuthSessionInfo(String appId, String secretId, String code) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.CODE_SESSION, appId, secretId, code)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, AuthSessionData.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
