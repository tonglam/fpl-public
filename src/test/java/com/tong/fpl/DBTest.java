package com.tong.fpl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.*;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.ValueChangeType;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/1/19
 */
public class DBTest extends FplApplicationTests {

	@Autowired
	private PlayerService playerService;
	@Autowired
	private PlayerValueService playerValueService;
	@Autowired
	private TournamentEntryService tournamentEntryService;
	@Autowired
	private EntryEventResultService entryEventResultService;
	@Autowired
	private IQuerySerivce querySerivce;
	@Autowired
	private TournamentKnockoutService tournamentKnockoutService;
	@Autowired
	private LeagueEventStatService teamSelectStatService;

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
		List<EntryPickData> pickList = Lists.newArrayList();
		MybatisPlusConfig.season.set("1920");
		List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEntry, 130889)
				.gt(EntryEventResultEntity::getEventPoints, 0));
		entryEventResultEntityList.forEach(o ->
				pickList.addAll(this.querySerivce.qryPickListFromPicks("1920", o.getEventPicks())));
		Map<String, Long> groupingMap = pickList
				.stream()
				.collect(Collectors.groupingBy(EntryPickData::getWebName, Collectors.counting()));
		Map<String, Integer> result = groupingMap.entrySet()
				.stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.limit(10)
				.collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
		result.forEach((k, v) -> System.out.println(k + " , 次数: " + v));
//		System.out.println(1);
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
		List<EntryPickData> pickList = this.querySerivce.qryPickListFromPicks("1920", eventPicks);
		return pickList.stream().filter(EntryPickData::isCaptain).collect(Collectors.toMap(EntryPickData::getElement, EntryPickData::getPoints));
	}

	@ParameterizedTest
	@CsvSource({"1, 6"})
	void knockout(int tournamentId, int event) {
		List<TournamentKnockoutEntity> list = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
				.eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
				.ge(TournamentKnockoutEntity::getStartGw, event)
				.le(TournamentKnockoutEntity::getEndGw, event));
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource("这破游戏⚽️让让群姐妹联赛大乱斗, 1")
	void teamSelect(String leagueName, int event) {
		List<Integer> elementList = Lists.newArrayList();
		this.teamSelectStatService.list(new QueryWrapper<LeagueEventStatEntity>().lambda()
				.eq(LeagueEventStatEntity::getLeagueName, leagueName)
				.eq(LeagueEventStatEntity::getEvent, event))
				.forEach(o -> {
					elementList.add(o.getPosition1());
					elementList.add(o.getPosition2());
					elementList.add(o.getPosition3());
					elementList.add(o.getPosition4());
					elementList.add(o.getPosition5());
					elementList.add(o.getPosition6());
					elementList.add(o.getPosition7());
					elementList.add(o.getPosition8());
					elementList.add(o.getPosition9());
					elementList.add(o.getPosition10());
					elementList.add(o.getPosition11());
					elementList.add(o.getPosition12());
					elementList.add(o.getPosition13());
					elementList.add(o.getPosition14());
					elementList.add(o.getPosition15());
				});
		Map<Integer, Long> map = elementList
				.stream()
				.collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
		Map<Integer, Integer> result = map.entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
				.limit(20)
				.collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
		result.forEach((k, v) ->
				System.out.println(this.playerService.getById(k).getWebName() + ": " + NumberUtil.div(v.intValue(), 113, 2) * 100 + "%"));
		System.out.println(1);
	}

	@Test
	void addPlayerStartPrice() {
		Map<Integer, Integer> startPriceMap = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
				.eq(PlayerValueEntity::getChangeType, ValueChangeType.Start.name()))
				.stream()
				.collect(Collectors.toMap(PlayerValueEntity::getElement, PlayerValueEntity::getValue));
		List<PlayerEntity> list = this.playerService.list();
		list.forEach(o -> {
			int startPrice = startPriceMap.getOrDefault(o.getElement(), 0);
			o.setStartPrice(startPrice);
		});
		this.playerService.updateBatchById(list);
		System.out.println(1);
	}

	@Test
	void getPlayerCurrentPrice() {
		Multimap<Integer, PlayerValueEntity> playerValueMap = HashMultimap.create();
		this.playerValueService.list().forEach(o -> playerValueMap.put(o.getElement(), o));
		int a = playerValueMap.get(202)
				.stream()
				.sorted(Comparator.comparing(PlayerValueEntity::getUpdateTime))
				.map(PlayerValueEntity::getValue)
				.max(Integer::compareTo)
				.orElse(0);
		System.out.println(1);
	}

}
