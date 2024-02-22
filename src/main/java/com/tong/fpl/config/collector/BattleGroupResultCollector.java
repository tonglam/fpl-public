package com.tong.fpl.config.collector;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.tong.fpl.domain.entity.TournamentBattleGroupResultEntity;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupEventEntryFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupEventGroupFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupFixtureData;
import com.tong.fpl.utils.CommonUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * input: tournament_battle_group_result, order by event, groupId
 * accumulate: table -> row:event, column:group_id, value:tournament_group_event_fixture
 * return: List<TournamentGroupFixtureData> list
 * <p>
 * Create by tong on 2020/9/10
 */
public class BattleGroupResultCollector implements Collector<TournamentBattleGroupResultEntity, Table<Integer, Integer, List<TournamentGroupEventEntryFixtureData>>, List<TournamentGroupFixtureData>> {

    @Override
    public Supplier<Table<Integer, Integer, List<TournamentGroupEventEntryFixtureData>>> supplier() {
        return HashBasedTable::create;
    }

    @Override
    public BiConsumer<Table<Integer, Integer, List<TournamentGroupEventEntryFixtureData>>, TournamentBattleGroupResultEntity> accumulator() {
        return (Table<Integer, Integer, List<TournamentGroupEventEntryFixtureData>> table, TournamentBattleGroupResultEntity o) -> {
            int event = o.getEvent();
            int groupId = o.getGroupId();
            List<TournamentGroupEventEntryFixtureData> list = Lists.newArrayList();
            if (table.contains(event, groupId)) {
                list = table.get(event, groupId);
            }
            TournamentGroupEventEntryFixtureData tournamentGroupFixtureData = new TournamentGroupEventEntryFixtureData();
            tournamentGroupFixtureData
                    .setHomeEntry(o.getHomeEntry())
                    .setHomeEntryNetPoints(o.getHomeEntryNetPoints())
                    .setAwayEntry(o.getAwayEntry())
                    .setAwayEntryNetPoints(o.getAwayEntryNetPoints());
            if (CollectionUtils.isEmpty(list)) {
                list = Lists.newArrayList();
            }
            list.add(tournamentGroupFixtureData);
            table.put(event, groupId, list);
        };
    }

    @Override
    public BinaryOperator<Table<Integer, Integer, List<TournamentGroupEventEntryFixtureData>>> combiner() {
        return (table1, table2) -> {
            table1.putAll(table2);
            return table1;
        };
    }

    @Override
    public Function<Table<Integer, Integer, List<TournamentGroupEventEntryFixtureData>>, List<TournamentGroupFixtureData>> finisher() {
        return table -> {
            List<TournamentGroupFixtureData> list = Lists.newArrayList();
            table.rowKeySet().forEach(event -> {
                // each event
                TournamentGroupFixtureData groupFixtureData = new TournamentGroupFixtureData();
                groupFixtureData.setEvent(event);
                List<TournamentGroupEventGroupFixtureData> groupEventFixtureList = Lists.newArrayList();
                // each group
                table.row(event).keySet().forEach(groupId ->
                        groupEventFixtureList.add(new TournamentGroupEventGroupFixtureData()
                                .setGroupId(groupId)
                                .setGroupName(CommonUtils.getCapitalLetterFromNum(groupId))
                                .setEventEntryFixtureList(table.get(event, groupId))));
                groupFixtureData.setGroupEventFixtureList(groupEventFixtureList
                        .stream()
                        .sorted(Comparator.comparing(TournamentGroupEventGroupFixtureData::getGroupId))
                        .collect(Collectors.toList()));
                list.add(groupFixtureData);
            });
            return list
                    .stream()
                    .sorted(Comparator.comparing(TournamentGroupFixtureData::getEvent))
                    .collect(Collectors.toList());
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
    }

}
