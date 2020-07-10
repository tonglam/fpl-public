package com.tong.fpl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.utils.HttpUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
		String a = "20200708";
		String b = "20200709";
		LocalDate d1 = LocalDate.parse(a, DateTimeFormatter.ofPattern(Constant.SHORTDAY));
		LocalDate d2 = LocalDate.parse(b, DateTimeFormatter.ofPattern(Constant.SHORTDAY));
		System.out.println(d1.compareTo(d2));
	}

	@Test
	public void http() {
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

}
