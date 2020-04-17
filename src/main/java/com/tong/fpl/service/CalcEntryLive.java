package com.tong.fpl.service;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.db.EntryLive;
import com.tong.fpl.domain.db.EventLive;
import com.tong.fpl.domain.response.UserPicksRes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/3/12
 */
@Service
@Slf4j
public class CalcEntryLive extends BaseService {

    private final InterfaceService interfaceService;
    private final StaticService staticService;

    private List<EntryLive> entryLiveList = Lists.newArrayList();
    private Map<Integer, Integer> positonTypeMap = Maps.newHashMap();

    public CalcEntryLive(InterfaceService interfaceService, StaticService staticService) {
        this.interfaceService = interfaceService;
        this.staticService = staticService;
    }

    public int calcEntryLive(int entry, int event, String profile) {
        // update collection player and event_live
        this.staticService.insertPlayersService();
        this.staticService.insertEventLiveService(event, profile);
        // insert entry_live
        UserPicksRes userPicksRes = this.interfaceService.getUserPicks(entry, event, profile);
        if (userPicksRes == null) {
            return 0;
        }
        this.insertEntryLive(entry, event, userPicksRes);
        // initialize entry_live
        entryLiveList = this.mongoTemplate.find(new Query(Criteria.where("entry").is(entry).and("event").is(event))
                .with(Sort.by("positon")), EntryLive.class);
        for (EntryLive entryLive :
                entryLiveList) {
            positonTypeMap.put(entryLive.getPosition(), entryLive.getElementType());
        }
        // find chip
        String chip = Constant.NONE;
        if (StringUtils.isNotEmpty(userPicksRes.getActiveChip())) {
            chip = userPicksRes.getActiveChip();
        }
        // calculate points
        return this.calcActivePoints(chip);
    }

