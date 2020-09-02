package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryCaptainStatEntity;
import com.tong.fpl.mapper.EntryCaptainStatMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/9/2
 */
@Service
public class EntryCaptainStatService extends ServiceImpl<EntryCaptainStatMapper, EntryCaptainStatEntity> implements IService<EntryCaptainStatEntity> {
}
