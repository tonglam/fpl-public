package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryEventSimulatePickEntity;
import com.tong.fpl.mapper.EntryEventSimulatePickMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/1/28
 */
@Service
public class EntryEventSimulatePickService extends ServiceImpl<EntryEventSimulatePickMapper, EntryEventSimulatePickEntity> implements IService<EntryEventSimulatePickEntity> {

}
