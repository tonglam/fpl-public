package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.data.fpl.GwPointsData;
import com.tong.fpl.data.response.UserPicksRes;
import com.tong.fpl.data.userpick.Pick;
import com.tong.fpl.db.entity.EventLiveEntity;
import com.tong.fpl.service.db.EventLiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Calculate gw points
 * Create by tong on 2020/3/10
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CalcGwPointsService {

	private final EventLiveService eventLiveService;
	private final InterfaceService interfaceService;

	public List<GwPointsData> calcGwPoints(int event, List<Integer> entryList) {
		List<GwPointsData> gwPointsDataList = Lists.newArrayList();
		entryList.forEach(entry -> {
			GwPointsData gwPointsData = this.calcEntryGwPoints(event, entry);
			if (gwPointsData != null) {
				gwPointsDataList.add(gwPointsData);
			}
		});
		return gwPointsDataList;
	}

	private GwPointsData calcEntryGwPoints(int event, int entry) {
		Optional<UserPicksRes> userPicksRes = this.interfaceService.getUserPicks(entry, event, Constant.PL_PROFILE);
		if (userPicksRes.isPresent()) {
			GwPointsData gwPointsData = new GwPointsData();
			gwPointsData.setEvent(event);
			gwPointsData.setEntry(entry);
			gwPointsData.setGwPoint(userPicksRes.get().getEntryHistory().getPoints());
			gwPointsData.setEventCost(userPicksRes.get().getEntryHistory().getEventTransfersCost());
			gwPointsData.setNetPoint(gwPointsData.getGwPoint() - gwPointsData.getEventCost());
			gwPointsData.setTotalPoints(userPicksRes.get().getEntryHistory().getTotalPoints());
			gwPointsData.setOverallRank(userPicksRes.get().getEntryHistory().getOverallRank());
			gwPointsData.setActiveChips(userPicksRes.get().getActiveChip());
			gwPointsData.setPicks(this.setUserPicks(event, userPicksRes.get().getPicks()));
			return gwPointsData;
		}
		return null;
	}

	private List<Pick> setUserPicks(int event, List<Pick> picks) {
		List<Pick> pickList = Lists.newArrayList();
		picks.forEach(o -> {
			Pick pick = new Pick();
			pick.setElement(o.getElement());
			pick.setPosition(o.getPosition());
			pick.setMultiplier(o.getMultiplier());
			pick.setCaptain(o.isCaptain());
			pick.setViceCaptain(o.isViceCaptain());
			pick.setPoints(this.setElementPoints(o.getElement(), event));
			pickList.add(pick);
		});
		return pickList;
	}

	private int setElementPoints(int element, int event) {
		return this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getElement, element).eq(EventLiveEntity::getEvent, event)).getTotalPoints();
	}

}
