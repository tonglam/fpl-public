package com.tong.fpl.config.collector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tong.fpl.domain.entity.TournamentKnockoutResultEntity;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutEventFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutFixtureData;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * * input: tournament_knockout_result, order by event, match_id
 * * accumulate: map-> key:event, value:group_id, value:tournament_group_event_fixture
 * * return: List<TournamentGroupFixtureData> list
 * * <p>
 * Create by tong on 2020/9/10
 */
public class KnockoutResultCollector implements Collector<TournamentKnockoutResultEntity, Multimap<Integer, TournamentKnockoutResultEntity>, List<TournamentKnockoutFixtureData>> {

    @Override
    public Supplier<Multimap<Integer, TournamentKnockoutResultEntity>> supplier() {
        return HashMultimap::create;
    }

    @Override
    public BiConsumer<Multimap<Integer, TournamentKnockoutResultEntity>, TournamentKnockoutResultEntity> accumulator() {
        return (Multimap<Integer, TournamentKnockoutResultEntity> map, TournamentKnockoutResultEntity o) ->
                map.put(o.getEvent(), o);
    }

    @Override
    public BinaryOperator<Multimap<Integer, TournamentKnockoutResultEntity>> combiner() {
        return (map1, map2) -> {
            map1.putAll(map2);
            return map1;
        };
    }

    @Override
    public Function<Multimap<Integer, TournamentKnockoutResultEntity>, List<TournamentKnockoutFixtureData>> finisher() {
        return map -> {
            List<TournamentKnockoutFixtureData> list = Lists.newArrayList();
            map.keySet().forEach(event -> {
                TournamentKnockoutFixtureData knockoutFixtureData = new TournamentKnockoutFixtureData();
                knockoutFixtureData.setEvent(event);
                List<TournamentKnockoutEventFixtureData> knockoutEventFixtureList = Lists.newArrayList();
                map.get(event).forEach(o -> {
                    TournamentKnockoutEventFixtureData knockoutEventFixtureData = new TournamentKnockoutEventFixtureData();
                    knockoutEventFixtureData
                            .setMatchId(o.getMatchId())
                            .setPlayAgainstId(o.getPlayAginstId())
                            .setHomeEntry(o.getHomeEntry())
                            .setHomeEntryPoints(o.getHomeEntryNetPoints())
                            .setHomeEntryRank(o.getHomeEntryRank())
                            .setAwayEntry(o.getAwayEntry())
                            .setAwayEntryPoints(o.getAwayEntryNetPoints())
                            .setAwayEntryRank(o.getAwayEntryRank());
                    knockoutEventFixtureList.add(knockoutEventFixtureData);
                });
                knockoutFixtureData.setKnockoutEventFixtureList(knockoutEventFixtureList);
                list.add(knockoutFixtureData);
            });
            return list;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
    }

}
