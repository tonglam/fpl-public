package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryEventSimulateTransfersEntity;
import com.tong.fpl.mapper.EntryEventSimulateTransfersMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/1/11
 */
@Service
public class EntryEventSimulateTransfersService extends ServiceImpl<EntryEventSimulateTransfersMapper, EntryEventSimulateTransfersEntity> implements IService<EntryEventSimulateTransfersEntity> {

}
