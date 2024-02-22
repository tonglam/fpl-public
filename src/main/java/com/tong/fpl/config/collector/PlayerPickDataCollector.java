package com.tong.fpl.config.collector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/1/6
 */
public class PlayerPickDataCollector implements Collector<EntryPickData, Map<Integer, Map<Integer, PlayerPickData>>, List<PlayerPickData>> {

	@Override
	public Supplier<Map<Integer, Map<Integer, PlayerPickData>>> supplier() {
		return Maps::newHashMap;
	}

	@Override
	public BiConsumer<Map<Integer, Map<Integer, PlayerPickData>>, EntryPickData> accumulator() {
		return (Map<Integer, Map<Integer, PlayerPickData>> map, EntryPickData pickData) -> {
			int entry = pickData.getEntry();
			int event = pickData.getEvent();
			Map<Integer, PlayerPickData> eventMap = Maps.newHashMap();
			if (map.containsKey(entry)) {
				eventMap = map.get(entry);
			}
			// collect
			List<EntryPickData> gkpList = Lists.newArrayList();
			List<EntryPickData> defList = Lists.newArrayList();
			List<EntryPickData> midList = Lists.newArrayList();
			List<EntryPickData> fwdList = Lists.newArrayList();
			List<EntryPickData> subList = Lists.newArrayList();
			PlayerPickData data;
			if (!eventMap.containsKey(event)) {
				data = new PlayerPickData().setEntry(entry).setEvent(event);
			} else {
				data = eventMap.get(event);
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
					.setSubs(subList)
					.setFormation(StringUtils.joinWith("-", defList.size(), midList.size(), fwdList.size()));
			eventMap.put(event, data);
			map.put(entry, eventMap);
		};
	}

	@Override
	public BinaryOperator<Map<Integer, Map<Integer, PlayerPickData>>> combiner() {
		return (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		};
	}

	@Override
	public Function<Map<Integer, Map<Integer, PlayerPickData>>, List<PlayerPickData>> finisher() {
		return map -> {
			List<PlayerPickData> list = Lists.newArrayList();
			map.keySet().forEach(entry -> {
				Map<Integer, PlayerPickData> eventMap = map.get(entry);
				List<EntryPickData> gkpList = Lists.newArrayList();
				List<EntryPickData> defList = Lists.newArrayList();
				List<EntryPickData> midList = Lists.newArrayList();
				List<EntryPickData> fwdList = Lists.newArrayList();
				List<EntryPickData> subList = Lists.newArrayList();
				// merge
				eventMap.values().forEach(o -> {
					gkpList.addAll(o.getGkps());
					defList.addAll(o.getDefs());
					midList.addAll(o.getMids());
					fwdList.addAll(o.getFwds());
					subList.addAll(o.getSubs());
				});
				String formation = eventMap.values()
						.stream()
						.map(PlayerPickData::getFormation)
						.collect(Collectors.groupingBy(String::valueOf, Collectors.counting()))
						.entrySet()
						.stream()
						.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
						.map(Map.Entry::getKey)
						.findFirst()
						.orElse("");
				list.add(
						new PlayerPickData()
								.setEntry(entry)
								.setGkps(gkpList)
								.setDefs(defList)
								.setMids(midList)
								.setFwds(fwdList)
								.setSubs(subList)
								.setFormation(formation)
				);
			});
			return list;
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
	}

}
