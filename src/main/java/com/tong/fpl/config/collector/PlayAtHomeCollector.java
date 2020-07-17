package com.tong.fpl.config.collector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.domain.entity.EventFixtureEntity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * input: event_fixture, get home team and away team
 * accumulate: key:home team; value:away team; could be multi values because of dgw
 * return: key:team; value: played at home(true)
 * <p>
 * Create by tong on 2020/7/9
 */
public class PlayAtHomeCollector implements Collector<EventFixtureEntity, Multimap<Integer, Integer>, Map<Integer, String>> {

	@Override
	public Supplier<Multimap<Integer, Integer>> supplier() {
		return ArrayListMultimap::create;
	}

	@Override
	public BiConsumer<Multimap<Integer, Integer>, EventFixtureEntity> accumulator() {
		return (Multimap<Integer, Integer> fixtureMap, EventFixtureEntity eventFixture) -> fixtureMap.put(eventFixture.getTeamH(), eventFixture.getTeamA());
	}

	@Override
	public BinaryOperator<Multimap<Integer, Integer>> combiner() {
		return (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		};
	}

	@Override
	public Function<Multimap<Integer, Integer>, Map<Integer, String>> finisher() {
		return fixtureMap -> {
			Map<Integer, String> playAtHomeMap = Maps.newHashMap();
			for (int home : fixtureMap.keySet()) {
				playAtHomeMap.put(home, "1");
				fixtureMap.get(home).forEach(away -> {
					if (playAtHomeMap.containsKey(away)) {
						String oldValue = playAtHomeMap.get(away);
						playAtHomeMap.put(away, oldValue + "0");
					} else {
						playAtHomeMap.put(away, "0");
					}
				});
			}
			return playAtHomeMap;
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
	}

}
