package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.db.entity.CupInfoEntity;
import com.tong.fpl.mapper.CupInfoMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/23
 */
@Service
public class CupInfoService extends ServiceImpl<CupInfoMapper, CupInfoEntity> implements IService<CupInfoEntity> {
}
