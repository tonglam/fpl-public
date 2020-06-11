package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.db.entity.CupKnockoutEntity;
import com.tong.fpl.mapper.CupKnockoutMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/11
 */
@Service
public class CupKnockoutService extends ServiceImpl<CupKnockoutMapper, CupKnockoutEntity> implements IService<CupKnockoutEntity> {
}
