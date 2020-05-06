package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.tong.fpl.db.entity.EntryLiveEntity;
import com.tong.fpl.mapper.EntryLiveMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Create by tong on 2020/1/19
 */
public class DBTest extends FplApplicationTests {

    @Autowired
    private EntryLiveMapper entryLiveMapper;

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
        List<EntryLiveEntity> entryLiveList = new LambdaQueryChainWrapper<>(entryLiveMapper).select().list();
        System.out.println(entryLiveList.size());
    }

}
