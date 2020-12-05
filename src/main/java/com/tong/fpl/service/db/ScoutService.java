package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.ScoutEntity;
import com.tong.fpl.mapper.ScoutMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/12/5
 */
@Service
public class ScoutService extends ServiceImpl<ScoutMapper, ScoutEntity> implements IService<ScoutEntity> {

}
