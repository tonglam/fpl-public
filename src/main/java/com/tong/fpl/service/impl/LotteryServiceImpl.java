package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.domain.entity.LotteryEntity;
import com.tong.fpl.domain.letletme.lottery.LotteryData;
import com.tong.fpl.service.ILotteryService;
import com.tong.fpl.service.db.LotteryService;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Create by tong on 2021/5/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LotteryServiceImpl implements ILotteryService {

    private final LotteryService lotteryService;

    @Override
    public String addLottery(LotteryData data) {
        if (this.lotteryService.count(new QueryWrapper<LotteryEntity>().lambda()
                .eq(LotteryEntity::getName, data.getName())) > 0) {
            return "抽奖已存在";
        }
        this.lotteryService.save(new LotteryEntity()
                .setName(data.getName())
                .setCreator(data.getCreator())
                .setPrizeMap(JsonUtils.obj2json(data.getPrizeMap()))
                .setEntry("")
                .setResult("")
        );
        return "新增抽奖成功";
    }

    @Override
    public String updateLottery(LotteryData data) {
        LotteryEntity lotteryEntity = this.lotteryService.getOne(new QueryWrapper<LotteryEntity>().lambda()
                .eq(LotteryEntity::getName, data.getName()));
        if (lotteryEntity == null) {
            return "抽奖不存在";
        }
        BeanUtil.copyProperties(data, lotteryEntity, CopyOptions.create().ignoreNullValue());
        this.lotteryService.updateById(lotteryEntity);
        return "抽奖修改成功";
    }

    @Override
    public String deleteLottery(LotteryData data) {
        LotteryEntity lotteryEntity = this.lotteryService.getOne(new QueryWrapper<LotteryEntity>().lambda()
                .eq(LotteryEntity::getName, data.getName()));
        if (lotteryEntity == null) {
            return "抽奖不存在";
        }
        this.lotteryService.removeById(lotteryEntity.getId());
        return "抽奖删除成功";
    }

    @Override
    public String updatePrize(int lotteryId, Map<String, Integer> prizeMap) {
        LotteryEntity lotteryEntity = this.lotteryService.getById(lotteryId);
        if (lotteryEntity == null) {
            return "抽奖不存在";
        }
        lotteryEntity.setPrizeMap(JsonUtils.obj2json(prizeMap));
        this.lotteryService.updateById(lotteryEntity);
        return "奖品更新成功";
    }

    @Override
    public String addEntry(int lotteryId, int entry) {
        LotteryEntity lotteryEntity = this.lotteryService.getById(lotteryId);
        if (lotteryEntity == null) {
            return "抽奖不存在";
        }
        List<Integer> entryList = Lists.newArrayList(entry);
        if (StringUtils.isNoneEmpty(lotteryEntity.getEntry())) {
            entryList = JsonUtils.json2Collection(lotteryEntity.getEntry(), List.class, Integer.class);
        }
        lotteryEntity.setEntry(JsonUtils.obj2json(entryList));
        this.lotteryService.updateById(lotteryEntity);
        return "增加抽奖人员成功";
    }

    @Override
    public String deleteEntry(int lotteryId, int entry) {
        LotteryEntity lotteryEntity = this.lotteryService.getById(lotteryId);
        if (lotteryEntity == null) {
            return "抽奖不存在";
        }
        List<Integer> entryList = Lists.newArrayList();
        if (StringUtils.isNoneEmpty(lotteryEntity.getEntry())) {
            entryList = JsonUtils.json2Collection(lotteryEntity.getEntry(), List.class, Integer.class);
        }
        if (!CollectionUtils.isEmpty(entryList)) {
            entryList.remove(entry);
            lotteryEntity.setEntry(JsonUtils.obj2json(entryList));
            this.lotteryService.updateById(lotteryEntity);
        }
        return "删除抽奖人员成功";
    }

    @Override
    public List<String> getLotteryEntryList(int lotteryId) {
        LotteryEntity lotteryEntity = this.lotteryService.getById(lotteryId);
        if (lotteryEntity == null) {
            return Lists.newArrayList();
        }
        return JsonUtils.json2Collection(lotteryEntity.getEntry(), List.class, String.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void draw(int lotteryId) throws Exception {
        LotteryEntity lotteryEntity = this.lotteryService.getById(lotteryId);
        if (lotteryEntity == null) {
            throw new Exception("抽奖不存在");
        }
        // get prize
        Map<String, Integer> prizeMap = (Map<String, Integer>) JsonUtils.json2obj(lotteryEntity.getPrizeMap(), Map.class);
        if (CollectionUtils.isEmpty(prizeMap)) {
            return;
        }
        // get entry_list
        List<Integer> entryList = JsonUtils.json2Collection(lotteryEntity.getEntry(), List.class, Integer.class);
        if (CollectionUtils.isEmpty(entryList)) {
            return;
        }
        // draw
        Map<String, Integer> resultMap = Maps.newHashMap();
        Random random = new Random();
        for (String prize :
                prizeMap.keySet()) {
            int size = entryList.size();
            IntStream.range(1, prizeMap.get(prize))
                    .forEach(i -> resultMap.put(prize, entryList.get(random.nextInt(size))));
        }
        // update
        lotteryEntity.setResult(JsonUtils.obj2json(resultMap));
        this.lotteryService.updateById(lotteryEntity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Integer> getDrawResult(int lotteryId) {
        LotteryEntity lotteryEntity = this.lotteryService.getById(lotteryId);
        if (lotteryEntity == null) {
            return Maps.newHashMap();
        }
        return (Map<String, Integer>) JsonUtils.json2obj(lotteryEntity.getResult(), Map.class);
    }

}
