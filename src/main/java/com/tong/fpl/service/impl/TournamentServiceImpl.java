package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.event.CreateTournamentEventData;
import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.ITournamentService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
public class TournamentServiceImpl implements ITournamentService {

	private final EntryInfoService entryInfoService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentEntryService tournamentEntryService;
	private final TournamentGroupService tournamentGroupService;
	private final TournamentBattleGroupResultService tournamentGroupBattleService;
	private final TournamentKnockoutService tournamentKnockoutService;
	private final TournamentKnockoutResultService tournamentKnockoutResultService;
	private final IStaticSerive staticSerive;
	private final ApplicationContext context;

	@Override
	public String createNewTournament(TournamentCreateData tournamentCreateData) {
		if (GroupMode.valueOf(tournamentCreateData.getGroupMode()) == GroupMode.No_group &&
				KnockoutMode.valueOf(tournamentCreateData.getKnockoutMode()) == KnockoutMode.No_knockout) {
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
		tournamentInfoEntity.setAdminerEntry(tournamentCreateData.getAdminerEntry());
		tournamentInfoEntity.setSeason(CommonUtils.getCurrentSeason());
		tournamentInfoEntity.setLeagueType(tournamentCreateData.getLeagueType());
		tournamentInfoEntity.setLeagueId(tournamentCreateData.getLeagueId());
		tournamentInfoEntity.setTotalTeam(tournamentCreateData.getTotalTeam());
		tournamentInfoEntity.setKnockoutPlayAgainstNum(this.setKnouckoutPlayAgainstNum(tournamentCreateData.getKnockoutMode()));
		tournamentInfoEntity.setState(1);
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

	private int setKnouckoutPlayAgainstNum(String knockoutMode) {
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
		tournamentInfoEntity.setGroupMode(groupMode.toString());
		switch (groupMode) {
			case No_group: {
				tournamentInfoEntity.setTeamPerGroup(0);
				tournamentInfoEntity.setGroupNum(0);
				tournamentInfoEntity.setGroupStartGw(-1);
				tournamentInfoEntity.setGroupEndGw(-1);
				tournamentInfoEntity.setGroupFillAverage(false);
				tournamentInfoEntity.setGroupRounds(0);
				tournamentInfoEntity.setGroupPlayAgainstNum(0);
				tournamentInfoEntity.setGroupQualifiers(0);

				break;
			}
			case Points_race:
			case Battle_race: {
				tournamentInfoEntity.setTeamPerGroup(tournamentCreateData.getTeamsPerGroup());
				tournamentInfoEntity.setGroupNum(tournamentCreateData.getGroupNum());
				tournamentInfoEntity.setGroupStartGw(CommonUtils.getRealGw(tournamentCreateData.getGroupStartGw()));
				tournamentInfoEntity.setGroupEndGw(CommonUtils.getRealGw(tournamentCreateData.getGroupEndGw()));
				tournamentInfoEntity.setGroupRounds(tournamentInfoEntity.getGroupEndGw() - tournamentInfoEntity.getGroupStartGw() + 1);
				tournamentInfoEntity.setGroupFillAverage(tournamentCreateData.isGroupFillAverage());
				tournamentInfoEntity.setGroupPlayAgainstNum(this.setGroupPlayAgainstNumByMode(groupMode, tournamentInfoEntity.getGroupRounds(), tournamentInfoEntity.getTeamPerGroup()));
				tournamentInfoEntity.setGroupQualifiers(tournamentCreateData.getGroupQualifiers());
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
		tournamentInfoEntity.setKnockoutMode(knockoutMode.toString());
		switch (knockoutMode) {
			case No_knockout: {
				tournamentInfoEntity.setKnockoutTeam(0);
				tournamentInfoEntity.setKnockoutRounds(0);
				tournamentInfoEntity.setKnockoutEvents(0);
				tournamentInfoEntity.setKnockoutStartGw(-1);
				tournamentInfoEntity.setKnockoutEndGw(-1);
				break;
			}
			case Single_round:
			case Home_away: {
				tournamentInfoEntity.setKnockoutTeam(tournamentCreateData.getKnockoutTeam());
				tournamentInfoEntity.setKnockoutRounds(tournamentCreateData.getKnockoutRounds());
				tournamentInfoEntity.setKnockoutEvents(tournamentCreateData.getKnockoutEvents());
				tournamentInfoEntity.setKnockoutStartGw(CommonUtils.getRealGw(tournamentCreateData.getKnockoutStartGw()));
				tournamentInfoEntity.setKnockoutEndGw(CommonUtils.getRealGw(tournamentCreateData.getKnockoutEndGw()));
				break;
			}
			default:
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
		this.saveTournamentEntryInfo(tournamentId, tournamentInfo.getLeagueType(), tournamentInfo.getLeagueId(), tournamentInfo.isGroupFillAverage());
		// draw groups
		this.drawGroups(tournamentId, groupMode, tournamentInfo.getTeamPerGroup(), tournamentInfo.isGroupFillAverage(), groupNum,
				tournamentInfo.getGroupStartGw(), tournamentInfo.getGroupEndGw());
		// draw group battle
		this.drawGroupBattle(tournamentId, groupMode, tournamentInfo.getGroupPlayAgainstNum(), tournamentInfo.getTeamPerGroup(),
				groupNum, tournamentInfo.getGroupStartGw(), tournamentInfo.getGroupEndGw());
		// draw knockouts
		this.drawKnockouts(tournamentId, groupMode, groupNum,
				tournamentInfo.getGroupQualifiers(), tournamentInfo.getKnockoutMode(),
				tournamentInfo.getKnockoutPlayAgainstNum(), tournamentInfo.getKnockoutTeam(),
				tournamentInfo.getKnockoutStartGw(), tournamentInfo.getKnockoutRounds());
	}

	@Override
	public void saveTournamentEntryInfo(int tournamentId, String leagueType, int leagueId, boolean groupFillAverage) {
		// save entry_info
		List<EntryInfoEntity> entryInfoEntityList = this.saveEntryInfoFromFplServer(leagueType, leagueId);
		// save tournament_entry (add average)
		this.saveTournamentEntry(tournamentId, leagueId, groupFillAverage, entryInfoEntityList);
		log.info("create entry info success!");
	}

	private List<EntryInfoEntity> saveEntryInfoFromFplServer(String leagueType, int leagueId) {
		List<EntryInfoEntity> entryInfoEntityList = Lists.newArrayList();
		if (LeagueType.valueOf(leagueType) == LeagueType.Classic) {
			entryInfoEntityList = this.staticSerive.getEntryInfoListFromClassic(leagueId);
		} else if (LeagueType.valueOf(leagueType) == LeagueType.H2h) {
			entryInfoEntityList = this.staticSerive.getEntryInfoListFromH2h(leagueId);
		}
		entryInfoEntityList.forEach(entryInfoEntity -> {
			Optional<EntryRes> entryRes = this.staticSerive.getEntry(entryInfoEntity.getEntry());
			entryRes.ifPresent(o -> entryInfoEntity
					.setEntryName(o.getName())
					.setPlayerName(o.getPlayerFirstName() + " " + o.getPlayerLastName())
					.setRegion(o.getPlayerRegionName())
					.setStartedEvent(o.getStartedEvent())
					.setOverallPoints(o.getSummaryOverallPoints())
					.setOverallRank(o.getSummaryOverallRank())
					.setBank(o.getLastDeadlineBank())
					.setTeamValue(o.getLastDeadlineValue())
					.setTotalTransfers(o.getLastDeadlineTotalTransfers()));
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

	@Override
	public void drawGroups(int tournamentId, String groupMode, int teamsPerGroup, boolean groupFillAverage, int groupNum,
	                       int groupStartGw, int groupEndGw) {
		if (GroupMode.valueOf(groupMode) == GroupMode.No_group) {
			return;
		}
		// check exist
		if (this.tournamentGroupService.count(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)) > 0) {
			log.info("tournament-{} groups exist!", tournamentId);
			return;
		}
		List<TournamentGroupEntity> tournamentGroupList = Lists.newArrayList();
		// get entryList from input classic league
		List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
				.eq(TournamentEntryEntity::getTournamentId, tournamentId)
				.gt(TournamentEntryEntity::getEntry, 0))
				.stream()
				.filter(o -> o.getEntry() > 0)
				.map(TournamentEntryEntity::getEntry)
				.collect(Collectors.toList());
		// draw
		Multimap<Integer, Integer> teamInGroupMap = ArrayListMultimap.create();
		Multimap<Integer, Integer> groupIndexMap = ArrayListMultimap.create();
		Random random = new Random();
		// add average, represent by nagative num
		List<Integer> averageList = Lists.newArrayList();
		int averageNum = teamsPerGroup * groupNum - entryList.size();
		if (averageNum > 0) {
			IntStream.range(1, averageNum + 1).forEach(i -> averageList.add(-1 * i));
			// draw average to diffrent group
			averageList.forEach(entry -> {
				int groupId = this.drawAverageToGroup(random, entry, groupNum, teamInGroupMap);
				TournamentGroupEntity tournamentGroupEntity = new TournamentGroupEntity()
						.setTournamentId(tournamentId)
						.setGroupId(groupId)
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
						.setQualified(false)
						.setOverallPoints(0)
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
					.setQualified(false)
					.setOverallPoints(0)
					.setOverallRank(0)
			);
		});
		// update
		this.tournamentGroupService.saveBatch(tournamentGroupList);
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

	private int drawGroupIndex(Random random, int groupId, int teamsPerGroup, Multimap<Integer, Integer> groupIndexMap) {
		int index = random.nextInt(teamsPerGroup) + 1;
		while (groupIndexMap.containsEntry(groupId, index)) {
			index = random.nextInt(teamsPerGroup) + 1;
		}
		groupIndexMap.put(groupId, index);
		return index;
	}

	@Override
	public void drawGroupBattle(int tournamentId, String groupMode, int playAgainstNum, int teamPerGroup, int groupNum, int groupStartGw, int groupEndGw) {
		if (GroupMode.valueOf(groupMode) != GroupMode.Battle_race) {
			log.info("do not need to draw group battle!");
			return;
		}
		int groupRound = groupEndGw - groupStartGw + 1;
		Multimap<Integer, String> abstractBattleMap = this.drawAbstractBattle(teamPerGroup, playAgainstNum, groupRound);
		// draw single group
		List<TournamentBattleGroupResultEntity> groupBattleResultList = Lists.newArrayList();
		IntStream.range(1, groupNum + 1).forEach(groupId ->
				this.drawSingleGroupBattle(tournamentId, groupId, abstractBattleMap, groupBattleResultList));
		// save
		this.tournamentGroupBattleService.saveBatch(groupBattleResultList);
		log.info("draw group battle success!");
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
			ArrayList<String> battleList = this.replaceBattleEntry(abstractBattleMap.get(event), groupIndexMap);
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
		IntStream.range(1, teamPerGroup + 1).forEach(entryList::add);
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

	@Override
	public void drawKnockouts(int tournamentId, String groupMode, int groupNum, int groupQualifiers,
	                          String knockoutMode, int knockoutPlayAgainstNum, int knockoutTeam,
	                          int knockoutStartGw, int knockoutRounds) {
		if (KnockoutMode.valueOf(knockoutMode) == KnockoutMode.No_knockout) {
			log.info("do not need to draw knockouts!");
			return;
		}
		if (knockoutPlayAgainstNum == 0) {
			log.error("tournament-{} knockouct stage play against each other at least one time!", tournamentId);
			return;
		}
		// check exist
		if (this.tournamentKnockoutService.count(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)) > 0) {
			log.error("tournament-{} knockouts exist!", tournamentId);
			return;
		}
		// get knockout entry
		ArrayList<Integer> entryList = this.getKnockoutEntryList(tournamentId, groupMode, groupNum, groupQualifiers);
		if (CollectionUtils.isEmpty(entryList)) {
			log.error("draw knockouts no entry!");
			return;
		}
		// add blank teams
		int blankNum = (int) Math.pow(2, knockoutRounds) - knockoutTeam;
		if (blankNum >= entryList.size()) {
			log.error("blank num is bigger than entry num!");
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
		if (GroupMode.valueOf(groupMode) == GroupMode.No_group) {
			List<Integer> entryInfoList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
					.eq(TournamentEntryEntity::getTournamentId, tournamentId)
					.orderByAsc(TournamentEntryEntity::getId))
					.stream()
					.map(TournamentEntryEntity::getEntry)
					.collect(Collectors.toList());
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
						.setEvent(roundMatchEntity.getStartGw() - 1 + i)
						.setMatchId(roundMatchEntity.getMatchId())
						.setPlayAginstId(i)
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

	@Cacheable(value = "countTournamentLeagueTeams")
	@Override
	public int countTournamentLeagueTeams(String url) {
		return url.contains("/standings/c") ?
				this.staticSerive.getEntryInfoListFromClassic(this.getLeagueIdByType(url, LeagueType.Classic.name())).size()
				: this.staticSerive.getEntryInfoListFromH2h(this.getLeagueIdByType(url, LeagueType.H2h.name())).size();
	}

	private int getLeagueIdByType(String url, String leagueType) {
		switch (LeagueType.valueOf(leagueType)) {
			case Classic:
				return Integer.parseInt(StringUtils.substringBetween(url, "https://fantasy.premierleague.com/leagues/", "/standings/c"));
			case H2h:
				return Integer.parseInt(StringUtils.substringBetween(url, "https://fantasy.premierleague.com/leagues/", "/standings/h"));
			default:
				return 0;
		}
	}

	@Override
	public boolean checkTournamentName(String name) {
		return this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, name)) == null;
	}

	@Override
	public String updateTournament(TournamentCreateData tournamentCreateData) {
		boolean update = false;
		TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, tournamentCreateData.getTournamentName()));
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
				.eq(TournamentInfoEntity::getName, name));
		if (tournamentInfoEntity == null) {
			return "删除失败，赛事不存在！";
		}
		// set state = 0
		tournamentInfoEntity.setState(0);
		this.tournamentInfoService.updateById(tournamentInfoEntity);
		return "删除成功";
	}

}
