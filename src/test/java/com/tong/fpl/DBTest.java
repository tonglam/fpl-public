package com.tong.fpl;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.bootstrapStaic.Player;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.db.EntryLive;
import com.tong.fpl.domain.db.EventLive;
import com.tong.fpl.domain.db.FACup;
import com.tong.fpl.domain.response.UserPicksRes;
import com.tong.fpl.service.InterfaceService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * Create by tong on 2020/1/19
 */
public class DBTest extends FplApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private InterfaceService interfaceService;

    @Test
    public void dbTest() {
        this.mongoTemplate.createCollection(Player.class);
    }

    @Test
    public void update() {
        Query query = new Query(Criteria.where("round_2_event").is(0));
        Update update = new Update().set("round_2_event", 31);
        this.mongoTemplate.updateMulti(query, update, FACup.class);
    }

    @Test
    public void insert() {
        List<EntryLive> list = Lists.newArrayList();
        UserPicksRes userPicksRes = this.interfaceService.getUserPicks(3697, 29, Constant.PL_PROFILE);
        if (userPicksRes == null) {
            return;
        }
        for (Pick pick :
                userPicksRes.getPicks()) {
            EventLive eventLive = this.mongoTemplate.findOne(new Query(Criteria.where("element").is(pick.getElement())), EventLive.class);
            if (eventLive == null) {
                continue;
            }
            EntryLive entryLive = new EntryLive();
            entryLive.setEntry(3697);
            entryLive.setEvent(29);
            entryLive.setElemnet(pick.getElement());
            entryLive.setElementType(eventLive.getElementType());
            entryLive.setPosition(pick.getPosition());
            entryLive.setMinutes(eventLive.getMinutes());
            entryLive.setPoint(eventLive.getTotalPoints());
            entryLive.setCaptain(pick.isCaptain());
            entryLive.setViceCaptain(pick.isViceCaptain());
            list.add(entryLive);
        }
        this.mongoTemplate.insert(list, EntryLive.class);
    }

    @Test
    public void query() {
        long startingDef = this.mongoTemplate.count(new Query(Criteria.where("entry").is(3697).and("event").is(29)
                .and("element_type").is(2).and("positon").lte(11)), EntryLive.class);
        System.out.println(startingDef);
    }
}
