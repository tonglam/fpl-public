package com.tong.fpl.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

import java.util.Collections;
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

    private Map<Integer, List<Pick>> userPickMap = Maps.newHashMap();
    private Map<Integer, Pick> activePickMap = Maps.newHashMap();

    public CalcEntryLive(InterfaceService interfaceService, StaticService staticService) {
        this.interfaceService = interfaceService;
        this.staticService = staticService;
    }

    public int calcEntryLive(int entry, int event, String profile) {

        // update collection player and event_live
        this.staticService.insertPlayersService();
        this.staticService.insertEventLiveService(event, profile);

        // insert entry live
        UserPicksRes userPicksRes = this.interfaceService.getUserPicks(entry, event, profile);
        if (userPicksRes == null) {
            return 0;
        }
        this.insertEntryLive(entry, event, userPicksRes);
        String chip = "normal";
        // only 3c and bb change the calc rule
        if (StringUtils.isNotEmpty(userPicksRes.getActiveChip()) &&
                (StringUtils.equals("3xc", userPicksRes.getActiveChip()) || StringUtils.equals("bboost", userPicksRes.getActiveChip()))) {
            chip = userPicksRes.getActiveChip();
        }

        // calc points
        return this.calcActivePoints(entry, event, chip);
    }

    private int calcActivePoints(int entry, int event, String chips) {
        switch (chips) {
            case "normal":
                return this.calcNormalPoints(entry, event);
            case "3xc":
                return this.calc3cPoints(entry, event);
            case "bboost":
                return this.calcBbPoints(entry, event);
        }
        return 0;
    }

    private int calcNormalPoints(int entry, int event) {
        List<EntryLive> activePicks = Lists.newArrayList();
        // get all picks
        Map<Integer, EntryLive> positonLiveMap = Maps.newHashMap();
        List<EntryLive> entryLiveList = this.mongoTemplate.find(new Query(Criteria.where("entry").is(entry).and("event").is(event))
                .with(Sort.by("position")), EntryLive.class);
        if (CollectionUtils.isEmpty(entryLiveList)) {
            return 0;
        }
        for (EntryLive entryLive :
                entryLiveList) {
            positonLiveMap.put(entryLive.getPosition(), entryLive);
        }

        // gkp, lock position in (1,12)
        if (positonLiveMap.get(1).getMinutes() == 0 && positonLiveMap.get(12).getMinutes() > 0) {
            activePicks.add(positonLiveMap.get(12));
        } else {
            activePicks.add(positonLiveMap.get(1));
        }

        // def, num between 3-5
        long startingDef = this.mongoTemplate.count(new Query(Criteria.where("entry").is(entry).and("event").is(event)
                .and("element_type").is(2).and("positon").lte(11)), EntryLive.class);


        return 0;
    }

    private int calc3cPoints(int entry, int event) {
        return 0;
    }

    private int calcBbPoints(int entry, int event) {
        return 0;
    }

    private void insertEntryLive(int entry, int event, UserPicksRes userPicksRes) {
        List<EntryLive> list = Lists.newArrayList();
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
        }
        this.mongoTemplate.insert(list, EntryLive.class);
    }

    private void setElementActive() {
        // overall rule: 1 goalkeeper, at least 3 defenders and at least 1 forward
        this.setGkpActive();
        this.setDefActive();
//        this.setMidActive();
//        this.setFwdActive();
        //        this.setRestActive();
    }

    private void setDefActive() {
        // defs num between 3-5
        List<Pick> defPicks = Lists.newArrayList();
        List<Pick> defs = this.sortPickList(2);
        for (Pick pick :
                defs) {
            if (pick.getPosition() <= 11) {
                defPicks.add(pick);
            }
        }
        int size = defPicks.size();
        // autoSub by the def rule
        switch (size) {
            case 3:
                for (Pick pick :
                        defPicks) {
                    if (this.activePickMap.size() == 4) {
                        break;
                    }
                    if (1 == 2) {
                        this.activePickMap.put(pick.getPosition(), pick);
                    } else {
                        // autoSub eihter def 4th or 5th
                        Pick subDef = defs.get(4);
                        if (this.ifAutosub(pick, subDef, 2)) {
                            this.activePickMap.put(subDef.getPosition(), subDef);
                            continue;
                        }
                        subDef = defs.get(5);
                        if (this.ifAutosub(pick, subDef, 2)) {
                            this.activePickMap.put(subDef.getPosition(), subDef);
                        }
                    }
                }
            case 4:
                ;
            case 5:
                ;
        }


    }

    private void setGkpActive() {
        List<Pick> gkps = this.sortPickList(1);
        if (!this.ifAutosub(gkps.get(0), gkps.get(1), 1)) {
            this.activePickMap.put(1, gkps.get(0));
        } else {
            this.activePickMap.put(1, gkps.get(1));
        }
    }

    private boolean ifAutosub(Pick A, Pick B, int elementType) {
//        if (this.activePickMap.containsKey(B.getPosition()) || B.getElementType() != elementType) {
//            return false;
//        }
//        return A.getMinutes() == 0 && B.getMinutes() > 0;
        return false;
    }

    private List<Pick> sortPickList(int i) {
        if (!this.userPickMap.containsKey(i)) {
            return Lists.newArrayList();
        }
        List<Pick> picks = this.userPickMap.get(i);
        Collections.sort(picks);
        return picks;
    }

}
