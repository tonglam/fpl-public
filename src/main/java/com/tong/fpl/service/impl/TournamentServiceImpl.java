package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.constant.enums.TournamentMode;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.event.CreateTournamentEventData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.ITournamentService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/6/11
 */
@Validated
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentServiceImpl implements ITournamentService {

    private final ApplicationContext context;

    private final IInterfaceService interfaceService;
    private final IQueryService queryService;
    private final IDataService dataService;

    private final EntryInfoService entryInfoService;
    private final EntryHistoryInfoService entryHistoryInfoService;
    private final EntryLeagueInfoService entryLeagueInfoService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    @Override
    public String createNewTournament(TournamentCreateData tournamentCreateData) {
        if (GroupMode.valueOf(tournamentCreateData.getGroupMode()) == GroupMode.No_group &&
                KnockoutMode.valueOf(tournamentCreateData.getKnockoutMode()) == KnockoutMode.No_knockout) {
            return "创建失败，小组赛和淘汰赛至少要有一项！";
        }
        TournamentInfoEntity tournamentInfoEntity = new TournamentInfoEntity();
        // config basic info
        tournamentInfoEntity
                .setName(tournamentCreateData.getTournamentName())
                .setCreator(tournamentCreateData.getCreator())
                .setAdminerEntry(tournamentCreateData.getAdminerEntry())
                .setSeason(CommonUtils.getCurrentSeason())
                .setLeagueType(tournamentCreateData.getLeagueType())
                .setLeagueId(tournamentCreateData.getLeagueId())
                .setTotalTeam(tournamentCreateData.getTotalTeam())
                .setTournamentMode(TournamentMode.Normal.name())
                .setKnockoutPlayAgainstNum(this.setKnockoutPlayAgainstNum(tournamentCreateData.getKnockoutMode()))
                .setState(1);
        // config group info
        this.configGroupInfo(tournamentInfoEntity, tournamentCreateData);
        // config knockout info
        this.configKnockoutInfo(tournamentInfoEntity, tournamentCreateData);
        // save
        this.tournamentInfoService.save(tournamentInfoEntity);
        // publish event
        this.context.publishEvent(new CreateTournamentEventData(this, tournamentCreateData.getTournamentName()));
        return "创建成功！";
    }

    private int setKnockoutPlayAgainstNum(String knockoutMode) {
        switch (KnockoutMode.valueOf(knockoutMode)) {
            case Single_round:
                return 1;
            case Home_away:
                return 2;
            default:
                return 0;
        }
    }

    private void configGroupInfo(TournamentInfoEntity tournamentInfoEntity, TournamentCreateData tournamentCreateData) {
        GroupMode groupMode = GroupMode.valueOf(tournamentCreateData.getGroupMode());
        tournamentInfoEntity.setGroupMode(groupMode.name());
        switch (groupMode) {
            case No_group: {
                tournamentInfoEntity
                        .setTeamPerGroup(0)
                        .setGroupNum(0)
                        .setGroupStartGw(-1)
                        .setGroupEndGw(-1)
                        .setGroupFillAverage(false)
                        .setGroupRounds(0)
                        .setGroupPlayAgainstNum(0)
                        .setGroupQualifiers(0);

                break;
            }
            case Points_race:
            case Battle_race: {
                tournamentInfoEntity
                        .setTeamPerGroup(tournamentCreateData.getTeamPerGroup())
                        .setGroupNum(tournamentCreateData.getGroupNum())
                        .setGroupStartGw(tournamentCreateData.getGroupStartGw())
                        .setGroupEndGw(tournamentCreateData.getGroupEndGw())
                        .setGroupRounds(tournamentInfoEntity.getGroupEndGw() - tournamentInfoEntity.getGroupStartGw() + 1)
                        .setGroupFillAverage(tournamentCreateData.isGroupFillAverage())
                        .setGroupPlayAgainstNum(this.setGroupPlayAgainstNumByMode(groupMode, tournamentInfoEntity.getGroupRounds(), tournamentInfoEntity.getTeamPerGroup()))
                        .setGroupQualifiers(tournamentCreateData.getGroupQualifiers());
                break;
            }
            default:
        }
    }

    private int setGroupPlayAgainstNumByMode(GroupMode groupMode, int groupRounds, int teamPerGroup) {
        if (groupMode.equals(GroupMode.Points_race)) {
            return 0;
        }
        return (int) Math.ceil(groupRounds * 1.0 / (teamPerGroup - 1));
    }

    private void configKnockoutInfo(TournamentInfoEntity tournamentInfoEntity, TournamentCreateData tournamentCreateData) {
        KnockoutMode knockoutMode = KnockoutMode.valueOf(tournamentCreateData.getKnockoutMode());
        tournamentInfoEntity.setKnockoutMode(knockoutMode.name());
        switch (knockoutMode) {
            case No_knockout: {
                tournamentInfoEntity
                        .setKnockoutTeam(0)
                        .setKnockoutRounds(0)
                        .setKnockoutEvents(0)
                        .setKnockoutStartGw(-1)
                        .setKnockoutEndGw(-1);
                break;
            }
            case Single_round:
            case Home_away: {
                tournamentInfoEntity
                        .setKnockoutTeam(tournamentCreateData.getKnockoutTeam())
                        .setKnockoutRounds(tournamentCreateData.getKnockoutRounds())
                        .setKnockoutEvents(tournamentCreateData.getKnockoutEvents())
                        .setKnockoutStartGw(tournamentCreateData.getKnockoutStartGw())
                        .setKnockoutEndGw(tournamentCreateData.getKnockoutEndGw());
                break;
            }
            default:
        }
    }

    @Override
    public void createNewTournamentBackground(String tournamentName) {
        TournamentInfoEntity tournamentInfo = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getName, tournamentName)
                .eq(TournamentInfoEntity::getState, 1));
        if (tournamentInfo == null) {
            return;
        }
        int tournamentId = tournamentInfo.getId();
        String groupMode = tournamentInfo.getGroupMode();
        String knockoutMode = tournamentInfo.getKnockoutMode();
        int groupNum = tournamentInfo.getGroupNum();
        int groupStartGw = tournamentInfo.getGroupStartGw();
        int groupEndGw = tournamentInfo.getGroupEndGw();
        // save entry_info
        this.saveTournamentEntryInfo(tournamentId, tournamentInfo.getLeagueType(), tournamentInfo.getLeagueId(), tournamentInfo.getGroupFillAverage());
        // draw groups
        this.drawGroups(tournamentId, groupMode, tournamentInfo.getTeamPerGroup(), tournamentInfo.getGroupFillAverage(), groupNum, groupStartGw, groupEndGw);
        // create points_group_result
        this.createPointsGroupResult(tournamentId, groupMode, groupStartGw, groupEndGw);
        // create battle_group_result
        this.createBattleGroupResult(tournamentId, groupMode, tournamentInfo.getGroupPlayAgainstNum(), tournamentInfo.getTeamPerGroup(), groupNum, groupStartGw, groupEndGw);
        // draw knockouts
        int knockoutStartGw = tournamentInfo.getKnockoutStartGw();
        int knockoutEndGw = tournamentInfo.getKnockoutRounds();
        this.drawKnockouts(tournamentId, groupMode, groupNum,
                tournamentInfo.getGroupQualifiers(), knockoutMode,
                tournamentInfo.getKnockoutPlayAgainstNum(), tournamentInfo.getKnockoutTeam(),
                knockoutStartGw, knockoutEndGw);
        // update gw result
        int startGw = groupStartGw == 0 ? knockoutStartGw : groupStartGw;
        int endGw = knockoutEndGw == 0 ? groupEndGw : knockoutEndGw;
        this.updateGwResult(tournamentId, startGw, endGw);
    }

    private void saveTournamentEntryInfo(int tournamentId, String leagueType, int leagueId, boolean groupFillAverage) {
        // save entry
        List<EntryInfoData> entryInfoEntityList = this.saveEntryFromFplServer(leagueType, leagueId);
        // save tournament_entry (add average)
        this.saveTournamentEntry(tournamentId, leagueId, groupFillAverage, entryInfoEntityList);
        log.info("tournament:{}, save entry info success!", tournamentId);
    }

    private List<EntryInfoData> saveEntryFromFplServer(String leagueType, int leagueId) {
        List<EntryInfoData> entryInfoList = Lists.newArrayList();
        if (LeagueType.valueOf(leagueType) == LeagueType.Classic) {
            entryInfoList = this.interfaceService.getEntryInfoListFromClassic(leagueId);
            if (CollectionUtils.isEmpty(entryInfoList)) {
                entryInfoList = this.interfaceService.getNewEntryInfoListFromClassic(leagueId);
            }
        } else if (LeagueType.valueOf(leagueType) == LeagueType.H2h) {
            entryInfoList = this.interfaceService.getEntryInfoListFromH2h(leagueId);
            if (CollectionUtils.isEmpty(entryInfoList)) {
                entryInfoList = this.interfaceService.getNewEntryInfoListFromH2h(leagueId);
            }
        }
        List<Integer> entryList = entryInfoList
                .stream()
                .map(EntryInfoData::getEntry)
                .collect(Collectors.toList());
        // save entry_info
        this.saveEntryInfoFromFplServer(entryList);
        // save entry_history_info
        this.saveEntryHistoryInfoFromFplServer(entryList);
        // return
        return entryInfoList;
    }

    private void saveEntryInfoFromFplServer(List<Integer> entryList) {
        // prepare
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        // init data
        List<CompletableFuture<EntryRes>> entryResFuture = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.interfaceService.getEntry(o).orElse(null), this.forkJoinPool))
                .collect(Collectors.toList());
        Map<Integer, EntryRes> entryResMap = entryResFuture
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(EntryRes::getId, o -> o));
        // entry_info
        List<CompletableFuture<EntryInfoEntity>> entryInfoFuture = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.initEntryInfo(entryResMap.get(o)), this.forkJoinPool))
                .collect(Collectors.toList());
        List<EntryInfoEntity> entryInfoList = entryInfoFuture
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // save or update
        List<EntryInfoEntity> insertEntryInfoList = Lists.newArrayList();
        List<EntryInfoEntity> updateEntryInfoList = Lists.newArrayList();
        entryInfoList.forEach(o -> {
            if (!entryInfoMap.containsKey(o.getEntry())) {
                insertEntryInfoList.add(o);
            } else {
                updateEntryInfoList.add(o);
            }
        });
        this.entryInfoService.saveBatch(insertEntryInfoList);
        log.info("insert entry_info size:{}", insertEntryInfoList.size());
        this.entryInfoService.updateBatchById(updateEntryInfoList);
        log.info("update entry_info size:{}", updateEntryInfoList.size());
        // entry_league_info
        Map<String, EntryLeagueInfoEntity> entryLeagueInfoMap = this.entryLeagueInfoService.list(new QueryWrapper<EntryLeagueInfoEntity>().lambda()
                .in(EntryLeagueInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getEntry(), k.getLeagueId(), k.getLeagueType()), v -> v));
        // entry_league_info
        List<CompletableFuture<List<EntryLeagueInfoEntity>>> entryLeagueInfoFuture = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.initEntryLeagueInfo(o, entryResMap.get(o)), this.forkJoinPool))
                .collect(Collectors.toList());
        List<EntryLeagueInfoEntity> entryInfoLeagueList = Lists.newArrayList();
        entryLeagueInfoFuture
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .forEach(entryInfoLeagueList::addAll);
        // save or update
        List<EntryLeagueInfoEntity> insertLeagueInfoList = Lists.newArrayList();
        List<EntryLeagueInfoEntity> updateLeagueInfoList = Lists.newArrayList();
        entryInfoLeagueList.forEach(o -> {
            String key = StringUtils.joinWith("-", o.getEntry(), o.getLeagueId(), o.getLeagueType());
            if (!entryLeagueInfoMap.containsKey(key)) {
                insertLeagueInfoList.add(o);
            } else {
                o.setId(entryLeagueInfoMap.get(key).getId());
                updateLeagueInfoList.add(o);
            }
        });
        this.entryLeagueInfoService.saveBatch(insertLeagueInfoList);
        log.info("insert entry_league_info size:{}", insertLeagueInfoList.size());
        this.entryLeagueInfoService.updateBatchById(updateLeagueInfoList);
        log.info("update entry_league_info size:{}", updateLeagueInfoList.size());
    }

    private EntryInfoEntity initEntryInfo(EntryRes entryRes) {
        return new EntryInfoEntity()
                .setEntry(entryRes.getId())
                .setEntryName(entryRes.getName())
                .setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName())
                .setRegion(entryRes.getPlayerRegionName())
                .setStartedEvent(entryRes.getStartedEvent())
                .setOverallPoints(entryRes.getSummaryOverallPoints())
                .setOverallRank(entryRes.getSummaryOverallRank())
                .setBank(entryRes.getLastDeadlineBank())
                .setTeamValue(entryRes.getLastDeadlineValue())
                .setTotalTransfers(entryRes.getLastDeadlineTotalTransfers())
                .setLastOverallPoints(0)
                .setLastOverallRank(0)
                .setLastTeamValue(0);
    }

    private List<EntryLeagueInfoEntity> initEntryLeagueInfo(int entry, EntryRes entryRes) {
        if (entryRes == null || entryRes.getLeagues() == null) {
            log.error("entry:{}, get fpl server entry empty", entry);
            return null;
        }
        List<EntryLeagueInfoEntity> entryLeagueInfoEntityList = Lists.newArrayList();
        // classic
        entryRes.getLeagues().getClassic().forEach(o -> {
            int leagueId = o.getId();
            entryLeagueInfoEntityList.add(
                    new EntryLeagueInfoEntity()
                            .setEntry(entry)
                            .setLeagueId(leagueId)
                            .setType(StringUtils.equals("x", o.getLeagueType()) ? "private" : "public")
                            .setLeagueType(LeagueType.Classic.name())
                            .setLeagueName(o.getName())
                            .setEntryRank(o.getEntryRank())
                            .setEntryLastRank(o.getEntryLastRank())
                            .setStartEvent(o.getStartEvent())
                            .setCreated(o.getCreated())
            );
        });
        // h2h
        entryRes.getLeagues().getH2h().forEach(o -> {
            int leagueId = o.getId();
            entryLeagueInfoEntityList.add(
                    new EntryLeagueInfoEntity()
                            .setEntry(entry)
                            .setLeagueId(leagueId)
                            .setType(StringUtils.equals("x", o.getLeagueType()) ? "private" : "public")
                            .setLeagueType(LeagueType.H2h.name())
                            .setLeagueName(o.getName())
                            .setEntryRank(o.getEntryRank())
                            .setEntryLastRank(o.getEntryLastRank())
                            .setStartEvent(o.getStartEvent())
                            .setCreated(o.getCreated())
            );
        });
        return entryLeagueInfoEntityList;
    }

    private void saveEntryHistoryInfoFromFplServer(List<Integer> entryList) {
        // exists
        List<Integer> existsList = this.entryHistoryInfoService.list(new QueryWrapper<EntryHistoryInfoEntity>().lambda()
                .in(EntryHistoryInfoEntity::getEntry, entryList))
                .stream()
                .map(EntryHistoryInfoEntity::getEntry)
                .collect(Collectors.toList());
        entryList = entryList
                .stream()
                .filter(o -> !existsList.contains(o))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            log.error("no need to insert");
            return;
        }
        // prepare
        List<CompletableFuture<List<EntryHistoryInfoEntity>>> future = entryList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.initEntryHistoryInfo(o), this.forkJoinPool))
                .collect(Collectors.toList());
        List<EntryHistoryInfoEntity> entryHistoryInfoEntityList = Lists.newArrayList();
        future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .forEach(entryHistoryInfoEntityList::addAll);
        // save
        this.entryHistoryInfoService.saveBatch(entryHistoryInfoEntityList);
        log.info("insert entry_history_info size:{}", entryHistoryInfoEntityList.size());
    }

    private List<EntryHistoryInfoEntity> initEntryHistoryInfo(int entry) {
        UserHistoryRes userHistoryRes = this.interfaceService.getUserHistory(entry).orElse(null);
        if (userHistoryRes == null || CollectionUtils.isEmpty(userHistoryRes.getPast())) {
            return null;
        }
        return userHistoryRes.getPast()
                .stream()
                .map(o ->
                        new EntryHistoryInfoEntity()
                                .setEntry(userHistoryRes.getEntry())
                                .setSeason(o.getSeasonName())
                                .setTotalPoints(o.getTotalPoints())
                                .setOverallRank(o.getRank())
                )
                .collect(Collectors.toList());
    }

    private void saveTournamentEntry(int tournamentId, int leagueId, boolean groupFillAverage, List<EntryInfoData> entryInfoEntityList) {
        List<TournamentEntryEntity> tournamentEntryEntityList = Lists.newArrayList();
        Map<Integer, TournamentEntryEntity> tournamentEntryMap = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .collect(Collectors.toMap(TournamentEntryEntity::getEntry, o -> o));
        entryInfoEntityList.forEach(entryInfoEntity -> {
            int entry = entryInfoEntity.getEntry();
            if (tournamentEntryMap.containsKey(entry)) {
                return;
            }
            tournamentEntryEntityList.add(
                    new TournamentEntryEntity()
                            .setTournamentId(tournamentId)
                            .setLeagueId(leagueId)
                            .setEntry(entry)
            );
        });
        if (groupFillAverage) {
            tournamentEntryEntityList.add(
                    new TournamentEntryEntity()
                            .setTournamentId(tournamentId)
                            .setLeagueId(leagueId)
                            .setEntry(-1));
        }
        this.tournamentEntryService.saveBatch(tournamentEntryEntityList);
        log.info("insert tournament_entry size:{}", tournamentEntryEntityList.size());
    }

    private void drawGroups(int tournamentId, String groupMode, int teamsPerGroup, boolean groupFillAverage, int groupNum,
                            int groupStartGw, int groupEndGw) {
        if (GroupMode.valueOf(groupMode) == GroupMode.No_group) {
            return;
        }
        // check exist
        if (this.tournamentGroupService.count(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)) > 0) {
            log.error("tournament:{}, groups exist!", tournamentId);
            return;
        }
        List<TournamentGroupEntity> tournamentGroupList = Lists.newArrayList();
        // get entryList from input classic league
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // draw
        Multimap<Integer, Integer> teamInGroupMap = ArrayListMultimap.create();
        Multimap<Integer, Integer> groupIndexMap = ArrayListMultimap.create();
        Random random = new Random();
        // add average, represent by negative num
        List<Integer> averageList = Lists.newArrayList();
        int averageNum = teamsPerGroup * groupNum - entryList.size();
        if (averageNum > 0) {
            IntStream.rangeClosed(1, averageNum).forEach(i -> averageList.add(-1 * i));
            // draw average to different group
            averageList.forEach(entry -> {
                int groupId = this.drawAverageToGroup(random, entry, groupNum, teamInGroupMap);
                TournamentGroupEntity tournamentGroupEntity = new TournamentGroupEntity()
                        .setTournamentId(tournamentId)
                        .setGroupId(groupId)
                        .setGroupName(CommonUtils.getCapitalLetterFromNum(groupId))
                        .setGroupIndex(this.drawGroupIndex(random, groupId, teamsPerGroup, groupIndexMap))
                        .setEntry(entry)
                        .setStartGw(groupStartGw)
                        .setEndGw(groupEndGw)
                        .setGroupPoints(0)
                        .setGroupRank(1)
                        .setPlay(0)
                        .setWin(0)
                        .setDraw(0)
                        .setLose(0)
                        .setTotalPoints(0)
                        .setQualified(false)
                        .setOverallRank(0);
                if (groupFillAverage) {
                    tournamentGroupList.add(tournamentGroupEntity);
                }
            });
        }
        // shuffle
        Collections.shuffle(entryList);
        // draw entry list
        IntStream.range(0, entryList.size()).forEach(i -> {
            int groupId = this.drawToGroup(random, entryList.get(i), groupNum, teamsPerGroup, teamInGroupMap);
            tournamentGroupList.add(
                    new TournamentGroupEntity()
                            .setTournamentId(tournamentId)
                            .setGroupId(groupId)
                            .setGroupName(CommonUtils.getCapitalLetterFromNum(groupId))
                            .setGroupIndex(this.drawGroupIndex(random, groupId, teamsPerGroup, groupIndexMap))
                            .setEntry(entryList.get(i))
                            .setStartGw(groupStartGw)
                            .setEndGw(groupEndGw)
                            .setGroupPoints(0)
                            .setGroupRank(1)
                            .setPlay(0)
                            .setWin(0)
                            .setDraw(0)
                            .setLose(0)
                            .setTotalPoints(0)
                            .setQualified(false)
                            .setOverallRank(0)
            );
        });
        // update
        this.tournamentGroupService.saveBatch(tournamentGroupList);
        log.info("tournament:{}, draw groups success!", tournamentId);
    }

    private int drawAverageToGroup(Random random, int entry, int groupNum, Multimap<Integer, Integer> teamInGroup) {
        int groupId = random.nextInt(groupNum) + 1;
        while (teamInGroup.get(groupId).size() > 0 && teamInGroup.get(groupId).stream().parallel().anyMatch(o -> o < 0)) { // each group one average max
            groupId = random.nextInt(groupNum) + 1;
        }
        teamInGroup.put(groupId, entry);
        return groupId;
    }

    private int drawToGroup(Random random, int entry, int groupNum, int teamsPerGroup, Multimap<Integer, Integer> teamInGroup) {
        int groupId = random.nextInt(groupNum) + 1;
        while (teamInGroup.get(groupId).size() + 1 > teamsPerGroup) {
            groupId = random.nextInt(groupNum) + 1;
        }
        teamInGroup.put(groupId, entry);
        return groupId;
    }

    private int drawGroupIndex(Random random, int groupId, int teamsPerGroup, Multimap<Integer, Integer> groupIndexMap) {
        int index = random.nextInt(teamsPerGroup) + 1;
        while (groupIndexMap.containsEntry(groupId, index)) {
            index = random.nextInt(teamsPerGroup) + 1;
        }
        groupIndexMap.put(groupId, index);
        return index;
    }

    private void createPointsGroupResult(int tournamentId, String groupMode, int groupStartGw, int groupEndGw) {
        if (GroupMode.valueOf(groupMode) != GroupMode.Points_race) {
            log.info("tournament:{}, do not need to create points group result!", tournamentId);
            return;
        }
        List<TournamentPointsGroupResultEntity> pointsGroupResultList = Lists.newArrayList();
        // tournament_group
        Map<Integer, Integer> entryGroupIdMap = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .orderByAsc(TournamentGroupEntity::getGroupId)
                .orderByAsc(TournamentGroupEntity::getGroupIndex))
                .stream()
                .collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getGroupId));
        // tournament_group_result
        entryGroupIdMap.keySet().forEach(entry ->
                IntStream.rangeClosed(groupStartGw, groupEndGw).forEach(event ->
                        pointsGroupResultList.add(new TournamentPointsGroupResultEntity()
                                .setTournamentId(tournamentId)
                                .setGroupId(entryGroupIdMap.getOrDefault(entry, 0))
                                .setEvent(event)
                                .setEntry(entry)
                                .setEventGroupRank(0)
                                .setEventPoints(0)
                                .setEventCost(0)
                                .setEventNetPoints(0)
                                .setEventRank(0)
                        )));
        // save
        this.tournamentPointsGroupResultService.saveBatch(pointsGroupResultList);
        log.info("tournament:{}, create points group result success!", tournamentId);
    }

    private void createBattleGroupResult(int tournamentId, String groupMode, int playAgainstNum, int teamPerGroup, int groupNum, int groupStartGw, int groupEndGw) {
        if (GroupMode.valueOf(groupMode) != GroupMode.Battle_race) {
            log.info("tournament:{}, do not need to create battle group result!", tournamentId);
            return;
        }
        int groupRound = groupEndGw - groupStartGw + 1;
        Multimap<Integer, String> abstractBattleMap = this.drawAbstractBattle(teamPerGroup, playAgainstNum, groupRound);
        // single group
        List<TournamentBattleGroupResultEntity> battleGroupResultList = Lists.newArrayList();
        IntStream.rangeClosed(1, groupNum).forEach(groupId ->
                this.drawSingleGroupBattle(tournamentId, groupId, abstractBattleMap, battleGroupResultList));
        // save
        this.tournamentBattleGroupResultService.saveBatch(battleGroupResultList);
        log.info("tournament:{}, create battle group result success!", tournamentId);
    }

    private void drawSingleGroupBattle(int tournamentId, int groupId, Multimap<Integer, String> abstractBattleMap, List<TournamentBattleGroupResultEntity> groupBattleResultList) {
        // get group entry list
        BiMap<Integer, Integer> groupIndexMap = HashBiMap.create();
        this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .eq(TournamentGroupEntity::getGroupId, groupId)
                .orderByAsc(TournamentGroupEntity::getGroupIndex))
                .forEach(tournamentGroupEntity -> groupIndexMap.put(tournamentGroupEntity.getGroupIndex(), tournamentGroupEntity.getEntry()));
        if (CollectionUtils.isEmpty(groupIndexMap)) {
            return;
        }
        // update every group round
        abstractBattleMap.keySet().forEach(event -> {
            List<String> battleList = this.replaceBattleEntry(abstractBattleMap.get(event), groupIndexMap);
            if (CollectionUtils.isEmpty(battleList)) {
                return;
            }
            battleList.forEach(battle -> {
                int homeEntry = Integer.parseInt(StringUtils.substringBefore(battle, "vs"));
                int awayEntry = Integer.parseInt(StringUtils.substringAfter(battle, "vs"));
                groupBattleResultList.add(new TournamentBattleGroupResultEntity()
                        .setTournamentId(tournamentId)
                        .setGroupId(groupId)
                        .setEvent(event)
                        .setHomeIndex(homeEntry == 0 ? 0 : groupIndexMap.inverse().get(homeEntry))
                        .setHomeEntry(homeEntry)
                        .setHomeEntryNetPoints(0)
                        .setHomeEntryRank(0)
                        .setHomeEntryMatchPoints(-1)
                        .setAwayIndex(awayEntry == 0 ? 0 : groupIndexMap.inverse().get(awayEntry))
                        .setAwayEntry(awayEntry)
                        .setAwayEntryNetPoints(0)
                        .setAwayEntryRank(0)
                        .setAwayEntryMatchPoints(-1)
                );
            });
        });
    }

    private Multimap<Integer, String> drawAbstractBattle(int teamPerGroup, int playAgainstNum, int groupRound) {
        // make virtual entry list
        ArrayList<Integer> entryList = Lists.newArrayList();
        IntStream.rangeClosed(1, teamPerGroup).forEach(entryList::add);
        // make it even
        if (entryList.size() % 2 == 1) {
            entryList.add(0); // means blank
        }
        int entryNum = entryList.size();
        // play against each other once
        Multimap<Integer, String> abstractBattleOnceMap = ArrayListMultimap.create(); // round->home+vs+away
        LinkedList<Integer> battleList = Lists.newLinkedList(entryList);
        IntStream.range(1, entryNum).forEach(round -> {
            IntStream.range(0, entryNum / 2).forEach(i -> {
                if (i <= groupRound) {
                    if (i % 2 == 1) {
                        abstractBattleOnceMap.put(round, battleList.get(i) + "vs" + battleList.get(battleList.size() - 1 - i));
                    } else {
                        abstractBattleOnceMap.put(round, battleList.get(battleList.size() - 1 - i) + "vs" + battleList.get(i));
                    }
                }
            });
            battleList.add(1, battleList.pollLast());
        });
        // play against each other more than once
        int battleOnceRounds = abstractBattleOnceMap.keySet().size();
        Multimap<Integer, String> abstractBattleMap = ArrayListMultimap.create();
        for (int i = 1; i < playAgainstNum + 1; i++) {
            if (i % 2 == 1) {
                this.fulfillAbstractBattleMap(groupRound, i, battleOnceRounds, abstractBattleOnceMap, abstractBattleMap);
            } else {
                Multimap<Integer, String> reverseBattleOnceMap = this.reverseBattleOnce(abstractBattleOnceMap, entryNum);
                this.fulfillAbstractBattleMap(groupRound, i, battleOnceRounds, reverseBattleOnceMap, abstractBattleMap);
            }
        }
        return abstractBattleMap;
    }

    private List<String> replaceBattleEntry(Collection<String> roundBattles, BiMap<Integer, Integer> groupIndexMap) {
        List<String> battleList = Lists.newArrayList();
        roundBattles.forEach(battle -> {
            int homeBattleVirtual = Integer.parseInt(StringUtils.substringBefore(battle, "vs"));
            int homeEntry = homeBattleVirtual == 0 ? 0 : groupIndexMap.get(homeBattleVirtual);
            int awayBattleVirtual = Integer.parseInt(StringUtils.substringAfter(battle, "vs"));
            int awayEntry = awayBattleVirtual == 0 ? 0 : groupIndexMap.get(awayBattleVirtual);
            String homeBattle = String.valueOf(homeEntry);
            String awayBattle = String.valueOf(awayEntry);
            battleList.add(homeBattle + "vs" + awayBattle);
        });
        return battleList;
    }

    private Multimap<Integer, String> reverseBattleOnce(Multimap<Integer, String> abstractBattleOnceMap, int entryNum) {
        Multimap<Integer, String> reverseMap = ArrayListMultimap.create();
        IntStream.range(1, entryNum).forEach(round -> {
            Collection<String> roundBattleAgainst = abstractBattleOnceMap.get(round);
            roundBattleAgainst.forEach(battleAgainst -> reverseMap.put(round, StringUtils.substringAfter(battleAgainst, "vs")
                    + "vs" + StringUtils.substringBefore(battleAgainst, "vs")));
        });
        return reverseMap;
    }

    private void fulfillAbstractBattleMap(int groupRound, int againstNum, int battleOnceRounds, Multimap<Integer, String> onceMap, Multimap<Integer, String> battleMap) {
        onceMap.keySet().forEach(round -> {
            if (battleMap.keySet().size() < groupRound) {
                int currentRound = round + battleOnceRounds * (againstNum - 1);
                onceMap.get(round).forEach(value -> battleMap.put(currentRound, value));
            }
        });
    }

    private void drawKnockouts(int tournamentId, String groupMode, int groupNum, int groupQualifiers,
                               String knockoutMode, int knockoutPlayAgainstNum, int knockoutTeam,
                               int knockoutStartGw, int knockoutRounds) {
        if (KnockoutMode.valueOf(knockoutMode) == KnockoutMode.No_knockout) {
            log.info("tournament:{}, do not need to draw knockouts!", tournamentId);
            return;
        }
        if (knockoutPlayAgainstNum == 0) {
            log.error("tournament:{}, knockout stage play against each other at least one time!", tournamentId);
            return;
        }
        // check exist
        if (this.tournamentKnockoutService.count(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)) > 0) {
            log.error("tournament:{}, knockouts exist!", tournamentId);
            return;
        }
        // get knockout entry
        List<Integer> entryList = this.getKnockoutEntryList(tournamentId, groupMode, groupNum, groupQualifiers);
        if (CollectionUtils.isEmpty(entryList)) {
            log.error("tournament:{}, no entry for knockout!", tournamentId);
            return;
        }
        // add blank teams
        int blankNum = (int) Math.pow(2, knockoutRounds) - knockoutTeam;
        if (blankNum >= entryList.size()) {
            log.error("tournament:{}, blank num is bigger than entry num!", tournamentId);
            return;
        }
        List<TournamentKnockoutEntity> knockoutEntityList = Lists.newArrayList();
        IntStream.rangeClosed(1, blankNum).forEach(i -> entryList.add(-1 * i));
        // shuffle
        do {
            Collections.shuffle(entryList);
        } while (this.checkDrawListLegal(entryList));
        // draw firstRound
        List<List<Integer>> drawLists = Lists.partition(entryList, 2);
        int firstRoundMatchNum = (int) Math.pow(2, knockoutRounds - 1);
        IntStream.rangeClosed(1, drawLists.size()).forEach(i -> {
            List<Integer> subList = drawLists.get(i - 1);
            knockoutEntityList.add(new TournamentKnockoutEntity()
                    .setTournamentId(tournamentId)
                    .setRound(1)
                    .setStartGw(knockoutStartGw)
                    .setEndGw(knockoutStartGw + knockoutPlayAgainstNum - 1)
                    .setHomeEntry(subList.get(0))
                    .setAwayEntry(subList.get(1))
                    .setMatchId(i)
                    .setNextMatchId(i % 2 == 0 ? (i / 2) + firstRoundMatchNum : ((i + 1) / 2) + firstRoundMatchNum)
                    .setRoundWinner(0)
            );
        });
        // other matches
        IntStream.rangeClosed(2, knockoutRounds).forEach(i -> {
            int roundMatchNum = (int) Math.pow(2, knockoutRounds - i);
            int prevMatchNum = IntStream.range(1, i).reduce(0, (sum, round) -> sum += (int) Math.pow(2, knockoutRounds - round));
            IntStream.rangeClosed(1, roundMatchNum).forEach(j -> knockoutEntityList.add(new TournamentKnockoutEntity()
                            .setTournamentId(tournamentId)
                            .setRound(i)
                            .setStartGw(knockoutStartGw + knockoutPlayAgainstNum * (i - 1))
                            .setEndGw(knockoutStartGw + knockoutPlayAgainstNum * i - 1)
                            .setHomeEntry(0)
                            .setAwayEntry(0)
                            .setMatchId(j + prevMatchNum)
                            .setNextMatchId(i == knockoutRounds ? -1 : this.getNextMatchId(j, prevMatchNum, roundMatchNum))
                            .setRoundWinner(0)
                    )
            );
        });
        // save knockout
        this.tournamentKnockoutService.saveBatch(knockoutEntityList);
        // create knockout result
        this.createKnockoutResult(knockoutEntityList, knockoutPlayAgainstNum);
        log.info("tournament:{}, draw knockouts success!", tournamentId);
    }

    private List<Integer> getKnockoutEntryList(int tournamentId, String groupMode, int groupNum, int groupQualifiers) {
        List<Integer> entryList = Lists.newArrayList();
        if (GroupMode.valueOf(groupMode) == GroupMode.No_group) {
            List<Integer> entryInfoList = this.queryService.qryEntryListByTournament(tournamentId);
            entryList.addAll(entryInfoList);
        } else {
            IntStream.rangeClosed(1, groupNum).forEach(i ->
                    IntStream.rangeClosed(1, groupQualifiers).forEach(j -> entryList.add(10 * i + j)));
        }
        return entryList;
    }

    private boolean checkDrawListLegal(List<Integer> entryList) {
        return Lists.partition(entryList, 2).stream().anyMatch(subList -> subList.get(0) < 0 && subList.get(1) < 0);
    }

    private int getNextMatchId(int index, int prevMatchNum, int roundMatchNum) {
        return index % 2 == 0 ? (index / 2) + prevMatchNum + roundMatchNum : ((index + 1) / 2) + prevMatchNum + roundMatchNum;
    }

    private void createKnockoutResult(List<TournamentKnockoutEntity> knockoutEntityList, int knockoutPlayAgainstNum) {
        List<TournamentKnockoutResultEntity> resultEntityList = Lists.newArrayList();
        knockoutEntityList.forEach(roundMatchEntity ->
                IntStream.rangeClosed(1, knockoutPlayAgainstNum).forEach(i -> resultEntityList.add(new TournamentKnockoutResultEntity()
                        .setTournamentId(roundMatchEntity.getTournamentId())
                        .setEvent(roundMatchEntity.getStartGw() - 1 + i)
                        .setMatchId(roundMatchEntity.getMatchId())
                        .setPlayAgainstId(i)
                        .setHomeEntry(i % 2 == 1 ? roundMatchEntity.getHomeEntry() : roundMatchEntity.getAwayEntry())
                        .setHomeEntryNetPoints(0)
                        .setHomeEntryRank(0)
                        .setAwayEntry(i % 2 == 1 ? roundMatchEntity.getAwayEntry() : roundMatchEntity.getHomeEntry())
                        .setAwayEntryNetPoints(0)
                        .setAwayEntryRank(0)
                        .setMatchWinner(0)
                )));
        this.tournamentKnockoutResultService.saveBatch(resultEntityList);
    }

    private void updateGwResult(int tournamentId, int startGw, int endGw) {
        int current = this.queryService.getCurrentEvent();
        if (endGw >= current) {
            endGw = current;
        }
        IntStream.rangeClosed(startGw, endGw).forEach(event -> {
            // entry_event_result
            this.dataService.upsertTournamentEventResult(event, tournamentId);
            // points_group_result
            this.dataService.updatePointsRaceGroupResult(event, tournamentId);
            // battle_group_result
            this.dataService.updateBattleRaceGroupResult(event, tournamentId);
            // knockout_result
            this.dataService.updateKnockoutResult(event, tournamentId);
            log.info("tournament:{}, event:{}, update gw result success!", tournamentId, event);
        });
    }

    @Override
    public boolean checkTournamentName(String name) {
        return this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getName, name)
                .eq(TournamentInfoEntity::getState, 1)) == null;
    }

    @Override
    public String updateTournamentInfo(TournamentCreateData tournamentCreateData) {
        boolean update = false;
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getName, tournamentCreateData.getTournamentName())
                .eq(TournamentInfoEntity::getState, 1));
        if (tournamentInfoEntity == null) {
            return "更新失败，赛事不存在！";
        }
        if (!StringUtils.equals(tournamentCreateData.getCreator(), tournamentInfoEntity.getCreator())) {
            tournamentInfoEntity.setCreator(tournamentCreateData.getCreator());
            update = true;
        }
        if (tournamentCreateData.getAdminerEntry() != tournamentInfoEntity.getAdminerEntry()) {
            tournamentInfoEntity.setAdminerEntry(tournamentCreateData.getAdminerEntry());
            update = true;
        }
        if (!update) {
            return "更新失败，没有要更新的数据！";
        }
        this.tournamentInfoService.updateById(tournamentInfoEntity);
        return "更新成功！";
    }

    @Override
    public String deleteTournamentByName(String name) {
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getName, name)
                .eq(TournamentInfoEntity::getState, 1));
        if (tournamentInfoEntity == null) {
            return "删除失败，赛事不存在！";
        }
        tournamentInfoEntity.setState(0);
        this.tournamentInfoService.updateById(tournamentInfoEntity);
        return "删除成功！";
    }

    @Override
    public void addTournamentNewEntry(int tournamentId) {
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("赛事不存在！");
            return;
        }
        if (!StringUtils.equals(tournamentInfoEntity.getTournamentMode(), TournamentMode.Normal.name()) ||
                !StringUtils.equals(tournamentInfoEntity.getGroupMode(), GroupMode.Points_race.name()) ||
                !StringUtils.equals(tournamentInfoEntity.getKnockoutMode(), KnockoutMode.No_knockout.name())) {
            log.error("赛事：{}，只有纯积分赛模式能更新！", tournamentId);
            return;
        }
        String leagueType = tournamentInfoEntity.getLeagueType();
        int leagueId = tournamentInfoEntity.getLeagueId();
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        // save new entry_info
        List<EntryInfoData> newEntryInfoList = this.saveTournamentNewEntryInfo(tournamentId, tournamentInfoEntity, leagueType, leagueId);
        if (CollectionUtils.isEmpty(newEntryInfoList)) {
            log.info("赛事：{}，没有新增的队伍", tournamentId);
            return;
        }
        int currentEvent = this.queryService.getCurrentEvent();
        // create new tournament_group and tournament_group_result
        this.createTournamentNewGroupData(tournamentId, groupStartGw, groupEndGw, newEntryInfoList);
        // update tournament new entry_event_pick && entry_event_result
        List<Integer> newEntryList = newEntryInfoList
                .stream()
                .map(EntryInfoData::getEntry)
                .collect(Collectors.toList());
        this.updateTournamentNewEntryEventPick(currentEvent, tournamentId, groupStartGw, groupEndGw, newEntryList);
        this.updateTournamentNewEntryEventCupResult(currentEvent, tournamentId, groupStartGw, groupEndGw, newEntryList);
        this.updateTournamentNewEntryEventResult(currentEvent, tournamentId, groupStartGw, groupEndGw, newEntryList);
        // update tournament_points_group_result
        this.updateTournamentPointsGroupResult(currentEvent, tournamentId, groupStartGw, groupEndGw);
        // update league_event_report
        this.updateTournamentLeagueEventReportPick(currentEvent, tournamentId);
        this.updateTournamentLeagueEventReportResult(currentEvent, tournamentId, newEntryList);
        // return
        log.info("赛事：{}，更新联赛队伍成功，新增队伍数量:{}，更新名单：{}", tournamentId, newEntryInfoList.size(), newEntryInfoList);
    }

    private List<EntryInfoData> saveTournamentNewEntryInfo(int tournamentId, TournamentInfoEntity tournamentInfoEntity, String leagueType, int leagueId) {
        // tournament_entry
        List<Integer> tournamentEntryList = this.queryService.qryEntryListByTournament(tournamentId);
        if (CollectionUtils.isEmpty(tournamentEntryList)) {
            return Lists.newArrayList();
        }
        //  get entry list from fpl server
        List<EntryInfoData> entryInfoList = Lists.newArrayList();
        if (LeagueType.valueOf(leagueType) == LeagueType.Classic) {
            entryInfoList = this.interfaceService.getEntryInfoListFromClassic(leagueId);
        } else if (LeagueType.valueOf(leagueType) == LeagueType.H2h) {
            entryInfoList = this.interfaceService.getEntryInfoListFromH2h(leagueId);
        }
        if (CollectionUtils.isEmpty(entryInfoList)) {
            return Lists.newArrayList();
        }
        int newTeamNum = entryInfoList.size();
        // update tournament_info
        tournamentInfoEntity
                .setTotalTeam(newTeamNum)
                .setTeamPerGroup(newTeamNum);
        this.tournamentInfoService.updateById(tournamentInfoEntity);
        // get new entry
        List<EntryInfoData> newEntryInfoList = Lists.newArrayList();
        entryInfoList.forEach(o -> {
            if (!tournamentEntryList.contains(o.getEntry())) {
                newEntryInfoList.add(o);
            }
        });
        if (CollectionUtils.isEmpty(newEntryInfoList)) {
            return Lists.newArrayList();
        }
        // save new entry_info
        List<Integer> newEntryList = newEntryInfoList
                .stream()
                .map(EntryInfoData::getEntry)
                .collect(Collectors.toList());
        this.dataService.upsertEntryInfoByList(newEntryList);
        this.dataService.upsertEntryHistoryInfoByList(newEntryList);
        // save tournament_entry
        this.tournamentEntryService.saveBatch(
                newEntryInfoList
                        .stream()
                        .map(o ->
                                new TournamentEntryEntity()
                                        .setTournamentId(tournamentId)
                                        .setLeagueId(leagueId)
                                        .setEntry(o.getEntry())
                        )
                        .collect(Collectors.toList())
        );
        log.info("tournament:{}, save new entry info success!", tournamentId);
        return newEntryInfoList;
    }

    private void createTournamentNewGroupData(int tournamentId, int groupStartGw, int groupEndGw, List<EntryInfoData> newEntryInfoList) {
        List<TournamentGroupEntity> tournamentGroupList = Lists.newArrayList();
        List<TournamentPointsGroupResultEntity> tournamentPointsGroupResultList = Lists.newArrayList();
        // group params
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId));
        int startGroupIndex = tournamentGroupEntityList.size() + 1;
        // add new group data
        for (int i = 0; i < newEntryInfoList.size(); i++) {
            int entry = newEntryInfoList.get(i).getEntry();
            // tournament_group
            tournamentGroupList.add(new TournamentGroupEntity()
                    .setTournamentId(tournamentId)
                    .setGroupId(1)
                    .setGroupName("A")
                    .setGroupIndex(startGroupIndex + i)
                    .setEntry(entry)
                    .setStartGw(groupStartGw)
                    .setEndGw(groupEndGw)
                    .setGroupPoints(0)
                    .setGroupRank(0)
                    .setPlay(0)
                    .setWin(0)
                    .setDraw(0)
                    .setLose(0)
                    .setTotalPoints(0)
                    .setTotalTransfersCost(0)
                    .setTotalNetPoints(0)
                    .setQualified(false)
                    .setOverallRank(0)
            );
            // tournament_points_group_result
            IntStream.rangeClosed(groupStartGw, groupEndGw).forEach(event ->
                    tournamentPointsGroupResultList.add(new TournamentPointsGroupResultEntity()
                            .setTournamentId(tournamentId)
                            .setGroupId(1)
                            .setEvent(event)
                            .setEntry(entry)
                            .setEventGroupRank(0)
                            .setEventPoints(0)
                            .setEventCost(0)
                            .setEventNetPoints(0)
                            .setEventRank(0)
                    ));
        }
        this.tournamentGroupService.saveBatch(tournamentGroupList);
        this.tournamentPointsGroupResultService.saveBatch(tournamentPointsGroupResultList);
        log.info("tournament:{}, create new group data success!", tournamentId);
    }

    private void updateTournamentNewEntryEventPick(int currentEvent, int tournamentId, int groupStartGw, int groupEndGw, List<Integer> newEntryList) {
        if (currentEvent < groupStartGw) {
            return;
        }
        if (currentEvent > groupEndGw) {
            currentEvent = groupEndGw;
        }
        IntStream.rangeClosed(groupStartGw, currentEvent).forEach(event -> this.dataService.insertEventPickByEntryList(event, newEntryList));
        log.info("tournament:{}, update new entry event pick success!", tournamentId);
    }

    private void updateTournamentNewEntryEventCupResult(int currentEvent, int tournamentId, int groupStartGw, int groupEndGw, List<Integer> newEntryList) {
        if (currentEvent < groupStartGw) {
            return;
        }
        if (currentEvent > groupEndGw) {
            currentEvent = groupEndGw;
        }
        IntStream.rangeClosed(groupStartGw, currentEvent).forEach(event -> this.dataService.upsertEventCupResultByEntryList(event, newEntryList));
        log.info("tournament:{}, update new entry event cup result success!", tournamentId);
    }

    private void updateTournamentNewEntryEventResult(int currentEvent, int tournamentId, int groupStartGw, int groupEndGw, List<Integer> newEntryList) {
        if (currentEvent < groupStartGw) {
            return;
        }
        if (currentEvent > groupEndGw) {
            currentEvent = groupEndGw;
        }
        IntStream.rangeClosed(groupStartGw, currentEvent).forEach(event -> this.dataService.upsertEventResultByEntryList(event, newEntryList));
        log.info("tournament:{}, update new entry event result success!", tournamentId);
    }

    private void updateTournamentPointsGroupResult(int currentEvent, int tournamentId, int groupStartGw, int groupEndGw) {
        if (currentEvent < groupStartGw) {
            return;
        }
        if (currentEvent > groupEndGw) {
            currentEvent = groupEndGw;
        }
        IntStream.rangeClosed(groupStartGw, currentEvent).forEach(event -> this.dataService.updatePointsRaceGroupResult(event, tournamentId));
        log.info("tournament:{}, update points group result success!", tournamentId);
    }

    private void updateTournamentLeagueEventReportPick(int currentEvent, int tournamentId) {
        IntStream.rangeClosed(1, currentEvent).forEach(event -> {
            this.dataService.insertLeagueEventPick(event, tournamentId);
            log.info("event:{}, tournament:{}, update entry tournament league event report pick success!", event, tournamentId);
        });
    }

    private void updateTournamentLeagueEventReportResult(int currentEvent, int tournamentId, List<Integer> newEntryList) {
        IntStream.rangeClosed(1, currentEvent).forEach(event ->
                newEntryList.forEach(entry -> {
                    this.dataService.updateEntryLeagueEventResult(event, tournamentId, entry);
                    log.info("event:{}, tournament:{}, entry:{}, update entry tournament league event report result success!", event, tournamentId, entry);
                }));
    }

}
