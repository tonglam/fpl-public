package com.tong.fpl;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.data.fpl.GwPointsData;
import com.tong.fpl.data.response.LeagueClassicRes;
import com.tong.fpl.db.entity.EntryLiveEntity;
import com.tong.fpl.service.CalcGwPointsService;
import com.tong.fpl.service.CalcLivePointsService;
import com.tong.fpl.service.InterfaceService;
import com.tong.fpl.service.StaticService;
import com.tong.fpl.service.impl.TournamentManagementImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Create by tong on 2020/1/20
 */
public class ServiceTest extends FplApplicationTests {

	@Autowired
	private StaticService staticService;

	@Autowired
	private InterfaceService interfaceService;

	@Autowired
	private CalcGwPointsService calcGwPointsService;

	@Autowired
	private CalcLivePointsService calcLivePointsService;

	@Autowired
	private TournamentManagementImpl createNewCupsService;

	@Test
	public void insertEvents() {
		this.staticService.insertEvent();
	}

	@Test
	public void insertTeams() {
		this.staticService.insertTeam();
	}

	@Test
	public void insertPlayers() {
		this.staticService.insertPlayers();
	}

	@Test
	public void insertGwLive() {
		this.staticService.insertEventLive(39, Constant.PL_PROFILE);
	}

	@Test
	public void userHostory() {
		this.interfaceService.getUserHistory(3697, Constant.PL_PROFILE);
	}

	@Test
	public void classic() {
		Optional<LeagueClassicRes> leagueClassic = this.interfaceService.getLeaguesClassic(710, Constant.PL_PROFILE, 1);
		System.out.println("done!");
	}

	@Test
	public void weekPoint() {
		List<Integer> entryList = Lists.newArrayList();
		entryList.add(3697);
		List<GwPointsData> list = this.calcGwPointsService.calcGwPoints(39, entryList);
		System.out.println(list.toString());
	}

	@Test
	public void calcPoints() {
		Map<List<EntryLiveEntity>, Integer> resultMap = this.calcLivePointsService.calcLivePointsService(3697, 39, Constant.PL_PROFILE);
		System.out.println(resultMap.toString());
	}

	@Test
	public void createNewCup() {
		this.createNewCupsService.createNewTournament(null);
	}

}
