package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.TournamentKnockoutEntity;
import com.tong.fpl.mapper.TournamentKnockoutMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/6/29
 */
@Service
public class TournamentKnockoutService extends ServiceImpl<TournamentKnockoutMapper, TournamentKnockoutEntity> implements IService<TournamentKnockoutEntity> {

}
