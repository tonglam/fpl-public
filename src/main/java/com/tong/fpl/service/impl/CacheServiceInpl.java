package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.EventEntity;
import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.TeamNameEntity;
import com.tong.fpl.service.ICacheSerive;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.db.EventFixtureService;
import com.tong.fpl.service.db.EventService;
import com.tong.fpl.service.db.TeamNameService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CacheServiceInpl implements ICacheSerive {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IInterfaceService interfaceService;
    private final TeamNameService teamNameService;
    private final EventService eventService;
    private final EventFixtureService eventFixtureService;

    @Override
    public void insertTeamName() {
        Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
        result.ifPresent(staticRes -> {
            this.teamNameService.remove(new QueryWrapper<TeamNameEntity>().eq("1", 1));
            List<TeamNameEntity> teamNameList = Lists.newArrayList();
            staticRes.getTeams().forEach(bootstrapTeam -> {
                TeamNameEntity teamNameEntity = new TeamNameEntity();
                teamNameEntity.setTeamId(bootstrapTeam.getId());
                teamNameEntity.setName(bootstrapTeam.getName());
                teamNameEntity.setShortName(bootstrapTeam.getShortName());
                teamNameList.add(teamNameEntity);
                // set cache
                String key = this.createKey(teamNameEntity, teamNameEntity.getTeamId());
                this.redisTemplate.opsForValue().set(key, teamNameEntity);
                this.redisTemplate.persist(key);
            });
            this.teamNameService.saveBatch(teamNameList);
            log.info("insert team name size is " + teamNameList.size() + "!");
        });
    }

    @Override
    public void insertEvent() {
        Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
        result.ifPresent(staticRes -> {
            this.eventService.remove(new QueryWrapper<EventEntity>().eq("1", 1));
            List<EventEntity> eventList = Lists.newArrayList();
            staticRes.getEvents().forEach(bootstrapEvent -> {
                EventEntity eventEntity = new EventEntity();
                BeanUtil.copyProperties(bootstrapEvent, eventEntity, CopyOptions.create().ignoreNullValue());
                eventEntity.setEvent(bootstrapEvent.getId())
                        .setDeadlineTime(CommonUtils.getZoneDate(bootstrapEvent.getDeadlineTime()));
                eventList.add(eventEntity);
                // set cache
                String key = this.createKey(eventEntity, eventEntity.getEvent());
                this.redisTemplate.opsForValue().set(key, eventEntity);
                this.redisTemplate.persist(key);
            });
            this.eventService.saveBatch(eventList);
            log.info("insert event size is " + eventList.size() + "!");
        });
    }

    @Override
    public void insertEventFixture() {
        int currentEvent = CommonUtils.getCurrentEvent();
        IntStream.range(currentEvent, 39).forEach(this::insertEventFixtureByEvent);
    }

    private void insertEventFixtureByEvent(int event) {
        log.info("start insert gw{} fixtures!", event);
        this.eventFixtureService.remove(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event));
        List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
        Optional<List<EventFixturesRes>> eventFixtureResList = this.interfaceService.getEventFixture(event);
        eventFixtureResList.ifPresent(list -> {
            list.forEach(o -> {
                EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
                BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
                eventFixtureEntity.setKickoffTime(CommonUtils.getZoneDate(o.getKickoffTime()));
                eventFixtureList.add(eventFixtureEntity);
                // set cache
                String key = this.createKey(eventFixtureEntity, eventFixtureEntity.getId());
                this.redisTemplate.opsForValue().set(key, eventFixtureEntity);
                this.redisTemplate.persist(key);
            });
            this.eventFixtureService.saveBatch(eventFixtureList);
            log.info("insert event_fixture size is " + eventFixtureList.size() + "!");
        });
    }

    @Override
    public void insertPlayers() {

    }

    @Override
    public void insertPlayerValue() {

    }

    private <T> String createKey(T entity, Object key) {
        return CommonUtils.getCurrentSeason() + "-" + entity.getClass().getSimpleName() + "::" + key;
    }

}
