package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.CupLeagueTypeEnum;
import com.tong.fpl.data.fpl.QueryParam;
import com.tong.fpl.data.fpl.TournamentCreateData;
import com.tong.fpl.data.response.LeagueClassicRes;
import com.tong.fpl.db.entity.EntryInfoEntity;
import com.tong.fpl.db.entity.TournamentGroupEntity;
import com.tong.fpl.db.entity.TournamentInfoEntity;
import com.tong.fpl.db.entity.TournamentKnockoutEntity;
import com.tong.fpl.event.CreateTournamentEvent;
import com.tong.fpl.service.ITournamentManagementService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.TournamentGroupService;
import com.tong.fpl.service.db.TournamentInfoService;
import com.tong.fpl.service.db.TournamentKnockoutService;
import com.tong.fpl.utils.HttpUtils;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
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
public class TournamentManagementImpl implements ITournamentManagementService {

	private final ApplicationContext context;
	private final EntryInfoService entryInfoService;
	private final TournamentInfoService tournamentInfoService;
	private final TournamentGroupService tournamentGroupService;
	private final TournamentKnockoutService tournamentKnockoutService;

	@Override
	public String createNewTournament(TournamentCreateData tournamentCreateData) {
		this.tournamentInfoService.save(new TournamentInfoEntity()
				.setName(tournamentCreateData.getTournamentName())
				.setCreator(tournamentCreateData.getCreator())
				.setLeagueType(CupLeagueTypeEnum.Classic.toString())
				.setLeagueId(StringUtils.substringBetween(tournamentCreateData.getUrl(), "https://fantasy.premierleague.com/leagues/", "/standings/c"))
				.setStartGw(tournamentCreateData.getStartGw().contains("+") ?
						Integer.parseInt(StringUtils.remove(tournamentCreateData.getStartGw(), "+")) + 9 :
						Integer.parseInt(tournamentCreateData.getStartGw()))
				.setEndGw(tournamentCreateData.getEndGw().contains("+") ?
						Integer.parseInt(StringUtils.remove(tournamentCreateData.getEndGw(), "+")) + 9 :
						Integer.parseInt(tournamentCreateData.getEndGw()))
				.setTeamPerGroup(tournamentCreateData.getTeamsPerGroup())
				.setQualifiers(tournamentCreateData.getQualifiers())
				.setFillAverage(tournamentCreateData.isFillAverage())
				.setDrawAfterGroupQualify(tournamentCreateData.isDrawAfterGroupQualify())
				.setKnockoutRounds(tournamentCreateData.getKnockoutRounds())
				.setHomeAwayMode(tournamentCreateData.isHomeAwayMode())
				.setCreateTime(new Date())
		);
		// publish event
		context.publishEvent(new CreateTournamentEvent(this, tournamentCreateData.getTournamentName(), tournamentCreateData.isDrawKnockoutsNow()));
		return "创建成功！";
	}

