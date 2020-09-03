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
import com.tong.fpl.domain.entity.EntryCaptainStatEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TournamentEntryEntity;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.EntryTournamentData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.db.EntryCaptainStatService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.TournamentEntryService;
import com.tong.fpl.service.db.TournamentInfoService;
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
    private final EntryCaptainStatService entryCaptainStatService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentEntryService tournamentEntryService;

    @Override
    public TableData<PlayerInfoData> qryPagePlayerDataList(long page, long limit) {
        List<PlayerInfoData> list = Lists.newArrayList();
        Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<>());
        playerPage.getRecords().forEach(o -> list.add(BeanUtil.copyProperties(this.querySerivce.initPlayerInfo(o), PlayerInfoData.class)));
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
    public TableData<EntryTournamentData> qryEntryTournamentList(int entry) {
        List<EntryTournamentData> list = Lists.newArrayList();
        if (entry == 0) {
            return new TableData<>();
        }
        // get tournament_list
        List<Integer> tournamentList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getEntry, entry))
                .stream()
                .map(TournamentEntryEntity::getTournamentId)
                .collect(Collectors.toList());
        // return
        this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                .in(TournamentInfoEntity::getId, tournamentList)
                .eq(TournamentInfoEntity::getState, 1))
                .forEach(o -> list.add(new EntryTournamentData()
                        .setEntry(entry)
                        .setTournamentId(o.getId())
                        .setName(o.getName())
                        .setCreator(o.getCreator())
                        .setSeason(o.getSeason())
                        .setLeagueType(o.getLeagueType())
                        .setLeagueId(o.getLeagueId())
                        .setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "))
                ));
        return new TableData<>(list);
    }

    @Cacheable(value = "qryPageEntryInfoByTournament")
    @Override
    public TableData<EntryInfoData> qryPageEntryInfoByTournament(String season, int tournamentId, long page, long limit) {
        List<EntryInfoData> entryInfoList = Lists.newArrayList();
        MybatisPlusConfig.season.set(season);
        Page<TournamentEntryEntity> tournamentEntryPage = this.tournamentEntryService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<TournamentEntryEntity>().lambda()
                        .eq(TournamentEntryEntity::getTournamentId, tournamentId)
                        .orderByAsc(TournamentEntryEntity::getEntry));
        List<Integer> entryList = tournamentEntryPage.getRecords()
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
        entryList.forEach(entry -> {
            List<EntryCaptainStatEntity> captainStatList = this.entryCaptainStatService.list(new QueryWrapper<EntryCaptainStatEntity>().lambda()
                    .eq(EntryCaptainStatEntity::getEntry, entry));
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
        Page<EntryInfoData> pageResult = new Page<>(page, limit, tournamentEntryPage.getTotal());
        pageResult.setRecords(entryInfoList);
        MybatisPlusConfig.season.remove();
        return new TableData<>(pageResult);
    }

    private int calcEntryCapTotalPoints(List<EntryCaptainStatEntity> captainStatList) {
        return captainStatList.stream().mapToInt(EntryCaptainStatEntity::getTotalPoints).sum();
    }

    @Cacheable(value = "qryEntryCaptainList")
    @Override
    public TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry, long page, long limit) {
        List<EntryEventCaptainData> entryEventCaptainList = Lists.newArrayList();
        MybatisPlusConfig.season.set(season);
        // entry_captain_stat list
        Page<EntryCaptainStatEntity> entryCaptainStatPage = this.entryCaptainStatService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<EntryCaptainStatEntity>().lambda()
                        .eq(EntryCaptainStatEntity::getEntry, entry)
                        .orderByAsc(EntryCaptainStatEntity::getEvent));
        if (entryCaptainStatPage == null) {
            MybatisPlusConfig.season.remove();
            return new TableData<>(entryEventCaptainList);
        }
        entryCaptainStatPage.getRecords().forEach(o ->
                entryEventCaptainList.add(new EntryEventCaptainData()
                        .setEntry(o.getEntry())
                        .setEvent(o.getEvent())
                        .setChip(o.getChip())
                        .setElement(o.getElement())
                        .setWebName(o.getWebName())
                        .setPoints(o.getPoints())
                        .setTotalPoints(o.getTotalPoints())
                ));
        Page<EntryEventCaptainData> pageResult = new Page<>(page, limit, entryCaptainStatPage.getTotal());
        pageResult.setRecords(entryEventCaptainList);
        MybatisPlusConfig.season.remove();
        return new TableData<>(pageResult);
    }

    @Override
    public TableData<PlayerInfoData> qryPagePlayerList(String season, long page, long limit) {
        List<PlayerInfoData> playerList = Lists.newArrayList();
        MybatisPlusConfig.season.set(season);
        Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<PlayerEntity>().lambda()
                        .orderByAsc(PlayerEntity::getElement));
        if (playerPage == null) {
            MybatisPlusConfig.season.remove();
            return new TableData<>(playerList);
        }
        Map<Integer, String> teamNameMap = this.redisCacheSerive.getTeamNameMap();
        Map<Integer, String> positionMap = CommonUtils.getPositonMap();
        playerPage.getRecords().forEach(o ->
                playerList.add(new PlayerInfoData()
                        .setElement(o.getElement())
                        .setCode(o.getCode())
                        .setWebName(o.getWebName())
                        .setElementType(o.getElementType())
                        .setElementTypeName(positionMap.get(o.getElementType()))
                        .setTeamId(o.getTeamId())
                        .setTeamName(teamNameMap.get(o.getTeamId()))
                        .setPrice(NumberUtil.div(o.getPrice(), 10, 1))
                ));
        Page<PlayerInfoData> pageResult = new Page<>(page, limit, playerPage.getTotal());
        pageResult.setRecords(playerList);
        MybatisPlusConfig.season.remove();
        return new TableData<>(pageResult);
    }

}
