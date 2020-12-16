package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.bootstrapStaic.Event;
import com.tong.fpl.domain.data.leaguesClassic.ClassicInfo;
import com.tong.fpl.domain.data.leaguesH2h.H2hInfo;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IStaticService;
import com.tong.fpl.service.db.EntryEventResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/1/19
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StaticServiceImpl implements IStaticService {

	private final IInterfaceService interfaceService;
	private final EntryEventResultService entryEventResultService;

	@Override
	public void insertAverageEventResult(int event, StaticRes staticRes) {
		int averageScore = staticRes.getEvents().stream()
				.filter(o -> o.getId() == event)
				.map(Event::getAverageEntryScore)
				.findFirst()
				.orElse(0);
		EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, -1));
		if (entryEventResultEntity != null) {
			entryEventResultEntity.setEventPoints(averageScore).setEventNetPoints(averageScore);
		} else {
			entryEventResultEntity = new EntryEventResultEntity()
					.setEntry(-1)
					.setEvent(event)
					.setEventPoints(averageScore)
					.setEventTransfers(0)
					.setEventTransfersCost(0)
					.setEventNetPoints(averageScore)
					.setEventBenchPoints(0)
					.setEventRank(0)
					.setOverallRank(0)
					.setEventChip(Chip.NONE.getValue())
					.setEventPicks("");
		}
		this.entryEventResultService.saveOrUpdate(entryEventResultEntity);
	}

	@Override
	public List<EntryInfoData> getEntryInfoListFromClassic(int classicId) {
		return this.getOnePageEntryListFromClassic(new LeagueInfoData(), classicId, 1, 0).getEntryInfoList();
	}

	@Override
	public List<EntryInfoData> getEntryInfoListFromH2h(int h2hId) {
		return this.getOnePageEntryListFromH2h(new LeagueInfoData(), h2hId, 1, 0).getEntryInfoList();
	}

	@Override
	public LeagueInfoData getEntryInfoListFromClassicByLimit(int classicId, int limit) {
		int endPage = this.getEndPage(limit);
		return this.getOnePageEntryListFromClassic(new LeagueInfoData(), classicId, 1, endPage);
	}

	@Override
	public LeagueInfoData getEntryInfoListFromH2hByLimit(int h2hId, int limit) {
		int endPage = this.getEndPage(limit);
		return this.getOnePageEntryListFromH2h(new LeagueInfoData(), h2hId, 1, endPage);
	}

	private int getEndPage(int limit) {
		if (limit > 0 && limit <= 50) {
			return 1;
		}
		return (int) Math.ceil(limit * 1.0 / 50);
	}

	private LeagueInfoData getOnePageEntryListFromClassic(LeagueInfoData leagueInfoData, int classicId, int page, int endPage) {
		Optional<LeagueClassicRes> resResult = this.interfaceService.getLeaguesClassic(classicId, page);
		if (resResult.isPresent()) {
			LeagueClassicRes leagueClassicRes = resResult.get();
			if (page == 1) {
				// league info
				ClassicInfo classicInfo = leagueClassicRes.getLeague();
				leagueInfoData
						.setId(classicInfo.getId())
						.setType(LeagueType.Classic.name())
						.setName(classicInfo.getName())
						.setCreated(classicInfo.getCreated())
						.setAdminEntry(classicInfo.getAdminEntry())
						.setStartEvent(classicInfo.getStartEvent());
			}
			if (!CollectionUtils.isEmpty(leagueClassicRes.getStandings().getResults())) {
				List<EntryInfoData> list = Lists.newArrayList();
				leagueClassicRes.getStandings().getResults().forEach(o ->
						list.add(new EntryInfoData()
								.setEntry(o.getEntry())
								.setEntryName(o.getEntryName())
								.setPlayerName(o.getPlayerName()))
				);
				// set entry info list
				if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
					leagueInfoData.setEntryInfoList(list);
				} else {
					List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
					entryInfoDataList.addAll(list);
					leagueInfoData.setEntryInfoList(entryInfoDataList);
				}
			}
			if (leagueClassicRes.getStandings().isHasNext()) {
				page++;
				if (endPage > 0 && page > endPage) {
					return leagueInfoData;
				}
				getOnePageEntryListFromClassic(leagueInfoData, classicId, page, endPage);
			}
		}
		return leagueInfoData;
	}

	private LeagueInfoData getOnePageEntryListFromH2h(LeagueInfoData leagueInfoData, int h2hId, int page, int endPage) {
		Optional<LeagueH2hRes> resResult = this.interfaceService.getLeagueH2H(h2hId, page);
		if (resResult.isPresent()) {
			LeagueH2hRes leagueH2hRes = resResult.get();
			if (page == 1) {
				// league info
				H2hInfo h2hInfo = leagueH2hRes.getLeague();
				leagueInfoData
						.setId(h2hInfo.getId())
						.setType(LeagueType.H2h.name())
						.setName(h2hInfo.getName())
						.setCreated(h2hInfo.getCreated())
						.setAdminEntry(h2hInfo.getAdminEntry())
						.setStartEvent(h2hInfo.getStartEvent())
						.setEntryInfoList(Lists.newArrayList());
			}
			if (!CollectionUtils.isEmpty(leagueH2hRes.getStandings().getResults())) {
				List<EntryInfoData> list = Lists.newArrayList();
				leagueH2hRes.getStandings().getResults().forEach(o ->
						list.add(new EntryInfoData()
								.setEntry(o.getEntry())
								.setEntryName(o.getEntryName())
								.setPlayerName(o.getPlayerName()))
				);
				// set entry info list
				if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
					leagueInfoData.setEntryInfoList(list);
				} else {
					List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
					entryInfoDataList.addAll(list);
					leagueInfoData.setEntryInfoList(entryInfoDataList);
				}
				if (leagueH2hRes.getStandings().isHasNext()) {
					page++;
					if (endPage > 0 && page > endPage) {
						return leagueInfoData;
					}
					getOnePageEntryListFromH2h(leagueInfoData, h2hId, page, endPage);
				}
			}
		}
		return leagueInfoData;
	}

	@Override
	public Optional<UserHistoryRes> getUserHistory(int entry) {
		return this.interfaceService.getUserHistory(entry);
	}

	@Override
	public Optional<ElementSummaryRes> getElementSummary(int element) {
		return this.interfaceService.getElementSummary(element);
	}

	@Override
	public Optional<EntryRes> getEntry(int entry) {
		return this.interfaceService.getEntry(entry);
	}

	@Override
	public Optional<UserPicksRes> getUserPicks(int event, int entry) {
		return this.interfaceService.getUserPicks(event, entry);
	}

	@Override
	public Optional<List<TransferRes>> getTransfer(int entry) {
		return this.interfaceService.getTransfer(entry);
	}

}
