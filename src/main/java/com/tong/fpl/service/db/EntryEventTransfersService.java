package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryEventTransfersEntity;
import com.tong.fpl.mapper.EntryEventTransfersMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/12/14
 */
@Service
public class EntryEventTransfersService extends ServiceImpl<EntryEventTransfersMapper, EntryEventTransfersEntity> implements IService<EntryEventTransfersEntity> {

}
