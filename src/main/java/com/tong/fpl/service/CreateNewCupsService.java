package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.CupLeagueTypeEnum;
import com.tong.fpl.data.response.LeagueClassicRes;
import com.tong.fpl.db.entity.CupGroupEntity;
import com.tong.fpl.db.entity.CupInfoEntity;
import com.tong.fpl.db.entity.EntryInfoEntity;
import com.tong.fpl.service.db.CupGroupService;
import com.tong.fpl.service.db.CupInfoService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.utils.HttpUtils;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
public class CreateNewCupsService {

	private final EntryInfoService entryInfoService;
	private final CupInfoService cupInfoService;
	private final CupGroupService cupGroupService;

	public String createNewCup(@Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*") String url,
	                           @NotNull String cupName, @NotNull String creator,
	                           @NotNull String startGw, @NotNull String endGw,
	                           @NotNull int teamsPerGroup, @NotNull int qualifiers,
	                           @NotNull boolean fillAverage) {
		String classicId = StringUtils.substringBetween(url, "https://fantasy.premierleague.com/leagues/", "/standings/c");
		// create a new cup
		this.cupInfoService.save(new CupInfoEntity()
				.setName(cupName)
				.setCreator(creator)
				.setLeagueType(CupLeagueTypeEnum.Classic.toString())
				.setLeagueId(classicId)
				.setStartGw(startGw.contains("+") ? Integer.parseInt(StringUtils.remove(startGw, "+")) + 9 : Integer.parseInt(startGw))
				.setEndGw(endGw.contains("+") ? Integer.parseInt(StringUtils.remove(endGw, "+")) + 9 : Integer.parseInt(endGw))
				.setTeamPerGroup(teamsPerGroup)
				.setQualifiers(qualifiers)
				.setFillAverage(fillAverage)
				.setCreateTime(new Date())
		);
		log.info("create cup info success!");
		// save entryList from input classic league
		int cupId = this.cupInfoService.getOne(new QueryWrapper<CupInfoEntity>().lambda().eq(CupInfoEntity::getName, cupName)).getId();
		this.setEntryList(cupId, classicId);
		log.info("create entry info success!");
		// update cup_info
		int totalTeam = this.entryInfoService.count(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getCupId, cupId));
		CupInfoEntity cupInfoEntity = this.cupInfoService.getById(cupId);
		this.cupInfoService.updateById(cupInfoEntity.setTotalTeam(totalTeam).setGroupNum((int) (Math.ceil(totalTeam * 1.0 / teamsPerGroup))));
		// drawGroups
		this.drawGroups(cupId);
		log.info("draw groups success!");
		return "创建成功！";
	}


	private void drawGroups(@NotNull int cupId) {
		Multimap<Integer, Integer> teamInGroup = ArrayListMultimap.create();
		// get cup_info
		CupInfoEntity cupInfoEntity = this.cupInfoService.getById(cupId);
		// get entryList from input classic league
		List<Integer> entryList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getCupId, cupId))
				.stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
		// shuffle
		Collections.shuffle(entryList);
		// save groups stage
		// add average, represent by nagative num
		Random random = new Random();
		int groupNum = cupInfoEntity.getGroupNum();
		int teamsPerGroup = cupInfoEntity.getTeamPerGroup();
		int averageNum = groupNum * teamsPerGroup - entryList.size();
		IntStream.range(1, averageNum + 1).forEach(i -> {
			int entry = -1 * i;
			this.cupGroupService.save(new CupGroupEntity()
					.setCupId(cupId)
					.setGroupId(this.drawAverageToGroup(random, entry, groupNum, teamInGroup))
					.setEntry(entry)
					.setCreateTime(new Date())
			);
		});
		// draw average first
		entryList.forEach(entry -> this.cupGroupService.save(new CupGroupEntity()
				.setCupId(cupId)
				.setGroupId(this.drawToGroup(random, entry, groupNum, teamsPerGroup, teamInGroup))
				.setEntry(entry)
				.setCreateTime(new Date())
		));
	}

	private void setEntryList(int cupId, String classicId) {
		this.entryInfoService.getBaseMapper().truncateTable();
		this.setOnePageEntryList(cupId, classicId, 1);
	}

	private void setOnePageEntryList(int cupId, String classicId, int page) {
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
										.setCupId(cupId)
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
						setOnePageEntryList(cupId, classicId, page);
					}
				}
			}
		} catch (IOException e) {
			log.error("getLeaguesClassic error: " + e.getMessage());
		}
	}

	private int drawAverageToGroup(Random random, int entry, int groupNum, Multimap<Integer, Integer> teamInGroup) {
		int groupId = random.nextInt(groupNum) + 1;
		while (teamInGroup.get(groupId).size() > 0 && teamInGroup.get(groupId).stream().parallel().anyMatch(o -> o < 0)) {
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

	public void drawKnockouts(int cupId) {
	}

	private int countGwNum(int teamNum) {
		for (int i = 0; i < 99; i++) {
			if (Math.pow(2, i) > teamNum) {
				return i;
			}
		}
		return 0;
	}

}
