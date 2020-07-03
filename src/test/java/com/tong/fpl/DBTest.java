package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.TournamentKnockoutResultEntity;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.EventResultService;
import com.tong.fpl.service.db.TournamentKnockoutResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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

}
