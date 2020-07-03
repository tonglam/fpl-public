package com.tong.fpl;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.utils.HttpUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
		int knockoutTeam = 64;
		List<Integer> entryList = Lists.newArrayList();
		IntStream.range(1, knockoutTeam + 1).forEach(entryList::add);
		System.out.println(1);
	}

	@Test
	public void http() {
		try {
//			String profile = HttpUtils.httpLogin("bluedragon00000@sina.com", "9111130609fpl");
			String result = HttpUtils.httpGet(String.format(Constant.LEAGUES_CLASSIC, 12683, 1)).orElse("");
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(1);
	}

}
