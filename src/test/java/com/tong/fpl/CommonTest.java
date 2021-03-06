package com.tong.fpl;

import cn.hutool.core.bean.NullWrapperBean;
import cn.hutool.core.io.CharsetDetector;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.tong.fpl.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
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
    void letters() {
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

    @Test
    void shuffle() {
        List<String> list = Lists.newArrayList();
        list.add("??????");
        list.add("??????");
        list.add("??????");
        list.add("?????????");
        list.add("?????????");
        list.add("??????");
        list.add("?????????");
        list.add("??????");
        list.add("??????");
        list.add("???????????????");
        list.add("?????????");
        list.add("??????????????????");
        list.add("?????????");
        Collections.shuffle(list);
        list.forEach(System.out::println);
    }

    @Test
    void test() {
        String time = "2021-08-13T17:30:00Z";
        ZoneId zoneId = ZonedDateTime.now().getZone();
        LocalDateTime a = LocalDateTime.ofInstant(Instant.parse(time), zoneId).atZone(zoneId).toLocalDateTime();
        System.out.println(a);
    }

    @ParameterizedTest
    @CsvSource("E://hold_large_epay_2018-12-27.txt")
    void charset(String fileName) {
        try {
            Charset charset = CharsetDetector.detect(Files.newInputStream(Paths.get(fileName)), Charset.forName("GBK"));
            System.out.println(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @CsvSource("updateEventData")
    void invokeRedisEventDataService(String methodName) {
        CommonUtils.invokeRedisEventDataService(methodName, NullWrapperBean.class);
    }

}
