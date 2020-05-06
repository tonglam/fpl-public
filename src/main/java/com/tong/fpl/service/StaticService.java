package com.tong.fpl.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.data.bootstrapStaic.Event;
import com.tong.fpl.data.bootstrapStaic.Player;
import com.tong.fpl.data.bootstrapStaic.Team;
import com.tong.fpl.data.eventLive.Element;
import com.tong.fpl.data.eventLive.ElementStat;
import com.tong.fpl.data.response.EventLiveRes;
import com.tong.fpl.data.response.StaticRes;
import com.tong.fpl.db.entity.EventEntity;
import com.tong.fpl.db.entity.EventLiveEntity;
import com.tong.fpl.db.entity.PlayerEntity;
import com.tong.fpl.db.entity.TeamEntity;
import com.tong.fpl.mapper.EventLiveMapper;
import com.tong.fpl.mapper.EventMapper;
import com.tong.fpl.mapper.PlayerMapper;
import com.tong.fpl.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Create by tong on 2020/1/19
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StaticService {

    private final EventMapper eventMapper;
    private final TeamMapper teamMapper;
    private final PlayerMapper playerMapper;
    private final EventLiveMapper eventLiveMapper;
    private final InterfaceService interfaceService;

    public void insertEvent() {
        List<EventEntity> eventList = Lists.newArrayList();
        Optional<StaticRes> staticRes = this.interfaceService.getBootstrapStaic();
        staticRes.ifPresent(o -> {
            List<Event> events = staticRes.get().getEvents();
            events.forEach(bootstrapEvent -> {
                EventEntity event = new EventEntity();
                BeanUtil.copyProperties(bootstrapEvent, event);
                eventList.add(event);
            });
            this.eventMapper.truncateTable();
            this.eventMapper.batchInsert(eventList);
            log.info("insert event size is " + eventList.size() + "!");
        });
    }

    public void insertTeam() {
        List<TeamEntity> teamList = Lists.newArrayList();
        Optional<StaticRes> staticRes = this.interfaceService.getBootstrapStaic();
        staticRes.ifPresent(o -> {
            List<Team> teams = staticRes.get().getTeams();
            teams.forEach(bootstrapTeam -> {
                TeamEntity team = new TeamEntity();
                BeanUtil.copyProperties(bootstrapTeam, team);
                teamList.add(team);
            });
            this.teamMapper.truncateTable();
            this.teamMapper.batchInsert(teamList);
            log.info("insert team size is " + teamList.size() + "!");
        });
    }

    public void insertPlayers() {
        List<PlayerEntity> playerList = Lists.newArrayList();
        Optional<StaticRes> staticRes = this.interfaceService.getBootstrapStaic();
        staticRes.ifPresent(o -> {
            List<Player> players = staticRes.get().getPlayers();
            players.forEach(bootstrapPlayer -> {
                PlayerEntity player = new PlayerEntity();
                BeanUtil.copyProperties(bootstrapPlayer, player, CopyOptions.create().ignoreNullValue());
                playerList.add(player);
            });
            this.playerMapper.truncateTable();
            this.playerMapper.batchInsert(playerList);
            log.info("insert player size is " + playerList.size() + "!");
        });
    }


    public void insertEventLive(int event, String profile) {
        Map<Integer, Integer> playerTypeMap = Maps.newHashMap();
        List<PlayerEntity> playerEntities = new LambdaQueryChainWrapper<>(playerMapper).select().list();
        playerEntities.forEach(obj -> playerTypeMap.put(obj.getId(), obj.getElementType()));
        List<EventLiveEntity> eventLiveList = Lists.newArrayList();
        Optional<EventLiveRes> eventLiveRes = this.interfaceService.getEventLive(event, profile);
        eventLiveRes.ifPresent(o -> {
            List<Element> elements = eventLiveRes.get().getElements();
            elements.forEach(element -> {
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
            this.eventLiveMapper.truncateTable();
            this.eventLiveMapper.batchInsert(eventLiveList);
            log.info("insert event_live size is " + eventLiveList.size() + "!");
        });
    }

}
