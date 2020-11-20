package com.tong.fpl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
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
		String a = "owl, fox, rice, egg, wolf, pea, frog, duck, soup, milk";
		List<String> letterList = Lists.newArrayList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
		int count = 0;
		for (String o :
				letterList) {
			int i = StringUtils.countMatches(a, o);
			count += i;
			System.out.println(o + ": " + i);
		}
		System.out.println("total: " + count);
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
