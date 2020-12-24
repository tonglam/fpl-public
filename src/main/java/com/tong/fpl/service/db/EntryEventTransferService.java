package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.EntryEventTransferEntity;
import com.tong.fpl.mapper.EntryEventTransferMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/12/14
 */
@Service
public class EntryEventTransferService extends ServiceImpl<EntryEventTransferMapper, EntryEventTransferEntity> implements IService<EntryEventTransferEntity> {

}
