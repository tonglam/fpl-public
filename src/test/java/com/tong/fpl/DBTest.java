package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.ValueChangeType;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.db.*;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/1/19
 */
public class DBTest extends FplApplicationTests {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerValueService playerValueService;
    @Autowired
    private EventLiveService eventLiveService;
    @Autowired
    private EntryEventTransfersService entryEventTransfersService;
    @Autowired
    private EntryEventResultService entryEventResultService;
    @Autowired
    private IQueryService querySerivce;
    @Autowired
    private TournamentKnockoutService tournamentKnockoutService;
    @Autowired
    private LeagueEventReportService leagueEventReportService;
    @Autowired
    private ScoutService scoutService;
    @Autowired
    private EntryEventSimulatePickService entryEventSimulatePickService;

    @Test
    void test() {
        List<ScoutEntity> list = Lists.newArrayList();
        Map<Integer, Integer> elementTeamMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getTeamId));
        this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, 15))
                .forEach(o -> {
                    o
                            .setGkpTeamId(elementTeamMap.getOrDefault(o.getGkp(), 0))
                            .setDefTeamId(elementTeamMap.getOrDefault(o.getDef(), 0))
                            .setMidTeamId(elementTeamMap.getOrDefault(o.getMid(), 0))
                            .setFwdTeamId(elementTeamMap.getOrDefault(o.getFwd(), 0))
                            .setCaptainTeamId(elementTeamMap.getOrDefault(o.getCaptain(), 0));
                    list.add(o);
                });
        this.scoutService.updateBatchById(list);
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
        System.out.println(1);
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

    @Test
    void fixStartPrice() {
        List<PlayerEntity> list = Lists.newArrayList();
        Map<Integer, Integer> startMap = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                .eq(PlayerValueEntity::getChangeType, ValueChangeType.Start.name()))
                .stream()
                .collect(Collectors.toMap(PlayerValueEntity::getElement, PlayerValueEntity::getValue));
        this.playerService.list().forEach(o -> list.add(o.setStartPrice(startMap.getOrDefault(o.getElement(), 0))));
        this.playerService.updateBatchById(list);
    }

    @Test
    void groupByElementType() {
        List<PlayerPickData> dataList = Lists.newArrayList();
        IntStream.rangeClosed(1, 16).forEach(event -> dataList.add(this.querySerivce.qryEntryPickData(event, 1870)));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"21, 4074865, 1870"})
    void entryEventSimulatePick(int event, int entry, int operator) {
        long start = System.currentTimeMillis();
        EntryEventSimulatePickEntity entryEventSimulatePickEntity = this.entryEventSimulatePickService.getOne(new QueryWrapper<EntryEventSimulatePickEntity>().lambda()
                .eq(EntryEventSimulatePickEntity::getEntry, entry)
                .eq(EntryEventSimulatePickEntity::getEvent, event)
                .eq(EntryEventSimulatePickEntity::getOperator, operator));
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @Test
    void badChoice() {
        List<LeagueEventReportEntity> list = Lists.newArrayList();
        List<LeagueEventReportEntity> list2 = Lists.newArrayList();
        this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getEvent, 32)
                .eq(LeagueEventReportEntity::getLeagueId, 314)
                .eq(LeagueEventReportEntity::getLeagueType, "Classic"))
                .forEach(o -> {
                    int count = 0;
                    count = this.verify(count, o.getPosition1());
                    count = this.verify(count, o.getPosition2());
                    count = this.verify(count, o.getPosition3());
                    count = this.verify(count, o.getPosition4());
                    count = this.verify(count, o.getPosition5());
                    count = this.verify(count, o.getPosition6());
                    count = this.verify(count, o.getPosition7());
                    count = this.verify(count, o.getPosition8());
                    count = this.verify(count, o.getPosition9());
                    count = this.verify(count, o.getPosition10());
                    count = this.verify(count, o.getPosition11());
                    if (StringUtils.equals(Chip.BB.getValue(), o.getEventChip())) {
                        count = this.verify(count, o.getPosition12());
                        count = this.verify(count, o.getPosition13());
                        count = this.verify(count, o.getPosition14());
                        count = this.verify(count, o.getPosition15());
                    }
                    if (count >= 2) {
                        list.add(o);
                        if (StringUtils.equals(Chip.BB.getValue(), o.getEventChip())) {
                            list2.add(o);
                        }
                    }
                });
        System.out.println(1);
    }

    private int verify(int count, int element) {
        if (element == 74 || element == 273 || element == 496 || element == 576) {
            return count + 1;
        }
        return count;
    }

    @Test
    void countZero() {
        List<LeagueEventReportEntity> list = Lists.newArrayList();
        List<LeagueEventReportEntity> list2 = Lists.newArrayList();
        List<LeagueEventReportEntity> list3 = Lists.newArrayList();

        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, 34))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getEvent, 34)
                .eq(LeagueEventReportEntity::getLeagueId, 65)
                .eq(LeagueEventReportEntity::getLeagueType, "Classic"))
                .forEach(o -> {
                    int count = 0;
                    count = this.zero(count, o.getPosition1(), eventLiveMap);
                    count = this.zero(count, o.getPosition2(), eventLiveMap);
                    count = this.zero(count, o.getPosition3(), eventLiveMap);
                    count = this.zero(count, o.getPosition4(), eventLiveMap);
                    count = this.zero(count, o.getPosition5(), eventLiveMap);
                    count = this.zero(count, o.getPosition6(), eventLiveMap);
                    count = this.zero(count, o.getPosition7(), eventLiveMap);
                    count = this.zero(count, o.getPosition8(), eventLiveMap);
                    count = this.zero(count, o.getPosition9(), eventLiveMap);
                    count = this.zero(count, o.getPosition10(), eventLiveMap);
                    count = this.zero(count, o.getPosition11(), eventLiveMap);
                    count = this.zero(count, o.getPosition12(), eventLiveMap);
                    count = this.zero(count, o.getPosition13(), eventLiveMap);
                    count = this.zero(count, o.getPosition14(), eventLiveMap);
                    count = this.zero(count, o.getPosition15(), eventLiveMap);
                    if (count >= 12) {
                        list.add(o);
                    }
                    if (count >= 13) {
                        list2.add(o);
                    }
                    if (count >= 14) {
                        list3.add(o);
                    }
                });
        System.out.println(1);
    }

    private int zero(int count, int element, Map<Integer, EventLiveEntity> eventLiveMap) {
        if (eventLiveMap.containsKey(element)) {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(element);
            if (eventLiveEntity.getTotalPoints() == 0) {
                return count + 1;
            }
        }
        return count;
    }

    @Test
    void valueDiff() {
        List<PlayerEntity> playerEntity = this.playerService.list()
                .stream()
                .sorted((o1, o2) -> {
                    int aa = o1.getPrice() - o1.getStartPrice();
                    int bb = o2.getPrice() - o2.getStartPrice();
                    return bb - aa;
                })
                .collect(Collectors.toList());
        System.out.println(1);
    }

}
