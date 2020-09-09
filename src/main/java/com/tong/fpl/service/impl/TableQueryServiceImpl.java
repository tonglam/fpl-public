package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/8/28
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TableQueryServiceImpl implements ITableQueryService {

    private final IQuerySerivce querySerivce;
    private final IRedisCacheSerive redisCacheSerive;
    private final PlayerService playerService;
    private final EntryInfoService entryInfoService;
    private final EntryCaptainStatService entryCaptainStatService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;

    @Override
    public TableData<PlayerInfoData> qryPagePlayerDataList(long page, long limit) {
        List<PlayerInfoData> list = Lists.newArrayList();
        Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<>());
        playerPage.getRecords().forEach(o ->
                list.add(BeanUtil.copyProperties(this.querySerivce.initPlayerInfo(CommonUtils.getCurrentSeason(), o), PlayerInfoData.class)));
        Page<PlayerInfoData> pageResult = new Page<>(page, limit, playerPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    private boolean setSearchTotal(long current) {
        return current == 1;
    }

    @Override
    public TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
        List<TournamentInfoData> list = Lists.newArrayList();
        // get tournament info
        LambdaQueryWrapper<TournamentInfoEntity> queryWrapper = new QueryWrapper<TournamentInfoEntity>().lambda();
        if (StringUtils.isNotBlank(param.getName())) {
            queryWrapper.eq(TournamentInfoEntity::getName, param.getName());
        } else {
            if (StringUtils.isNotBlank(param.getCreator())) {
                queryWrapper.eq(TournamentInfoEntity::getCreator, param.getCreator());
            } else if (param.getLeagueId() > 0) {
                queryWrapper.eq(TournamentInfoEntity::getLeagueId, param.getLeagueId());
            } else if (StringUtils.isNotBlank(param.getCreateTime())) {
                queryWrapper.gt(TournamentInfoEntity::getCreateTime, param.getCreateTime());
                queryWrapper.lt(TournamentInfoEntity::getCreateTime, LocalDate.parse(param.getCreateTime()).plusDays(1).format(DateTimeFormatter.ofPattern(Constant.DATE)));
            }
        }
        if (queryWrapper.getExpression().getNormal().size() == 0) {
            return new TableData<>();
        }
        queryWrapper.eq(TournamentInfoEntity::getState, 1);
        // return
        this.tournamentInfoService.list(queryWrapper).forEach(o -> {
            TournamentInfoData tournamentInfoData = new TournamentInfoData();
            BeanUtil.copyProperties(o, tournamentInfoData, CopyOptions.create().ignoreNullValue());
            tournamentInfoData.setGroupMode(GroupMode.valueOf(o.getGroupMode()).getModeName())
                    .setGroupStartGw(CommonUtils.setRealGw(o.getGroupStartGw()))
                    .setGroupEndGw(CommonUtils.setRealGw(o.getGroupEndGw()))
                    .setKnockoutMode(KnockoutMode.valueOf(o.getKnockoutMode()).getModeName())
                    .setKnockoutStartGw(CommonUtils.setRealGw(o.getKnockoutStartGw()))
                    .setKnockoutEndGw(CommonUtils.setRealGw(o.getKnockoutEndGw()))
                    .setGroupFillAverage(o.isGroupFillAverage() ? "是" : "否")
                    .setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "));
            list.add(tournamentInfoData);
        });
        return new TableData<>(list);
    }

    @Override
    public TableData<TournamentEntryData> qryEntryTournamentList(int entry) {
        List<TournamentEntryData> list = Lists.newArrayList();
        if (entry == 0) {
            return new TableData<>();
        }
        int currentEvent = this.redisCacheSerive.getCurrentEvent();
        // get tournament_list
        List<Integer> tournamentList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getEntry, entry))
                .stream()
                .map(TournamentEntryEntity::getTournamentId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tournamentList)) {
            return new TableData<>();
        }
        // stadge_mode
        Map<String, GroupMode> groupModeMap = Arrays.stream(GroupMode.values()).collect(Collectors.toMap(Enum::name, v -> v));
        Map<String, KnockoutMode> knockModeMap = Arrays.stream(KnockoutMode.values()).collect(Collectors.toMap(Enum::name, v -> v));
        // return
        this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                .in(TournamentInfoEntity::getId, tournamentList)
                .eq(TournamentInfoEntity::getState, 1))
                .forEach(o ->
                        list.add(new TournamentEntryData()
                                .setEntry(entry)
                                .setTournamentId(o.getId())
                                .setName(o.getName())
                                .setCreator(o.getCreator())
                                .setSeason(o.getSeason())
                                .setLeagueType(o.getLeagueType())
                                .setLeagueId(o.getLeagueId())
                                .setGroupMode(groupModeMap.get(o.getGroupMode()).getModeName())
                                .setKnockoutMode(knockModeMap.get(o.getKnockoutMode()).getModeName())
                                .setStadge(this.setCurrentStadge(currentEvent, groupModeMap.get(o.getGroupMode()), o))
                                .setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "))
                        ));
        return new TableData<>(list);
    }

    private String setCurrentStadge(int currentEvent, GroupMode groupMode, TournamentInfoEntity tournamentInfoEntity) {
        switch (groupMode) {
            case No_group: {
                if (currentEvent > tournamentInfoEntity.getKnockoutStartGw()) {
                    return "淘汰赛";
                } else if (currentEvent > tournamentInfoEntity.getKnockoutEndGw()) {
                    return "已结束";
                }
                break;
            }
            case Points_race:
            case Battle_race: {
                int groupStartGw = tournamentInfoEntity.getGroupStartGw();
                int knockoutStartGw = tournamentInfoEntity.getKnockoutStartGw();
                int knockoutEndGw = tournamentInfoEntity.getKnockoutEndGw();
                if (groupStartGw > 0 && currentEvent > groupStartGw) {
                    return "小组赛";
                } else if (knockoutStartGw > 0 && currentEvent > knockoutStartGw) {
                    return "淘汰赛";
                } else if (knockoutEndGw > 0 && currentEvent > knockoutEndGw) {
                    return "已结束";
                }
                break;
            }
        }
        return "未开始";
    }

    @Cacheable(value = "qryPageEntryInfoByTournament")
    @Override
    public TableData<EntryInfoData> qryEntryInfoByTournament(String season, int tournamentId) {
        List<EntryInfoData> entryInfoList = Lists.newArrayList();
        MybatisPlusConfig.season.set(season);
        List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getTournamentId, tournamentId)
                .orderByAsc(TournamentEntryEntity::getEntry))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
        entryList.forEach(entry -> {
            List<EntryCaptainStatEntity> captainStatList = this.entryCaptainStatService.list(new QueryWrapper<EntryCaptainStatEntity>().lambda()
                    .eq(EntryCaptainStatEntity::getEntry, entry)
                    .orderByAsc(EntryCaptainStatEntity::getOverallRank));
            if (CollectionUtils.isEmpty(captainStatList)) {
                return;
            }
            EntryCaptainStatEntity entryCaptainStatEntity = captainStatList.get(0);
            EntryInfoData entryInfoData = new EntryInfoData()
                    .setEntry(entry)
                    .setEntryName(entryCaptainStatEntity.getEntryName())
                    .setPlayerName(entryCaptainStatEntity.getPlayerName())
                    .setOverallPoints(entryCaptainStatEntity.getOverallPoints())
                    .setOverallRank(entryCaptainStatEntity.getOverallRank())
                    .setCapTotalPoints(this.calcEntryCapTotalPoints(captainStatList));
            String percent = NumberUtil.decimalFormat("#.##%", NumberUtil.div(entryInfoData.getCapTotalPoints(), entryInfoData.getOverallPoints(), 2));
            entryInfoData.setPercent(percent);
            entryInfoList.add(entryInfoData);
        });
        MybatisPlusConfig.season.remove();
        return new TableData<>(entryInfoList);
    }

    private int calcEntryCapTotalPoints(List<EntryCaptainStatEntity> captainStatList) {
        return captainStatList.stream().mapToInt(EntryCaptainStatEntity::getTotalPoints).sum();
    }

    @Cacheable(value = "qryEntryCaptainList")
    @Override
    public TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry) {
        List<EntryEventCaptainData> entryEventCaptainList = Lists.newArrayList();
        MybatisPlusConfig.season.set(season);
        // entry_captain_stat list
        this.entryCaptainStatService.list(new QueryWrapper<EntryCaptainStatEntity>().lambda()
                .eq(EntryCaptainStatEntity::getEntry, entry).orderByAsc(EntryCaptainStatEntity::getEvent))
                .forEach(o ->
                        entryEventCaptainList.add(new EntryEventCaptainData()
                                .setEntry(o.getEntry())
                                .setEvent(o.getEvent())
                                .setChip(o.getChip())
                                .setElement(o.getElement())
                                .setWebName(o.getWebName())
                                .setPoints(o.getPoints())
                                .setTotalPoints(o.getTotalPoints())
                        ));
        MybatisPlusConfig.season.remove();
        return new TableData<>(entryEventCaptainList);
    }

    @Override
    public TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId) {
        List<TournamentGroupData> list = Lists.newArrayList();
        this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .eq(TournamentGroupEntity::getGroupId, groupId)
                .orderByAsc(TournamentGroupEntity::getGroupRank)
                .orderByAsc(TournamentGroupEntity::getGroupIndex))
                .forEach(o -> {
                    TournamentGroupData tournamentGroupData = new TournamentGroupData();
                    BeanUtil.copyProperties(o, tournamentGroupData, CopyOptions.create().ignoreNullValue());
                    TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                            .eq(TournamentInfoEntity::getId, tournamentId));
                    if (tournamentInfoEntity != null) {
                        tournamentGroupData.setGroupMode(tournamentInfoEntity.getGroupMode());
                    }
                    tournamentGroupData
                            .setStartGw(o.getStartGw())
                            .setEndGw(o.getEndGw());
                    if (o.getEntry() < 0) {
                        tournamentGroupData
                                .setEntryName("平均分")
                                .setPlayerName("平均分");
                    } else {
                        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(o.getEntry());
                        if (entryInfoEntity != null) {
                            tournamentGroupData
                                    .setEntryName(entryInfoEntity.getEntryName())
                                    .setPlayerName(entryInfoEntity.getPlayerName());
                        }
                    }
                    list.add(tournamentGroupData);
                });
        return new TableData<>(list);
    }

    @Override
    public TableData<PlayerInfoData> qryPlayerList(String season) {
        List<PlayerInfoData> list = this.querySerivce.qryAllPlayers(season);
        list = list.stream().sorted(Comparator.comparing(PlayerInfoData::getPrice).reversed()).collect(Collectors.toList());
        return new TableData<>(list);
    }

    @Override
    public TableData<TournamentPointsGroupEventResultData> qryPointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        List<TournamentPointsGroupEventResultData> list = Lists.newArrayList();
        Page<TournamentPointsGroupResultEntity> pointsGroupResultPage = this.tournamentPointsGroupResultService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                        .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentPointsGroupResultEntity::getGroupId, groupId)
                        .eq(TournamentPointsGroupResultEntity::getEntry, entry)
        );
        pointsGroupResultPage.getRecords().forEach(o -> {
            TournamentPointsGroupEventResultData pointsGroupEventResultData = new TournamentPointsGroupEventResultData();
            BeanUtil.copyProperties(o, pointsGroupEventResultData, CopyOptions.create().ignoreNullValue());
            list.add(pointsGroupEventResultData);
        });
        Page<TournamentPointsGroupEventResultData> pageResult = new Page<>(page, limit, pointsGroupResultPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    @Override
    public TableData<TournamentBattleGroupEventResultData> qryBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        List<TournamentBattleGroupEventResultData> list = Lists.newArrayList();
        Page<TournamentBattleGroupResultEntity> battleGroupResultPage = this.tournamentBattleGroupResultService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<TournamentBattleGroupResultEntity>().lambda()
                        .eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentBattleGroupResultEntity::getGroupId, groupId)
                        .and(o -> o.eq(TournamentBattleGroupResultEntity::getHomeEntry, entry)
                                .or(i -> i.eq(TournamentBattleGroupResultEntity::getAwayEntry, entry)))
        );
        battleGroupResultPage.getRecords().forEach(o -> {
            TournamentBattleGroupEventResultData battleGroupEventResultData = new TournamentBattleGroupEventResultData();
            BeanUtil.copyProperties(o, battleGroupEventResultData, CopyOptions.create().ignoreNullValue());
            battleGroupEventResultData
                    .setHomeEntryName(this.setBattleGroupEntryName(o.getHomeEntry()))
                    .setAwayEntryName(this.setBattleGroupEntryName(o.getAwayEntry()))
                    .setScore(o.getHomeEntryNetPoints() + "-" + o.getAwayEntryNetPoints());
            list.add(battleGroupEventResultData);
        });
        Page<TournamentBattleGroupEventResultData> pageResult = new Page<>(page, limit, battleGroupResultPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    private String setBattleGroupEntryName(int entry) {
        if (entry < 0) {
            return "平均分";
        } else if (entry == 0) {
            return "轮空";
        }
        EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(entry);
        if (entryInfoEntity == null) {
            return "";
        }
        return entryInfoEntity.getEntryName();
    }

}