	@Override
	public void saveTournamentEntryInfo(String tournamentName) {
		// save entryList from input classic league
		TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda().eq(TournamentInfoEntity::getName, tournamentName));
		if (tournamentInfoEntity == null) {
			return;
		}
		int tournamentId = tournamentInfoEntity.getId();
		this.setEntryList(tournamentId, tournamentInfoEntity.getLeagueId());
		log.info("create entry info success!");
		// update tournament_info
		int totalTeam = this.entryInfoService.count(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getTournamentId, tournamentId));
		this.tournamentInfoService.updateById(
				tournamentInfoEntity.setTotalTeam(totalTeam)
						.setGroupRounds(tournamentInfoEntity.getEndGw() - tournamentInfoEntity.getStartGw() + 1)
						.setGroupNum((int) (Math.ceil(totalTeam * 1.0 / tournamentInfoEntity.getTeamPerGroup())))
		);
	}

	private void setEntryList(int tournamentId, String classicId) {
		this.entryInfoService.getBaseMapper().truncateTable();
		this.setOnePageEntryList(tournamentId, classicId, 1);
	}

	private void setOnePageEntryList(int tournamentId, String classicId, int page) {
		try {
			String url = String.format("https://fantasy.premierleague.com/api/leagues-classic/%s/standings/?page_standings=%s", classicId, page);
			String result = HttpUtils.httpGetWithHeader(url, Constant.PL_PROFILE).orElse("");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			Optional<LeagueClassicRes> resResult = Optional.ofNullable(mapper.readValue(result, LeagueClassicRes.class));
			if (resResult.isPresent()) {
				LeagueClassicRes leagueClassicRes = resResult.get();
				if (!CollectionUtils.isEmpty(leagueClassicRes.getStandings().getResults())) {
					leagueClassicRes.getStandings().getResults().forEach(o -> {
								// save entry_info
								this.entryInfoService.saveOrUpdate(new EntryInfoEntity()
										.setTournamentId(tournamentId)
										.setEntry(o.getEntry())
										.setEntryName(EmojiManager.containsEmoji(o.getEntryName()) ? EmojiParser.parseToHtmlDecimal(o.getEntryName()) : o.getEntryName())
										.setPlayerName(EmojiManager.containsEmoji(o.getPlayerName()) ? EmojiParser.parseToHtmlDecimal(o.getPlayerName()) : o.getPlayerName())
										.setRank(o.getRank())
										.setLastRank(o.getLastRank())
										.setCreateTime(new Date())
								);
							}
					);
					if (leagueClassicRes.getStandings().isHasNext()) {
						page++;
						setOnePageEntryList(tournamentId, classicId, page);
					}
				}
			}
		} catch (IOException e) {
			log.error("getLeaguesClassic error: " + e.getMessage());
		}
	}

	@Override
	public void drawGroups(String tournamentName) {
		Multimap<Integer, Integer> teamInGroup = ArrayListMultimap.create();
		// get tournament_info
		TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, tournamentName));
		if (tournamentInfoEntity == null) {
			return;
		}
		int tournamentId = tournamentInfoEntity.getId();
		// check exist
		if (this.tournamentGroupService.count(new QueryWrapper<TournamentGroupEntity>().lambda()
				.eq(TournamentGroupEntity::getTournamentId, tournamentId)) > 0) {
			log.info("tournament{} groups exist!", tournamentId);
			return;
		}
		int groupNum = tournamentInfoEntity.getGroupNum();
		int teamsPerGroup = tournamentInfoEntity.getTeamPerGroup();
		// get entryList from input classic league
		List<Integer> entryList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getTournamentId, tournamentId))
				.stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
		// shuffle
		Collections.shuffle(entryList);
		// add average, represent by nagative num
		Random random = new Random();
		if (tournamentInfoEntity.getFillAverage()) {
			int averageNum = groupNum * teamsPerGroup - entryList.size();
			IntStream.range(1, averageNum + 1).forEach(i -> {
				int entry = -1 * i;
				this.tournamentGroupService.save(new TournamentGroupEntity()
						.setTournamentId(tournamentId)
						.setGroupId(this.drawAverageToGroup(random, entry, groupNum, teamInGroup))
						.setEntry(entry)
						.setGroupPoint(0)
						.setGroupRank(0)
						.setCreateTime(new Date())
				);
			});
		}
		// draw entry list
		entryList.forEach(entry -> this.tournamentGroupService.save(new TournamentGroupEntity()
				.setTournamentId(tournamentId)
				.setGroupId(this.drawToGroup(random, entry, groupNum, teamsPerGroup, teamInGroup))
				.setEntry(entry)
				.setGroupPoint(0)
				.setGroupRank(0)
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
	public void drawKnockouts(String tournamentName) throws Exception {
		// get qualified entry list
		TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, tournamentName));
		if (tournamentInfoEntity == null) {
			return;
		}
		int tournamentId = tournamentInfoEntity.getId();
		// check exist
		if (this.tournamentKnockoutService.count(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)) > 0) {
			log.info("tournament{} knockouts exist!", tournamentId);
			return;
		}
		List<Integer> entryList = this.getKnockoutEntryList(tournamentInfoEntity);
		if (CollectionUtils.isEmpty(entryList)) {
			return;
		}
		// knockout rounds
		int knockoutRounds = tournamentInfoEntity.getKnockoutRounds();
		int totalTeamNum = (int) Math.pow(2, knockoutRounds);
		// add blank teams
		int blankNum = totalTeamNum - entryList.size();
		if (blankNum >= entryList.size()) {
			throw new Exception("decrease your knockout rounds!");
		}
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
			this.tournamentKnockoutService.save(new TournamentKnockoutEntity()
					.setTournamentId(tournamentId)
					.setHomeEntry(subList.get(0))
					.setAwayEntry(subList.get(1))
					.setKnockoutRound(1)
					.setMatchId(i)
					.setNextMatchId(i % 2 == 0 ? (i / 2) + firstRoundMatchNum : ((i + 1) / 2) + firstRoundMatchNum)
					.setRoundWinner(0)
					.setCreateTime(new Date())
			);
		});
		// other matches
		IntStream.range(2, knockoutRounds).forEach(i -> {
			int roundMatchNum = (int) Math.pow(2, knockoutRounds - i);
			int prevMatchNum = IntStream.range(1, i).reduce(0, (sum, round) -> sum += (int) Math.pow(2, knockoutRounds - round));
			IntStream.range(1, roundMatchNum + 1).forEach(j -> this.tournamentKnockoutService.save(new TournamentKnockoutEntity()
					.setTournamentId(tournamentId)
					.setHomeEntry(0)
					.setAwayEntry(0)
					.setKnockoutRound(i)
					.setMatchId(j + prevMatchNum)
					.setNextMatchId(j % 2 == 0 ? (j / 2) + prevMatchNum + roundMatchNum : ((j + 1) / 2) + prevMatchNum + roundMatchNum)
					.setRoundWinner(0)
					.setCreateTime(new Date())));
		});
		log.info("draw knockouts success!");
	}

	private List<Integer> getKnockoutEntryList(TournamentInfoEntity tournamentInfoEntity) {
		if (tournamentInfoEntity.getDrawAfterGroupQualify()) {
			return this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
					.eq(TournamentGroupEntity::getTournamentId, tournamentInfoEntity.getId()).eq(TournamentGroupEntity::getQualified, 1))
					.stream()
					.map(TournamentGroupEntity::getEntry).collect(Collectors.toList());
		} else {
			List<Integer> entryList = Lists.newArrayList();
			IntStream.range(1, tournamentInfoEntity.getGroupNum() + 1).forEach(i ->
					IntStream.range(1, tournamentInfoEntity.getQualifiers() + 1).forEach(j -> entryList.add(10 * i + j)));
			return entryList;
		}
	}

	private boolean checkDrawListLegal(List<Integer> entryList) {
		return Lists.partition(entryList, 2).stream().anyMatch(subList -> subList.get(0) < 0 && subList.get(1) < 0);
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

	@Override
	public void updateQualifiedTeams(String tournamentName) {
		TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, tournamentName));
		if (tournamentInfoEntity == null) {
			return;
		}
		IntStream.range(1, tournamentInfoEntity.getGroupNum()).forEach(i -> {
			List<TournamentGroupEntity> groupList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
					.eq(TournamentGroupEntity::getTournamentId, tournamentInfoEntity.getId()).eq(TournamentGroupEntity::getGroupId, i));
			// get qualified teams
			List<TournamentGroupEntity> qualifiedList = groupList.stream()
					.filter(tournamentGroupEntity -> tournamentGroupEntity.getEntry() > 0)
					.sorted(Comparator.comparing(TournamentGroupEntity::getGroupPoint))
					.limit(tournamentInfoEntity.getQualifiers())
					.collect(Collectors.toList());
			// update tournament_group
			qualifiedList.forEach(tournamentGroupEntity -> {
				this.tournamentGroupService.updateById(
						tournamentGroupEntity.setQualified(true).setUpdateTime(new Date()));
				// update real entry
				if (!tournamentInfoEntity.getDrawAfterGroupQualify()) {
					this.updateRealEntry(i, tournamentGroupEntity);
				}
			});
		});
	}

	private void updateRealEntry(int groupIndex, TournamentGroupEntity tournamentGroupEntity) {
		int virtualEntry = groupIndex * 10 + tournamentGroupEntity.getGroupRank();
		// search for home entry
		TournamentKnockoutEntity tournamentKnockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentGroupEntity.getTournamentId())
				.eq(TournamentKnockoutEntity::getHomeEntry, virtualEntry));
		if (tournamentKnockoutEntity != null) {
			this.tournamentKnockoutService.updateById(tournamentKnockoutEntity.setHomeEntry(tournamentGroupEntity.getEntry()));
		} else { // search for away entry
			tournamentKnockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
					.eq(TournamentKnockoutEntity::getTournamentId, tournamentGroupEntity.getTournamentId())
					.eq(TournamentKnockoutEntity::getAwayEntry, virtualEntry));
			if (tournamentKnockoutEntity != null) {
				this.tournamentKnockoutService.updateById(tournamentKnockoutEntity.setHomeEntry(tournamentGroupEntity.getEntry()));
			}
		}
	}

	@Override
	public int countEntryNumInGroup(String tournamentName) {
		TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
				.eq(TournamentInfoEntity::getName, tournamentName));
		if (tournamentInfoEntity == null) {
			return 0;
		}
		return this.entryInfoService.count(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getTournamentId, tournamentInfoEntity.getId()));
	}

}
