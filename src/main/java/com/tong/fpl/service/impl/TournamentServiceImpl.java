package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.constant.enums.TournamentMode;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.event.CreateTournamentEventData;
import com.tong.fpl.domain.event.CreateZjTournamentEventData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupData;
import com.tong.fpl.domain.letletme.tournament.ZjTournamentCreateData;
import com.tong.fpl.domain.letletme.tournament.ZjTournamentGroupData;
import com.tong.fpl.service.*;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final IStaticService staticService;
    private final IQueryService queryService;
    private final IEventDataService eventDataService;
    private final IReportService reportService;
    private final EntryInfoService entryInfoService;
    private final EntryEventPickService entryEventPickService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;
    private final ZjTournamentCaptainService zjTournamentCaptainService;
    private final ZjTournamentResultService zjTournamentResultService;

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
        this.context.publishEvent(new CreateTournamentEventData(this, tournamentCreateData.getTournamentName(), tournamentCreateData.getInputEntryList()));
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
    public void createNewTournamentBackground(String tournamentName, List<Integer> inputEntryList) {
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
        this.saveTournamentEntryInfo(tournamentId, tournamentInfo.getLeagueType(), tournamentInfo.getLeagueId(), inputEntryList, tournamentInfo.getGroupFillAverage());
        // draw groups
        this.drawGroups(tournamentId, groupMode, tournamentInfo.getTeamPerGroup(), tournamentInfo.getGroupFillAverage(), groupNum, groupStartGw, groupEndGw);
        // create points_group_result
        this.createPointsGroupResult(tournamentId, groupMode, groupStartGw, groupEndGw);
        // create battle_group_result
        this.createBattleGroupResult(tournamentId, groupMode, tournamentInfo.getGroupPlayAgainstNum(), tournamentInfo.getTeamPerGroup(), groupNum, groupStartGw, groupEndGw);
        // draw knockouts
        this.drawKnockouts(tournamentId, groupMode, groupNum,
                tournamentInfo.getGroupQualifiers(), knockoutMode,
                tournamentInfo.getKnockoutPlayAgainstNum(), tournamentInfo.getKnockoutTeam(),
                tournamentInfo.getKnockoutStartGw(), tournamentInfo.getKnockoutRounds());
        // update gw result
        this.updateGwResult(tournamentId);
    }

    private void saveTournamentEntryInfo(int tournamentId, String leagueType, int leagueId, List<Integer> inputEntryList, boolean groupFillAverage) {
        // save entry_info
        List<EntryInfoEntity> entryInfoEntityList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(inputEntryList)) {
            entryInfoEntityList = this.saveEntryInfoFromFplServer(leagueType, leagueId);
        } else {
            List<EntryInfoEntity> inputEntryInfoList = inputEntryList
                    .stream()
                    .map(this.queryService::qryEntryInfo)
                    .collect(Collectors.toList());
            entryInfoEntityList.addAll(inputEntryInfoList);
        }
        // save tournament_entry (add average)
        this.saveTournamentEntry(tournamentId, leagueId, groupFillAverage, entryInfoEntityList);
        log.info("tournament:{}, save entry info success!", tournamentId);
    }

    private List<EntryInfoEntity> saveEntryInfoFromFplServer(String leagueType, int leagueId) {
        List<EntryInfoEntity> entryInfoEntityList = Lists.newArrayList();
        List<EntryInfoData> entryInfoList = Lists.newArrayList();
        if (LeagueType.valueOf(leagueType) == LeagueType.Classic) {
            entryInfoList = this.staticService.getEntryInfoListFromClassic(leagueId);
        } else if (LeagueType.valueOf(leagueType) == LeagueType.H2h) {
            entryInfoList = this.staticService.getEntryInfoListFromH2h(leagueId);
        }
        entryInfoList.parallelStream().forEach(entryInfoEntity -> {
            Optional<EntryRes> entryRes = this.staticService.getEntry(entryInfoEntity.getEntry());
            entryRes.ifPresent(o -> entryInfoEntityList.add(new EntryInfoEntity()
                            .setEntry(entryInfoEntity.getEntry())
                            .setEntryName(o.getName())
                            .setPlayerName(o.getPlayerFirstName() + " " + o.getPlayerLastName())
                            .setRegion(o.getPlayerRegionName())
                            .setStartedEvent(o.getStartedEvent())
                            .setOverallPoints(o.getSummaryOverallPoints())
                            .setOverallRank(o.getSummaryOverallRank())
                            .setBank(o.getLastDeadlineBank())
                            .setTeamValue(o.getLastDeadlineValue())
                            .setTotalTransfers(o.getLastDeadlineTotalTransfers())
                            .setLastOverallPoints(o.getSummaryOverallPoints())
                            .setLastOverallRank(o.getSummaryOverallRank())
                            .setLastTeamValue(o.getLastDeadlineValue())
                    )
            );
        });
        this.entryInfoService.saveOrUpdateBatch(entryInfoEntityList);
        return entryInfoEntityList;
    }

    private void saveTournamentEntry(int tournamentId, int leagueId, boolean groupFillAverage, List<EntryInfoEntity> entryInfoEntityList) {
        List<TournamentEntryEntity> tournamentEntryEntityList = Lists.newArrayList();
        entryInfoEntityList.forEach(entryInfoEntity -> tournamentEntryEntityList.add(new TournamentEntryEntity()
                .setTournamentId(tournamentId)
                .setLeagueId(leagueId)
                .setEntry(entryInfoEntity.getEntry())
        ));
        if (groupFillAverage) {
            tournamentEntryEntityList.add(new TournamentEntryEntity()
                    .setTournamentId(tournamentId)
                    .setLeagueId(leagueId)
                    .setEntry(-1));
        }
        this.tournamentEntryService.saveBatch(tournamentEntryEntityList);
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
            tournamentGroupList.add(new TournamentGroupEntity()
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

    private void updateGwResult(int tournamentId) {
        int current = this.queryService.getCurrentEvent();
        IntStream.rangeClosed(current, current).forEach(event -> {
            // entry_event_result
            this.eventDataService.upsertTournamentEntryEventResult(event, tournamentId);
            // points_group_result
            this.eventDataService.updatePointsRaceGroupResult(event, tournamentId);
            // battle_group_result
            this.eventDataService.updateBattleRaceGroupResult(event, tournamentId);
            // knockout_result
            this.eventDataService.updateKnockoutResult(event, tournamentId);
        });
        log.info("tournament:{}, update gw result success!", tournamentId);
    }

    @Override
    public String createNewZjTournament(ZjTournamentCreateData zjTournamentCreateData) {
        TournamentInfoEntity tournamentInfoEntity = new TournamentInfoEntity()
                .setName(zjTournamentCreateData.getTournamentName())
                .setCreator(zjTournamentCreateData.getCreator())
                .setAdminerEntry(zjTournamentCreateData.getAdminerEntry())
                .setSeason(CommonUtils.getCurrentSeason())
                .setLeagueType(LeagueType.Custom.name())
                .setLeagueId(0)
                .setTotalTeam(zjTournamentCreateData.getTotalTeam())
                .setTournamentMode(TournamentMode.Zj.name())
                .setGroupMode(GroupMode.Custom.name())
                .setTeamPerGroup(zjTournamentCreateData.getTeamPerGroup())
                .setGroupNum(zjTournamentCreateData.getGroupNum())
                .setGroupStartGw(zjTournamentCreateData.getPhaseOneStartGw())
                .setGroupEndGw(zjTournamentCreateData.getPhaseTwoEndGw())
                .setGroupFillAverage(false)
                .setGroupRounds(0)
                .setGroupPlayAgainstNum(0)
                .setGroupQualifiers(0)
                .setKnockoutMode(KnockoutMode.PK.name())
                .setKnockoutTeam(zjTournamentCreateData.getTotalTeam())
                .setKnockoutRounds(zjTournamentCreateData.getPkRound())
                .setKnockoutEvents(zjTournamentCreateData.getPkRound())
                .setKnockoutPlayAgainstNum(1)
                .setKnockoutStartGw(zjTournamentCreateData.getPkStartGw())
                .setKnockoutEndGw(zjTournamentCreateData.getPkEndGw())
                .setState(1);
        // save
        this.tournamentInfoService.save(tournamentInfoEntity);
        // publish event
        this.context.publishEvent(new CreateZjTournamentEventData(this, zjTournamentCreateData));
        return "创建成功！";
    }

    @Override
    public void createNewZjTournamentBackground(ZjTournamentCreateData zjTournamentCreateData) {
        List<ZjTournamentGroupData> groupDataList = zjTournamentCreateData.getGroupDataList();
        if (CollectionUtils.isEmpty(groupDataList)) {
            log.error("zj tournament:{}, input group data can not be empty!", zjTournamentCreateData.getTournamentName());
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getName, zjTournamentCreateData.getTournamentName())
                .eq(TournamentInfoEntity::getState, 1));
        if (tournamentInfoEntity == null) {
            log.error("zj tournament:{}, not exists!", zjTournamentCreateData.getTournamentName());
            return;
        }
        int tournamentId = tournamentInfoEntity.getId();
        // get entry list
        List<Integer> entryList = Lists.newArrayList();
        groupDataList.forEach(o -> entryList.addAll(o.getGroupEntryList()));
        // save zj entry_info
        this.saveZjTournamentEntryInfo(tournamentId, entryList);
        // draw groups
        this.drawZjGroups(tournamentId, groupDataList, groupDataList.size(), groupDataList.get(0).getGroupEntryList().size(),
                zjTournamentCreateData.getPhaseOneStartGw(), zjTournamentCreateData.getPhaseOneEndGw(),
                zjTournamentCreateData.getPhaseTwoStartGw(), zjTournamentCreateData.getPhaseTwoEndGw());
        // draw pk, one round knockout
        this.drawZjPkKnockout(tournamentId, entryList, zjTournamentCreateData.getPkRound(), zjTournamentCreateData.getPkStartGw(), zjTournamentCreateData.getPkEndGw());
        // save zj_tournament_result
        this.saveZjTournamentResult(tournamentId, groupDataList.size());
    }

    private void saveZjTournamentEntryInfo(int tournamentId, List<Integer> entryList) {
        // entry_info
        List<EntryInfoEntity> insertEntryInfoList = Lists.newArrayList();
        List<EntryInfoEntity> updateEntryInfoList = Lists.newArrayList();
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list()
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        entryList.parallelStream().forEach(entry -> {
            Optional<EntryRes> entryRes = this.staticService.getEntry(entry);
            entryRes.ifPresent(o -> {
                if (!entryInfoMap.containsKey(entry)) {
                    insertEntryInfoList.add(new EntryInfoEntity()
                            .setEntry(entry)
                            .setEntryName(o.getName())
                            .setPlayerName(o.getPlayerFirstName() + " " + o.getPlayerLastName())
                            .setRegion(o.getPlayerRegionName())
                            .setStartedEvent(o.getStartedEvent())
                            .setOverallPoints(o.getSummaryOverallPoints())
                            .setOverallRank(o.getSummaryOverallRank())
                            .setBank(o.getLastDeadlineBank())
                            .setTeamValue(o.getLastDeadlineValue())
                            .setTotalTransfers(o.getLastDeadlineTotalTransfers())
                    );
                } else {
                    updateEntryInfoList.add(new EntryInfoEntity()
                            .setEntry(entry)
                            .setEntryName(o.getName())
                            .setPlayerName(o.getPlayerFirstName() + " " + o.getPlayerLastName())
                            .setRegion(o.getPlayerRegionName())
                            .setStartedEvent(o.getStartedEvent())
                            .setOverallPoints(o.getSummaryOverallPoints())
                            .setOverallRank(o.getSummaryOverallRank())
                            .setBank(o.getLastDeadlineBank())
                            .setTeamValue(o.getLastDeadlineValue())
                            .setTotalTransfers(o.getLastDeadlineTotalTransfers())
                    );
                }
            });
        });
        this.entryInfoService.saveBatch(insertEntryInfoList);
        this.entryInfoService.updateBatchById(updateEntryInfoList);
        // tournament_entry
        List<TournamentEntryEntity> tournamentEntryList = Lists.newArrayList();
        entryList.forEach(entry ->
                tournamentEntryList.add(new TournamentEntryEntity()
                        .setTournamentId(tournamentId)
                        .setLeagueId(0)
                        .setEntry(entry)
                ));
        this.tournamentEntryService.saveBatch(tournamentEntryList);
        log.info("zj tournament:{}, save entry info success!", tournamentId);
    }

    private void drawZjGroups(int tournamentId, List<ZjTournamentGroupData> groupDataList, int groupNum, int teamPerGroup,
                              int phaseOneStartGw, int phaseOneEndGw, int phaseTwoStartGw, int phaseTwoEndGw) {
        List<ZjTournamentCaptainEntity> zjTournamentCaptainList = Lists.newArrayList();
        List<TournamentGroupEntity> tournamentGroupList = Lists.newArrayList();
        List<TournamentPointsGroupResultEntity> tournamentPointsGroupResultList = Lists.newArrayList();
        groupDataList.forEach(o -> {
            int phaseOneGroupId = o.getGroupId();
            // group captain
            zjTournamentCaptainList.add(new ZjTournamentCaptainEntity()
                    .setTournamentId(tournamentId)
                    .setGroupId(phaseOneGroupId)
                    .setCaptainEntry(o.getCaptainEntry())
                    .setPhaseTwoDeadline(o.getPhaseTwoDeadline())
                    .setPkDeadline(o.getPkDeadline())
            );
            for (int i = 1; i < teamPerGroup + 1; i++) {
                int phaseOneEntry = o.getGroupEntryList().get(i - 1);
                // phase one points_group
                tournamentGroupList.add(new TournamentGroupEntity()
                        .setTournamentId(tournamentId)
                        .setGroupId(phaseOneGroupId)
                        .setGroupName(o.getGroupName())
                        .setGroupIndex(i)
                        .setEntry(phaseOneEntry)
                        .setStartGw(phaseOneStartGw)
                        .setEndGw(phaseOneEndGw)
                        .setGroupPoints(0)
                        .setGroupRank(0)
                        .setPlay(0)
                        .setWin(0)
                        .setDraw(0)
                        .setLose(0)
                        .setTotalPoints(0)
                        .setQualified(false)
                        .setOverallRank(0)
                );
                // phase one points_group_result
                IntStream.rangeClosed(phaseOneStartGw, phaseOneEndGw).forEach(event ->
                        tournamentPointsGroupResultList.add(new TournamentPointsGroupResultEntity()
                                .setTournamentId(tournamentId)
                                .setGroupId(phaseOneGroupId)
                                .setEvent(event)
                                .setEntry(phaseOneEntry)
                                .setEventGroupRank(0)
                                .setEventPoints(0)
                                .setEventCost(0)
                                .setEventNetPoints(0)
                                .setEventRank(0)
                        ));
                // phase two points_group
                int phaseTwoGroupId = groupNum + i;
                int phaseTwoEntry = -1 * (10 * phaseOneGroupId + i);
                tournamentGroupList.add(new TournamentGroupEntity()
                        .setTournamentId(tournamentId)
                        .setGroupId(phaseTwoGroupId)
                        .setGroupName(o.getGroupName())
                        .setGroupIndex(phaseOneGroupId)
                        .setEntry(phaseTwoEntry)
                        .setStartGw(phaseTwoStartGw)
                        .setEndGw(phaseTwoEndGw)
                        .setGroupPoints(0)
                        .setGroupRank(0)
                        .setPlay(0)
                        .setWin(0)
                        .setDraw(0)
                        .setLose(0)
                        .setTotalPoints(0)
                        .setQualified(false)
                        .setOverallRank(0)
                );
                // phase two points_group_result
                IntStream.rangeClosed(phaseTwoStartGw, phaseTwoEndGw).forEach(event ->
                        tournamentPointsGroupResultList.add(new TournamentPointsGroupResultEntity()
                                .setTournamentId(tournamentId)
                                .setGroupId(phaseTwoGroupId)
                                .setEvent(event)
                                .setEntry(phaseTwoEntry)
                                .setEventGroupRank(0)
                                .setEventPoints(0)
                                .setEventCost(0)
                                .setEventNetPoints(0)
                                .setEventRank(0)
                        ));
            }
        });
        this.zjTournamentCaptainService.saveBatch(zjTournamentCaptainList);
        this.tournamentGroupService.saveBatch(tournamentGroupList);
        this.tournamentPointsGroupResultService.saveBatch(tournamentPointsGroupResultList);
        log.info("zj tournament:{}, draw group success!", tournamentId);
    }

    private void drawZjPkKnockout(int tournamentId, List<Integer> entryList, int pkRound, int pkStartGw, int pkEndGw) {
        List<TournamentKnockoutEntity> tournamentKnockoutList = Lists.newArrayList();
        List<Integer> pkList = Lists.newArrayList();
        IntStream.rangeClosed(1, entryList.size()).forEach(pkEntry -> pkList.add(-1 * pkEntry));
        List<List<Integer>> drawLists = Lists.partition(pkList, 2);
        int matchNum = (int) Math.pow(2, pkRound - 1);
        IntStream.rangeClosed(1, drawLists.size()).forEach(i -> {
            List<Integer> subList = drawLists.get(i - 1);
            tournamentKnockoutList.add(new TournamentKnockoutEntity()
                    .setTournamentId(tournamentId)
                    .setRound(1)
                    .setStartGw(pkStartGw)
                    .setEndGw(pkEndGw)
                    .setHomeEntry(subList.get(0))
                    .setAwayEntry(subList.get(1))
                    .setMatchId(i)
                    .setNextMatchId(i % 2 == 0 ? (i / 2) + matchNum : ((i + 1) / 2) + matchNum)
                    .setRoundWinner(0)
            );
        });
        this.tournamentKnockoutService.saveBatch(tournamentKnockoutList);
        // create knockout result
        this.createKnockoutResult(tournamentKnockoutList, 1);
        log.info("zj tournament:{}, draw pk knockout success!", tournamentId);
    }

    private void saveZjTournamentResult(int tournamentId, int groupNum) {
        List<ZjTournamentResultEntity> zjTournamentResultEntityList = Lists.newArrayList();
        // tournament_group
        List<Integer> groupList = Lists.newArrayList();
        IntStream.rangeClosed(1, groupNum).forEach(groupList::add);
        this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                        .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                        .in(TournamentGroupEntity::getGroupId, groupList)
                        .eq(TournamentGroupEntity::getGroupIndex, 1))
                .forEach(o ->
                        zjTournamentResultEntityList.add(new ZjTournamentResultEntity()
                                .setTournamentId(tournamentId)
                                .setGroupId(o.getGroupId())
                                .setGroupName(o.getGroupName())
                                .setPhaseOneTotalPoints(0)
                                .setPhaseOneGroupPoints(0)
                                .setPhaseOneTotalGroupPoints(0)
                                .setPhaseTwoTotalPoints(0)
                                .setPhaseTwoGroupPoints(0)
                                .setPhaseTwoTotalGroupPoints(0)
                                .setPkTotalPoints(0)
                                .setPkGroupPoints(0)
                                .setPkTotalGroupPoints(0)
                                .setTournamentTotalPoints(0)
                                .setTournamentTotalGroupPoints(0)
                                .setTournamentRank(0)
                        ));
        this.zjTournamentResultService.saveBatch(zjTournamentResultEntityList);
        log.info("zj tournament:{}, save result success!", tournamentId);
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
        this.updateTournamentNewEntryEventResult(currentEvent, tournamentId, groupStartGw, groupEndGw, newEntryList);
        // update tournament_points_group_result
        this.updateTournamentPointsGroupResult(currentEvent, tournamentId, groupStartGw, groupEndGw);
        // update league_event_report
        newEntryList.forEach(entry -> this.updateEntryTournamentLeagueEventReport(currentEvent, leagueId, leagueType, tournamentId, entry));
        // return
        log.info("赛事：{}，更新联赛队伍成功，新增队伍数量:{}，更新名单：{}", tournamentId, newEntryInfoList.size(), newEntryInfoList);
    }

    private List<EntryInfoData> saveTournamentNewEntryInfo(int tournamentId, TournamentInfoEntity tournamentInfoEntity, String leagueType, int leagueId) {
        // tournament_entry
        List<Integer> tournamentEntryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                        .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tournamentEntryList)) {
            return Lists.newArrayList();
        }
        //  get entry list from fpl server
        List<EntryInfoData> entryInfoList = Lists.newArrayList();
        if (LeagueType.valueOf(leagueType) == LeagueType.Classic) {
            entryInfoList = this.staticService.getEntryInfoListFromClassic(leagueId);
        } else if (LeagueType.valueOf(leagueType) == LeagueType.H2h) {
            entryInfoList = this.staticService.getEntryInfoListFromH2h(leagueId);
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
        List<EntryInfoEntity> entryInfoEntityList = Lists.newArrayList();
        newEntryInfoList.parallelStream().forEach(entryInfoEntity -> {
            int entry = entryInfoEntity.getEntry();
            EntryInfoEntity newEntryInfoEntity = new EntryInfoEntity().setEntry(entry);
            Optional<EntryRes> entryRes = this.staticService.getEntry(entry);
            entryRes.ifPresent(o ->
                    newEntryInfoEntity
                            .setEntryName(o.getName())
                            .setPlayerName(o.getPlayerFirstName() + " " + o.getPlayerLastName())
                            .setRegion(o.getPlayerRegionName())
                            .setStartedEvent(o.getStartedEvent())
                            .setOverallPoints(o.getSummaryOverallPoints())
                            .setOverallRank(o.getSummaryOverallRank())
                            .setBank(o.getLastDeadlineBank())
                            .setTeamValue(o.getLastDeadlineValue())
                            .setTotalTransfers(o.getLastDeadlineTotalTransfers())
                            .setLastTeamValue(o.getLastDeadlineValue())
            );
            Optional<UserPicksRes> userPicksRes = this.staticService.getUserPicks(this.queryService.getLastEvent(), entry);
            userPicksRes.ifPresent(o ->
                    newEntryInfoEntity
                            .setLastOverallPoints(o.getEntryHistory().getTotalPoints())
                            .setLastOverallRank(o.getEntryHistory().getOverallRank())
            );
            entryInfoEntityList.add(newEntryInfoEntity);
        });
        this.entryInfoService.saveOrUpdateBatch(entryInfoEntityList);
        // save tournament_entry
        List<TournamentEntryEntity> tournamentEntryEntityList = Lists.newArrayList();
        entryInfoEntityList.forEach(entryInfoEntity ->
                tournamentEntryEntityList.add(new TournamentEntryEntity()
                        .setTournamentId(tournamentId)
                        .setLeagueId(leagueId)
                        .setEntry(entryInfoEntity.getEntry())
                ));
        this.tournamentEntryService.saveBatch(tournamentEntryEntityList);
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
        IntStream.rangeClosed(groupStartGw, currentEvent).forEach(event ->
                newEntryList.forEach(entry ->
                        this.eventDataService.insertEntryEventPick(event, entry)));
        log.info("tournament:{}, update new entry event pick success!", tournamentId);
    }

    private void updateTournamentNewEntryEventResult(int currentEvent, int tournamentId, int groupStartGw, int groupEndGw, List<Integer> newEntryList) {
        if (currentEvent < groupStartGw) {
            return;
        }
        if (currentEvent > groupEndGw) {
            currentEvent = groupEndGw;
        }
        IntStream.rangeClosed(groupStartGw, currentEvent).forEach(event ->
                newEntryList.forEach(entry ->
                        this.eventDataService.upsertEntryEventResult(event, entry)));
        log.info("tournament:{}, update new entry event result success!", tournamentId);
    }

    private void updateTournamentPointsGroupResult(int currentEvent, int tournamentId, int groupStartGw, int groupEndGw) {
        if (currentEvent < groupStartGw) {
            return;
        }
        if (currentEvent > groupEndGw) {
            currentEvent = groupEndGw;
        }
        IntStream.rangeClosed(groupStartGw, currentEvent).forEach(event ->
                this.eventDataService.updatePointsRaceGroupResult(event, tournamentId));
        log.info("tournament:{}, update points group result success!", tournamentId);
    }

    private void updateEntryTournamentLeagueEventReport(int currentEvent, int leagueId, String leagueType, int tournamentId, int entry) {
        IntStream.rangeClosed(1, currentEvent).forEach(event -> {
            if (!this.queryService.qryTournamentUpdateNeeded(event, tournamentId)) {
                return;
            }
            if (this.reportService.entryEvenLeagueEventExists(event, leagueId, leagueType, entry)) {
                return;
            }
            this.reportService.insertEntryLeagueEventSelect(event, leagueId, leagueType, entry);
            this.reportService.updateEntryLeagueEventResult(event, leagueId, leagueType, entry);
            log.info("tournament:{}, event:{}, entry:{}, update entry tournament league event report success!", tournamentId, event, entry);
        });
    }

    @Override
    public String updateZjTournamentPhaseTwoGroupData(List<TournamentGroupData> groupDataList, int captainEntry) {
        int tournamentId = groupDataList.get(0).getTournamentId();
        // deadline
        ZjTournamentCaptainEntity zjTournamentCaptainEntity = this.zjTournamentCaptainService.getOne(new QueryWrapper<ZjTournamentCaptainEntity>().lambda()
                .eq(ZjTournamentCaptainEntity::getTournamentId, tournamentId)
                .eq(ZjTournamentCaptainEntity::getCaptainEntry, captainEntry));
        if (zjTournamentCaptainEntity == null) {
            return "无队长数据";
        }
        if (LocalDateTime.now().isAfter(LocalDateTime.parse(zjTournamentCaptainEntity.getPhaseTwoDeadline(), DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
            return "死线已过，提交不了！";
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return "赛事不存在！";
        }
        int groupNum = tournamentInfoEntity.getGroupNum();
        int teamPerGroup = tournamentInfoEntity.getTeamPerGroup();
        groupDataList.forEach(o -> {
            int groupId = o.getGroupId();
            // tournament_group
            TournamentGroupEntity tournamentGroupEntity = this.tournamentGroupService.getOne(new QueryWrapper<TournamentGroupEntity>().lambda()
                    .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                    .eq(TournamentGroupEntity::getGroupId, groupId)
                    .eq(TournamentGroupEntity::getGroupName, o.getGroupName()));
            if (tournamentGroupEntity == null) {
                return;
            }
            tournamentGroupEntity.setEntry(o.getEntry());
            this.tournamentGroupService.updateById(tournamentGroupEntity);
            // tournament_points_group_result
            int phaseTwoGroupId = groupId + groupNum;
            int virtualEntry = -1 * (groupId * 10 + (phaseTwoGroupId - teamPerGroup));
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = this.tournamentPointsGroupResultService.getOne(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                    .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                    .eq(TournamentPointsGroupResultEntity::getGroupId, phaseTwoGroupId)
                    .eq(TournamentPointsGroupResultEntity::getEntry, virtualEntry));
            if (tournamentPointsGroupResultEntity == null) {
                return;
            }
            tournamentPointsGroupResultEntity.setEntry(o.getEntry());
            this.tournamentPointsGroupResultService.updateById(tournamentPointsGroupResultEntity);
        });
        return "分配小组成功!";
    }

    @Override
    public String updateZjTournamentPkData(int tournamentId, int entry, int pkEntry, int captainEntry) {
        // deadline
        List<ZjTournamentCaptainEntity> zjTournamentCaptainList = this.zjTournamentCaptainService.list(new QueryWrapper<ZjTournamentCaptainEntity>().lambda()
                .eq(ZjTournamentCaptainEntity::getTournamentId, tournamentId));
        if (CollectionUtils.isEmpty(zjTournamentCaptainList)) {
            return "无队长数据";
        }
        if (LocalDateTime.now().isAfter(LocalDateTime.parse(zjTournamentCaptainList.get(0).getPkDeadline(), DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
            return "死线已过，提交不了！";
        }
        // group captain_entry
        Map<Integer, Integer> groupCaptainMap = zjTournamentCaptainList
                .stream()
                .collect(Collectors.toMap(ZjTournamentCaptainEntity::getGroupId, ZjTournamentCaptainEntity::getCaptainEntry));
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return "赛事不存在！";
        }
        int groupNum = tournamentInfoEntity.getGroupNum();
        // group tournament rank
        List<Integer> groupRankList = this.zjTournamentResultService.list(new QueryWrapper<ZjTournamentResultEntity>().lambda()
                        .eq(ZjTournamentResultEntity::getTournamentId, tournamentId)
                        .orderByAsc(ZjTournamentResultEntity::getTournamentRank))
                .stream()
                .map(ZjTournamentResultEntity::getGroupId)
                .collect(Collectors.toList());
        // tournament_knockout
        List<TournamentKnockoutEntity> tournamentKnockoutEntityList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutEntity::getRound, 1)
                .orderByAsc(TournamentKnockoutEntity::getMatchId));
        if (CollectionUtils.isEmpty(tournamentKnockoutEntityList)) {
            return "淘汰赛不存在！";
        }
        int matchNum = tournamentKnockoutEntityList.size();
        // pick order
        LinkedHashMap<Integer, Integer> pickOrderMap = Maps.newLinkedHashMap(); // matchId -> captainEntry
        IntStream.range(0, matchNum / groupNum).forEach(repeatTime -> {
            for (int i = 1; i < groupRankList.size() + 1; i++) {
                int matchId = i + groupNum * repeatTime;
                int captain = groupCaptainMap.getOrDefault(groupRankList.get(i - 1), 0);
                pickOrderMap.put(matchId, captain);
            }
        });
        int pickMatchId = tournamentKnockoutEntityList
                .stream()
                .filter(o -> o.getHomeEntry() < 0 && o.getAwayEntry() < 0)
                .map(TournamentKnockoutEntity::getMatchId)
                .findFirst()
                .orElse(0);
        int pickCaptain = pickOrderMap.get(pickMatchId);
        if (pickCaptain != captainEntry) {
            EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(pickCaptain);
            return "不是你的分配轮次，请等待：" + entryInfoEntity.getEntryName() + " (" + entryInfoEntity.getPlayerName() + ") 分配！";
        }
        // tournament_knockout
        tournamentKnockoutEntityList
                .stream()
                .filter(o -> o.getMatchId() == pickMatchId)
                .findFirst()
                .ifPresent(o -> {
                    o.setHomeEntry(entry).setAwayEntry(pkEntry);
                    this.tournamentKnockoutService.updateById(o);
                });
        // tournament_knockout_result
        TournamentKnockoutResultEntity tournamentKnockoutResultEntity = this.tournamentKnockoutResultService.getOne(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutResultEntity::getMatchId, pickMatchId)
                .eq(TournamentKnockoutResultEntity::getPlayAgainstId, 1));
        if (tournamentKnockoutResultEntity != null) {
            tournamentKnockoutResultEntity.setHomeEntry(entry).setAwayEntry(pkEntry);
            this.tournamentKnockoutResultService.updateById(tournamentKnockoutResultEntity);
        }
        if (pickMatchId < pickOrderMap.size()) {
            return "PK对阵分配成功！请等待下一轮次选择！";
        } else {
            return "PK对阵分配成功！PK赛分组结束！";
        }
    }

}
