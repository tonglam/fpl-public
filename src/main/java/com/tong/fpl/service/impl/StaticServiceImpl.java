package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.domain.data.eventLive.ElementStat;
import com.tong.fpl.domain.data.response.EventLiveRes;
import com.tong.fpl.domain.data.response.LeagueClassicRes;
import com.tong.fpl.domain.data.response.LeagueH2hRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.EventService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.TeamService;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Create by tong on 2020/1/19
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StaticServiceImpl implements IStaticSerive {

	private final EventService eventService;
	private final TeamService teamService;
	private final PlayerService playerService;
	private final EventLiveService eventLiveService;
	private final InterfaceServiceImpl interfaceService;

	public void insertTeam() {
		Optional<StaticRes> staticRes = this.interfaceService.getBootstrapStaic();
		staticRes.ifPresent(this::insertTeamEntity);
	}

	private void insertTeamEntity(StaticRes staticRes) {
		List<TeamEntity> teamList = Lists.newArrayList();
		staticRes.getTeams().forEach(bootstrapTeam -> {
			TeamEntity teamEntity = new TeamEntity();
			BeanUtil.copyProperties(bootstrapTeam, teamEntity);
			teamList.add(teamEntity);
		});
		this.teamService.saveOrUpdateBatch(teamList);
		log.info("insert team size is " + teamList.size() + "!");
	}

	public void insertPlayers() {
		Optional<StaticRes> staticRes = this.interfaceService.getBootstrapStaic();
		staticRes.ifPresent(this::insertPlayerEntity);
	}

	private void insertPlayerEntity(StaticRes staticRes) {
		List<PlayerEntity> playerList = Lists.newArrayList();
		staticRes.getPlayers().forEach(bootstrapPlayer -> {
			PlayerEntity playerEntity = new PlayerEntity();
			BeanUtil.copyProperties(bootstrapPlayer, playerEntity, CopyOptions.create().ignoreNullValue());
			playerList.add(playerEntity);
			this.playerService.saveOrUpdateBatch(playerList);
			log.info("insert player size is " + playerList.size() + "!");
		});
	}

	public void insertEvent() {
		Optional<StaticRes> staticRes = this.interfaceService.getBootstrapStaic();
		staticRes.ifPresent(this::insertEventEntity);
	}

	private void insertEventEntity(StaticRes staticRes) {
		List<EventEntity> eventList = Lists.newArrayList();
		staticRes.getEvents().forEach(bootstrapEvent -> {
			EventEntity eventEntity = new EventEntity();
			BeanUtil.copyProperties(bootstrapEvent, eventEntity);
			eventList.add(eventEntity);
			this.eventService.saveOrUpdateBatch(eventList);
			log.info("insert event size is " + eventList.size() + "!");
		});
	}

	@Override
	public void insertBaseData(int event) {
		Optional<StaticRes> staticRes = this.interfaceService.getBootstrapStaic();
		staticRes.ifPresent(o -> {
			// team
			insertTeamEntity(o);
			// player
			insertPlayerEntity(o);
			// event
			insertEventEntity(o);
		});
	}

	public void insertEventLive(int event) {
		Map<Integer, Integer> playerTypeMap = Maps.newHashMap();
		List<PlayerEntity> playerEntities = this.playerService.list();
		playerEntities.forEach(obj -> playerTypeMap.put(obj.getId(), obj.getElementType()));
		List<EventLiveEntity> eventLiveList = Lists.newArrayList();
		Optional<EventLiveRes> eventLiveRes = this.interfaceService.getEventLive(event);
		eventLiveRes.ifPresent(o -> {
			o.getElements().forEach(element -> {
				ElementStat elementStat = element.getStats();
				EventLiveEntity eventLive = new EventLiveEntity();
				BeanUtil.copyProperties(elementStat, eventLive);
				if (playerTypeMap.containsKey(element.getId())) {
					eventLive.setElementType(playerTypeMap.get(element.getId()));
				}
				eventLive.setElement(element.getId());
				eventLive.setEvent(event);
				eventLiveList.add(eventLive);
			});
			this.eventLiveService.saveOrUpdateBatch(eventLiveList);
			log.info("insert event_live size is " + eventLiveList.size() + "!");
		});
	}

	@Override
	public List<EntryInfoEntity> getEntryInfoListFromClassic(int classicId) {
		List<EntryInfoEntity> list = Lists.newArrayList();
		this.getOnePageEntryListFromClassic(list, classicId, 1);
		return list;
	}

	private void getOnePageEntryListFromClassic(List<EntryInfoEntity> list, int classicId, int page) {
		Optional<LeagueClassicRes> resResult = this.interfaceService.getLeaguesClassic(classicId, page);
		if (resResult.isPresent()) {
			LeagueClassicRes leagueClassicRes = resResult.get();
			if (!CollectionUtils.isEmpty(leagueClassicRes.getStandings().getResults())) {
				leagueClassicRes.getStandings().getResults().forEach(o -> list.add(new EntryInfoEntity()
						.setEntry(o.getEntry())
						.setEntryName(EmojiManager.containsEmoji(o.getEntryName()) ? EmojiParser.parseToHtmlDecimal(o.getEntryName()) : o.getEntryName())
						.setPlayerName(EmojiManager.containsEmoji(o.getPlayerName()) ? EmojiParser.parseToHtmlDecimal(o.getPlayerName()) : o.getPlayerName())));
			}
			if (leagueClassicRes.getStandings().isHasNext()) {
				page++;
				getOnePageEntryListFromClassic(list, classicId, page);
			}
		}
	}

	@Override
	public List<EntryInfoEntity> getEntryInfoListFromH2h(int h2hId) {
		List<EntryInfoEntity> list = Lists.newArrayList();
		this.getOnePageEntryListFromH2h(list, h2hId, 1);
		return list;
	}

	private void getOnePageEntryListFromH2h(List<EntryInfoEntity> list, int h2hId, int page) {
		Optional<LeagueH2hRes> resResult = this.interfaceService.getH2HClassic(h2hId, page);
		if (resResult.isPresent()) {
			LeagueH2hRes leagueH2hRes = resResult.get();
			if (!CollectionUtils.isEmpty(leagueH2hRes.getStandings().getResults())) {
				leagueH2hRes.getStandings().getResults().forEach(o -> list.add(new EntryInfoEntity()
						.setEntry(o.getEntry())
						.setEntryName(EmojiManager.containsEmoji(o.getEntryName()) ? EmojiParser.parseToHtmlDecimal(o.getEntryName()) : o.getEntryName())
						.setPlayerName(EmojiManager.containsEmoji(o.getPlayerName()) ? EmojiParser.parseToHtmlDecimal(o.getPlayerName()) : o.getPlayerName())));
				if (leagueH2hRes.getStandings().isHasNext()) {
					page++;
					getOnePageEntryListFromH2h(list, h2hId, page);
				}
			}
		}
	}

}
