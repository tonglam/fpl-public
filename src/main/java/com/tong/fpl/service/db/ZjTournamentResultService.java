package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.ZjTournamentResultEntity;
import com.tong.fpl.mapper.ZjTournamentResultMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/10/21
 */
@Service
public class ZjTournamentResultService extends ServiceImpl<ZjTournamentResultMapper, ZjTournamentResultEntity> implements IService<ZjTournamentResultEntity> {

}
