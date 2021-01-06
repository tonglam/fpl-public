package com.tong.fpl.config.collector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Create by tong on 2021/1/6
 */
public class PlayerPickDataCollector implements Collector<EntryPickData, Map<Integer, PlayerPickData>, List<PlayerPickData>> {

	@Override
	public Supplier<Map<Integer, PlayerPickData>> supplier() {
		return Maps::newHashMap;
	}

	@Override
	public BiConsumer<Map<Integer, PlayerPickData>, EntryPickData> accumulator() {
		return (Map<Integer, PlayerPickData> map, EntryPickData pickData) -> {
			int entry = pickData.getEntry();
			// collect
			List<EntryPickData> gkpList = Lists.newArrayList();
			List<EntryPickData> defList = Lists.newArrayList();
			List<EntryPickData> midList = Lists.newArrayList();
			List<EntryPickData> fwdList = Lists.newArrayList();
			List<EntryPickData> subList = Lists.newArrayList();
			PlayerPickData data;
			if (!map.containsKey(entry)) {
				data = new PlayerPickData().setEntry(entry);
			} else {
				data = map.get(entry);
				gkpList = data.getGkps();
				defList = data.getDefs();
				midList = data.getMids();
				fwdList = data.getFwds();
				subList = data.getSubs();
			}
			if (pickData.getPosition() <= 11) {
				switch (pickData.getElementType()) {
					case 1: {
						gkpList.add(pickData);
						break;
					}
					case 2: {
						defList.add(pickData);
						break;
					}
					case 3: {
						midList.add(pickData);
						break;
					}
					case 4: {
						fwdList.add(pickData);
						break;
					}
				}
			} else {
				subList.add(pickData);
			}
			data
					.setGkps(gkpList)
					.setDefs(defList)
					.setMids(midList)
					.setFwds(fwdList)
					.setSubs(subList);
			map.put(entry, data);
		};
	}

	@Override
	public BinaryOperator<Map<Integer, PlayerPickData>> combiner() {
		return (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		};
	}

	@Override
	public Function<Map<Integer, PlayerPickData>, List<PlayerPickData>> finisher() {
		return map -> Lists.newArrayList(map.values());
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
	}

}
