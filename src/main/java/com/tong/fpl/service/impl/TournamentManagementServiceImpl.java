package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.ChipEnum;
import com.tong.fpl.constant.GroupModeEnum;
import com.tong.fpl.constant.KnockoutModeEnum;
import com.tong.fpl.constant.LeagueTypeEnum;
import com.tong.fpl.domain.data.fpl.QueryParam;
import com.tong.fpl.domain.data.fpl.TournamentCreateData;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.event.CreateTournamentEventData;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.ITournamentManagementService;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/6/11
 */
@Validated
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentManagementServiceImpl implements ITournamentManagementService {

	private final ApplicationContext context;
	private final EntryInfoService entryInfoService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentGroupService tournamentGroupService;
	private final TournamentGroupResultService tournamentGroupResultService;
	private final TournamentKnockoutService tournamentKnockoutService;
	private final TournamentKnockoutResultService tournamentKnockoutResultService;
	private final IStaticSerive staticSerive;

	@Override
	public String createNewTournament(TournamentCreateData tournamentCreateData) {
		if (GroupModeEnum.valueOf(tournamentCreateData.getGroupMode()) == GroupModeEnum.No_group &&
				KnockoutModeEnum.valueOf(tournamentCreateData.getKnockoutMode()) == KnockoutModeEnum.No_knockout) {
			return "创建失败，小组赛和淘汰赛至少要有一项！";
		}
		TournamentInfoEntity tournamentInfoEntity = new TournamentInfoEntity();
		if (this.tournamentInfoService.count(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, tournamentCreateData.getTournamentName())) > 0) {
			return "创建失败，赛事已存在，请更换名称！";
		}
		// config basic info
		tournamentInfoEntity.setName(tournamentCreateData.getTournamentName());
		tournamentInfoEntity.setCreator(tournamentCreateData.getCreator());
		tournamentInfoEntity.setLeagueType(this.setLeagueTypeByUrl(tournamentCreateData.getUrl()));
		tournamentInfoEntity.setLeagueId(this.setLeagueIdByType(tournamentCreateData.getUrl(), tournamentInfoEntity.getLeagueType()));
		tournamentInfoEntity.setTotalTeam(this.calcTotalTeamInLeague(tournamentInfoEntity.getLeagueType(), tournamentInfoEntity.getLeagueId()));
		tournamentInfoEntity.setKnockoutPlayAgainstNum(this.setKnouckoutPlayAgainstNum(tournamentCreateData.getKnockoutMode()));
		// config group info
		this.configGroupInfo(tournamentInfoEntity, tournamentCreateData);
		// config knockout info
		this.configKnockoutInfo(tournamentInfoEntity, tournamentCreateData);
		// save
		this.tournamentInfoService.save(tournamentInfoEntity);
		// publish event
		context.publishEvent(new CreateTournamentEventData(this, tournamentCreateData.getTournamentName()));
		return "创建成功！";
	}

	private String setLeagueTypeByUrl(String url) {
		if (url.contains("/standings/c")) {
			return LeagueTypeEnum.Classic.name();
		} else if (url.contains("/standings/h")) {
			return LeagueTypeEnum.H2h.name();
		}
		return "";
	}

	private int setLeagueIdByType(String url, String leagueType) {
		switch (LeagueTypeEnum.valueOf(leagueType)) {
			case Classic:
				return Integer.parseInt(StringUtils.substringBetween(url, "https://fantasy.premierleague.com/leagues/", "/standings/c"));
			case H2h:
				return Integer.parseInt(StringUtils.substringBetween(url, "https://fantasy.premierleague.com/leagues/", "/standings/h"));
		}
		return 0;
	}

	private int calcTotalTeamInLeague(String leagueType, int leagueId) {
		switch (LeagueTypeEnum.valueOf(leagueType)) {
			case Classic:
				return this.staticSerive.getEntryInfoListFromClassic(leagueId).size();
			case H2h:
				return this.staticSerive.getEntryInfoListFromH2h(leagueId).size();
		}
		return 0;
	}

	private int setKnouckoutPlayAgainstNum(String knockoutMode) {
		switch (KnockoutModeEnum.valueOf(knockoutMode)) {
			case No_knockout:
				return 0;
			case Single_round:
				return 1;
			case Home_away:
				return 2;
		}
		return 0;
	}

	private void configGroupInfo(TournamentInfoEntity tournamentInfoEntity, TournamentCreateData tournamentCreateData) {
		GroupModeEnum groupMode = GroupModeEnum.valueOf(tournamentCreateData.getGroupMode());
		tournamentInfoEntity.setGroupMode(groupMode.toString());
		switch (groupMode) {
			case No_group: {
				tournamentInfoEntity.setGroupPlayAgainstNum(0);
				tournamentInfoEntity.setTeamPerGroup(0);
				tournamentInfoEntity.setGroupStartGw(-1);
				tournamentInfoEntity.setGroupEndGw(-1);
				tournamentInfoEntity.setGroupRounds(0);
				tournamentInfoEntity.setGroupQualifiers(0);
				tournamentInfoEntity.setGroupFillAverage(false);
				tournamentInfoEntity.setGroupNum(0);
				break;
			}
			case Points_race: {
				tournamentInfoEntity.setGroupPlayAgainstNum(0);
				tournamentInfoEntity.setTeamPerGroup(tournamentCreateData.getTeamsPerGroup());
				tournamentInfoEntity.setGroupStartGw(CommonUtils.getRealGw(tournamentCreateData.getGroupStartGw()));
				tournamentInfoEntity.setGroupEndGw(CommonUtils.getRealGw(tournamentCreateData.getGroupEndGw()));
				tournamentInfoEntity.setGroupRounds(tournamentInfoEntity.getGroupEndGw() - tournamentInfoEntity.getGroupStartGw() + 1);
				tournamentInfoEntity.setGroupQualifiers(tournamentCreateData.getGroupQualifiers());
				tournamentInfoEntity.setGroupFillAverage(false);
				tournamentInfoEntity.setGroupNum((int) (Math.ceil(tournamentInfoEntity.getTotalTeam() * 1.0 / tournamentInfoEntity.getTeamPerGroup())));
				break;
			}
			case Battle_race: {
				tournamentInfoEntity.setGroupPlayAgainstNum(tournamentCreateData.getGroupPlayAgainstNum());
				tournamentInfoEntity.setTeamPerGroup(tournamentCreateData.getTeamsPerGroup());
				tournamentInfoEntity.setGroupStartGw(CommonUtils.getRealGw(tournamentCreateData.getGroupStartGw()));
				tournamentInfoEntity.setGroupRounds((tournamentInfoEntity.getTeamPerGroup() - 1) * tournamentInfoEntity.getGroupPlayAgainstNum());
				tournamentInfoEntity.setGroupEndGw(tournamentInfoEntity.getGroupStartGw() + tournamentInfoEntity.getGroupRounds() - 1);
				tournamentInfoEntity.setGroupQualifiers(tournamentCreateData.getGroupQualifiers());
				tournamentInfoEntity.setGroupFillAverage(tournamentCreateData.isGroupFillAverage());
				tournamentInfoEntity.setGroupNum((int) (Math.ceil(tournamentInfoEntity.getTotalTeam() * 1.0 / tournamentInfoEntity.getTeamPerGroup())));
				break;
			}
			case All_group: {
				tournamentInfoEntity.setGroupPlayAgainstNum(tournamentCreateData.getGroupPlayAgainstNum());
				tournamentInfoEntity.setTeamPerGroup(tournamentCreateData.getTeamsPerGroup());
				tournamentInfoEntity.setGroupStartGw(CommonUtils.getRealGw(tournamentCreateData.getGroupStartGw()));
				tournamentInfoEntity.setGroupRounds((tournamentInfoEntity.getTeamPerGroup() - 1) * tournamentInfoEntity.getGroupPlayAgainstNum());
				tournamentInfoEntity.setGroupEndGw(tournamentInfoEntity.getGroupStartGw() + tournamentInfoEntity.getGroupRounds() - 1);
				tournamentInfoEntity.setGroupQualifiers(tournamentCreateData.getGroupQualifiers());
				tournamentInfoEntity.setGroupFillAverage(tournamentCreateData.isGroupFillAverage());
				tournamentInfoEntity.setGroupNum((int) (Math.ceil(tournamentInfoEntity.getTotalTeam() * 1.0 / tournamentInfoEntity.getTeamPerGroup())));
				tournamentInfoEntity.setKnockoutMode(KnockoutModeEnum.No_knockout.name());
				break;
			}
		}
	}

	private void configKnockoutInfo(TournamentInfoEntity tournamentInfoEntity, TournamentCreateData tournamentCreateData) {
		KnockoutModeEnum knockoutMode = KnockoutModeEnum.valueOf(tournamentCreateData.getKnockoutMode());
		tournamentInfoEntity.setKnockoutMode(knockoutMode.toString());
		switch (knockoutMode) {
			case No_knockout: {
				tournamentInfoEntity.setKnockoutTeam(0);
				tournamentInfoEntity.setKnockoutStartGw(-1);
				tournamentInfoEntity.setKnockoutEndGw(-1);
				tournamentInfoEntity.setKnockoutRounds(0);
				break;
			}
			case Single_round:
			case Home_away: {
				if (GroupModeEnum.valueOf(tournamentInfoEntity.getGroupMode()) == GroupModeEnum.No_group) {
					tournamentInfoEntity.setKnockoutTeam(tournamentInfoEntity.getTotalTeam());
					tournamentInfoEntity.setKnockoutStartGw(CommonUtils.getRealGw(tournamentCreateData.getKnockoutStartGw()));
					tournamentInfoEntity.setKnockoutRounds((int) Math.ceil(Math.log(tournamentInfoEntity.getKnockoutTeam()) / Math.log(2)) *
							tournamentInfoEntity.getKnockoutPlayAgainstNum());
					tournamentInfoEntity.setKnockoutEndGw(tournamentInfoEntity.getKnockoutStartGw() + tournamentInfoEntity.getKnockoutRounds() - 1);
				} else {
					tournamentInfoEntity.setKnockoutTeam(tournamentInfoEntity.getGroupQualifiers() * tournamentInfoEntity.getGroupNum());
					tournamentInfoEntity.setKnockoutStartGw(tournamentInfoEntity.getGroupEndGw() + 1);
					tournamentInfoEntity.setKnockoutRounds((int) Math.ceil(Math.log(tournamentInfoEntity.getKnockoutTeam()) / Math.log(2)) *
							tournamentInfoEntity.getKnockoutPlayAgainstNum());
					tournamentInfoEntity.setKnockoutEndGw(tournamentInfoEntity.getKnockoutStartGw() + tournamentInfoEntity.getKnockoutRounds() - 1);
				}
				break;
			}
		}
	}

	@Override
	public void createNewTournamentBackground(String tournamentName) {
		TournamentInfoEntity tournamentInfo = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, tournamentName));
		if (tournamentInfo == null) {
			return;
		}
		int tournamentId = tournamentInfo.getId();
		String groupMode = tournamentInfo.getGroupMode();
		int groupNum = tournamentInfo.getGroupNum();
		// save entry_info
		this.saveTournamentEntryInfo(tournamentId, tournamentInfo.getLeagueType(), tournamentInfo.getLeagueId());
		// draw groups
		this.drawGroups(tournamentId, groupMode, tournamentInfo.getTeamPerGroup(), tournamentInfo.getGroupFillAverage(), groupNum);
		// draw group battle
		this.drawGroupBattle(tournamentId, groupMode, tournamentInfo.getGroupPlayAgainstNum(), tournamentInfo.getKnockoutTeam(), groupNum);
		// draw knockouts
		this.drawKnockouts(tournamentId, groupMode, groupNum, tournamentInfo.getGroupQualifiers(),
				tournamentInfo.getKnockoutPlayAgainstNum(), tournamentInfo.getKnockoutTeam(), tournamentInfo.getKnockoutStartGw(), tournamentInfo.getKnockoutRounds());
	}

	@Override
	public void saveTournamentEntryInfo(int tournamentId, String leagueType, int leagueId) {
		this.entryInfoService.remove(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getTournamentId, tournamentId));
		List<EntryInfoEntity> entryInfoEntityList = Lists.newArrayList();
		if (LeagueTypeEnum.valueOf(leagueType) == LeagueTypeEnum.Classic) {
			entryInfoEntityList = this.staticSerive.getEntryInfoListFromClassic(leagueId);
		} else if (LeagueTypeEnum.valueOf(leagueType) == LeagueTypeEnum.H2h) {
			entryInfoEntityList = this.staticSerive.getEntryInfoListFromH2h(leagueId);
		}
		entryInfoEntityList.forEach(entryInfoEntity -> entryInfoEntity.setTournamentId(tournamentId).setLeagueId(leagueId));
		this.entryInfoService.saveBatch(entryInfoEntityList);
		log.info("create entry info success!");
	}

	@Override
	public void drawGroups(int tournamentId, String groupMode, int teamsPerGroup, boolean groupFillAverage, int groupNum) {
		if (GroupModeEnum.valueOf(groupMode) == GroupModeEnum.No_group) {
			return;
		}
		Multimap<Integer, Integer> teamInGroupMap = ArrayListMultimap.create();
		// check exist
		if (this.tournamentGroupService.count(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)) > 0) {
			log.info("tournament-{} groups exist!", tournamentId);
			return;
		}
		// get entryList from input classic league
		List<Integer> entryList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getTournamentId, tournamentId))
				.stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
		// shuffle
		Collections.shuffle(entryList);
		// add average, represent by nagative num
		Random random = new Random();
		if (groupFillAverage) {
			int averageNum = groupNum * teamsPerGroup - entryList.size();
			IntStream.range(1, averageNum + 1).forEach(i -> {
				int entry = -1 * i;
				this.tournamentGroupService.save(new TournamentGroupEntity()
						.setTournamentId(tournamentId)
						.setGroupId(this.drawAverageToGroup(random, entry, groupNum, teamInGroupMap))
						.setEntry(entry)
						.setPoints(0)
						.setRank(0)
						.setCreateTime(new Date())
				);
			});
		}
		// draw entry list
		IntStream.range(0, entryList.size()).forEach(i -> this.tournamentGroupService.save(new TournamentGroupEntity()
				.setTournamentId(tournamentId)
				.setGroupId(this.drawToGroup(random, entryList.get(i), groupNum, teamsPerGroup, teamInGroupMap))
				.setIndex(i + 1)
				.setEntry(entryList.get(i))
				.setPoints(0)
				.setPlay(0)
				.setWin(0)
				.setDraw(0)
				.setLose(0)
				.setRank(0)
				.setQualified(false)
				.setCreateTime(new Date())
		));
		log.info("draw groups success!");
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

	@Override
	public void drawGroupBattle(int tournamentId, String groupMode, int playAgainstNum, int teamsPerGroup, int groupNum) {
		if (GroupModeEnum.valueOf(groupMode) != GroupModeEnum.Battle_race) {
			return;
		}
		Multimap<Integer, String> abstractBattleMap = this.drawAbstarctBattle(teamsPerGroup, playAgainstNum);
		// draw single round
		List<TournamentGroupResultEntity> resultList = Lists.newArrayList();
		IntStream.range(1, groupNum + 1).forEach(groupId ->
				this.drawSingleGroupBattle(tournamentId, groupId, abstractBattleMap, resultList));
		// batch update all group result
		this.tournamentGroupResultService.saveBatch(resultList);
		log.info("draw group battle success!");
	}

	private Multimap<Integer, String> drawAbstarctBattle(int knockoutTeam, int playAgainstNum) {
		Multimap<Integer, String> abstractBattleMap = ArrayListMultimap.create();
		// make virtual entry list
		ArrayList<Integer> entryList = Lists.newArrayList();
		IntStream.range(1, knockoutTeam + 1).forEach(entryList::add);
		// make it even
		if (entryList.size() % 2 == 1) {
			entryList.add(0); // means blank
		}
		int entryNum = knockoutTeam % 2 == 0 ? knockoutTeam : knockoutTeam + 1;
		// play against each other once
		Multimap<Integer, String> abstractBattleOnceMap = ArrayListMultimap.create();
		LinkedList<Integer> battleList = Lists.newLinkedList(entryList);
		IntStream.range(1, entryNum).forEach(round -> {
			IntStream.range(0, entryNum / 2).forEach(i -> abstractBattleOnceMap.put(round, battleList.get(i) + "vs" + battleList.get(battleList.size() - 1 - i)));
			battleList.add(1, battleList.pollLast());
		});
		// play against each other more than once
		IntStream.range(1, playAgainstNum + 1).forEach(againstNum -> {
			if (againstNum % 2 == 1) {
				abstractBattleMap.putAll(abstractBattleOnceMap);
			} else {
				this.reverseBattleOnce(abstractBattleOnceMap, entryNum);
				abstractBattleMap.putAll(abstractBattleOnceMap);
			}
		});
		return abstractBattleMap;
	}

	private Multimap<Integer, String> reverseBattleOnce(Multimap<Integer, String> abstractBattleOnceMap, int entryNum) {
		IntStream.range(1, entryNum).forEach(round -> {
			Collection<String> roundBattleAgainst = abstractBattleOnceMap.get(round);
			roundBattleAgainst.forEach(battleAgainst -> abstractBattleOnceMap.put(round, StringUtils.substringAfter(battleAgainst, "vs")
					+ "vs" + StringUtils.substringBefore(battleAgainst, "vs")));
		});
		return abstractBattleOnceMap;
	}

	private void drawSingleGroupBattle(int tournamentId, int groupId, Multimap<Integer, String> abstractBattleMap, List<TournamentGroupResultEntity> resultList) {
		// get group entry list
		BiMap<Integer, Integer> groupIndexMap = HashBiMap.create();
		this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)
				.eq(TournamentGroupEntity::getGroupId, groupId)
				.orderByAsc(TournamentGroupEntity::getIndex))
				.forEach(tournamentGroupEntity -> groupIndexMap.put(tournamentGroupEntity.getIndex(), tournamentGroupEntity.getEntry()));
		if (CollectionUtils.isEmpty(groupIndexMap)) {
			return;
		}
		// update every group round
		abstractBattleMap.keySet().forEach(round -> {
			ArrayList<String> battleList = this.replaceBattleEntry(abstractBattleMap.get(round), groupIndexMap);
			if (CollectionUtils.isEmpty(battleList)) {
				return;
			}
			battleList.forEach(battle -> {
				int homeEntry = Integer.parseInt(StringUtils.substringBefore(battle, "vs"));
				int awayEntry = Integer.parseInt(StringUtils.substringAfter(battle, "vs"));
				resultList.add(new TournamentGroupResultEntity()
						.setTournamentId(tournamentId)
						.setGroupId(groupId)
						.setRound(round)
						.setHomeIndex(homeEntry == 0 ? 0 : groupIndexMap.inverse().get(homeEntry))
						.setHomeEntry(homeEntry)
						.setHomeEntryNetPoint(0)
						.setHomeEntryRank(0)
						.setAwayIndex(awayEntry == 0 ? 0 : groupIndexMap.inverse().get(awayEntry))
						.setAwayEntry(awayEntry)
						.setAwayEntryNetPoint(0)
						.setAwayEntryRank(0)
						.setRoundWinner(0)
						.setUpdateTime(new Date()));
			});
		});
	}

	private ArrayList<String> replaceBattleEntry(Collection<String> roundBattles, BiMap<Integer, Integer> groupIndexMap) {
		ArrayList<String> battleList = Lists.newArrayList();
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

	@Override
	public void drawKnockouts(int tournamentId, String groupMode, int groupNum, int groupQualifiers,
	                          int knockoutPlayAgainstNum, int knockoutTeam, int knockoutStartGw, int knockoutRounds) {
		if (knockoutPlayAgainstNum == 0) {
			return;
		}
		// check exist
		if (this.tournamentKnockoutService.count(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)) > 0) {
			log.info("tournament-{} knockouts exist!", tournamentId);
			return;
		}
		ArrayList<Integer> entryList = this.getKnockoutEntryList(tournamentId, groupMode, groupNum, groupQualifiers);
		if (CollectionUtils.isEmpty(entryList)) {
			log.info("draw knockouts no entry!");
			return;
		}
		// add blank teams
		int blankNum = (int) Math.pow(2, knockoutRounds) - knockoutTeam;
		if (blankNum >= entryList.size()) {
			return;
		}
		List<TournamentKnockoutEntity> knockoutEntityList = Lists.newArrayList();
		IntStream.range(1, blankNum + 1).forEach(i -> entryList.add(-1 * i));
		// shuffle
		do {
			Collections.shuffle(entryList);
		} while (this.checkDrawListLegal(entryList));
		// draw firstRound
		List<List<Integer>> drawLists = Lists.partition(entryList, 2);
		int firstRoundMatchNum = (int) Math.pow(2, knockoutRounds - 1);
		IntStream.range(1, drawLists.size() + 1).forEach(i -> {
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
					.setCreateTime(new Date())
			);
		});
		// other matches
		IntStream.range(2, knockoutRounds + 1).forEach(i -> {
			int roundMatchNum = (int) Math.pow(2, knockoutRounds - i);
			int prevMatchNum = IntStream.range(1, i).reduce(0, (sum, round) -> sum += (int) Math.pow(2, knockoutRounds - round));
			IntStream.range(1, roundMatchNum + 1).forEach(j -> knockoutEntityList.add(new TournamentKnockoutEntity()
							.setTournamentId(tournamentId)
							.setRound(i)
							.setStartGw(knockoutStartGw + knockoutPlayAgainstNum * (i - 1))
							.setEndGw(knockoutStartGw + knockoutPlayAgainstNum * i - 1)
							.setHomeEntry(0)
							.setAwayEntry(0)
							.setMatchId(j + prevMatchNum)
							.setNextMatchId(i == knockoutRounds ? -1 : this.getNextMatchId(j, prevMatchNum, roundMatchNum))
							.setRoundWinner(0)
							.setCreateTime(new Date())
					)
			);
		});
		// save knouckout
		this.tournamentKnockoutService.saveBatch(knockoutEntityList);
		// create knockout result
		this.createKnockoutResult(knockoutEntityList, knockoutPlayAgainstNum);
		log.info("draw knockouts success!");
	}

	private ArrayList<Integer> getKnockoutEntryList(int tournamentId, String groupMode, int groupNum, int groupQualifiers) {
		ArrayList<Integer> entryList = Lists.newArrayList();
		if (GroupModeEnum.valueOf(groupMode) == GroupModeEnum.No_group) {
			List<Integer> entryInfoList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
					.eq(EntryInfoEntity::getTournamentId, tournamentId).orderByAsc(EntryInfoEntity::getId))
					.stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
			entryList.addAll(entryInfoList);
		} else {
			IntStream.range(1, groupNum + 1).forEach(i ->
					IntStream.range(1, groupQualifiers + 1).forEach(j -> entryList.add(10 * i + j)));
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
				IntStream.range(1, knockoutPlayAgainstNum + 1).forEach(i -> resultEntityList.add(new TournamentKnockoutResultEntity()
						.setTournamentId(roundMatchEntity.getTournamentId())
						.setRound(roundMatchEntity.getRound())
						.setMatchId(roundMatchEntity.getMatchId())
						.setPlayAginstId(i)
						.setEvent(roundMatchEntity.getStartGw() - 1 + i)
						.setHomeEntry(i % 2 == 1 ? roundMatchEntity.getHomeEntry() : roundMatchEntity.getAwayEntry())
						.setHomeEntryNetPoint(0)
						.setHomeEntryRank(0)
						.setHomeEntryChip(ChipEnum.NONE.getValue())
						.setAwayEntry(i % 2 == 1 ? roundMatchEntity.getAwayEntry() : roundMatchEntity.getHomeEntry())
						.setAwayEntryNetPoint(0)
						.setAwayEntryRank(0)
						.setAwayEntryChip(ChipEnum.NONE.getValue())
						.setMatchWinner(0)
				)));
		this.tournamentKnockoutResultService.saveBatch(resultEntityList);
	}

	@Override
	public List<TournamentInfoEntity> queryTournamentInfo(QueryParam param) {
		return this.tournamentInfoService.queryTournamentInfo(param);
	}

	@Override
	public String deleteTournamentByCupName(String tournamentName) {
		TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, tournamentName));
		if (tournamentInfoEntity == null) {
			return "删除失败，赛事不存在！";
		}
		this.tournamentInfoService.removeById(tournamentInfoEntity.getId());
		return "删除成功";
	}

}
