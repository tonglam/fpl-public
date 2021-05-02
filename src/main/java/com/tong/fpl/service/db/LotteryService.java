package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.LotteryEntity;
import com.tong.fpl.mapper.LotteryMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/5/2
 */
@Service
public class LotteryService extends ServiceImpl<LotteryMapper, LotteryEntity> implements IService<LotteryEntity> {

}
