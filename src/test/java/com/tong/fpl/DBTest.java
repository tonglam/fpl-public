package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TournamentEntryEntity;
import com.tong.fpl.domain.entity.TournamentKnockoutResultEntity;
import com.tong.fpl.service.db.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/1/19
 */
public class DBTest extends FplApplicationTests {

	@Autowired
	private EntryInfoService entryInfoService;
	@Autowired
	private TournamentEntryService tournamentEntryService;
	@Autowired
	private EntryEventResultService entryEventResultService;
	@Autowired
	private TournamentKnockoutResultService tournamentKnockoutResultService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private TournamentInfoService tournamentInfoService;

	@Test
	void test1() {
		int entry = 1404;
		List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
				.eq(TournamentKnockoutResultEntity::getTournamentId, 1)
				.eq(TournamentKnockoutResultEntity::getEvent, 1)
				.and(o -> o.eq(TournamentKnockoutResultEntity::getHomeEntry, entry)
						.or(i -> i.eq(TournamentKnockoutResultEntity::getAwayEntry, entry)
						))
				.orderByAsc(TournamentKnockoutResultEntity::getMatchId));
		System.out.println(1);
	}

	@Test
	void test2() {
		List<Integer> entryList = this.entryInfoService.list().stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
		System.out.println(entryList.size());
	}

	@Test
	void test3() {
		List<PlayerEntity> list = this.playerService.list();
		List<String> names = list.stream()
				.filter(playerEntity -> playerEntity.getChanceOfPlayingNextRound() != 0)
				.filter(playerEntity -> playerEntity.getElementType() == 1)
				.map(playerEntity -> playerEntity.getFirstName() + "-" + playerEntity.getSecondName())
				.sorted(Comparator.comparing(String::length).reversed())
				.limit(2)
				.collect(Collectors.toList());
		System.out.println(names.toString());
	}

	@Test
	void test4() {
		Map<String, Integer> map = Maps.newHashMap();
		List<TournamentEntryEntity> tournamentEntryEntities = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda().eq(TournamentEntryEntity::getTournamentId, 1));
		tournamentEntryEntities.forEach(o -> {
			int total = this.entryEventResultService.sumEntryNetPoint(o.getEntry());
			map.put(this.entryInfoService.getOne(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getEntry, o.getEntry())).getEntryName(), total);
		});
		System.out.println(1);
	}

}
