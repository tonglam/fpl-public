package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.fpl.domain.entity.ScoutEntity;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IScoutService;
import com.tong.fpl.service.db.ScoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/12/9
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScoutServiceImpl implements IScoutService {

	private final ScoutService scoutService;

	@Override
	public void upsertEventScout(ScoutData scoutData) {
		ScoutEntity scoutEntity = this.scoutService.getOne(new QueryWrapper<ScoutEntity>().lambda()
				.eq(ScoutEntity::getEvent, scoutData.getEvent())
				.eq(ScoutEntity::getEntry, scoutData.getEntry()));
		if (scoutEntity == null) {
			scoutEntity = new ScoutEntity()
					.setEvent(scoutData.getEvent())
					.setEntry(scoutData.getEntry())
					.setScoutName(scoutData.getScoutName())
					.setGkp(scoutData.getGkp())
					.setDef(scoutData.getDef())
					.setMid(scoutData.getMid())
					.setFwd(scoutData.getFwd())
					.setCaptain(scoutData.getCaptain())
					.setReason(StringUtils.isBlank(scoutData.getReason()) ? "" : scoutData.getReason());
			this.scoutService.save(scoutEntity);
		} else {
			scoutEntity
					.setGkp(scoutData.getGkp())
					.setDef(scoutData.getDef())
					.setMid(scoutData.getMid())
					.setFwd(scoutData.getFwd())
					.setCaptain(scoutData.getCaptain())
					.setReason(StringUtils.isBlank(scoutData.getReason()) ? "" : scoutData.getReason());
			this.scoutService.updateById(scoutEntity);
		}


	}

}
