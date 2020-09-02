package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TournamentEntryEntity;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
	private TournamentKnockoutResultService tournamentKnockoutResultService;
	@Autowired
	private PlayerService playerService;
	@Autowired
	private PlayerStatService playerStatService;
	@Autowired
	private TournamentEntryService tournamentEntryService;
	@Autowired
	private EntryEventResultService entryEventResultService;
	@Autowired
	private IQuerySerivce querySerivce;

	@Test
	void test() {
		long startTime = System.currentTimeMillis();
		List<PlayerEntity> list = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getElement, 211));
		long endTime = System.currentTimeMillis();
		System.out.println("escape: " + (endTime - startTime) + "ms");
		System.out.println(1);
	}

	@Test
	void page() {
		Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
				new Page<>(2, 20, false), new QueryWrapper<>());
		System.out.println(1);
	}

	@Test
	void dynamic() {
		MybatisPlusConfig.season.set("1920");
		List<EntryInfoEntity> entryInfoEntity = this.entryInfoService.list();
		System.out.println(1);
	}

	@Test
	void test2() {
		Table<Integer, Integer, Map<Integer, Integer>> table = HashBasedTable.create(); // entry-> event -> (element, point)
		List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
				.eq(TournamentEntryEntity::getTournamentId, 1))
				.stream()
				.map(TournamentEntryEntity::getEntry)
				.collect(Collectors.toList());
		entryList.forEach(entry -> {
			if (entry != 3697) {
				return;
			}
			List<EntryEventResultEntity> list = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
					.eq(EntryEventResultEntity::getEntry, entry)
					.orderByAsc(EntryEventResultEntity::getEvent));
			list.forEach(o -> {
				Map<Integer, Integer> eventMap = this.calcEvenCaptain(o);
				table.put(entry, o.getEvent(), eventMap);
			});
		});
		System.out.println(1);

	}

	private Table<Integer, Integer, Map<Integer, Integer>> getTableData() {
		return null;
	}

	private Map<Integer, Integer> calcEvenCaptain(EntryEventResultEntity entryEventResultEntity) {
		Map<Integer, Integer> eventMap = Maps.newHashMap();
		Map<Integer, Integer> captainMap = this.getCaptainFromPick(entryEventResultEntity.getEventPicks());
		int element = captainMap.keySet().stream().findFirst().orElse(0);
		if (element == 0) {
			return eventMap;
		}
		int points = captainMap.get(element);
		if (Chip.getChipFromValue(entryEventResultEntity.getEventChip()).equals(Chip.TC)) {
			eventMap.put(element, 3 * points);
		} else {
			eventMap.put(element, 2 * points);
		}
		return eventMap;
	}

	private Map<Integer, Integer> getCaptainFromPick(String eventPicks) {
		List<Pick> pickList = this.querySerivce.qryPickListFromPicks("1920", eventPicks);
		return pickList.stream().filter(Pick::isCaptain).collect(Collectors.toMap(Pick::getElement, Pick::getPoints));
	}

}
