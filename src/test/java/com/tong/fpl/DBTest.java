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

import java.time.LocalDateTime;
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
	private PlayerStatService playerStatService;
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
	private LeagueEventReportService leagueEventReportService;

	@Test
	void test() {
		Map<Integer, String> playerNameMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
		List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
				.eq(LeagueEventReportEntity::getLeagueId, 4089)
				.eq(LeagueEventReportEntity::getLeagueType, "Classic")
				.eq(LeagueEventReportEntity::getEvent, 1));
		int size = leagueEventReportEntityList.size();
		Map<Integer, Long> selectMap = leagueEventReportEntityList
				.stream()
				.collect(Collectors.groupingBy(LeagueEventReportEntity::getCaptain, Collectors.counting()));
		Map<String, String> map = Maps.newHashMap();
		selectMap.forEach((element, count) ->
				map.put(playerNameMap.getOrDefault(element, ""), NumberUtil.formatPercent(NumberUtil.div(count.intValue(), size), 1)));
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

	@ParameterizedTest
	@CsvSource({"8"})
	void updateLeagueEventReport(int event) {
		List<LeagueEventReportEntity> list = Lists.newArrayList();
		Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
		this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
				.eq(PlayerStatEntity::getEvent, event))
				.forEach(o -> {
					int element = o.getElement();
					if (playerStatMap.containsKey(element)) {
						PlayerStatEntity playerStatEntity = playerStatMap.get(element);
						if (LocalDateTime.parse(playerStatEntity.getUpdateTime().replace(" ", "T"))
								.isAfter(LocalDateTime.parse(o.getUpdateTime().replace(" ", "T")))) {
							playerStatMap.put(element, playerStatEntity);
						}
					} else {
						playerStatMap.put(element, o);
					}
				});
		List<Integer> leagueList = Lists.list(3571, 11316, 4029, 23451, 72516, 65, 314);
		leagueList.forEach(leagueId -> {
			this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
					.eq(LeagueEventReportEntity::getLeagueId, leagueId)
					.eq(LeagueEventReportEntity::getLeagueType, "Classic")
					.eq(LeagueEventReportEntity::getEvent, event))
					.forEach(o -> {
						o.
								setCaptainSelected(playerStatMap.get(o.getCaptain()).getSelectedByPercent() + '%')
								.setViceCaptainSelected(playerStatMap.get(o.getViceCaptain()).getSelectedByPercent() + '%')
								.setHighestScoreSelected(playerStatMap.get(o.getHighestScore()).getSelectedByPercent() + '%');
						list.add(o);
					});
			// update
			this.leagueEventReportService.updateBatchById(list);
			System.out.println("update leagueId:" + leagueId);
		});
	}

}
