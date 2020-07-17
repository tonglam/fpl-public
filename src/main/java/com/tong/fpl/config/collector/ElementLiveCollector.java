package com.tong.fpl.config.collector;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.domain.data.fpl.ElementLiveData;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * input: ElementLiveData(element_type, isGwStarted, isplayed)
 * accumulate: map-> key:element_type, value:dataList
 * return: map->key:element_type, value:table->row(active), column(start), value(dataList)
 * <p>
 * Create by tong on 2020/7/13
 */
public class ElementLiveCollector implements Collector<ElementLiveData, Map<Integer, List<ElementLiveData>>, Map<Integer, Table<Boolean, Boolean, List<ElementLiveData>>>> {

	@Override
	public Supplier<Map<Integer, List<ElementLiveData>>> supplier() {
		return Maps::newHashMap;
	}

	@Override
	public BiConsumer<Map<Integer, List<ElementLiveData>>, ElementLiveData> accumulator() {
		return (Map<Integer, List<ElementLiveData>> map, ElementLiveData o) -> {
			int elementType = o.getElementType();
			if (map.containsKey(elementType)) {
				List<ElementLiveData> dataList = map.get(elementType);
				dataList.add(o);
				map.put(elementType, dataList);
			} else {
				map.put(elementType, Lists.newArrayList(o));
			}
		};
	}

	@Override
	public BinaryOperator<Map<Integer, List<ElementLiveData>>> combiner() {
		return (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		};
	}

	@Override
	public Function<Map<Integer, List<ElementLiveData>>, Map<Integer, Table<Boolean, Boolean, List<ElementLiveData>>>> finisher() {
		return map -> {
			Map<Integer, Table<Boolean, Boolean, List<ElementLiveData>>> collectMap = Maps.newHashMap();
			map.keySet().forEach(elementType -> {
				//init table, all cell not null
				Table<Boolean, Boolean, List<ElementLiveData>> table = HashBasedTable.create(2, 2);
				table.put(true, true, Lists.newArrayList());
				table.put(true, false, Lists.newArrayList());
				table.put(false, true, Lists.newArrayList());
				table.put(false, false, Lists.newArrayList());
				// put the real value
				map.get(elementType).forEach(o -> {
					boolean active = !o.isGwStarted() || o.isPlayed();
					boolean start = o.getPosition() < 12;
					List<ElementLiveData> list = table.get(active, start);
					list.add(o);
					table.put(active, start, list);
				});
				collectMap.put(elementType, table);
			});
			return collectMap;
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
	}

}