    private int calcActivePoints(String chips) {
        // get active pickups
        List<EntryLive> activePicks = this.getActivePicks();
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
                return this.calcBBPoints(activePicks);
        }
        return 0;
    }

    private List<EntryLive> getActivePicks() {
        // active formation
        return this.checkActiveFormation();
//        // gkp, lock position in (1,12)
//        this.setActiveGkp(activePicks);
//        // def, num between 3-5
//        this.setActiveDefs(activePicks, Integer.parseInt(typesNum[0]));
//        // mid
//        this.setActiveMids(activePicks, Integer.parseInt(typesNum[1]));
//        // fwd, num between 1-3
//        this.setActiveFwds(activePicks, Integer.parseInt(typesNum[2]));
        // return result
//        return activePicks;
    }

    private List<EntryLive> getCertainTypeEntryLives(int elementType) {
        List<EntryLive> list = Lists.newArrayList();
        for (EntryLive entryLive :
                entryLiveList) {
            if (entryLive.getElementType() == elementType) {
                list.add(entryLive);
            }
        }
        return list;
    }

    private List<EntryLive> checkActiveFormation() {
        // active picks
        List<EntryLive> activePicks = Lists.newArrayList();
        List<EntryLive> standByPicks = Lists.newArrayList();
        List<EntryLive> benchPicks = Lists.newArrayList();
        this.getCurrentActivePicks(activePicks, standByPicks, benchPicks);
        // check element_type
        this.checkCurrentActiveFormation(activePicks, standByPicks, benchPicks);
        return activePicks;
    }

    private void getCurrentActivePicks(List<EntryLive> activePicks, List<EntryLive> standByPicks, List<EntryLive> benchPicks) {
        for (EntryLive entryLive :
                entryLiveList) {
            if (entryLive.getPosition() > 11) {
                benchPicks.add(entryLive);
            }
            if (entryLive.getMinutes() > 0) {
                activePicks.add(entryLive);
            } else {
                standByPicks.add(entryLive);
            }
        }
    }

    private void checkCurrentActiveFormation(List<EntryLive> activePicks, List<EntryLive> standByPicks, List<EntryLive> benchPicks) {
        Multiset<Integer> typeSet = HashMultiset.create();
        for (EntryLive entryLive :
                activePicks) {
            typeSet.add(positonTypeMap.get(entryLive.getPosition()));
        }
        // rulr required
        // 先满足规则再random补满；先塞进list最后再排序
        int defNum = typeSet.count(2);
        if (defNum < 3) {
            for (int i = 0, len = benchPicks.size(); i < len; ++i) {
                EntryLive entryLive = benchPicks.get(i);
                if (entryLive.getElementType() != 2) {
                    continue;
                }
                if (entryLive.getMinutes() == 0) {
                    continue;
                }
                activePicks.add(entryLive);
                benchPicks.remove(i);
                --len;
                --i;
                defNum++;
                if (defNum >= 3) {
                    break;
                }
            }
        }
        int fwdNum = typeSet.count(4);
        if (fwdNum < 1) {
            for (int i = 0, len = benchPicks.size(); i < len; ++i) {
                EntryLive entryLive = benchPicks.get(i);
                if (entryLive.getElementType() != 4) {
                    continue;
                }
                if (entryLive.getMinutes() == 0) {
                    continue;
                }
                activePicks.add(entryLive);
                benchPicks.remove(i);
                --len;
                --i;
                break;
            }
        }
        // use all the bench
        if (activePicks.size() < 11 && benchPicks.size() > 0) {
            for (EntryLive entryLive :
                    benchPicks) {
                if (entryLive.getMinutes() > 0) {
                    activePicks.add(entryLive);
                }
            }
        }
        // fulfill activePicks
        if (activePicks.size() < 11) {
            for (EntryLive entryLive :
                    standByPicks) {
                if (activePicks.size() == 11) {
                    break;
                }
                activePicks.add(entryLive);
            }
        }
    }

    private void setActiveGkp(List<EntryLive> activePicks) {
        EntryLive start = entryLiveList.get(0);
        EntryLive backup = entryLiveList.get(11);
        if (backup != null && start.getMinutes() == 0 && backup.getMinutes() > 0) {
            activePicks.add(backup);
        }
        activePicks.add(start);
    }

    private void setActiveDefs(List<EntryLive> activePicks, int defNum) {
        int activeDefNum = 0;
        List<EntryLive> defs = this.getCertainTypeEntryLives(2);
        for (EntryLive entryLive :
                defs) {
            if (defNum == activeDefNum) {
                break;
            }
            activeDefNum++;
            activePicks.add(entryLive);
        }
    }

    private void setActiveMids(List<EntryLive> activePicks, int midNum) {
        int activeMidNum = 0;
        List<EntryLive> mids = this.getCertainTypeEntryLives(3);
        for (EntryLive entryLive :
                mids) {
            if (midNum == activeMidNum) {
                break;
            }
            activeMidNum++;
            activePicks.add(entryLive);
        }
    }

    private void setActiveFwds(List<EntryLive> activePicks, int fwdNum) {
        int activeFwdNum = 0;
        List<EntryLive> fwds = this.getCertainTypeEntryLives(4);
        for (EntryLive entryLive :
                fwds) {
            if (fwdNum == activeFwdNum) {
                break;
            }
            activeFwdNum++;
            activePicks.add(entryLive);
        }
    }

    private int calcNormalPoints(List<EntryLive> activePicks) {
        // just sum up
        int point = 0;
        for (EntryLive entryLive :
                activePicks) {
            if (entryLive.getPosition() > 11) {
                continue;
            }
            // count captain points
            if (entryLive.isCaptain()) {
                point += (entryLive.getPoint()) * 2;
            } else {
                point += entryLive.getPoint();
            }
        }
        return point;
    }

    private int calcTcPoints(List<EntryLive> activePicks) {
        // captain triple points
        int point = 0;
        for (EntryLive entryLive :
                activePicks) {
            if (entryLive.getPosition() > 11) {
                continue;
            }
            if (entryLive.isCaptain()) {
                point += (entryLive.getPoint()) * 3;
            } else {
                point += entryLive.getPoint();
            }
        }
        return point;
    }

    private int calcBBPoints(List<EntryLive> activePicks) {
        // count all picks
        int point = 0;
        for (EntryLive entryLive :
                activePicks) {
            if (entryLive.isCaptain()) {
                point += (entryLive.getPoint()) * 2;
            } else {
                point += entryLive.getPoint();
            }
        }
        return point;
    }

    private void insertEntryLive(int entry, int event, UserPicksRes userPicksRes) {
        Map<Integer, EntryLive> map = Maps.newHashMap();
        List<EntryLive> captainList = Lists.newArrayList();
        for (Pick pick :
                userPicksRes.getPicks()) {
            EventLive eventLive = this.mongoTemplate.findOne(new Query(Criteria.where("element").is(pick.getElement())), EventLive.class);
            if (eventLive == null) {
                continue;
            }
            EntryLive entryLive = new EntryLive();
            entryLive.setEntry(entry);
            entryLive.setEvent(event);
            entryLive.setElemnet(pick.getElement());
            entryLive.setElementType(eventLive.getElementType());
            entryLive.setPosition(pick.getPosition());
            entryLive.setMinutes(eventLive.getMinutes());
            entryLive.setPoint(eventLive.getTotalPoints());
            entryLive.setCaptain(pick.isCaptain());
            entryLive.setViceCaptain(pick.isViceCaptain());
            if (pick.isCaptain() || pick.isViceCaptain()) {
                captainList.add(entryLive);
            }
            map.put(entryLive.getPosition(), entryLive);
        }
        this.setEntryLiveCapain(captainList, map);
        List<EntryLive> list = Lists.newArrayList();
        list.addAll(map.values());
        this.mongoTemplate.insert(list, EntryLive.class);
    }

    private void setEntryLiveCapain(List<EntryLive> captainList, Map<Integer, EntryLive> map) {
        EntryLive captain = captainList.get(0);
        EntryLive viceCaptain = captainList.get(1);
        if (captain.getMinutes() == 0 && viceCaptain.getMinutes() > 0) {
            captain.setCaptain(false);
            map.put(captain.getPosition(), captain);
            viceCaptain.setCaptain(true);
            map.put(viceCaptain.getPosition(), viceCaptain);
        }
    }

}
