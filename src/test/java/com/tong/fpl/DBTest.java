package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TournamentEntryEntity;
import com.tong.fpl.domain.entity.TournamentKnockoutResultEntity;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/1/19
 */
public class DBTest extends FplApplicationTests {

    @Autowired
    private EntryInfoService entryInfoService;
    @Autowired
    private TournamentEntryService tournamentEntryService;
    @Autowired
    private EntryEventResultService entryEventResultService;
    @Autowired
    private TournamentKnockoutResultService tournamentKnockoutResultService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerValueService playerValueService;
    @Autowired
    private PlayerStatService playerStatService;

    @Test
    void test1() {
        int entry = 1404;
        List<TournamentKnockoutResultEntity> knockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, 1)
                .eq(TournamentKnockoutResultEntity::getEvent, 1)
                .and(o -> o.eq(TournamentKnockoutResultEntity::getHomeEntry, entry)
                        .or(i -> i.eq(TournamentKnockoutResultEntity::getAwayEntry, entry)
                        ))
                .orderByAsc(TournamentKnockoutResultEntity::getMatchId));
        System.out.println(1);
    }

    @Test
    void test2() {
        List<EntryInfoEntity> entryInfoList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                .orderByAsc(EntryInfoEntity::getOverallRank).last("limit 1000"));
        System.out.println(1);
    }

    @Test
    void test4() {
        Map<String, Integer> map = Maps.newHashMap();
        List<TournamentEntryEntity> tournamentEntryEntities = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda().eq(TournamentEntryEntity::getTournamentId, 1));
        tournamentEntryEntities.forEach(o -> {
            int total = this.entryEventResultService.sumEntryNetPoint(o.getEntry());
            map.put(this.entryInfoService.getOne(new QueryWrapper<EntryInfoEntity>().lambda().eq(EntryInfoEntity::getEntry, o.getEntry())).getEntryName(), total);
        });
        System.out.println(1);
    }

    @Test
    void test5() {
        List<Pick> picks = CommonUtils.getPickList(42, 1908330);
        picks.forEach(o -> {
                    if (o.isCaptain()) {
                        System.out.println(o.getWebName() + "-" + 2 * o.getPoints());
                    }
                    System.out.println(o.getWebName() + "-" + o.getPoints());
                }
        );
        System.out.println(1);
    }

    @Test
    void page() {
        Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
                new Page<>(2, 20, false), new QueryWrapper<>());
        System.out.println(1);
    }

    @Test
    void dynamic() {
        MybatisPlusConfig.season.set("1920");
        List<EntryInfoEntity> entryInfoEntity = this.entryInfoService.list();
        System.out.println(1);
    }

}
