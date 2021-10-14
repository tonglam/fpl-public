package com.tong.fpl.config.collector;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * input: ElementLiveData(element_type, isGwStarted, isPlayed)
 * accumulate: map -> key:element_type, value:dataList
 * return: map ->key:element_type, value:table->row(active), column(start), value(dataList)
 * <p>
 * Create by tong on 2020/7/13
 */
public class ElementLiveCollector implements Collector<ElementEventResultData, Map<Integer, List<ElementEventResultData>>, Map<Integer, Table<Boolean, Boolean, List<ElementEventResultData>>>> {

    @Override
    public Supplier<Map<Integer, List<ElementEventResultData>>> supplier() {
        return Maps::newHashMap;
    }

    @Override
    public BiConsumer<Map<Integer, List<ElementEventResultData>>, ElementEventResultData> accumulator() {
        return (Map<Integer, List<ElementEventResultData>> map, ElementEventResultData o) -> {
            int elementType = o.getElementType();
            if (map.containsKey(elementType)) {
                List<ElementEventResultData> dataList = map.get(elementType);
                dataList.add(o);
                map.put(elementType, dataList);
            } else {
                map.put(elementType, Lists.newArrayList(o));
            }
        };
    }

    @Override
    public BinaryOperator<Map<Integer, List<ElementEventResultData>>> combiner() {
        return (map1, map2) -> {
            map1.putAll(map2);
            return map1;
        };
    }

    @Override
    public Function<Map<Integer, List<ElementEventResultData>>, Map<Integer, Table<Boolean, Boolean, List<ElementEventResultData>>>> finisher() {
        return map -> {
            Map<Integer, Table<Boolean, Boolean, List<ElementEventResultData>>> collectMap = Maps.newHashMap();
            map.keySet().forEach(elementType -> {
                //init table, all cell not null
                Table<Boolean, Boolean, List<ElementEventResultData>> table = HashBasedTable.create(2, 2);
                table.put(true, true, Lists.newArrayList());
                table.put(true, false, Lists.newArrayList());
                table.put(false, true, Lists.newArrayList());
                table.put(false, false, Lists.newArrayList());
                // put the real value
                map.get(elementType).forEach(o -> {
                    boolean active = !o.isGwFinished() || (o.isGwStarted() && o.isPlayed());
                    boolean start = o.getPosition() < 12;
                    List<ElementEventResultData> list = table.get(active, start);
                    if (CollectionUtils.isEmpty(list)) {
                        list = Lists.newArrayList();
                    }
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
