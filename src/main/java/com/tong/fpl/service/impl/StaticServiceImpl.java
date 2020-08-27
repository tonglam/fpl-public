package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.config.collector.PlayAtHomeCollector;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.data.bootstrapStaic.Event;
import com.tong.fpl.domain.data.eventLive.ElementStat;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.db.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/1/19
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StaticServiceImpl implements IStaticSerive {

	private final EventService eventService;
	private final PlayerService playerService;
	private final EntryEventResultService entryEventResultService;
	private final EventFixtureService eventFixtureService;
	private final EventLiveService eventLiveService;
	private final IInterfaceService interfaceService;

	@Override
	public void insertEventLive(int event) {
		this.eventLiveService.remove(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event));
		Map<Integer, String> playAtHomeMap = this.eventFixtureService.list(new QueryWrapper<EventFixtureEntity>().lambda()
				.eq(EventFixtureEntity::getEvent, event))
				.stream()
				.collect(new PlayAtHomeCollector());
		Map<Integer, PlayerEntity> playerMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
		List<EventLiveEntity> eventLiveList = Lists.newArrayList();
		Optional<EventLiveRes> result = this.interfaceService.getEventLive(event);
		result.ifPresent(eventLiveRes -> {
			eventLiveRes.getElements().forEach(o -> {
				int element = o.getId();
				int teamId = playerMap.containsKey(element) ? playerMap.get(element).getTeamId() : 0;
				ElementStat elementStat = o.getStats();
				EventLiveEntity eventLive = new EventLiveEntity();
				BeanUtil.copyProperties(elementStat, eventLive, CopyOptions.create().ignoreNullValue());
				eventLive.setElement(element);
				eventLive.setElementType(playerMap.containsKey(element) ?
						playerMap.get(element).getElementType() : 0);
				eventLive.setEvent(event);
				eventLive.setWasHome(playAtHomeMap.getOrDefault(teamId, null));
				eventLiveList.add(eventLive);
			});
			this.eventLiveService.saveBatch(eventLiveList);
			log.info("insert event_live size is " + eventLiveList.size() + "!");
			eventLiveList.clear();
		});
	}

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
					.setEventPicks("")
					.setEventFinished(this.eventService.getById(event).isFinished());
		}
		this.entryEventResultService.saveOrUpdate(entryEventResultEntity);
	}

	@Override
	public List<EntryInfoEntity> getEntryInfoListFromClassic(int classicId) {
		List<EntryInfoEntity> list = Lists.newArrayList();
		this.getOnePageEntryListFromClassic(list, classicId, 1, true);
		return list;
	}

	@Override
	public List<EntryInfoEntity> getLeaguesClassicByPage(int classicId, int page) {
		List<EntryInfoEntity> list = Lists.newArrayList();
		this.getOnePageEntryListFromClassic(list, classicId, 1, false);
		return list;
	}

	@Override
	public List<EntryInfoEntity> getEntryInfoListFromH2h(int h2hId) {
		List<EntryInfoEntity> list = Lists.newArrayList();
		this.getOnePageEntryListFromH2h(list, h2hId, 1, true);
		return list;
	}

	@Override
	public List<EntryInfoEntity> getEntryInfoListFromH2hByPage(int h2hId, int page) {
		List<EntryInfoEntity> list = Lists.newArrayList();
		this.getOnePageEntryListFromH2h(list, h2hId, 1, false);
		return list;
	}

	@Override
	public Optional<UserPicksRes> getUserPicks(int event, int entry) {
		return this.interfaceService.getUserPicks(event, entry);
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

	private void getOnePageEntryListFromClassic(List<EntryInfoEntity> list, int classicId, int page, boolean recursive) {
		Optional<LeagueClassicRes> resResult = this.interfaceService.getLeaguesClassic(classicId, page);
		if (resResult.isPresent()) {
			LeagueClassicRes leagueClassicRes = resResult.get();
			if (!CollectionUtils.isEmpty(leagueClassicRes.getNewEntries().getResults())) {
				leagueClassicRes.getNewEntries().getResults().forEach(o -> list.add(new EntryInfoEntity()
						.setEntry(o.getEntry())
						.setEntryName(o.getEntryName())
						.setPlayerName(o.getPlayerName()))
				);
			}
			if (recursive && leagueClassicRes.getStandings().isHasNext()) {
				page++;
				getOnePageEntryListFromClassic(list, classicId, page, true);
			}
		}
	}

	private void getOnePageEntryListFromH2h(List<EntryInfoEntity> list, int h2hId, int page, boolean recursive) {
		Optional<LeagueH2hRes> resResult = this.interfaceService.getLeagueH2H(h2hId, page);
		if (resResult.isPresent()) {
			LeagueH2hRes leagueH2hRes = resResult.get();
			if (!CollectionUtils.isEmpty(leagueH2hRes.getStandings().getResults())) {
				leagueH2hRes.getStandings().getResults().forEach(o -> list.add(new EntryInfoEntity()
						.setEntry(o.getEntry())
						.setEntryName(o.getEntryName())
						.setPlayerName(o.getPlayerName()))
				);
				if (recursive && leagueH2hRes.getStandings().isHasNext()) {
					page++;
					getOnePageEntryListFromH2h(list, h2hId, page, true);
				}
			}
		}
	}

}
