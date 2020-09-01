package com.tong.fpl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Create by tong on 2020/4/29
 */
public class CommonTest extends FplApplicationTests {

	@SafeVarargs
	private final <T> Stream<T> createSteam(T... values) {
		Stream.Builder<T> builder = Stream.builder();
		Arrays.asList(values).forEach(builder::add);
		return builder.build();
	}

	@Test
	void group() {
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
	void test() {
		TournamentInfoData a = new TournamentInfoData().setId(1111);
		TableData<TournamentInfoData> b = new TableData<>(a);
		System.out.println(1);
	}

	@Test
	void tableData() {
		TournamentInfoData a = new TournamentInfoData().setId(1111);
		TableData<TournamentInfoData> aa = new TableData<>(a);
		List<TournamentInfoData> list = Lists.newArrayList();
		list.add(a);
		TableData<TournamentInfoData> ab = new TableData<>(list);
		Page<TournamentInfoData> page = new Page<>();
		page.setRecords(list);
		TableData<TournamentInfoData> ac = new TableData<>(page);
		System.out.println(1);
	}

	@Test
	void guavaTable() {
		Table<Boolean, Boolean, List<Integer>> table = HashBasedTable.create();
		table.put(true, true, Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
		table.put(true, false, Lists.newArrayList(3));
		table.put(false, true, Lists.newArrayList(13, 14));
		table.put(false, false, Lists.newArrayList(12, 15));
		Collection<List<Integer>> a = table.values();
		int b = a.stream().findFirst().orElse(Lists.newArrayList(0)).get(0);
		System.out.println(1);
	}

}
