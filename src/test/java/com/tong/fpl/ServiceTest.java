package com.tong.fpl;

import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.GwEntry;
import com.tong.fpl.domain.data.bootstrapStaic.Player;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.response.LeagueClassicRes;
import com.tong.fpl.domain.response.UserPicksRes;
import com.tong.fpl.service.FAservice;
import com.tong.fpl.service.InterfaceService;
import com.tong.fpl.service.StaticService;
import com.tong.fpl.service.WeekPointsService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * Create by tong on 2020/1/20
 */
public class ServiceTest extends FplApplicationTests {

    @Autowired
    private StaticService staticService;

    @Autowired
    private InterfaceService interfaceService;

    @Autowired
    private WeekPointsService weekPointsService;

    @Autowired
    private FAservice fAservice;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void insertSettings() {
        this.staticService.insertSettingsService();
    }

    @Test
    public void insertEvents() {
        this.staticService.insertEventsService();
    }

    @Test
    public void insertTeams() {
        this.staticService.insertTeamsService();
    }

    @Test
    public void insertPlayers() {
        this.staticService.insertPlayersService();
    }

    @Test
    public void insertGwLive() {
        this.staticService.insertEventLiveService(29, Constant.PL_PROFILE);
    }

    @Test
    public void userHostory() {
        this.interfaceService.getUserHistory(3697, Constant.PL_PROFILE);
    }

    @Test
    public void classic() {
        LeagueClassicRes leagueClassic = this.interfaceService.getLeaguesClassic(710, Constant.PL_PROFILE, 1);
        System.out.println("done!");
    }

    @Test
    public void weekPoint() {
        List<GwEntry> list = this.weekPointsService.calcWeekPoints(710, 31);
        System.out.println(list.toString());
    }

    @Test
    public void calcRound1() throws Exception {
        this.fAservice.calcPoint(710, 31);
    }

    @Test
    public void picks() {
        UserPicksRes userPicksRes = this.interfaceService.getUserPicks(3697, 31, Constant.PL_PROFILE);
        List<Pick> picks = userPicksRes.getPicks();
        for (Pick pick : picks
        ) {
            int element = pick.getElement();
            Player player = this.mongoTemplate.findOne(new Query(Criteria.where("_id1").is(element)), Player.class);

            System.out.println(player.toString());
        }

        System.out.println("done");
    }
}
