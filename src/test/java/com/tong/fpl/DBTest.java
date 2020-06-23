package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.fpl.db.entity.EntryInfoEntity;
import com.tong.fpl.db.entity.EntryLiveEntity;
import com.tong.fpl.mapper.EntryLiveMapper;
import com.tong.fpl.service.db.EntryInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/1/19
 */
public class DBTest extends FplApplicationTests {

    @Autowired
    private EntryLiveMapper entryLiveMapper;
    @Autowired
    private EntryInfoService entryInfoService;

    @Test
    public void test1() {
        LambdaQueryWrapper<EntryLiveEntity> queryWrapper = new QueryWrapper<EntryLiveEntity>().lambda()
                .eq(EntryLiveEntity::getEvent, 29)
                .eq(EntryLiveEntity::getEntry, 3697);
        List<EntryLiveEntity> entryLiveList = this.entryLiveMapper.selectList(queryWrapper);
        System.out.println(entryLiveList.size());
    }

    @Test
    public void test2() {
        List<Integer> entryList = this.entryInfoService.list().stream().map(EntryInfoEntity::getEntry).collect(Collectors.toList());
        System.out.println(entryList.size());
    }

}
