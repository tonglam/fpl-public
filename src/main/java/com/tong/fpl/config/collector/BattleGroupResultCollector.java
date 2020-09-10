package com.tong.fpl.config.collector;

import com.google.common.collect.Table;
import com.tong.fpl.domain.entity.TournamentBattleGroupResultEntity;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupEventFixtureData;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * * input: ElementLiveData(element_type, isGwStarted, isplayed)
 * * accumulate: map-> key:element_type, value:dataList
 * * return: map->key:element_type, value:table->row(active), column(start), value(dataList)
 * * <p>
 * Create by tong on 2020/9/10
 * // event -> groupId -> data
 */
public class BattleGroupResultCollector implements Collector<TournamentBattleGroupResultEntity, Map<Integer, List<TournamentGroupEventFixtureData>>, Table<Integer, Integer, List<TournamentGroupEventFixtureData>>> {

	@Override
	public Supplier<Map<Integer, List<TournamentGroupEventFixtureData>>> supplier() {
		return null;
	}

	@Override
	public BiConsumer<Map<Integer, List<TournamentGroupEventFixtureData>>, TournamentBattleGroupResultEntity> accumulator() {
		return null;
	}

	@Override
	public BinaryOperator<Map<Integer, List<TournamentGroupEventFixtureData>>> combiner() {
		return null;
	}

	@Override
	public Function<Map<Integer, List<TournamentGroupEventFixtureData>>, Table<Integer, Integer, List<TournamentGroupEventFixtureData>>> finisher() {
		return null;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return null;
	}

}
