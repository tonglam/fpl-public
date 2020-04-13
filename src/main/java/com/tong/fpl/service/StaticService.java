package com.tong.fpl.service;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.bootstrapStaic.*;
import com.tong.fpl.domain.data.eventLive.Element;
import com.tong.fpl.domain.data.eventLive.ElementStat;
import com.tong.fpl.domain.db.EventLive;
import com.tong.fpl.domain.response.EventLiveRes;
import com.tong.fpl.domain.response.StaticRes;
import com.tong.fpl.utils.BatchUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Create by tong on 2020/1/19
 */
@Service
@Slf4j
public class StaticService extends BaseService {

    private final InterfaceService interfaceService;

    public StaticService(InterfaceService interfaceService) {
        this.interfaceService = interfaceService;
    }

    public void insertEventsService() {
        StaticRes staticRes = this.interfaceService.getBootstrapStaic();
        if (staticRes == null || CollectionUtils.isEmpty(staticRes.getEvents())) {
            return;
        }
        List<Events> eventsList = staticRes.getEvents();
        if (!CollectionUtils.isEmpty(eventsList)) {
            if (this.mongoTemplate.collectionExists("event")) {
                this.mongoTemplate.dropCollection("event");
            }
            List<List<?>> eventsAll = BatchUtils.batchList(eventsList, Constant.BATCH_COUNT);
            for (List list : eventsAll
            ) {
                BatchUtils.batchInsetMongo(list, "event");
            }
            log.info("insert events size is " + eventsList.size() + "!");
        }
    }

    public void insertTeamsService() {
        StaticRes staticRes = this.interfaceService.getBootstrapStaic();
        if (staticRes == null || CollectionUtils.isEmpty(staticRes.getTeams())) {
            return;
        }
        List<Teams> teamsList = staticRes.getTeams();
        if (!CollectionUtils.isEmpty(teamsList)) {
            if (this.mongoTemplate.collectionExists("team")) {
                this.mongoTemplate.dropCollection("team");
            }
            List<List<?>> teamsAll = BatchUtils.batchList(teamsList, Constant.BATCH_COUNT);
            for (List list : teamsAll
            ) {
                BatchUtils.batchInsetMongo(list, "team");
            }
            log.info("insert teams size is " + teamsList.size() + "!");
        }
    }

    public void insertPlayersService() {
        StaticRes staticRes = this.interfaceService.getBootstrapStaic();
        if (staticRes == null || CollectionUtils.isEmpty(staticRes.getPlayers())) {
            return;
        }
        List<Player> players = staticRes.getPlayers();
        if (!CollectionUtils.isEmpty(players)) {
            if (this.mongoTemplate.collectionExists("player")) {
                this.mongoTemplate.dropCollection("player");
            }
            List<List<?>> playersAll = BatchUtils.batchList(players, Constant.BATCH_COUNT);
            for (List list : playersAll
            ) {
                BatchUtils.batchInsetMongo(list, "player");
            }
            log.info("insert players size is " + players.size() + "!");
        }
    }

    public void insertSettingsService() {
        StaticRes staticRes = this.interfaceService.getBootstrapStaic();
        if (staticRes == null) {
            return;
        }
        // game_settings
        GameSettings gameSettings = staticRes.getGameSettings();
        if (gameSettings != null) {
            if (this.mongoTemplate.collectionExists("game_settings")) {
                this.mongoTemplate.dropCollection("game_settings");
            }
            this.mongoTemplate.insert(gameSettings, "game_settings");
        }
        // phases
        List<Phases> phasesList = staticRes.getPhases();
        if (!CollectionUtils.isEmpty(phasesList)) {
            if (this.mongoTemplate.collectionExists("phase")) {
                this.mongoTemplate.dropCollection("phase");
            }
            List<List<?>> phasesAll = BatchUtils.batchList(phasesList, Constant.BATCH_COUNT);
            for (List list : phasesAll
            ) {
                BatchUtils.batchInsetMongo(list, "phase");
            }
        }
        // element_stats
        List<ElementStats> elementStatsList = staticRes.getElementStats();
        if (!CollectionUtils.isEmpty(elementStatsList)) {
            if (this.mongoTemplate.collectionExists("element_stat")) {
                this.mongoTemplate.dropCollection("element_stat");
            }
            for (ElementStats elementStats :
                    elementStatsList) {
                this.mongoTemplate.insert(elementStats, "element_stat");
            }
        }
        // element_type
        List<ElementTypes> elementTypeList = staticRes.getElementTypes();
        if (!CollectionUtils.isEmpty(elementTypeList)) {
            if (this.mongoTemplate.collectionExists("element_type")) {
                this.mongoTemplate.dropCollection("element_type");
            }
            for (ElementTypes elementTypes :
                    elementTypeList) {
                this.mongoTemplate.insert(elementTypes, "element_type");
            }
        }
    }

    public void insertEventLiveService(int event, String profile) {
        List<EventLive> eventLiveList = Lists.newArrayList();
        EventLiveRes eventLiveRes = this.interfaceService.getEventLive(event, profile);
        if (eventLiveRes == null) {
            return;
        }
        List<Element> elements = eventLiveRes.getElements();
        for (Element element : elements
        ) {
            ElementStat elementStat = element.getStats();
            EventLive eventLive = new EventLive();
            eventLive.setElement(element.getId());
            Player player = this.mongoTemplate.findOne(new Query(Criteria.where("_id").is(element.getId())), Player.class);
            if (player != null) {
                eventLive.setElementType(player.getElementType());
            }
            eventLive.setEvent(event);
            eventLive.setMinutes(elementStat.getMinutes());
            eventLive.setGoalsScored(elementStat.getGoalsScored());
            eventLive.setAssists(elementStat.getAssists());
            eventLive.setCleanSheets(elementStat.getCleanSheets());
            eventLive.setGoalsConceded(elementStat.getGoalsConceded());
            eventLive.setOwnGoals(elementStat.getOwnGoals());
            eventLive.setPenaltiesSaved(elementStat.getPenaltiesSaved());
            eventLive.setPenaltiesMissed(elementStat.getPenaltiesMissed());
            eventLive.setYellowCards(elementStat.getYellowCards());
            eventLive.setRedCards(elementStat.getRedCards());
            eventLive.setSaves(elementStat.getSaves());
            eventLive.setBonus(elementStat.getBonus());
            eventLive.setBps(elementStat.getBps());
            eventLive.setTotalPoints(elementStat.getTotalPoints());
            eventLiveList.add(eventLive);
        }
        if (!CollectionUtils.isEmpty(elements)) {
            if (this.mongoTemplate.collectionExists("event_live")) {
                this.mongoTemplate.dropCollection("event_live");
            }
            List<List<?>> eventLiveAll = BatchUtils.batchList(eventLiveList, Constant.BATCH_COUNT);
            for (List list : eventLiveAll
            ) {
                BatchUtils.batchInsetMongo(list, "event_live");
            }
            log.info("insert event_live size is " + elements.size() + "!");
        }
    }


}
