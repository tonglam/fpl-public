package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryCupResultEntity;
import com.tong.fpl.mapper.EntryCupResultMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/2/24
 */
@Service
public class EntryCupResultService extends ServiceImpl<EntryCupResultMapper, EntryCupResultEntity> implements IService<EntryCupResultEntity> {

}
