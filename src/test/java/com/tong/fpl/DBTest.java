package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.fpl.config.collector.PlayAtHomeCollector;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
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
	private EventResultService eventResultService;
	@Autowired
	private TournamentKnockoutResultService tournamentKnockoutResultService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private EventFixtureService eventFixtureService;

	@Test
	public void test1() {
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
	public void test2() {
		List<Integer> entryList = this.entryInfoService.list().stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
		System.out.println(entryList.size());
	}

	@Test
	public void test3() {
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
	public void colletor() {
		Map<Integer, Boolean> playAtHomeMap = this.eventFixtureService.list(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, 39))
				.stream()
				.collect(new PlayAtHomeCollector());
		System.out.println(1);
	}

}
