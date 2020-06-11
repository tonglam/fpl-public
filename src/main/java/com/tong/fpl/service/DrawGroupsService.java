package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.data.response.LeagueClassicRes;
import com.tong.fpl.db.entity.CupGroupEntity;
import com.tong.fpl.db.entity.EntryInfoEntity;
import com.tong.fpl.service.db.CupGroupService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.utils.HttpUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/6/11
 */
@Validated
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DrawGroupsService {

	private final EntryInfoService entryInfoService;
	private final CupGroupService cupGroupService;

	public void drawGroups(@Pattern(regexp = "^https://fantasy.premierleague.com/api/leagues-classic/.*") String url,
	                       @NotNull int teamsPerGroup, @NotNull int qualifiers) {
		Multimap<Integer, Integer> teamInGroup = ArrayListMultimap.create();
		// format url
		url = StringUtils.removeEnd(url, "/c").concat("?page_standings=1");
		// get entryList from input classic league
		List<Integer> entryList = Lists.newArrayList();
		if (this.setEntryList(entryList, url)) {
			this.setEntryList(entryList, url);
		}
		// shuffle
		Collections.shuffle(entryList);
		// save groups stage
		int groupNum = Integer.parseInt(String.valueOf(Math.ceil(entryList.size() * 1.0 / teamsPerGroup)));
		Random random = new Random();
		entryList.forEach(entry -> this.cupGroupService.save(new CupGroupEntity()
				.setGroupId(this.drawToGroup(random, entry, groupNum, teamsPerGroup, teamInGroup))
				.setEntry(entry)
				.setQualifiers(qualifiers)
		));
	}

	public void drawKnockouts(int cupId) {
		int qualifiers = this.cupGroupService.list(new QueryWrapper<CupGroupEntity>().lambda().eq(CupGroupEntity::getCupId, cupId)).get(0).getQualifiers();
		int groupNum = this.cupGroupService.query().select("DISTINCT group_id").eq("cup_id", cupId).list().size();
		int teamNum = groupNum * qualifiers;
		int gwNum = this.countGwNum(teamNum);
		int firstGwNum = Integer.parseInt(String.valueOf(Math.pow(2, gwNum)));
		int blankTeamNum = firstGwNum - teamNum;
		// qualified teams
		List<Integer> list = this.cupGroupService.list(new QueryWrapper<CupGroupEntity>().lambda()
				.eq(CupGroupEntity::getCupId, cupId).eq(CupGroupEntity::isQualified, true))
				.stream().map(CupGroupEntity::getEntry).collect(Collectors.toList());
		// blank team
		IntStream.range(-1, blankTeamNum * -1).forEach(list::add);
		// draw
		Collections.shuffle(list);
		List<List<Integer>> drawLists = Lists.partition(list, 2);
		// save

	}

	private int countGwNum(int teamNum) {
		for (int i = 0; i < 99; i++) {
			if (Math.pow(2, i) > teamNum) {
				return i;
			}
		}
		return 0;
	}

	private int drawToGroup(Random random, int entry, int groupNum, int teamsPerGroup, Multimap<Integer, Integer> teamInGroup) {
		int groupId = random.nextInt(groupNum);
		if (this.checkGroupId(entry, groupId, teamsPerGroup, teamInGroup)) {
			teamInGroup.put(groupId, entry);
			return groupId;
		} else {
			this.drawToGroup(random, entry, groupNum, teamsPerGroup, teamInGroup);
		}
		return groupId;
	}

	private boolean checkGroupId(int entry, int groupId, int teamsPerGroup, Multimap<Integer, Integer> teamInGroup) {
		if (teamInGroup.get(groupId).size() + 1 < teamsPerGroup) {
			teamInGroup.put(groupId, entry);
			return true;
		}
		return false;
	}

	private boolean setEntryList(List<Integer> entryList, String url) {
		try {
			Optional<String> result = HttpUtils.httpGetWithHeader(url, Constant.PL_PROFILE);
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			Optional<LeagueClassicRes> resResult = Optional.ofNullable(mapper.readValue(result.toString(), LeagueClassicRes.class));
			if (resResult.isPresent()) {
				LeagueClassicRes leagueClassicRes = resResult.get();
				if (!CollectionUtils.isEmpty(leagueClassicRes.getStandings().getResults())) {
					leagueClassicRes.getStandings().getResults().forEach(o -> {
								entryList.add(o.getEntry());
								// save entry_info
								this.entryInfoService.saveOrUpdate(new EntryInfoEntity()
										.setEntry(o.getEntry())
										.setEntryName(o.getEntryName())
										.setPlayerName(o.getPlayerName())
										.setRank(o.getRank())
										.setLastRank(o.getLastRank())
								);
							}
					);
				}
				return leagueClassicRes.getStandings().isHasNext();
			}
		} catch (IOException e) {
			log.error("getLeaguesClassic error: " + e.getMessage());
		}
		return false;
	}

}
