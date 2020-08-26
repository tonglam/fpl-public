package com.tong.fpl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.utils.JsonUtils;
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
		String picks = "[{\"element\":366,\"points\":1,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":182,\"points\":5,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":181,\"points\":2,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":203,\"points\":8,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":141,\"points\":6,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":214,\"points\":20,\"is_captain\":false,\"is_vice_captain\":true},{\"element\":133,\"points\":1,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":265,\"points\":3,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":191,\"points\":12,\"is_captain\":true,\"is_vice_captain\":false},{\"element\":409,\"points\":2,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":68,\"points\":2,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":48,\"points\":0,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":420,\"points\":3,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":128,\"points\":6,\"is_captain\":false,\"is_vice_captain\":false},{\"element\":130,\"points\":1,\"is_captain\":false,\"is_vice_captain\":false}]";
		List<Pick> pickList = JsonUtils.json2Collection(picks, List.class, Pick.class);
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
