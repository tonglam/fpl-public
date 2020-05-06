package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.data.response.UserPicksRes;
import com.tong.fpl.data.userpick.Pick;
import com.tong.fpl.db.entity.EntryLiveEntity;
import com.tong.fpl.db.entity.EventLiveEntity;
import com.tong.fpl.mapper.EntryLiveMapper;
import com.tong.fpl.mapper.EventLiveMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * https://fantasy.premierleague.com/help/rules
 * 1.Your team can play in any formation providing that 1 goalkeeper, at least 3 defenders and at least 1 forward are selected at all times.
 * 2.a.If your captain plays 0 minutes in the Gameweek, the captain will be changed to the vice-captain.
 * b.If both captain and vice-captain play 0 minutes in a Gameweek, then no player's score will be doubled.
 * 3.Playing in a Gameweek means playing at least 1 minute or receiving a yellow / red card.
 * 4.a.If your Goalkeeper doesn't play in the Gameweek, he will be substituted by your replacement Goalkeeper, if he played in the Gameweek.
 * b.If any of your outfield players don't play in the Gameweek, they will be substituted by the highest priority outfield substitute who played in the Gameweek and doesn't break the formation rules.
 * <p>
 * Create by tong on 2020/3/12
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CalcLivePointsService {

    private final EntryLiveMapper entryLiveMapper;
    private final EventLiveMapper eventLiveMapper;
    private final InterfaceService interfaceService;
    private final StaticService staticService;

    private Map<Integer, Integer> positonTypeMap = Maps.newHashMap();

    public Map<List<EntryLiveEntity>, Integer> calcLivePointsService(int entry, int event, String profile) {
        Map<List<EntryLiveEntity>, Integer> resultMap = Maps.newHashMap(); // (k,v) -> (team,points)
        // update collection player and event_live
        this.staticService.insertPlayers();
        this.staticService.insertEventLive(event, profile);
        // insert entry_live
        Optional<UserPicksRes> userPicksRes = this.interfaceService.getUserPicks(entry, event, profile);
        userPicksRes.ifPresent(o -> {
            this.insertEntryLive(entry, event, userPicksRes.get());
            // initialize entry_live
            List<EntryLiveEntity> entryLiveList = this.entryLiveMapper.selectList(new QueryWrapper<EntryLiveEntity>().lambda()
                    .eq(EntryLiveEntity::getEvent, event).eq(EntryLiveEntity::getEntry, entry));
            entryLiveList.forEach(obj -> this.positonTypeMap.put(obj.getPosition(), obj.getElementType()));
            // find chip
            String chip = StringUtils.isNotEmpty(userPicksRes.get().getActiveChip()) ? userPicksRes.get().getActiveChip() : Constant.NONE;
            // retrun result
            int points = this.calcActivePoints(chip, entryLiveList);
            resultMap.put(entryLiveList, points);
        });
        return resultMap;
    }

    private void insertEntryLive(int entry, int event, UserPicksRes userPicksRes) {
        Map<Integer, EntryLiveEntity> map = Maps.newHashMap();
        List<EntryLiveEntity> captainList = Lists.newArrayList();
        for (Pick pick :
                userPicksRes.getPicks()) {
            EventLiveEntity eventLive = this.eventLiveMapper.selectOne(new QueryWrapper<EventLiveEntity>().lambda()
                    .eq(EventLiveEntity::getElement, pick.getElement()));
            if (eventLive == null) {
                continue;
            }
            EntryLiveEntity entryLive = new EntryLiveEntity();
            entryLive.setEntry(entry);
            entryLive.setEvent(event);
            entryLive.setElement(pick.getElement());
            entryLive.setElementType(eventLive.getElementType());
            entryLive.setPosition(pick.getPosition());
            entryLive.setMinutes(eventLive.getMinutes());
            entryLive.setPlayed(eventLive.getMinutes() > 0 || eventLive.getYellowCards() > 0 || eventLive.getRedCards() > 0);
            entryLive.setBonus(eventLive.getBonus());
            entryLive.setPoint(eventLive.getTotalPoints());
            entryLive.setCaptain(pick.isCaptain());
            entryLive.setViceCaptain(pick.isViceCaptain());
            if (pick.isCaptain() || pick.isViceCaptain()) {
                captainList.add(entryLive);
            }
            map.put(entryLive.getPosition(), entryLive);
        }
        this.setEntryLiveCapain(captainList, map);
        List<EntryLiveEntity> list = Lists.newArrayList();
        list.addAll(map.values());
        this.entryLiveMapper.truncateTable();
        this.entryLiveMapper.batchInsert(list);
        log.info("insert entry_live size is " + list.size() + "!");
    }

    private void setEntryLiveCapain(List<EntryLiveEntity> captainList, Map<Integer, EntryLiveEntity> map) {
        EntryLiveEntity captain = captainList.get(0);
        EntryLiveEntity viceCaptain = captainList.get(1);
        if (captain.getMinutes() == 0 && viceCaptain.getMinutes() > 0) {
            captain.setCaptain(false);
            map.put(captain.getPosition(), captain);
            viceCaptain.setCaptain(true);
            map.put(viceCaptain.getPosition(), viceCaptain);
        }
    }

    private int calcActivePoints(String chips, List<EntryLiveEntity> entryLiveList) {
        // get active pickups
        List<EntryLiveEntity> activePicks = this.getActivePicks(entryLiveList);
        if (CollectionUtils.isEmpty(activePicks)) {
            return 0;
        }
        // only 3c and bb change the calculate rule
        switch (chips) {
            case Constant.NONE:
                return this.calcNormalPoints(activePicks);
            case Constant.TC:
                return this.calcTcPoints(activePicks);
            case Constant.BB:
                return this.calcBBPoints(entryLiveList);
        }
        return 0;
    }

    private List<EntryLiveEntity> getActivePicks(List<EntryLiveEntity> entryLiveList) {
        // element_type -> active -> start
        Map<Integer, Map<Boolean, Map<Boolean, List<EntryLiveEntity>>>> map = entryLiveList.parallelStream()
                .collect(Collectors.groupingBy(EntryLiveEntity::getElementType,
                        Collectors.partitioningBy(EntryLiveEntity::isPlayed,
                                Collectors.partitioningBy(entryLiveEntity -> entryLiveEntity.getPosition() < 12))));
        // gkp
        List<EntryLiveEntity> gkps = this.createSteam(map.get(Constant.TYPE_GKP).get(true).get(true),
                map.get(Constant.TYPE_GKP).get(true).get(false), map.get(1).get(false).get(true))
                .flatMap(Collection::stream)
                .limit(Constant.MIN_NUM_GKP)
                .collect(Collectors.toList());
        // active defs
        List<EntryLiveEntity> defs = this.createSteam(map.get(Constant.TYPE_DEF).get(true).get(true),
                map.get(Constant.TYPE_DEF).get(true).get(false))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(EntryLiveEntity::getPosition))
                .collect(Collectors.toList());
        // def rule, at least 3
        if (defs.size() < Constant.MIN_NUM_DEF) {
            defs = this.createSteam(defs, map.get(Constant.TYPE_DEF).get(false).get(true))
                    .flatMap(Collection::stream)
                    .limit(Constant.MIN_NUM_DEF)
                    .sorted(Comparator.comparing(EntryLiveEntity::getPosition))
                    .collect(Collectors.toList());
        }
        // active fwds
        List<EntryLiveEntity> fwds = this.createSteam(map.get(Constant.TYPE_FWD).get(true).get(true),
                map.get(Constant.TYPE_FWD).get(true).get(false))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(EntryLiveEntity::getPosition))
                .collect(Collectors.toList());
        // fwd rule, at least 1
        if (fwds.size() < Constant.MIN_NUM_FWD) {
            fwds.add(map.get(Constant.TYPE_FWD).get(false).get(true).get(0));
        }
        // mids
        int maxMidNum = Constant.MIN_PLAYERS - gkps.size() - defs.size() - fwds.size();
        List<EntryLiveEntity> mids = this.createSteam(map.get(Constant.TYPE_MID).get(true).get(true),
                map.get(Constant.TYPE_MID).get(true).get(false))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(EntryLiveEntity::getPosition))
                .limit(maxMidNum)
                .collect(Collectors.toList());
        // active_list
        List<EntryLiveEntity> activeList = this.createSteam(gkps, defs, fwds, mids)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<EntryLiveEntity> standByList = this.createSteam(map.get(Constant.TYPE_DEF).get(false).get(true),
                map.get(Constant.TYPE_MID).get(false).get(true),
                map.get(Constant.TYPE_FWD).get(false).get(true))
                .flatMap(Collection::stream)
                .filter(o -> !activeList.contains(o))
                .sorted(Comparator.comparing(EntryLiveEntity::getPosition))
                .limit(Constant.MIN_PLAYERS - activeList.size())
                .collect(Collectors.toList());
        return this.createSteam(activeList, standByList)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(EntryLiveEntity::getElementType).thenComparing(EntryLiveEntity::getPosition))
                .collect(Collectors.toList());
    }

    @SafeVarargs
    private final <T> Stream<T> createSteam(T... values) {
        Stream.Builder<T> builder = Stream.builder();
        Arrays.asList(values).forEach(builder::add);
        return builder.build();
    }

    private int calcNormalPoints(List<EntryLiveEntity> activePicks) {
        int a = activePicks.stream().mapToInt(EntryLiveEntity::getPoint).sum();
        // just sum up
        int point = 0;
        for (EntryLiveEntity entryLive :
                activePicks) {
            // count captain points
            if (entryLive.isCaptain()) {
                point += (entryLive.getPoint()) * 2;
            } else {
                point += entryLive.getPoint();
            }
        }
        return point;
    }

    private int calcTcPoints(List<EntryLiveEntity> activePicks) {
        // captain triple points
        int point = 0;
        for (EntryLiveEntity entryLive :
                activePicks) {
            if (entryLive.isCaptain()) {
                point += (entryLive.getPoint()) * 3;
            } else {
                point += entryLive.getPoint();
            }
        }
        return point;
    }

    private int calcBBPoints(List<EntryLiveEntity> activePicks) {
        // count all picks
        int point = 0;
        for (EntryLiveEntity entryLive :
                activePicks) {
            if (entryLive.isCaptain()) {
                point += (entryLive.getPoint()) * 2;
            } else {
                point += entryLive.getPoint();
            }
        }
        return point;
    }

}
