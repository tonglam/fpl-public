package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tong.fpl.db.entity.EntryLiveEntity;
import com.tong.fpl.mapper.EntryLiveMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create by tong on 2020/4/29
 */
public class CommonTest extends FplApplicationTests {

	@Autowired
	private EntryLiveMapper entryLiveMapper;

	@Test
	public void calcLivePoints() {
		List<EntryLiveEntity> entryLiveList = this.entryLiveMapper.selectList(new QueryWrapper<EntryLiveEntity>().lambda()
				.eq(EntryLiveEntity::getEvent, 29).eq(EntryLiveEntity::getEntry, 3697).groupBy(EntryLiveEntity::getPosition));
		// element_type -> active -> start
		Map<Integer, Map<Boolean, Map<Boolean, List<EntryLiveEntity>>>> map = entryLiveList.stream()
				.collect(Collectors.groupingBy(EntryLiveEntity::getElementType,
						Collectors.partitioningBy(EntryLiveEntity::isPlayed,
								Collectors.partitioningBy(entryLiveEntity -> entryLiveEntity.getPosition() < 12))));
		// gkp
		List<EntryLiveEntity> gkps = this.createSteam(map.get(1).get(true).get(true), map.get(1).get(true).get(false), map.get(1).get(false).get(true))
				.flatMap(Collection::stream)
				.limit(1)
				.collect(Collectors.toList());
		// active def
		List<EntryLiveEntity> defs = this.createSteam(map.get(2).get(true).get(true), map.get(2).get(true).get(false))
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
				.collect(Collectors.toList());
		// def rule, at least 3
		if (defs.size() < 3) {
			defs = this.createSteam(defs, map.get(2).get(false).get(true))
					.flatMap(Collection::stream)
					.limit(3)
					.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
					.collect(Collectors.toList());
		}
		// active fwd
		List<EntryLiveEntity> fwds = this.createSteam(map.get(4).get(true).get(true), map.get(4).get(true).get(false))
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
				.collect(Collectors.toList());
		// fwd rule, at least 1
		if (fwds.size() < 1) {
			fwds.add(map.get(4).get(false).get(true).get(0));
		}
		//mid
		int maxMidNum = 11 - gkps.size() - defs.size() - fwds.size();
		List<EntryLiveEntity> mids = this.createSteam(map.get(3).get(true).get(true), map.get(3).get(true).get(false))
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
				.limit(maxMidNum)
				.collect(Collectors.toList());
		// active_list
		List<EntryLiveEntity> activeList = this.createSteam(gkps, defs, fwds, mids)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		List<EntryLiveEntity> standByList = this.createSteam(map.get(2).get(false).get(true), map.get(3).get(false).get(true), map.get(4).get(false).get(true))
				.flatMap(Collection::stream)
				.filter(o -> !activeList.contains(o))
				.sorted(Comparator.comparing(EntryLiveEntity::getPosition))
				.limit(11 - activeList.size())
				.collect(Collectors.toList());
		List<EntryLiveEntity> list = this.createSteam(activeList, standByList)
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(EntryLiveEntity::getElementType).thenComparing(EntryLiveEntity::getPosition))
				.collect(Collectors.toList());
		list.forEach(o -> System.out.println(o.getPosition()));
		int point = list.stream()
				.peek(o -> {
					if (o.isCaptain()) {
						o.setPoint(2 * o.getPoint());
					}
				})
				.mapToInt(EntryLiveEntity::getPoint)
				.sum();
		System.out.println(point);
	}

	@SafeVarargs
	private final <T> Stream<T> createSteam(T... values) {
		Stream.Builder<T> builder = Stream.builder();
		Arrays.asList(values).forEach(builder::add);
		return builder.build();
	}

	@Test
	public void group() {
		int num = 4;
		List<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
		int lenth = list.size();
		int size = lenth / num;
		Random random = new Random();
		List<Integer> a = Lists.newArrayList();
		for (int i = 0; i < size; i++) {
			int index = random.nextInt(lenth);
			a.add(list.get(index));
			list.remove(index);
		}
		System.out.println("1");
	}

	@Test
	public void test() {
		Multimap<Integer, Integer> teamInGroup = ArrayListMultimap.create();
		teamInGroup.put(1, 1);
		teamInGroup.put(1, 2);
		teamInGroup.put(1, 3);
		teamInGroup.put(1, 4);
		teamInGroup.put(1, 5);
		teamInGroup.put(1, 6);
//		teamInGroup.put(1, 7);
		boolean a = teamInGroup.get(1).stream().parallel().allMatch(o -> o > 0);
		System.out.println(a);
	}


}
