package com.tong.fpl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.utils.HttpUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
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
		System.out.println(Period.between(LocalDate.now(), LocalDate.of(2020, 9, 12)).getDays() + " Days");
		System.out.println("Deadline: " + Period.between(LocalDate.now(), LocalDate.of(2020, 9, 12)).getMonths() + " Month "
				+ Period.between(LocalDate.now(), LocalDate.of(2020, 9, 12)).getDays() + " Days");
	}

	@Test
	void http() {
		List<Integer> chinaList = Lists.newArrayList();
		IntStream.range(1, 7580961).forEach(entry -> {
			try {
				String result = HttpUtils.httpGet(String.format(Constant.ENTRY, entry)).orElse("");
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				EntryRes entryRes = mapper.readValue(result, EntryRes.class);
				if (entryRes == null) {
					return;
				}
				if (entryRes.getPlayerRegionId() == 45) {
					chinaList.add(entryRes.getId());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		System.out.println(chinaList.size());
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
