package com.tong.fpl.service;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Lists;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.entity.SubtitleEntity;
import com.tong.fpl.service.db.SubtitleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Create by tong on 2020/12/16
 */
public class SubtitleTest extends FplApplicationTests {

    @Autowired
    private ISubtitleService subtitleService;
    @Autowired
    private SubtitleService dbSubtitleService;

    @ParameterizedTest
    @CsvSource({"fight back Motivational Speech Video by Walter Bond.txt, false"})
    void mergeSubtitle(String fileName, boolean engSub) {
        this.subtitleService.mergeSubtitle(fileName, engSub);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"/home/workspace/songs/, Creep-Gamper & Dadoni Ember Island"})
    void parseIrcToWord(String dir, String name) {
        this.subtitleService.parseIrcToWord(dir, name);
        System.out.println(1);
    }

    @Test
    void test() {
        List<SubtitleEntity> list = Lists.newArrayList();
        this.dbSubtitleService.list().forEach(o -> {
            double protion = NumberUtil.div(Double.parseDouble(o.getProportion()), 10);
            int transLen = NumberUtil.round(o.getLength() * protion, 0).intValue();
            int proLen = o.getLength() - transLen;
            o
                    .setTranslatorLength(transLen)
                    .setProofreaderLength(proLen);
            list.add(o);
        });
        this.dbSubtitleService.updateBatchById(list);
        System.out.println(1);
    }

}
