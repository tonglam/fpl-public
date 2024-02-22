package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.VaastavDataGwEntity;
import com.tong.fpl.mapper.VaastavDataGwMapper;
import org.springframework.stereotype.Service;

/**
 * Created by tong on 2022/07/07
 */
@Service
public class VaastavDataGwService extends ServiceImpl<VaastavDataGwMapper, VaastavDataGwEntity> implements IService<VaastavDataGwEntity> {

}
