package com.tong.fpl.service.db;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.fpl.domain.entity.SubtitleEntity;
import com.tong.fpl.mapper.SubtitleMapper;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/12/2
 */
@Service
public class SubtitleService extends ServiceImpl<SubtitleMapper, SubtitleEntity> implements IService<SubtitleEntity> {

}
