package com.tong.fpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.NullWrapperBean;
import cn.hutool.core.io.CharsetDetector;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.EventLiveSummaryEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.letletme.element.ElementEventData;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.EventLiveSummaryService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.PlayerValueService;
import com.tong.fpl.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create by tong on 2020/4/29
 */
public class CommonTest extends FplApplicationTests {

    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerValueService playerValueService;
    @Autowired
    private EventLiveSummaryService eventLiveSummaryService;
    @Autowired
    private IRedisCacheService redisCacheService;
    @Autowired
    private EventLiveService eventLiveService;

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
        list.add("豌豆");
        list.add("豆荚");
        list.add("永远");
        list.add("更大的");
        list.add("兴奋的");
        list.add("子弹");
        list.add("懒惰的");
        list.add("屋顶");
        list.add("院子");
        list.add("碰撞；撞击");
        list.add("见世面");
        list.add("一个接一个地");
        list.add("往外看");
        Collections.shuffle(list);
        list.forEach(System.out::println);
    }

    @Test
    void test() {
        int size = 5;
        Random random = new Random();
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
        System.out.println(random.nextInt(size));
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

    @Test
    void fpler() {
        String webName = "aabbccasd";
        String a = "";
        if (webName.length() < 5) {
            a = StringUtils.rightPad(webName, 5, "x");
        }
        a = webName.substring(0, 5);
        System.out.println(a);
    }

    @Test
    void filterEnglish() {
        String regex = "[a-zA-Z]*";
        String content = "Schürrle";
        boolean a = Pattern.matches(regex, content);
        System.out.println(a);
    }

    @ParameterizedTest
    @CsvSource("2122")
    void highest(String season) {
        Table<Integer, Integer, Integer> formationTable = HashBasedTable.create();
        formationTable.put(3, 6, 1);
        formationTable.put(3, 5, 2);
        formationTable.put(3, 4, 3);
        formationTable.put(3, 6, 1);
        formationTable.put(4, 5, 1);
        formationTable.put(4, 4, 2);
        formationTable.put(5, 4, 1);
        formationTable.put(5, 3, 2);
        formationTable.put(5, 2, 3);
        MybatisPlusConfig.season.set(season);
        // prepare
        Map<Integer, List<ElementEventData>> map = Maps.newHashMap();
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        List<EventLiveSummaryEntity> eventLiveSummaryList = this.eventLiveSummaryService.list();
        // calc
        formationTable.rowKeySet().forEach(def -> {
            formationTable.row(def).forEach((mid, fwd) -> {
                List<ElementEventData> list = this.qryHighest(def, mid, fwd, eventLiveSummaryList, playerMap);
                int totalPoints = list
                        .stream()
                        .mapToInt(ElementEventData::getTotalPoints)
                        .sum();
                map.put(totalPoints, list);
            });
        });
        int highest = map.keySet()
                .stream()
                .max(Comparator.naturalOrder())
                .orElse(0);
        System.out.println("season: " + season + ", overall_points: " + highest);
        List<ElementEventData> result = map.get(highest);
        result.forEach(o -> {
            System.out.println("webName: " + o.getWebName() + ", position: " + o.getElementTypeName() + ", points: " + o.getTotalPoints());
        });
        MybatisPlusConfig.season.remove();
    }

    private List<ElementEventData> qryHighest(int def, int mid, int fwd, List<EventLiveSummaryEntity> eventLiveSummaryList, Map<Integer, PlayerEntity> playerMap) {
        List<ElementEventData> list = Lists.newArrayList();
        List<ElementEventData> gkpList = eventLiveSummaryList
                .stream()
                .filter(o -> o.getElementType() == Position.GKP.getElementType())
                .sorted(Comparator.comparing(EventLiveSummaryEntity::getTotalPoints).reversed())
                .limit(1)
                .map(o -> BeanUtil.copyProperties(o, ElementEventData.class))
                .collect(Collectors.toList());
        list.addAll(gkpList);
        List<ElementEventData> defList = eventLiveSummaryList
                .stream()
                .filter(o -> o.getElementType() == Position.DEF.getElementType())
                .sorted(Comparator.comparing(EventLiveSummaryEntity::getTotalPoints).reversed())
                .limit(def)
                .map(o -> BeanUtil.copyProperties(o, ElementEventData.class))
                .collect(Collectors.toList());
        list.addAll(defList);
        List<ElementEventData> midList = eventLiveSummaryList
                .stream()
                .filter(o -> o.getElementType() == Position.MID.getElementType())
                .sorted(Comparator.comparing(EventLiveSummaryEntity::getTotalPoints).reversed())
                .limit(mid)
                .map(o -> BeanUtil.copyProperties(o, ElementEventData.class))
                .collect(Collectors.toList());
        list.addAll(midList);
        List<ElementEventData> fwdList = eventLiveSummaryList
                .stream()
                .filter(o -> o.getElementType() == Position.FWD.getElementType())
                .sorted(Comparator.comparing(EventLiveSummaryEntity::getTotalPoints).reversed())
                .limit(fwd)
                .map(o -> BeanUtil.copyProperties(o, ElementEventData.class))
                .collect(Collectors.toList());
        list.addAll(fwdList);
        list.forEach(o -> {
            PlayerEntity playerEntity = playerMap.get(o.getElement());
            if (playerEntity == null) {
                return;
            }
            o.setWebName(playerEntity.getWebName());
            o.setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()));
        });
        return list;
    }

    @ParameterizedTest
    @CsvSource("Robertson, 2")
    void testsss(String webName, int event) {
        List<String> seasonList = Lists.newArrayList("1617", "1718", "1819", "1920", "2021", "2122");
        seasonList.forEach(season -> {
            Map<String, PlayerEntity> playerEntityMap = this.redisCacheService.getPlayerMap(season);
            int element = playerEntityMap.values()
                    .stream()
                    .filter(o -> StringUtils.equals(webName, o.getWebName()))
                    .map(PlayerEntity::getElement)
                    .findFirst()
                    .orElse(0);
            if (element == 0) {
                return;
            }
            MybatisPlusConfig.season.set(season);
            EventLiveEntity eventLiveEntity = this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda()
                    .eq(EventLiveEntity::getElement, element)
                    .eq(EventLiveEntity::getEvent, event));
            if (eventLiveEntity == null) {
                return;
            }
            System.out.println(season + "赛季-GW" + event + ", " + webName + ", 进球:" + eventLiveEntity.getGoalsScored() + ", 助攻:" + eventLiveEntity.getAssists() + ", BONUS:" + eventLiveEntity.getBonus() + ", 得分:" + eventLiveEntity.getTotalPoints());
            MybatisPlusConfig.season.remove();
        });
    }

    @ParameterizedTest
    @CsvSource("https://fantasy.premierleague.com/leagues/434/standings/c")
    void getLeagueId(String url) {
        int leagueId = CommonUtils.getLeagueId(url);
        System.out.println(leagueId);
    }

}
